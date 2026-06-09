package org.autostock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AutoStockApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoStockApplication.class, args);
    }
}
