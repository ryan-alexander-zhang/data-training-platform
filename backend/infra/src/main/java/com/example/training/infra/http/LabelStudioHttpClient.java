package com.example.training.infra.http;

import com.example.training.domain.LabelStudioService;
import com.example.training.domain.LabelStudioTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@lombok.extern.slf4j.Slf4j
public class LabelStudioHttpClient implements LabelStudioService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LabelStudioHttpClient(RestTemplate restTemplate,
                                 String baseUrl,
                                 String apiToken) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
    }

    @Override
    public long createProject(String title, String description, String labelConfig) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("label_config", labelConfig);
        payload.put("description", description);

        String url = baseUrl + "/api/projects";
        String body = toJson(payload);
        log.info("label studio create project url={}, payload={}", url, body);

        Map<String, Object> response = restTemplate.postForObject(
                url,
                new HttpEntity<>(body, buildHeaders()),
                Map.class
        );
        log.info("label studio create project response={}", response);
        if (response == null) {
            throw new IllegalStateException("label studio project create failed");
        }
        Object id = response.get("id");
        Object returnedTitle = response.get("title");
        Object returnedConfig = response.get("label_config");
        if (returnedTitle == null || returnedTitle.toString().isBlank() || returnedConfig == null || returnedConfig.toString().isBlank()) {
            throw new IllegalStateException("label studio project response missing title or label config");
        }
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        throw new IllegalStateException("label studio project create failed");
    }

    @Override
    public void importTasks(long projectId, List<LabelStudioTask> tasks) {
        List<Map<String, Object>> payload = tasks.stream()
                .map(task -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("image", task.imageUrl());
                    data.put("filename", task.filename());
                    Map<String, Object> item = new HashMap<>();
                    item.put("data", data);
                    return item;
                })
                .toList();
        log.info("label studio import tasks url={}, count={}, sample={}",
                baseUrl + "/api/projects/" + projectId + "/import",
                tasks.size(),
                payload.isEmpty() ? null : toJson(payload.get(0))
        );
        String url = baseUrl + "/api/projects/" + projectId + "/import";
        String body = toJson(payload);
        Map<String, Object> response = restTemplate.postForObject(
                url,
                new HttpEntity<>(body, buildHeaders()),
                Map.class
        );
        log.info("label studio import response={}", response);
        if (response == null) {
            throw new IllegalStateException("label studio import failed");
        }
    }

    @Override
    public String exportAnnotations(long projectId) {
        String response = restTemplate.exchange(
                baseUrl + "/api/projects/" + projectId + "/export?exportType=JSON",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(buildHeaders()),
                String.class
        ).getBody();
        if (response == null) {
            throw new IllegalStateException("label studio export failed");
        }
        return response;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiToken != null && !apiToken.isBlank()) {
            headers.set("Authorization", "Token " + apiToken);
        }
        return headers;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }
}
