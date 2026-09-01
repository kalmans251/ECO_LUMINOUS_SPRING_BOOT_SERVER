package com.ecoluminous.dto.rail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter // 💡 Setter 추가 (스프링이 JSON 값을 넣을 수 있도록)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RailModeRequestDto {

    @NotBlank(message = "API Key는 필수입니다.")
    private String apiKey;

    @NotNull(message = "난간 번호(seq)는 필수입니다.")
    private Integer railSeq;

    private Integer railMode;

    // 💡 [핵심] @NotNull 삭제 (상태 조회 시에는 배열을 보내지 않으므로 에러 방지)
    private List<Integer> commandBytes;

    // 💡 [핵심] 웹에서 파이썬으로 "STATUS_REQUEST" (상태 조회) 명령을 지시하기 위한 변수
    private String commandType;
}