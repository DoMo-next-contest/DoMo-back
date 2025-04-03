package next.domo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoBackApplication.class, args);
    }

}