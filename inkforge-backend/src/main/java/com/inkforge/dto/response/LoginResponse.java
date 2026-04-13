package com.inkforge.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String avatar;
    private String role;
}
