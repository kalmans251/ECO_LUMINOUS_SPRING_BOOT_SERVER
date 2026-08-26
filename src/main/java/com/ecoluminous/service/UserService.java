package com.ecoluminous.service;

import com.ecoluminous.dto.user.UserLoginRequestDto;
import com.ecoluminous.dto.user.UserRegisterRequestDto;
import com.ecoluminous.dto.user.UserRegisterResponseDto;
import com.ecoluminous.entity.User;
import com.ecoluminous.repository.UserRepository;
import com.ecoluminous.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 1. 회원가입
    public UserRegisterResponseDto registerUser(UserRegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 고유 API Key 생성 (중복 체크)
        String apiKey;
        do {
            apiKey = ApiKeyGenerator.generateKey();
        } while (userRepository.findByApiKey(apiKey).isPresent());

        User user = User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .name(dto.getName())
                .apiKey(apiKey)
                .build();

        User savedUser = userRepository.save(user);

        return UserRegisterResponseDto.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .apiKey(apiKey)
                .build();
    }

    // 💡 2. [신규 추가] 로그인 메서드
    @Transactional(readOnly = true)
    public UserRegisterResponseDto loginUser(UserLoginRequestDto dto) {
        // 이메일 존재 여부 확인
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 비밀번호 일치 여부 검증 (현재 평문 저장 방식 기준)
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 시 API Key가 포함된 DTO 반환
        return UserRegisterResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .apiKey(user.getApiKey())
                .build();
    }
}