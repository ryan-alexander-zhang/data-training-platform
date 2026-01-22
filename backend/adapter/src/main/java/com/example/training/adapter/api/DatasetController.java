package com.example.training.adapter.api;

import com.example.training.app.DatasetApplicationService;
import com.example.training.domain.Dataset;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 数据集 API。
 * <p>
 * - 创建数据集
 * - 标注完成通知
 * - 后续可扩展：分片上传会话、OSS 直传签名等
 */
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {
    private final DatasetApplicationService service;

    public DatasetController(DatasetApplicationService service) {
        this.service = service;
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

    @PostMapping("/{datasetId}/annotation/complete")
    public HttpStatus completeAnnotation(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID datasetId
    ) {
        service.completeAnnotation(tenantId, datasetId);
        return HttpStatus.ACCEPTED;
    }
}
