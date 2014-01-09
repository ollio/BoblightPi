package org.bozan.boblight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@ImportResource("integration-context.xml")
public class BoblightPiApp {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BoblightPiApp.class, args);
    }
}
