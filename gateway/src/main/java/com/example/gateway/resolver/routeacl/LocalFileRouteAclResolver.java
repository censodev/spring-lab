package com.example.gateway.resolver.routeacl;

import org.springframework.http.HttpMethod;
import com.example.gateway.config.RouteAclProperties;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFileRouteAclResolver implements RouteAclResolver {
    private final List<AuthorizedRequest> authorizedRequests;
    private final List<PublicRequest> publicRequests;

    public LocalFileRouteAclResolver(RouteAclProperties routeAclProperties) {
        authorizedRequests = routeAclProperties.getAuthorized()
                .stream()
                .map(pm -> new AuthorizedRequest(
                        pm.route(),
                        extractHttpMethods(pm.methods()),
                        extractPermissions(pm.permissions())))
                .toList();
        publicRequests = routeAclProperties.getAnonymous()
                .stream()
                .map(pm -> new PublicRequest(pm.route(), extractHttpMethods(pm.methods())))
                .toList();
    }

    @Override
    public List<AuthorizedRequest> getAuthorizedRequests() {
        return authorizedRequests;
    }

    @Override
    public List<PublicRequest> getPublicRequests() {
        return publicRequests;
    }

    private Set<HttpMethod> extractHttpMethods(Collection<String> methods) {
        return Optional.ofNullable(methods)
                .map(Collection::stream)
                .map(methodsStream -> methodsStream
                        .map(String::toUpperCase)
                        .map(HttpMethod::valueOf))
                .orElse(Stream.empty())
                .collect(Collectors.toSet());
    }

    private Set<String> extractPermissions(Collection<String> permissions) {
        return new HashSet<>(Optional.ofNullable(permissions).orElse(Collections.emptySet()));
    }
}
