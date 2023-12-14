package com.example.spring.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@SpringBootApplication
public class UsuariosServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsuariosServiceApplication.class, args);
	}

}
