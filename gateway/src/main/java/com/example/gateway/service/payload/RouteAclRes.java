package com.example.gateway.service.payload;

import java.util.List;

public record RouteAclRes(List<AnonymousRouteAcl> anonymous,
                          List<AuthorizedRouteAcl> authorized) {
    public record AnonymousRouteAcl(String endpoint,
                                    String[] methods) {
    }

    public record AuthorizedRouteAcl(String endpoint,
                                     String[] methods,
                                     String[] authorities) {
    }
}
