package com.example.training.adapter.api;

import com.example.training.app.DatasetApplicationService;
import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetFile;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TrainingResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 数据集 API。
 * <p>
 * - 创建数据集
 * - 上传文件
 * - 标注完成通知
 * - 查询数据集详情
 */
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {
    private final DatasetApplicationService service;
    private final ObjectStorageService storageService;
    private final String labelStudioBaseUrl;

    public DatasetController(DatasetApplicationService service,
                             ObjectStorageService storageService,
                             @Value("${training.label-studio.base-url:http://localhost:8080}") String labelStudioBaseUrl) {
        this.service = service;
        this.storageService = storageService;
        this.labelStudioBaseUrl = labelStudioBaseUrl;
    }

    @PostMapping
    public DatasetResponse createDataset(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody CreateDatasetRequest request
    ) {
        Dataset dataset = service.createDataset(tenantId, request.name());
        return new DatasetResponse(
                dataset.id().value(),
                dataset.tenantId().value(),
                dataset.name(),
                dataset.status().name(),
                dataset.createdAt()
        );
    }

    @GetMapping
    public List<DatasetSummaryResponse> listDatasets(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return service.listDatasets(tenantId).stream()
                .map(dataset -> new DatasetSummaryResponse(
                        dataset.id().value(),
                        dataset.name(),
                        dataset.status().name(),
                        service.countFiles(tenantId, dataset.id().value()),
                        dataset.createdAt(),
                        dataset.updatedAt()
                ))
                .toList();
    }

    @GetMapping("/{datasetId}")
    public DatasetDetailResponse getDataset(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        Dataset dataset = service.getDataset(tenantId, datasetId);
        List<DatasetFileResponse> files = service.listFiles(tenantId, datasetId).stream()
                .map(this::toFileResponse)
                .toList();
        TrainingResultResponse resultResponse = null;
        try {
            TrainingResult result = service.getTrainingResult(tenantId, datasetId);
            resultResponse = new TrainingResultResponse(result.artifactKey(), result.metricsKey(), result.createdAt());
        } catch (IllegalArgumentException ignored) {
            // no training result yet
        }
        return new DatasetDetailResponse(
                dataset.id().value(),
                dataset.name(),
                dataset.status().name(),
                service.countFiles(tenantId, dataset.id().value()),
                dataset.createdAt(),
                dataset.updatedAt(),
                buildLabelStudioUrl(dataset.id().value()),
                files,
                resultResponse
        );
    }

    @PostMapping("/{datasetId}/files")
    public DatasetFileResponse uploadFile(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        DatasetFile stored = service.uploadFile(
                tenantId,
                datasetId,
                Optional.ofNullable(file.getOriginalFilename()).orElse("upload.bin"),
                Optional.ofNullable(file.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE),
                file.getSize(),
                file.getInputStream()
        );
        return toFileResponse(stored);
    }

    @PostMapping("/{datasetId}/upload/complete")
    public HttpStatus completeUpload(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        service.completeUpload(tenantId, datasetId);
        return HttpStatus.ACCEPTED;
    }

    @PostMapping("/{datasetId}/annotation/complete")
    public HttpStatus completeAnnotation(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        service.completeAnnotation(tenantId, datasetId);
        return HttpStatus.ACCEPTED;
    }

    @GetMapping("/{datasetId}/results/model")
    public ResponseEntity<InputStreamResource> downloadModel(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        TrainingResult result = service.getTrainingResult(tenantId, datasetId);
        ObjectStorageService.StoredObject stored = storageService.download(result.artifactKey());
        return buildDownloadResponse(stored, "model.bin");
    }

    @GetMapping("/{datasetId}/results/metrics")
    public ResponseEntity<InputStreamResource> downloadMetrics(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        TrainingResult result = service.getTrainingResult(tenantId, datasetId);
        ObjectStorageService.StoredObject stored = storageService.download(result.metricsKey());
        return buildDownloadResponse(stored, "metrics.json");
    }

    private ResponseEntity<InputStreamResource> buildDownloadResponse(ObjectStorageService.StoredObject stored, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(stored.contentType()))
                .contentLength(stored.size())
                .body(new InputStreamResource(stored.stream()));
    }

    private DatasetFileResponse toFileResponse(DatasetFile file) {
        return new DatasetFileResponse(
                file.id(),
                file.filename(),
                file.objectKey(),
                file.size(),
                file.contentType(),
                file.uploadedAt()
        );
    }

    private String buildLabelStudioUrl(UUID datasetId) {
        String base = labelStudioBaseUrl.endsWith("/")
                ? labelStudioBaseUrl.substring(0, labelStudioBaseUrl.length() - 1)
                : labelStudioBaseUrl;
        return base + "/projects/" + datasetId;
    }
}
