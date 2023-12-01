package com.example.gateway.resolver.token;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface TokenResolver {
    String extract(ServerHttpRequest request) throws TokenResolveException;

    UserDetails resolve(String token) throws TokenResolveException;
}
