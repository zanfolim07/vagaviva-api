package br.com.fiap.vagaviva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class VagaVivaApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(Relogio.ZONA));
        SpringApplication.run(VagaVivaApplication.class, args);
    }
}
