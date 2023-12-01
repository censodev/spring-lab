package com.example.gateway.authentication;

import com.example.gateway.resolver.routeacl.RouteAclResolver;
import com.example.gateway.resolver.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
public class AuthenticationConverter implements ServerAuthenticationConverter {
    private final TokenResolver tokenResolver;
    private final RouteAclResolver routeAclResolver;

    public AuthenticationConverter(TokenResolver tokenResolver,
                                   RouteAclResolver routeAclResolver) {
        this.tokenResolver = tokenResolver;
        this.routeAclResolver = routeAclResolver;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        var matchers = routeAclResolver.getPublicRequests()
                .stream()
                .map(publicReq -> buildMatchers(publicReq.routePattern(), publicReq.methods()))
                .flatMap(Collection::stream)
                .toList();
        return Flux.fromIterable(matchers)
                .concatMap(m -> m.matches(exchange))
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .next()
                .map(mr -> List.of(new SimpleGrantedAuthority("anonymous")))
                .map(authorities -> (Authentication) new UsernamePasswordAuthenticationToken("1", "1", authorities))
                .switchIfEmpty(Mono.defer(() -> {
                    final String token = tokenResolver.extract(exchange.getRequest());
                    final UserDetails userDetails = tokenResolver.resolve(token);
                    if (userDetails != null && userDetails.isEnabled()) {
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails.getUsername(),
                                userDetails.getPassword(),
                                userDetails.getAuthorities());
                        return Mono.just(auth);
                    }
                    return Mono.empty();
                }));
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
