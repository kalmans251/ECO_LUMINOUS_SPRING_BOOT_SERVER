package com.ecoluminous.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RAIL_DATA_LOG", indexes = {
    @Index(name = "idx_rail_log_date", columnList = "rail_info_id, record_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RailDataLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rail_info_id", nullable = false)
    private RailInfo railInfo;

    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @Column(name = "left_watt")
    private Double leftWatt;

    @Column(name = "right_watt")
    private Double rightWatt;

    @Column(name = "battery_pct")
    private Integer batteryPct;

    @Column(name = "is_charging")
    private Boolean isCharging;
}