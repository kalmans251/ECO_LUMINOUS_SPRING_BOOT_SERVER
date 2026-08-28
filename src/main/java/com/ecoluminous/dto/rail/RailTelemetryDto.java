package com.ecoluminous.dto.rail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RailTelemetryDto {
    private String apiKey;
    private Integer railSeq;
    private Double leftWatt;
    private Double rightWatt;
    private Integer batteryPct;
    private Integer railMode;
    private Integer inCount;
    private Integer outCount;
    private List<RadarTargetDto> radar1Targets;
    private List<RadarTargetDto> radar2Targets;
    private Boolean radar1Detected;
    private Boolean radar2Detected;
    private Integer emergencyCode;
    private Boolean isCharging;
    private Boolean isEmergency;
    private Boolean isError;
}