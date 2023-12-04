package com.example.gateway.service;

import com.example.gateway.service.payload.RouteAclRes;
import com.example.gateway.service.payload.UserDetailsRes;
import com.example.gateway.service.payload.WrapperRes;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authServiceClient", url = "${resolver.route-acl.uri}")
public interface AuthServiceClient {

    @GetMapping("/resolve/token")
    @Headers({
            "Content-Type: application/json",
            "Authorization: {token}"
    })
    WrapperRes<UserDetailsRes> getUserDetails(@RequestHeader("Authorization") String token);

    @GetMapping("/resolve/route-acl")
    @Headers({
            "Content-Type: application/json"
    })
    WrapperRes<RouteAclRes> getPermissionRouter();
}
