package com.example.training.adapter.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台配置查询。
 */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final String labelStudioBaseUrl;
    private final String minioBucket;
    private final String minioEndpoint;

    public SettingsController(
            @Value("${training.label-studio.base-url:http://localhost:8080}") String labelStudioBaseUrl,
            @Value("${training.storage.minio.bucket:training-data}") String minioBucket,
            @Value("${training.storage.minio.endpoint:http://localhost:9000}") String minioEndpoint
    ) {
        this.labelStudioBaseUrl = labelStudioBaseUrl;
        this.minioBucket = minioBucket;
        this.minioEndpoint = minioEndpoint;
    }

    @GetMapping
    public Response<SettingsResponse> getSettings() {
        return Response.ok(new SettingsResponse(labelStudioBaseUrl, minioBucket, minioEndpoint));
    }
}
