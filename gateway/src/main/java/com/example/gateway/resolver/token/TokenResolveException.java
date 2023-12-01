package com.example.gateway.resolver.token;

import org.springframework.security.core.AuthenticationException;

public class TokenResolveException extends AuthenticationException {
    public TokenResolveException(String message) {
        super(message);
    }

    public TokenResolveException(String message, Throwable e) {
        super(message, e);
    }
}
