package com.example.gateway;

import com.example.gateway.service.payload.RouteAclRes;
import com.example.gateway.service.payload.UserDetailsRes;
import com.example.gateway.service.payload.WrapperRes;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 9999)
class GatewayApplicationTests {
    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        stubFor(get("/resolve/route-acl").willReturn(okForJson(mockRouteAcl())));
    }

    @Test
    void anonymous_2xx() {
        webTestClient.get().uri("/anonymous")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void anonymousAnyMethod_2xx() {
        webTestClient.get().uri("/anonymous-any-method")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorized_invalidHeader_401() {
        webTestClient.get().uri("/authorized")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_exceptionWhenResolveRouteAcl_401() {
        stubFor(get("/resolve/route-acl")
                .willReturn(serverError()));
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(false, new String[]{}))));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_exceptionWhenResolveToken_401() {
        stubFor(get("/resolve/token")
                .willReturn(serverError()));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_invalidToken_401() {
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(false, new String[]{}))));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_2xx() {
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(true, new String[]{}))));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorizedAnyMethod_2xx() {
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(true, new String[]{}))));
        webTestClient.get().uri("/authorized-any-method")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorizedWithPermission_403() {
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(true, new String[]{}))));
        webTestClient.get().uri("/authorized-with-permission")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isEqualTo(HttpStatusCode.valueOf(403));
    }

    @Test
    void authorizedWithPermission_2xx() {
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(true, new String[]{"read"}))));
        webTestClient.get().uri("/authorized-with-permission")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorizedWithPermissionAnyMethod_2xx() {
        stubFor(get("/resolve/token")
                .willReturn(okForJson(mockUserDetails(true, new String[]{"read"}))));
        webTestClient.get().uri("/authorized-with-permission-any-method")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    private WrapperRes<UserDetailsRes> mockUserDetails(boolean isActive, String[] permissions) {
        return new WrapperRes<>(new UserDetailsRes(
                1L,
                "1",
                isActive,
                Arrays.stream(permissions)
                        .map(UserDetailsRes.Permission::new)
                        .toList()
        ), "");
    }

    private WrapperRes<RouteAclRes> mockRouteAcl() {
        List<RouteAclRes.AnonymousRouteAcl> anonymous = List.of(
                new RouteAclRes.AnonymousRouteAcl("/anonymous", new String[]{"get"}),
                new RouteAclRes.AnonymousRouteAcl("/anonymous-any-method", null)
        );
        List<RouteAclRes.AuthorizedRouteAcl> authorized = List.of(
                new RouteAclRes.AuthorizedRouteAcl("/authorized", new String[]{"get"}, null),
                new RouteAclRes.AuthorizedRouteAcl("/authorized-any-method", null, null),
                new RouteAclRes.AuthorizedRouteAcl("/authorized-with-permission", new String[]{"get"}, new String[]{"read"}),
                new RouteAclRes.AuthorizedRouteAcl("/authorized-with-permission-any-method", null, new String[]{"read"})
        );
        return new WrapperRes<>(new RouteAclRes(anonymous, authorized), "");
    }
}
