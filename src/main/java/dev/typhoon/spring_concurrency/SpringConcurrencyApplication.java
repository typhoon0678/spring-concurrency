package dev.typhoon.spring_concurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class SpringConcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringConcurrencyApplication.class, args);
	}

}
