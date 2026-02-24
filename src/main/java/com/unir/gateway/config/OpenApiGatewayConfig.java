package com.unir.gateway.config;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGatewayConfig {

	private static final String API_DOCS_PATH = "/v3/api-docs";
	private static final String DEFAULT_APP_NAME = "gateway";

	@Value("${spring.application.name:" + DEFAULT_APP_NAME + "}")
	private String applicationName;

	@Value("${springdoc.api-docs.path:" + API_DOCS_PATH + "}")
	private String apiDocsPath;

	@Bean
	public ApplicationListener<ApplicationReadyEvent> swaggerUiConfig(RouteLocator locator,
			ObjectProvider<SwaggerUiConfigParameters> swaggerUiConfigParametersProvider) {
		return event -> {
			SwaggerUiConfigParameters params = swaggerUiConfigParametersProvider.getIfAvailable();
			if (params == null) {
				return;
			}
			refreshSwaggerUrls(locator, params);
		};
	}

	@Bean
	public ApplicationListener<RefreshRoutesEvent> swaggerUiConfigOnRefresh(RouteLocator locator,
			ObjectProvider<SwaggerUiConfigParameters> swaggerUiConfigParametersProvider) {
		return event -> {
			SwaggerUiConfigParameters params = swaggerUiConfigParametersProvider.getIfAvailable();
			if (params == null) {
				return;
			}
			refreshSwaggerUrls(locator, params);
		};
	}

	private void refreshSwaggerUrls(RouteLocator locator,
			SwaggerUiConfigParameters swaggerUiConfigParameters) {
		locator.getRoutes()
				.filter(route -> route.getUri() != null
						&& "lb".equalsIgnoreCase(route.getUri().getScheme()))
				.map(route -> route.getUri().getHost())
				.filter(Objects::nonNull)
				.map(serviceId -> serviceId.toLowerCase(Locale.ROOT))
				.filter(serviceId -> !isSelfService(serviceId))
				.distinct()
				.collectList()
				.subscribe(serviceIds -> {
					Set<SwaggerUrl> urls = new LinkedHashSet<>();
					for (String serviceId : serviceIds) {
						urls.add(new SwaggerUrl(serviceId, "/" + serviceId + normalizeApiDocsPath(), serviceId));
					}
					swaggerUiConfigParameters.setUrls(urls);
				});
	}

	private String normalizeApiDocsPath() {
		if (apiDocsPath == null || apiDocsPath.isBlank()) {
			return API_DOCS_PATH;
		}
		return apiDocsPath.startsWith("/") ? apiDocsPath : "/" + apiDocsPath;
	}

	private boolean isSelfService(String serviceId) {
		String appName = applicationName == null ? DEFAULT_APP_NAME : applicationName.trim();
		if (appName.isEmpty()) {
			appName = DEFAULT_APP_NAME;
		}
		String normalizedAppName = appName.toLowerCase(Locale.ROOT);
		return serviceId.equals(normalizedAppName) || serviceId.equals(DEFAULT_APP_NAME);
	}
}
