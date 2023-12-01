package com.example.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockController {
    @GetMapping("/anonymous")
    public String anonymous() {
        return "ok";
    }

    @GetMapping("/anonymous-any-method")
    public String anonymousAnyMethod() {
        return "ok";
    }

    @GetMapping("/authorized")
    public String authorized() {
        return "ok";
    }

    @GetMapping("/authorized-any-method")
    public String authorizedAnyMethod() {
        return "ok";
    }

    @GetMapping("/authorized-with-permission")
    public String authorizedWithPermission() {
        return "ok";
    }

    @GetMapping("/authorized-with-permission-any-method")
    public String authorizedWithPermissionAnyMethod() {
        return "ok";
    }
}
