package com.ecoluminous.controller;

import com.ecoluminous.dto.user.UserLoginRequestDto; // 💡 로그인 DTO 추가 필요
import com.ecoluminous.dto.user.UserRegisterRequestDto;
import com.ecoluminous.dto.user.UserRegisterResponseDto;
import com.ecoluminous.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDto> register(@Valid @RequestBody UserRegisterRequestDto dto) {
        UserRegisterResponseDto response = userService.registerUser(dto);
        return ResponseEntity.ok(response);
    }

    // 💡 [신규 추가] 로그인
    @PostMapping("/login")
    public ResponseEntity<UserRegisterResponseDto> login(@Valid @RequestBody UserLoginRequestDto dto) {
        // userService.loginUser() 에서 로그인 성공 시 해당 유저의 정보(apiKey 포함)를 반환하도록 호출
        UserRegisterResponseDto response = userService.loginUser(dto);
        return ResponseEntity.ok(response);
    }
}