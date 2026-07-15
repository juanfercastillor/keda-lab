package com.lab.keda;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "keda-lab.poll-interval-millis=3600000")
class KedaLabApplicationTests {

    @Test
    void contextLoads() {
    }
}
