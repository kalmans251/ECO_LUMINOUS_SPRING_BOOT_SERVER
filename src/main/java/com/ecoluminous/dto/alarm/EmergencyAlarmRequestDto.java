package com.ecoluminous.dto.alarm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmergencyAlarmRequestDto {

    @NotBlank(message = "API Key는 필수입니다.")
    private String apiKey;

    @NotNull(message = "난간 번호(seq)는 필수입니다.")
    private Integer railSeq;

    @NotBlank(message = "알림 유형은 필수입니다.")
    private String alarmType;

    @NotBlank(message = "알림 메시지는 필수입니다.")
    private String message;
}