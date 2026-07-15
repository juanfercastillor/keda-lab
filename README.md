# keda-lab

Laboratorio para practicar **KEDA** (Kubernetes Event-Driven Autoscaling) con una
aplicación **Spring Boot 4** / **Java 25**, construida con **Gradle** y organizada
según **arquitectura limpia** (clean / hexagonal architecture).

La aplicación expone una API para encolar "jobs". Un worker interno los procesa
de forma lenta a propósito, de modo que bajo carga la cola crece. La profundidad
de la cola se publica como métrica Prometheus (`keda_lab_pending_jobs`) y KEDA la
usa para escalar el `Deployment` (incluido escalar a cero).

## Stack

| Componente          | Versión / elección          |
|---------------------|-----------------------------|
| Java                | 25 (Temurin)                |
| Spring Boot         | 4.0.7                       |
| Build               | Gradle 9 (wrapper incluido) |
| Métricas            | Micrometer + Prometheus     |
| Autoescalado        | KEDA (`ScaledObject`)       |

## Arquitectura limpia

Las dependencias apuntan siempre hacia el dominio. El dominio y la capa de
aplicación no conocen Spring; la infraestructura implementa los puertos.

```
com.lab.keda
├── domain                      # Núcleo puro (sin framework)
│   ├── model                   #   Job, JobStatus (entidades)
│   └── port
│       ├── in                  #   Puertos de entrada (casos de uso)
│       └── out                 #   Puertos de salida (repositorio, cola)
├── application                 # Servicios de aplicación (orquestan el dominio)
│   ├── SubmitJobService
│   ├── GetJobService
│   └── ProcessJobService
└── infrastructure              # Adaptadores (dependen del dominio)
    ├── in
    │   ├── web                 #   Controlador REST + DTOs
    │   └── scheduler           #   Poller que drena la cola
    ├── out
    │   ├── persistence         #   Repositorio en memoria
    │   └── queue               #   Cola en memoria + gauge Micrometer
    └── config                  #   Composition root (wiring de beans)
```

El *composition root* (`UseCaseConfig`) es lo único que ensambla los servicios
puros con los adaptadores, permitiendo cambiar la persistencia o la cola sin
tocar el dominio.

## Endpoints

| Método | Ruta                     | Descripción                             |
|--------|--------------------------|-----------------------------------------|
| POST   | `/api/jobs`              | Encola un job `{ "payload": "..." }`    |
| GET    | `/api/jobs/{id}`         | Consulta el estado de un job            |
| GET    | `/api/jobs/stats`        | Profundidad de la cola y total de jobs  |
| GET    | `/actuator/health`       | Health (liveness/readiness probes)      |
| GET    | `/actuator/prometheus`   | Métricas (incluye `keda_lab_pending_jobs`) |

## Ejecutar localmente

Requiere Java 25 (o usa el wrapper con un JDK 25 en `JAVA_HOME`).

```bash
./gradlew bootRun
# en otra terminal
curl -X POST localhost:8080/api/jobs -H 'Content-Type: application/json' -d '{"payload":"hola"}'
curl localhost:8080/api/jobs/stats
```

Ejecutar tests:

```bash
./gradlew test
```

## Construir la imagen

```bash
docker build -t keda-lab:latest .
```

## Desplegar en Kubernetes con KEDA

Los ejemplos usan [kind](https://kind.sigs.k8s.io/), pero minikube funciona igual.

### 1. Crear el clúster y cargar la imagen

```bash
kind create cluster --name keda-lab
kind load docker-image keda-lab:latest --name keda-lab
```

### 2. Instalar KEDA

```bash
helm repo add kedacore https://kedacore.github.io/charts
helm repo update
helm install keda kedacore/keda --namespace keda --create-namespace
```

### 3. Instalar Prometheus (para el trigger por defecto)

El `ScaledObject` por defecto escala según la métrica en Prometheus. Instala
Prometheus en el namespace `monitoring`; su configuración por defecto descubre
pods con las anotaciones `prometheus.io/scrape` (ya presentes en el Deployment).

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/prometheus \
  --namespace monitoring --create-namespace
```

> El `serverAddress` del `ScaledObject` apunta a
> `http://prometheus-server.monitoring.svc.cluster.local:80`. Ajusta el host si
> instalas Prometheus con otro nombre/namespace.

### 4. Desplegar la aplicación + el ScaledObject

```bash
kubectl apply -k k8s/
# o manifiesto por manifiesto:
# kubectl apply -f k8s/namespace.yaml -f k8s/deployment.yaml -f k8s/service.yaml -f k8s/keda-scaledobject.yaml
```

### 5. Generar carga y observar el autoescalado

```bash
kubectl -n keda-lab port-forward svc/keda-lab 8080:80 &
./scripts/load-test.sh http://localhost:8080 300

# observa cómo KEDA crea réplicas
kubectl -n keda-lab get hpa,scaledobject,pods -w
```

Cuando la cola se vacía, KEDA reduce las réplicas y, tras el `cooldownPeriod`,
puede escalar a cero.

## Otros triggers

`k8s/keda-scaledobject-alternatives.yaml` incluye ejemplos de triggers `cpu` y
`cron` para experimentar. Aplica **un solo** `ScaledObject` por `Deployment` a la
vez (dos HPAs sobre el mismo target entran en conflicto).

## Parámetros del laboratorio

Configurables vía `application.yml` o variables de entorno (relaxed binding):

| Propiedad                       | Env var                         | Default | Descripción                          |
|---------------------------------|---------------------------------|---------|--------------------------------------|
| `keda-lab.processing-cost-millis` | `KEDALAB_PROCESSINGCOSTMILLIS` | `750`   | Costo simulado por job (ms)          |
| `keda-lab.poll-interval-millis`   | `KEDALAB_POLLINTERVALMILLIS`   | `200`   | Cada cuánto cada réplica drena la cola |

Sube el costo o la carga para que el backlog crezca más rápido y KEDA reaccione.
