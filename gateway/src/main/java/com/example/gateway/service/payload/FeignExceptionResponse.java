package com.example.gateway.service.payload;

import lombok.Data;

@Data
public class FeignExceptionResponse {

    private String message;

    private Object data;
}
