package com.example.training.adapter.api;

public record Response<T>(boolean success, String message, T data) {
    public static <T> Response<T> ok(T data) {
        return new Response<>(true, "OK", data);
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(false, message, null);
    }
}
