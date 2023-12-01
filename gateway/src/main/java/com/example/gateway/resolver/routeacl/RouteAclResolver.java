package com.example.gateway.resolver.routeacl;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

public interface RouteAclResolver {
    List<AuthorizedRequest> getAuthorizedRequests();

    List<PublicRequest> getPublicRequests();

    record AuthorizedRequest(String routePattern, Set<HttpMethod> methods, Set<String> authorities) {
    }

    record PublicRequest(String routePattern, Set<HttpMethod> methods) {
    }
}
