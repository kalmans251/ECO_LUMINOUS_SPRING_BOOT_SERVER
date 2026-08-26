package com.ecoluminous.dto.rail;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RailModeConfirmDto {
    private String apiKey;
    private Integer railSeq;      // 0 이면 전체 제어 결과
    private Integer railMode;     // 변경된 LED 모드 번호
    private List<Integer> successRails; // 성공한 난간 번호 리스트 [1, 2, 3, ...]
    private List<Integer> failRails;    // 실패한 난간 번호 리스트 [15]
}