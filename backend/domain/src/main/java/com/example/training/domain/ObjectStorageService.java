package com.example.training.domain;

import java.io.InputStream;

/**
 * 对象存储服务抽象。
 */
public interface ObjectStorageService {
    void upload(String objectKey, InputStream inputStream, long size, String contentType);

    StoredObject download(String objectKey);

    MultipartUpload initMultipartUpload(String objectKey, String contentType);

    MultipartUploadedPart uploadPart(String objectKey, String uploadId, int partNumber, InputStream inputStream, long size);

    void completeMultipartUpload(String objectKey, String uploadId, java.util.List<MultipartUploadPart> parts);

    record StoredObject(InputStream stream, long size, String contentType) {
    }
}
