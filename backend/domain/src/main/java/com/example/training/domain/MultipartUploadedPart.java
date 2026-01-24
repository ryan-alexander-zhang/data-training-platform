package com.example.training.domain;

public record MultipartUploadedPart(int partNumber, String etag, long size) {
}
