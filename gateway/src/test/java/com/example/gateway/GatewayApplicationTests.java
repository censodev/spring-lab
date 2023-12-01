package com.example.gateway;

import com.example.gateway.service.payload.GetUserDetailsResponse;
import com.example.gateway.service.payload.PermissionRouterResponseVO;
import com.example.gateway.service.payload.ResultVO;
import com.example.gateway.service.payload.RouterAnonymousVO;
import com.example.gateway.service.payload.RouterAuthorizedVO;
import com.example.gateway.service.payload.UserDetailsResponse;
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
        stubFor(get("/rest/api/v1/auth/permission-route").willReturn(okForJson(mockRouteAcl())));
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
        stubFor(get("/rest/api/v1/auth/permission-route")
                .willReturn(serverError()));
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(false, new String[]{}))));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_exceptionWhenResolveToken_401() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(serverError()));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_invalidToken_401() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(false, new String[]{}))));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authorized_2xx() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(true, new String[]{}))));
        webTestClient.get().uri("/authorized")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorizedAnyMethod_2xx() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(true, new String[]{}))));
        webTestClient.get().uri("/authorized-any-method")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorizedWithPermission_403() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(true, new String[]{}))));
        webTestClient.get().uri("/authorized-with-permission")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isEqualTo(HttpStatusCode.valueOf(403));
    }

    @Test
    void authorizedWithPermission_2xx() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(true, new String[]{"read"}))));
        webTestClient.get().uri("/authorized-with-permission")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void authorizedWithPermissionAnyMethod_2xx() {
        stubFor(get("/rest/api/v1/userdetails")
                .willReturn(okForJson(mockUserDetails(true, new String[]{"read"}))));
        webTestClient.get().uri("/authorized-with-permission-any-method")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    private GetUserDetailsResponse mockUserDetails(boolean isActive, String[] permissions) {
        var mocked = new GetUserDetailsResponse();
        mocked.setData(UserDetailsResponse.builder()
                .id(1L)
                .username("1")
                .permissions(Arrays.stream(permissions)
                        .map(p -> {
                            var permission = new UserDetailsResponse.Permission();
                            permission.setCode(p);
                            return permission;
                        })
                        .toList())
                .isActive(isActive)
                .build());
        return mocked;
    }

    private ResultVO<PermissionRouterResponseVO> mockRouteAcl() {
        List<RouterAnonymousVO> anonymous = List.of(
                new RouterAnonymousVO("/anonymous", new String[]{"get"}),
                new RouterAnonymousVO("/anonymous-any-method", null)
        );
        List<RouterAuthorizedVO> authorized = List.of(
                new RouterAuthorizedVO("/authorized", new String[]{"get"}, null),
                new RouterAuthorizedVO("/authorized-any-method", null, null),
                new RouterAuthorizedVO("/authorized-with-permission", new String[]{"get"}, new String[]{"read"}),
                new RouterAuthorizedVO("/authorized-with-permission-any-method", null, new String[]{"read"})
        );
        return new ResultVO<>(new PermissionRouterResponseVO(anonymous, authorized), "");
    }
}
