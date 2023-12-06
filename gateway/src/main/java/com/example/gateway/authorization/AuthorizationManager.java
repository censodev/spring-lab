package com.example.gateway.authorization;

import com.example.gateway.resolver.routeacl.RouteAclResolver;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class AuthorizationManager implements ReactiveAuthorizationManager<ServerWebExchange> {
    private final RouteAclResolver routeAclResolver;

    public AuthorizationManager(RouteAclResolver routeAclResolver) {
        this.routeAclResolver = routeAclResolver;
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, ServerWebExchange exchange) {
        ReactiveAuthorizationManager<AuthorizationContext> permitAllAuthorizationManager = (auth, context) -> Mono.just(new AuthorizationDecision(true));
        List<ServerWebExchangeMatcherEntry<ReactiveAuthorizationManager<AuthorizationContext>>> mappings = Stream
                .concat(
                        routeAclResolver.getPublicRequests()
                                .stream()
                                .flatMap(req -> buildMatchers(req.routePattern(), req.methods())
                                        .stream()
                                        .map(matcher -> new ServerWebExchangeMatcherEntry<>(matcher, permitAllAuthorizationManager))),
                        routeAclResolver.getAuthorizedRequests().stream()
                                .flatMap(req -> {
                                    var permissions = req.authorities().toArray(new String[0]);
                                    ReactiveAuthorizationManager<AuthorizationContext> entry = permissions.length == 0
                                            ? permitAllAuthorizationManager
                                            : AuthorityReactiveAuthorizationManager.hasAnyAuthority(permissions);
                                    return buildMatchers(req.routePattern(), req.methods())
                                            .stream()
                                            .map(matcher -> new ServerWebExchangeMatcherEntry<>(matcher, entry));
                                }))
                .toList();
        return Flux.fromIterable(mappings)
                .concatMap(mapping -> mapping.getMatcher()
                        .matches(exchange)
                        .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                        .map(ServerWebExchangeMatcher.MatchResult::getVariables)
                        .flatMap(variables -> mapping.getEntry()
                                .check(authentication, new AuthorizationContext(exchange, variables))
                        ))
                .next()
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    private List<PathPatternParserServerWebExchangeMatcher> buildMatchers(String routePattern, Set<HttpMethod> methods) {
        return CollectionUtils.isEmpty(methods)
                ? List.of(new PathPatternParserServerWebExchangeMatcher(routePattern))
                : methods
                .stream()
                .map(method -> new PathPatternParserServerWebExchangeMatcher(routePattern, method))
                .toList();
    }
}
