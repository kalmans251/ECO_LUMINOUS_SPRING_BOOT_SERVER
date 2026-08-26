package com.ecoluminous.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
// 💡 1. apiKey 단일 Unique를 제거하고, (apiKey + railSeq) 복합 Unique 제약조건으로 변경
@Table(name = "RAIL_INFO", uniqueConstraints = {
    @UniqueConstraint(name = "uk_apikey_railseq", columnNames = {"api_key", "rail_seq"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rail_info_id")
    private Long id;

    // 💡 2. unique = true 옵션 제거 (Table 레벨에서 복합 Unique로 처리)
    @Column(name = "api_key", nullable = false, length = 64)
    private String apiKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "rail_seq", nullable = false)
    @Builder.Default
    private Integer railSeq = 0;

    // 16비트 레일 모드 (0x0000 ~ 0xFFFF, Bitmask / Mode Flag 관리용)
    @Column(name = "rail_mode", nullable = false)
    @Builder.Default
    private Integer railMode = 0;

    @JsonProperty("lat")
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @JsonProperty("lng")
    @Column(precision = 11, scale = 7)
    private BigDecimal longitude;

    @Column(length = 20)
    @Builder.Default
    private String status = "INACTIVE";

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 사용자 등록 메서드 (railName 제거)
    public void registerUser(User user) {
        this.user = user;
        this.status = "ACTIVE";
    }

    // 통신 시 최근 연결시간 갱신
    public void updateLastConnected() {
        this.lastConnectedAt = LocalDateTime.now();
    }

    // 레일 모드 변경 메서드
    public void updateRailMode(Integer railMode) {
        this.railMode = railMode;
    }

    // 💡 3. 상태 변경 메서드 추가 (ACTIVE, OFFLINE 등 상태 변경용)
    public void updateStatus(String status) {
        this.status = status;
    }

    // ------------------ 실시간 상태 판별 ------------------

    public String getCalculatedStatus() {
        if (this.lastConnectedAt == null) {
            return "INACTIVE";
        }
        // 10분 주기 수신 시스템이므로 20분이 지나도 통신이 없으면 OFFLINE으로 판별
        if (this.lastConnectedAt.isBefore(LocalDateTime.now().minusMinutes(20))) {
            return "OFFLINE";
        }
        return "ACTIVE";
    }

    public boolean isOnline() {
        return "ACTIVE".equals(getCalculatedStatus());
    }
 // 위치(위도/경도) 변경 메서드 추가
    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}