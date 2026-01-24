package com.example.training.infra.storage;

import com.example.training.domain.ObjectStorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import java.io.InputStream;

/**
 * MinIO 对象存储实现。
 */
public class MinioObjectStorageService implements ObjectStorageService {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioObjectStorageService(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        ensureBucket();
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("upload to minio failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public StoredObject download(String objectKey) {
        try {
            var stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            return new StoredObject(stream, stat.size(), stat.contentType());
        } catch (Exception exception) {
            throw new IllegalArgumentException("object not found");
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
        } catch (Exception exception) {
            throw new IllegalStateException("minio bucket init failed: " + exception.getMessage(), exception);
        }
    }
}
