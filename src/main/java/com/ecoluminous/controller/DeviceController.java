package com.ecoluminous.controller;

import com.ecoluminous.dto.alarm.EmergencyAlarmRequestDto;
import com.ecoluminous.dto.device.DeviceLogRequestDto;
import com.ecoluminous.service.DeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // 1. 라즈베리파이 -> 10분 주기 센서 데이터 묶음 전송 API (List 형태)
    @PostMapping("/logs")
    public ResponseEntity<String> receiveLogs(@Valid @RequestBody List<DeviceLogRequestDto> dtoList) {
        deviceService.saveDeviceLogs(dtoList);
        return ResponseEntity.ok(dtoList.size() + "개 난간 데이터가 성공적으로 저장되었습니다.");
    }

    // 2. 라즈베리파이 -> 비상 음성("살려주세요")/소음 감지 즉시 전송 API (단건)
    @PostMapping("/emergency")
    public ResponseEntity<String> receiveEmergency(@Valid @RequestBody EmergencyAlarmRequestDto dto) {
        deviceService.saveEmergencyAlarm(dto);
        return ResponseEntity.ok("비상 알림이 성공적으로 기록되었습니다.");
    }
}