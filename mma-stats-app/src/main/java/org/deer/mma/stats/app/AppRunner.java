package org.deer.mma.stats.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "org.deer.mma")
public class AppRunner {

    public static void main(String[] args) {
        SpringApplication.run(AppRunner.class,args);
    }
}
