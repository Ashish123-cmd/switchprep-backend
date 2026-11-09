package com.switchmate.jobs.ingestion;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class JobSourceHttpClient {

	private final RestClient restClient;

	@Autowired
	public JobSourceHttpClient(
			RestClient.Builder builder,
			@org.springframework.beans.factory.annotation.Value("${app.jobs.http.connect-timeout:3s}") Duration connectTimeout,
			@org.springframework.beans.factory.annotation.Value("${app.jobs.http.read-timeout:20s}") Duration readTimeout) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeout);
		requestFactory.setReadTimeout(readTimeout);
		this.restClient = builder.requestFactory(requestFactory).build();
	}

	public JobSourceHttpClient(RestClient restClient) {
		this.restClient = restClient;
	}

	public JsonNode getJson(URI uri) {
		JsonNode body = restClient.get().uri(uri).retrieve().body(JsonNode.class);
		if (body == null) {
			throw new IllegalStateException("Job source returned an empty JSON response");
		}
		return body;
	}
}