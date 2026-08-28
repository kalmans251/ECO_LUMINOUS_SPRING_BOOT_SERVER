package com.ecoluminous.dto.rail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarTargetDto {
    private Integer x;
    private Integer y;
}