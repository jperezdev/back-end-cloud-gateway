package com.unir.gateway.config;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGatewayConfig {

	private static final String API_DOCS_PATH = "/v3/api-docs";

	@Bean
	public ApplicationListener<ApplicationReadyEvent> swaggerUiConfig(RouteDefinitionLocator locator,
			SwaggerUiConfigProperties swaggerUiConfigProperties) {
		return event -> locator.getRouteDefinitions()
				.filter(definition -> definition.getUri() != null
						&& "lb".equalsIgnoreCase(definition.getUri().getScheme()))
				.map(definition -> definition.getUri().getHost())
				.filter(Objects::nonNull)
				.map(String::toLowerCase)
				.distinct()
				.collectList()
				.subscribe(serviceIds -> {
					Set<SwaggerUrl> urls = new LinkedHashSet<>();
					for (String serviceId : serviceIds) {
						urls.add(new SwaggerUrl(serviceId, "/" + serviceId + API_DOCS_PATH, serviceId));
					}
					swaggerUiConfigProperties.setUrls(urls);
				});
	}
}
