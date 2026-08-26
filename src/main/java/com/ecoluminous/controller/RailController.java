package com.ecoluminous.controller;

import com.ecoluminous.dto.rail.RailStatusResponseDto;
import com.ecoluminous.service.RailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rails")
@RequiredArgsConstructor
public class RailController {

    private final RailService railService;

    /**
     * 대시보드 초기 진입 시, 특정 사용자의 전체 난간 실시간/최근 상태 목록 조회
     * GET /api/rails/status?apiKey=OMWM-KSUI-LJGE-TKKF
     */
    @GetMapping("/status")
    public ResponseEntity<List<RailStatusResponseDto>> getRailStatuses(@RequestParam(name = "apiKey") String apiKey) {
        List<RailStatusResponseDto> railStatuses = railService.getRailStatusesByApiKey(apiKey);
        return ResponseEntity.ok(railStatuses);
    }
}