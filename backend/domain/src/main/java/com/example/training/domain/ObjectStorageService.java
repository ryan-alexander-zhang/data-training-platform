package com.example.training.domain;

import java.io.InputStream;

/**
 * 对象存储服务抽象。
 */
public interface ObjectStorageService {
    void upload(String objectKey, InputStream inputStream, long size, String contentType);

    StoredObject download(String objectKey);

    record StoredObject(InputStream stream, long size, String contentType) {
    }
}
