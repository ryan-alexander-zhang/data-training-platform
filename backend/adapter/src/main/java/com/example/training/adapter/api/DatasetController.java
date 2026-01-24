package com.example.training.adapter.api;

import com.example.training.app.DatasetApplicationService;
import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetFile;
import com.example.training.domain.MultipartUploadPart;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TrainingResult;
import com.example.training.domain.UploadPart;
import com.example.training.domain.UploadSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@CrossOrigin
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
    public Response<DatasetResponse> createDataset(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @Valid @RequestBody CreateDatasetRequest request
    ) {
        Dataset dataset = service.createDataset(tenantId, request.name());
        return Response.ok(new DatasetResponse(
                dataset.id().value(),
                dataset.tenantId().value(),
                dataset.name(),
                dataset.status().name(),
                dataset.createdAt()
        ));
    }

    @GetMapping
    public Response<List<DatasetSummaryResponse>> listDatasets(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        List<DatasetSummaryResponse> payload = service.listDatasets(tenantId).stream()
                .map(dataset -> new DatasetSummaryResponse(
                        dataset.id().value(),
                        dataset.name(),
                        dataset.status().name(),
                        service.countFiles(tenantId, dataset.id().value()),
                        dataset.createdAt(),
                        dataset.updatedAt()
                ))
                .toList();
        return Response.ok(payload);
    }

    @GetMapping("/{datasetId}")
    public Response<DatasetDetailResponse> getDataset(
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
        String labelingUrl = null;
        try {
            labelingUrl = buildLabelStudioUrl(service.getLabelProject(tenantId, dataset.id().value()).labelStudioProjectId());
        } catch (IllegalArgumentException ignored) {
            // label project not created yet
        }
        return Response.ok(new DatasetDetailResponse(
                dataset.id().value(),
                dataset.name(),
                dataset.status().name(),
                service.countFiles(tenantId, dataset.id().value()),
                dataset.createdAt(),
                dataset.updatedAt(),
                labelingUrl,
                files,
                resultResponse
        ));
    }

    @PostMapping("/{datasetId}/files")
    public Response<DatasetFileResponse> uploadFile(
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
        return Response.ok(toFileResponse(stored));
    }

    @PostMapping("/{datasetId}/uploads")
    public Response<UploadSessionResponse> createUploadSession(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId,
            @Valid @RequestBody CreateUploadSessionRequest request
    ) {
        UploadSession session = service.createUploadSession(
                tenantId,
                datasetId,
                request.filename(),
                request.contentType(),
                request.size()
        );
        return Response.ok(new UploadSessionResponse(
                session.id(),
                session.uploadId(),
                session.partSize(),
                session.objectKey(),
                session.contentType()
        ));
    }

    @GetMapping("/{datasetId}/uploads/{uploadId}")
    public Response<UploadSessionDetailResponse> getUploadSession(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("uploadId") String uploadId
    ) {
        UploadSession session = service.getUploadSession(tenantId, datasetId, uploadId);
        List<UploadPartResponse> parts = service.listUploadParts(session.id()).stream()
                .map(part -> new UploadPartResponse(part.partNumber(), part.etag()))
                .toList();
        return Response.ok(new UploadSessionDetailResponse(session.uploadId(), session.partSize(), parts));
    }

    @PutMapping("/{datasetId}/uploads/{uploadId}/parts/{partNumber}")
    public Response<UploadPartResponse> uploadPart(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("uploadId") String uploadId,
            @PathVariable("partNumber") int partNumber,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        UploadPart part = service.uploadPart(
                tenantId,
                datasetId,
                uploadId,
                partNumber,
                file.getInputStream(),
                file.getSize()
        );
        return Response.ok(new UploadPartResponse(part.partNumber(), part.etag()));
    }


    @PostMapping("/{datasetId}/uploads/{uploadId}/complete")
    public Response<DatasetFileResponse> completeMultipartUpload(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("uploadId") String uploadId,
            @Valid @RequestBody CompleteUploadSessionRequest request
    ) {
        List<MultipartUploadPart> parts = request.parts().stream()
                .map(part -> new MultipartUploadPart(part.partNumber(), part.etag()))
                .toList();
        DatasetFile stored = service.completeMultipartUpload(
                tenantId,
                datasetId,
                uploadId,
                parts
        );
        return Response.ok(toFileResponse(stored));
    }

    @PostMapping("/{datasetId}/upload/complete")
    public Response<Void> completeUpload(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        service.completeUpload(tenantId, datasetId);
        return Response.ok(null);
    }

    @PostMapping("/{datasetId}/annotation/complete")
    public Response<Void> completeAnnotation(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        service.completeAnnotation(tenantId, datasetId);
        return Response.ok(null);
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

    @GetMapping("/{datasetId}/annotations")
    public Response<DatasetAnnotationResponse> exportAnnotations(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable("datasetId") UUID datasetId
    ) {
        String payload = service.exportAnnotations(tenantId, datasetId);
        return Response.ok(new DatasetAnnotationResponse(payload));
    }

    @GetMapping("/{datasetId}/files/{fileId}/preview")
    public ResponseEntity<InputStreamResource> previewFile(
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantId,
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("fileId") UUID fileId,
            @RequestParam(value = "tenantId", required = false) UUID tenantParam
    ) {
        UUID resolvedTenantId = tenantId != null ? tenantId : tenantParam;
        if (resolvedTenantId == null) {
            throw new IllegalStateException("tenant id required");
        }
        DatasetFile file = service.getFile(resolvedTenantId, datasetId, fileId);
        try {
            ObjectStorageService.StoredObject stored = storageService.download(file.objectKey());
            String contentType = file.contentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = stored.contentType();
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(stored.size())
                    .body(new InputStreamResource(stored.stream()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "file object not found: " + file.objectKey(),
                    exception
            );
        }
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

    private String buildLabelStudioUrl(long projectId) {
        String base = labelStudioBaseUrl.endsWith("/")
                ? labelStudioBaseUrl.substring(0, labelStudioBaseUrl.length() - 1)
                : labelStudioBaseUrl;
        return base + "/projects/" + projectId;
    }
}
