package com.example.training.domain;

import java.util.List;

public interface LabelStudioService {
    long createProject(String title, String description, String labelConfig);

    void importTasks(long projectId, List<LabelStudioTask> tasks);

    String exportAnnotations(long projectId);

    LabelStudioProjectSummary getProjectSummary(long projectId);

    void ensureWebhook(long projectId, String url);
}
