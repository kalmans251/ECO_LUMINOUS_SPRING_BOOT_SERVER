package com.ecoluminous.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegisterResponseDto {
    private Long userId;
    private String email;
    private String name;
    private String apiKey; // 라즈베리파이에 넣을 16자리 API 키
}