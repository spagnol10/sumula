package br.com.sumula;

import org.springframework.boot.SpringApplication;

public class TestSumulaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(SumulaApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
