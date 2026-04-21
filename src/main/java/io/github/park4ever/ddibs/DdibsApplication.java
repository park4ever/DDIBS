package io.github.park4ever.ddibs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DdibsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdibsApplication.class, args);
	}

}
