package com.example.training.adapter.api;

import com.example.training.app.DatasetApplicationService;
import com.example.training.domain.LabelProject;
import com.example.training.domain.LabelProjectRepository;
import com.example.training.domain.LabelStudioProjectSummary;
import com.example.training.domain.LabelStudioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@lombok.extern.slf4j.Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class LabelStudioWebhookController {
    private final DatasetApplicationService datasetService;
    private final LabelProjectRepository labelProjectRepository;
    private final LabelStudioService labelStudioService;
    private final String webhookToken;

    public LabelStudioWebhookController(DatasetApplicationService datasetService,
                                        LabelProjectRepository labelProjectRepository,
                                        LabelStudioService labelStudioService,
                                        @Value("${training.label-studio.webhook-token:}") String webhookToken) {
        this.datasetService = datasetService;
        this.labelProjectRepository = labelProjectRepository;
        this.labelStudioService = labelStudioService;
        this.webhookToken = webhookToken;
    }

    @PostMapping("/label-studio")
    public Response<Void> onLabelStudioWebhook(
            @RequestParam(value = "token", required = false) String token,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        if (webhookToken != null && !webhookToken.isBlank()) {
            if (token == null || !webhookToken.equals(token)) {
                log.warn("label studio webhook token mismatch");
                return Response.ok(null);
            }
        }

        Long projectId = extractProjectId(payload);
        if (projectId == null) {
            log.info("label studio webhook without project id, payload={}", payload);
            return Response.ok(null);
        }

        LabelProject project = labelProjectRepository.findByLabelStudioProjectId(projectId)
                .orElse(null);
        if (project == null) {
            log.info("label studio webhook unknown projectId={}", projectId);
            return Response.ok(null);
        }

        try {
            LabelStudioProjectSummary summary = labelStudioService.getProjectSummary(projectId);
            if (!summary.isCompleted()) {
                log.info(
                        "label studio project not completed yet: projectId={}, taskNumber={}, annotated={}",
                        projectId,
                        summary.taskNumber(),
                        summary.numTasksWithAnnotations()
                );
                return Response.ok(null);
            }

            datasetService.completeAnnotation(project.tenantId().value(), project.datasetId().value());
            log.info("label studio project completed, triggered completeAnnotation: projectId={}", projectId);
        } catch (Exception exception) {
            log.error("label studio webhook handling failed: projectId={}", projectId, exception);
        }

        return Response.ok(null);
    }

    private Long extractProjectId(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        Object direct = firstNonNull(
                payload.get("project"),
                payload.get("project_id"),
                payload.get("projectId")
        );
        Long parsed = tryParseLong(direct);
        if (parsed != null) {
            return parsed;
        }

        Object projectObject = payload.get("project");
        if (projectObject instanceof Map<?, ?> map) {
            Object id = map.get("id");
            return tryParseLong(id);
        }

        return null;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long tryParseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
