package com.example.gateway.service;

import com.example.gateway.service.payload.GetUserDetailsResponse;
import com.example.gateway.service.payload.PermissionRouterResponseVO;
import com.example.gateway.service.payload.ResultVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authServiceClient", url = "${resolver.route-acl.uri}")
public interface AuthServiceClient {

    @GetMapping("/userdetails")
    @Headers({
            "Content-Type: application/json",
            "Authorization: {token}"
    })
    GetUserDetailsResponse getUserDetails(@RequestHeader("Authorization") String token);

    @GetMapping("/auth/permission-route")
    @Headers({
            "Content-Type: application/json"
    })
    ResultVO<PermissionRouterResponseVO> getPermissionRouter();
}
