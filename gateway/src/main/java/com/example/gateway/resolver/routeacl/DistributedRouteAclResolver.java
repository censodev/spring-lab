package com.example.gateway.resolver.routeacl;

import com.example.gateway.service.AuthServiceClient;
import com.example.gateway.service.payload.RouteAclRes;
import com.example.gateway.service.payload.WrapperRes;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DistributedRouteAclResolver implements RouteAclResolver {
    private final AuthServiceClient authServiceClient;

    public DistributedRouteAclResolver(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public List<AuthorizedRequest> getAuthorizedRequests() {
        try {
            WrapperRes<RouteAclRes> wrapperRes = authServiceClient.getPermissionRouter();
            if (wrapperRes.data().authorized() != null) {
                return wrapperRes.data().authorized()
                        .stream()
                        .map(pm -> new AuthorizedRequest(
                                pm.endpoint(),
                                extractHttpMethods(pm.methods()),
                                extractAuthorities(pm.authorities())))
                        .toList();
            }
        } catch (FeignException e) {
            log.error(e.getMessage());
            log.error("FeignException->contentUTF8: {}", e.contentUTF8());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public List<PublicRequest> getPublicRequests() {
        try {
            WrapperRes<RouteAclRes> wrapperRes = authServiceClient.getPermissionRouter();
            if (wrapperRes.data().anonymous() != null) {
                return wrapperRes.data().anonymous()
                        .stream()
                        .map(pm -> new PublicRequest(pm.endpoint(), extractHttpMethods(pm.methods())))
                        .toList();
            }
        } catch (FeignException e) {
            log.error(e.getMessage());
            log.error("FeignException->contentUTF8: {}", e.contentUTF8());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    private Set<HttpMethod> extractHttpMethods(String[] methods) {
        return Optional.ofNullable(methods)
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .map(String::toUpperCase)
                .map(HttpMethod::valueOf)
                .collect(Collectors.toSet());
    }

    private Set<String> extractAuthorities(String[] authorities) {
        return Optional.ofNullable(authorities)
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }
}
