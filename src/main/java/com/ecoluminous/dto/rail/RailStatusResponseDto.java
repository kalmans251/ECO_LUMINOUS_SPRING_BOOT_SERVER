package com.ecoluminous.dto.rail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RailStatusResponseDto {
    private Long railInfoId;
    private Integer railSeq;          // 난간 번호 (1~10번 등)
    private Integer railMode;         // 16비트 레일 모드
    private Object status;            // "ACTIVE", "OFFLINE" 등 상태 (String 또는 Enum 모두 허용)
    private Boolean isOnline;         // 실시간 접속 여부
    private LocalDateTime lastConnectedAt;

    // 💡 센서 최신 정보 필드 추가
    private Double latestLeftWatt;
    private Double latestRightWatt;
    private Integer latestBatteryPct;
    private Boolean isCharging;
}