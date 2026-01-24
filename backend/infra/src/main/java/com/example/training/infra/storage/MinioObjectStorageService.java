package com.example.training.infra.storage;

import com.example.training.domain.MultipartUpload;
import com.example.training.domain.MultipartUploadPart;
import com.example.training.domain.MultipartUploadedPart;
import com.example.training.domain.ObjectStorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public MultipartUpload initMultipartUpload(String objectKey, String contentType) {
        ensureBucket();
        return new MultipartUpload(UUID.randomUUID().toString(), 20L * 1024L * 1024L);
    }

    @Override
    public MultipartUploadedPart uploadPart(String objectKey, String uploadId, int partNumber, InputStream inputStream, long size) {
        ensureBucket();
        try {
            String partObjectKey = buildPartKey(objectKey, uploadId, partNumber);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(partObjectKey)
                            .stream(inputStream, size, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
            return new MultipartUploadedPart(partNumber, UUID.randomUUID().toString(), size);
        } catch (Exception exception) {
            throw new IllegalStateException("upload part failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void completeMultipartUpload(String objectKey, String uploadId, List<MultipartUploadPart> parts) {
        ensureBucket();
        try {
            List<ComposeSource> sources = parts.stream()
                    .sorted(Comparator.comparingInt(MultipartUploadPart::partNumber))
                    .map(part -> ComposeSource.builder()
                            .bucket(bucketName)
                            .object(buildPartKey(objectKey, uploadId, part.partNumber()))
                            .build())
                    .collect(Collectors.toList());
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .sources(sources)
                            .build()
            );
            for (MultipartUploadPart part : parts) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(buildPartKey(objectKey, uploadId, part.partNumber()))
                                .build()
                );
            }
        } catch (Exception exception) {
            throw new IllegalStateException("complete multipart upload failed: " + exception.getMessage(), exception);
        }
    }

    private String buildPartKey(String objectKey, String uploadId, int partNumber) {
        return objectKey + ".part-" + uploadId + "-" + partNumber;
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
