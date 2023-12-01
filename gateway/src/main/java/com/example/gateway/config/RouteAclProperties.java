package com.example.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "resolver.route-acl.mappings")
@Getter
@Setter
public class RouteAclProperties {
    private List<RouteAclMapping> anonymous;
    private List<RouteAclMapping> authorized;

    public record RouteAclMapping(String route, Set<String> methods, Set<String> permissions) {
    }
}
