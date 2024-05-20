package com.login.springlogin.dto.response;

import lombok.Builder;

@Builder
public record UserDTO(
    String User,
    String email
){
}
