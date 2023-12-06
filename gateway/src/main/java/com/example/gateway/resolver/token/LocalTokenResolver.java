package com.example.gateway.resolver.token;

import com.example.gateway.authentication.DefaultUserDetails;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class LocalTokenResolver implements TokenResolver {
    @Override
    public String extract(ServerHttpRequest request) throws TokenResolveException {
        return "";
    }

    @Override
    public UserDetails resolve(String token) throws TokenResolveException {
        return new DefaultUserDetails(1L, "1", true, List.of(new SimpleGrantedAuthority("1")));
    }
}
