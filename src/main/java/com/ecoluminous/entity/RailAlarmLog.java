package com.ecoluminous.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RAIL_ALARM_LOG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RailAlarmLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    // 💡 어떤 사용자의 알림인지 직관적으로 파악하기 위해 User 연관관계 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rail_info_id", nullable = false)
    private RailInfo railInfo;

    // 💡 수신 당시의 API Key를 직접 기록 (조회/검증 용이)
    @Column(name = "api_key", length = 50)
    private String apiKey;

    @Column(name = "alarm_type", nullable = false, length = 50)
    private String alarmType;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}