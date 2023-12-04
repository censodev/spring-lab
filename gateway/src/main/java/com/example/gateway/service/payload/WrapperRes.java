package com.example.gateway.service.payload;

public record WrapperRes<T>(T data,
                            String message) {
}
