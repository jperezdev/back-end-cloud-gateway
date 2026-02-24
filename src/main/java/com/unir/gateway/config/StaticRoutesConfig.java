package com.unir.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaticRoutesConfig {

	@Bean
	public RouteLocator staticRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("catalogue", route -> route.path("/catalogue/**")
						.filters(filters -> filters.stripPrefix(1))
						.uri("lb://catalogue"))
				.route("payments", route -> route.path("/payments/**")
						.filters(filters -> filters.stripPrefix(1))
						.uri("lb://payments"))
				.build();
	}
}
