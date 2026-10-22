package br.com.sumula;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
class WebConfig implements WebMvcConfigurer {

	private final String[] origens;

	WebConfig(@Value("${sumula.cors.origens}") String[] origens) {
		this.origens = origens;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**").allowedOrigins(origens).allowedMethods("GET", "POST", "PUT", "DELETE");
	}

}
