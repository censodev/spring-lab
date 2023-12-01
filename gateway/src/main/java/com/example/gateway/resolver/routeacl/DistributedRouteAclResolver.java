package com.example.gateway.resolver.routeacl;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import com.example.gateway.service.AuthServiceClient;
import com.example.gateway.service.payload.PermissionRouterResponseVO;
import com.example.gateway.service.payload.ResultVO;
import com.example.gateway.service.payload.RouterAnonymousVO;
import com.example.gateway.service.payload.RouterAuthorizedVO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DistributedRouteAclResolver implements RouteAclResolver {
    private final AuthServiceClient authServiceClient;

    public DistributedRouteAclResolver(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public List<AuthorizedRequest> getAuthorizedRequests() {
        try {
            ResultVO<PermissionRouterResponseVO> resultVO = authServiceClient.getPermissionRouter();
            if (resultVO.getData().getAuthorizeds() != null) {
                List<RouterAuthorizedVO> authorizedVOs = resultVO.getData().getAuthorizeds();
                return authorizedVOs.stream()
                        .map(pm -> new AuthorizedRequest(pm.getEndpoint(),
                                extractHttpMethods(pm.getMethods() != null ? Arrays.asList(pm.getMethods())
                                        : Collections.emptyList()),
                                extractPermissions(pm.getAuthorities() != null ? Arrays.asList(pm.getAuthorities())
                                        : Collections.emptyList())))
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
            ResultVO<PermissionRouterResponseVO> resultVO = authServiceClient.getPermissionRouter();
            if (resultVO.getData().getAnonymous() != null) {
                List<RouterAnonymousVO> anonymousVOs = resultVO.getData().getAnonymous();
                return anonymousVOs.stream()
                        .map(pm -> new PublicRequest(pm.getEndpoint(), extractHttpMethods(
                                pm.getMethods() != null ? Arrays.asList(pm.getMethods()) : Collections.emptyList())))
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

    private Set<HttpMethod> extractHttpMethods(Collection<String> methods) {
        return Optional.ofNullable(methods).map(Collection::stream)
                .map(methodsStream -> methodsStream.map(String::toUpperCase).map(HttpMethod::valueOf))
                .orElse(Stream.empty()).collect(Collectors.toSet());
    }

    private Set<String> extractPermissions(Collection<String> permissions) {
        return new HashSet<>(Optional.ofNullable(permissions).orElse(Collections.emptySet()));
    }
}
