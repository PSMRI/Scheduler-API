package com.iemr.tm.utils.http;

import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpUtils {
	public static final String AUTHORIZATION = "Authorization";
	private RestTemplate rest;
	private HttpHeaders headers;
	private HttpStatus status;

	public HttpUtils() {
		if (rest == null) {
			rest = new RestTemplate();
			headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
		}
	}

	public String get(String uri) {
		HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
		ResponseEntity<String> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity, String.class);
		setStatus(responseEntity.getStatusCode());
		return responseEntity.getBody();
	}

	public String get(String uri, HashMap<String, Object> header) {
		HttpHeaders headers = new HttpHeaders();
		if (header.containsKey(HttpHeaders.AUTHORIZATION)) {
			headers.add(HttpHeaders.AUTHORIZATION, header.get(HttpHeaders.AUTHORIZATION).toString());
		}
		if (header.containsKey(HttpHeaders.CONTENT_TYPE)) {
			headers.add(HttpHeaders.CONTENT_TYPE, header.get(HttpHeaders.CONTENT_TYPE).toString());
		} else {
			headers.add("Content-Type", MediaType.APPLICATION_JSON.toString());
		}
		HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
		ResponseEntity<String> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity, String.class);
		setStatus(responseEntity.getStatusCode());
		return responseEntity.getBody();
	}

	public String post(String uri, String json) {
		HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
		ResponseEntity<String> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, String.class);
		setStatus(responseEntity.getStatusCode());
		return responseEntity.getBody();
	}

	public String post(String uri, String data, HashMap<String, Object> header) {
		HttpHeaders headers = new HttpHeaders();
		if (header.containsKey(HttpHeaders.AUTHORIZATION)) {
			headers.add(HttpHeaders.AUTHORIZATION, header.get(HttpHeaders.AUTHORIZATION).toString());
		}
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		HttpEntity<String> requestEntity;
		requestEntity = new HttpEntity<String>(data, headers);
		responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, String.class);
		setStatus(responseEntity.getStatusCode());
		return responseEntity.getBody();
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}
}