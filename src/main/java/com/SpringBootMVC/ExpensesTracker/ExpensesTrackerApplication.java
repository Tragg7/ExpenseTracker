package com.SpringBootMVC.ExpensesTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication
@EnableScheduling
public class ExpensesTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensesTrackerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();

		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();

		jsonConverter.setSupportedMediaTypes(Arrays.asList(
				MediaType.APPLICATION_JSON,
				MediaType.parseMediaType("application/javascript"),
				MediaType.parseMediaType("text/javascript"),
				MediaType.parseMediaType("application/x-javascript"),
				MediaType.parseMediaType("text/plain")
		));
		restTemplate.getMessageConverters()
				.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);

		restTemplate.getMessageConverters().add(jsonConverter);

		return restTemplate;
	}
}