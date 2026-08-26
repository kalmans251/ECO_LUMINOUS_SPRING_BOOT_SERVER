package com.ecoluminous.dto.rail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RailModeRequestDto {

    @NotBlank(message = "API Key는 필수입니다.")
    private String apiKey;

    @NotNull(message = "난간 번호(seq)는 필수입니다.")
    private Integer railSeq;

    @NotNull(message = "변경할 레일 모드는 필수입니다.")
    private Integer railMode;
}