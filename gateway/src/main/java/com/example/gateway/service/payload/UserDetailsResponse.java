package com.example.gateway.service.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class UserDetailsResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonIgnore
    @JsonProperty("password")
    private String password;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("roles")
    private List<Role> roles;

    @JsonProperty("permissions")
    private List<Permission> permissions;

    @Getter
    @Setter
    public static class Permission {

        private Long adPermissionId;

        private String code;

        private String name;

        private Integer seq_no;
    }

    @Getter
    @Setter
    public static class Role {

        private Long adRoleId;

        private String code;

        private String name;

        private Integer level;
    }
}
