package com.example.gateway.config;

import com.example.gateway.authentication.AuthenticationConverter;
import com.example.gateway.authentication.AuthenticationEntryPoint;
import com.example.gateway.authentication.AuthenticationManager;
import com.example.gateway.authorization.AccessDeniedHandler;
import com.example.gateway.authorization.AuthorizationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authorization.AuthorizationWebFilter;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import com.example.gateway.resolver.routeacl.RouteAclResolver;
import com.example.gateway.resolver.token.TokenResolver;
import com.example.gateway.resolver.routeacl.DistributedRouteAclResolver;
import com.example.gateway.resolver.token.DistributedTokenResolver;
import com.example.gateway.resolver.routeacl.LocalFileRouteAclResolver;
import com.example.gateway.resolver.token.LocalTokenResolver;
import com.example.gateway.service.AuthServiceClient;

@Configuration
@Slf4j
public class SecurityConfig {
    @Value("${resolver.token.type}")
    private String tokenResolverType;

    @Value("${resolver.route-acl.type}")
    private String routeAclResolverType;

    @Bean
    TokenResolver tokenResolver(AuthServiceClient authServiceClient,
                                ObjectMapper objectMapper) {
        var resolver = switch (tokenResolverType) {
            case "local" -> new LocalTokenResolver();
            case "distributed" -> new DistributedTokenResolver(authServiceClient, objectMapper);
            default -> throw new IllegalStateException("Unexpected value: " + tokenResolverType);
        };
        log.info("Token resolver: {}", resolver.getClass().getName());
        return resolver;
    }

    @Bean
    RouteAclResolver routeAclResolver(RouteAclProperties routeAclProperties,
                                      AuthServiceClient authServiceClient) {
        var resolver = switch (routeAclResolverType) {
            case "local-file" -> new LocalFileRouteAclResolver(routeAclProperties);
            case "distributed" -> new DistributedRouteAclResolver(authServiceClient);
            default -> throw new IllegalStateException("Unexpected value: " + routeAclResolverType);
        };
        log.info("Route ACL resolver: {}", resolver.getClass().getName());
        return resolver;
    }

    @Bean
    AuthenticationWebFilter authenticationWebFilter(TokenResolver tokenResolver,
                                                    RouteAclResolver routeAclResolver,
                                                    ServerAuthenticationEntryPoint serverAuthenticationEntryPoint) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(new AuthenticationManager());
        filter.setServerAuthenticationConverter(new AuthenticationConverter(tokenResolver, routeAclResolver));
        filter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(serverAuthenticationEntryPoint));
        return filter;
    }

    @Bean
    AuthorizationWebFilter authorizationWebFilter(RouteAclResolver routeAclResolver) {
        return new AuthorizationWebFilter(new AuthorizationManager(routeAclResolver));
    }

    @Bean
    ServerAuthenticationEntryPoint serverAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new AuthenticationEntryPoint(objectMapper);
    }

    @Bean
    ServerAccessDeniedHandler serverAccessDeniedHandler(ObjectMapper objectMapper) {
        return new AccessDeniedHandler(objectMapper);
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                     AuthenticationWebFilter authenticationWebFilter,
                                                     AuthorizationWebFilter authorizationWebFilter,
                                                     ServerAuthenticationEntryPoint serverAuthenticationEntryPoint,
                                                     ServerAccessDeniedHandler serverAccessDeniedHandler) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().authenticated())
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(authorizationWebFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(serverAuthenticationEntryPoint)
                        .accessDeniedHandler(serverAccessDeniedHandler))
                .build();
    }
}
