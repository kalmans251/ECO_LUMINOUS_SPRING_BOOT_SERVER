package com.ecoluminous.dto.device;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceLogRequestDto {

    @NotBlank(message = "API Key는 필수입니다.")
    private String apiKey;

    @NotNull(message = "난간 번호(seq)는 필수입니다.")
    @Min(value = 1, message = "난간 번호는 1 이상이어야 합니다.")
    private Integer railSeq;

    @NotNull(message = "좌측 발전량은 필수입니다.")
    private Double leftWatt;

    @NotNull(message = "우측 발전량은 필수입니다.")
    private Double rightWatt;

    @NotNull(message = "배터리 잔량은 필수입니다.")
    @Min(value = 0, message = "배터리는 0% 이상이어야 합니다.")
    @Max(value = 100, message = "배터리는 100% 이하이어야 합니다.")
    private Integer batteryPct;

    @NotNull(message = "충전 여부는 필수입니다.")
    private Boolean isCharging;
}