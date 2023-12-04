package com.example.gateway.service.payload;

import java.util.List;

public record UserDetailsRes(Long id,
                             String username,
                             Boolean isActive,
                             List<Permission> permissions) {
    public record Permission(String code) {
    }
}
