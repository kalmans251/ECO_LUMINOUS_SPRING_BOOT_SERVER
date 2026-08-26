package com.ecoluminous.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "RAIL_DAILY_SUMMARY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RailDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rail_daily_summary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rail_info_id", nullable = false)
    private RailInfo railInfo;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "total_left_wh")
    private Double totalLeftWh;

    @Column(name = "total_right_wh")
    private Double totalRightWh;

    @Column(name = "min_battery_pct")
    private Integer minBatteryPct;

    @Column(name = "max_battery_pct")
    private Integer maxBatteryPct;

    @Column(name = "data_count")
    private Integer dataCount; // 하루 동안 수집된 10분 데이터 건수 (정상: 144개)

    // 하루 가동률(%) 계산 (10분 주기 기준 144개 = 100%)
    public Double getOperatingRate() {
        if (this.dataCount == null || this.dataCount == 0) {
            return 0.0;
        }
        double rate = (double) this.dataCount / 144.0 * 100.0;
        return Math.min(Math.round(rate * 10.0) / 10.0, 100.0); // 소수점 첫째자리 반올림
    }

    // 하루 가동 상태 문자열 반환
    public String getDailyStatus() {
        double rate = getOperatingRate();
        if (rate >= 90.0) return "정상";
        if (rate >= 50.0) return "주의(일부 미가동)";
        if (rate > 0.0) return "경고(대부분 미가동)";
        return "미가동";
    }
}