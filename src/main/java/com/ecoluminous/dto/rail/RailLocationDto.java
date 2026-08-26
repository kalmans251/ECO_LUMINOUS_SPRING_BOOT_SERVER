package com.ecoluminous.dto.rail;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RailLocationDto {
    private String apiKey;
    private Integer railSeq;
    private Double lat;
    private Double lng;
}
