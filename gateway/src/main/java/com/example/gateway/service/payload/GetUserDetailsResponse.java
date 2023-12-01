package com.example.gateway.service.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserDetailsResponse {

    private UserDetailsResponse data;

    private String message;
}
