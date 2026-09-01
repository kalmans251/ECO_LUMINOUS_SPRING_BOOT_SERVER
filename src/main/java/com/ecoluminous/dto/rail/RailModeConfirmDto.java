package com.ecoluminous.dto.rail;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RailModeConfirmDto {
    private String apiKey;
    private Integer railSeq;      // 0 이면 전체 제어 결과
    private Integer railMode;     // (기존 레거시 유지용 - 추후 삭제 가능)
    
    // 💡 [신규 추가] 5-Byte 원샷 마스터 상태 배열 수신용
    private List<Integer> commandBytes; 
    
    private List<Integer> successRails; // 성공한 난간 번호 리스트
    private List<Integer> failRails;    // 실패한 난간 번호 리스트
}