package de.kreuzenonline.kreuzen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableTransactionManagement
@EnableAsync
public class KreuzenApplication {

    public static void main(String[] args) {
        SpringApplication.run(KreuzenApplication.class, args);
    }
}
