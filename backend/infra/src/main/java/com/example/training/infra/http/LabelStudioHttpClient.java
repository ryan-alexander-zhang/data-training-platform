package com.example.training.infra.http;

import com.example.training.domain.LabelStudioProjectSummary;
import com.example.training.domain.LabelStudioService;
import com.example.training.domain.LabelStudioTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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

    @Override
    public LabelStudioProjectSummary getProjectSummary(long projectId) {
        String url = baseUrl + "/api/projects/" + projectId;
        Map<String, Object> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(buildHeaders()),
                Map.class
        ).getBody();
        if (response == null) {
            throw new IllegalStateException("label studio project query failed");
        }
        long taskNumber = toLong(response.get("task_number"));
        long numTasksWithAnnotations = toLong(response.get("num_tasks_with_annotations"));
        return new LabelStudioProjectSummary(taskNumber, numTasksWithAnnotations);
    }

    @Override
    public void ensureWebhook(long projectId, String url) {
        Objects.requireNonNull(url, "url is required");
        String listUrl = baseUrl + "/api/webhooks/";
        List<Map<String, Object>> webhooks;
        try {
            webhooks = restTemplate.exchange(
                    listUrl,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    List.class
            ).getBody();
        } catch (RestClientException exception) {
            throw new IllegalStateException("label studio webhook list failed", exception);
        }

        if (webhooks != null) {
            boolean exists = webhooks.stream().anyMatch(item -> {
                long project = toLong(item.get("project"));
                Object itemUrl = item.get("url");
                return project == projectId && itemUrl != null && url.equals(itemUrl.toString());
            });
            if (exists) {
                return;
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("project", projectId);
        payload.put("url", url);
        String body = toJson(payload);
        log.info("label studio create webhook url={}, payload={}", listUrl, body);
        Map<String, Object> created = restTemplate.postForObject(
                listUrl,
                new HttpEntity<>(body, buildHeaders()),
                Map.class
        );
        log.info("label studio create webhook response={}", created);
        if (created == null) {
            throw new IllegalStateException("label studio webhook create failed");
        }
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

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
