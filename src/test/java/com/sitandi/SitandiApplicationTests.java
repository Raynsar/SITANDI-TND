package com.sitandi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SitandiApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: memastikan Spring application context dapat start tanpa error
    }

}
