package com.ecoluminous.service;

import com.ecoluminous.entity.RailAlarmLog;
import com.ecoluminous.entity.RailDataLog;
import com.ecoluminous.entity.RailInfo;
import com.ecoluminous.entity.User;
import com.ecoluminous.dto.alarm.EmergencyAlarmRequestDto;
import com.ecoluminous.dto.device.DeviceLogRequestDto;
import com.ecoluminous.repository.RailAlarmLogRepository;
import com.ecoluminous.repository.RailDataLogRepository;
import com.ecoluminous.repository.RailInfoRepository;
import com.ecoluminous.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {

	private final UserRepository userRepository;
	private final RailInfoRepository railInfoRepository;
    private final RailDataLogRepository railDataLogRepository;
    private final RailAlarmLogRepository railAlarmLogRepository;

    // 10분 주기 센서 로그 묶음 저장 (Batch)
    public void saveDeviceLogs(List<DeviceLogRequestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return;

        String apiKey = dtoList.get(0).getApiKey();
        List<RailInfo> railInfoList = railInfoRepository.findByApiKeyOrderByRailSeqAsc(apiKey);

        Map<Integer, RailInfo> railMap = railInfoList.stream()
                .collect(Collectors.toMap(RailInfo::getRailSeq, rail -> rail));

        List<RailDataLog> logList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (DeviceLogRequestDto dto : dtoList) {
            RailInfo railInfo = railMap.get(dto.getRailSeq());
            if (railInfo != null) {
                // 접속 시간(lastConnectedAt)만 최신화 (모드 변경 로직 제거)
                railInfo.updateLastConnected();

                // 10분 주기 센서 데이터 로그 생성 및 저장
                RailDataLog log = RailDataLog.builder()
                        .railInfo(railInfo)
                        .recordDate(now)
                        .leftWatt(dto.getLeftWatt())
                        .rightWatt(dto.getRightWatt())
                        .batteryPct(dto.getBatteryPct())
                        .isCharging(dto.getIsCharging())
                        .build();

                logList.add(log);
            }
        }

        railDataLogRepository.saveAll(logList);
    }

    public void saveEmergencyAlarm(EmergencyAlarmRequestDto dto) {
        // 1. api_key로 사용자 식별
        User user = userRepository.findByApiKey(dto.getApiKey())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 API Key입니다: " + dto.getApiKey()));

        // 2. api_key와 railSeq로 특정 난간 정보 조회 (없으면 자동 등록 또는 예외 처리)
        RailInfo railInfo = railInfoRepository.findByApiKeyAndRailSeq(dto.getApiKey(), dto.getRailSeq())
                .orElseGet(() -> {
                    // 현장 난간이 DB에 미처 등록 안 된 상태라면 자동 생성
                    RailInfo newRail = RailInfo.builder()
                            .apiKey(dto.getApiKey())
                            .railSeq(dto.getRailSeq())
                            .status("ACTIVE")
                            .build();
                    newRail.registerUser(user);
                    return railInfoRepository.save(newRail);
                });

        // 3. 비상 알림 로그 저장 (User와 RailInfo, apiKey 모두 기록)
        RailAlarmLog alarmLog = RailAlarmLog.builder()
                .user(user)             // 어떤 사용자의 기기인지
                .railInfo(railInfo)     // 몇 번 난간인지
                .apiKey(dto.getApiKey())
                .alarmType(dto.getAlarmType())
                .message(dto.getMessage())
                .build();

        railAlarmLogRepository.save(alarmLog);
    }
}