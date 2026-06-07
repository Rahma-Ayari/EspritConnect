package tn.esprit.espritconnect2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EspritConnect2Application {

    public static void main(String[] args) {
        SpringApplication.run(EspritConnect2Application.class, args);
    }

}
