package com.example.gateway.service.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouterAuthorizedVO {

	private String endpoint;

	private String[] methods;

	private String[] authorities;
}
