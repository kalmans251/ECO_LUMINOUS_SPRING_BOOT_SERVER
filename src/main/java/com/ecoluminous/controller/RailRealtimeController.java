package com.ecoluminous.controller;

import com.ecoluminous.dto.rail.RailModeConfirmDto;
import com.ecoluminous.dto.rail.RailModeRequestDto;
import com.ecoluminous.dto.rail.RailStatusResponseDto;
import com.ecoluminous.service.RailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RailRealtimeController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RailService railService;

    // ==========================================
    // 1. 실시간 난간 센서/배터리/레이더 데이터 중계
    // ==========================================
    @MessageMapping("/rails/realtime")
    public void receiveRealtimeData(RailStatusResponseDto dto) {
        log.info("📡 [중계 로그] railSeq: {}, radar1Targets: {}", dto.getRailSeq(), dto.getRadar1Targets());
        
        String destination = "/sub/rails/" + dto.getApiKey() + "/realtime";
        messagingTemplate.convertAndSend(destination, (Object) dto);
    }

    // ==========================================
    // 2. LED 모드 변경 명령 중계 (WebSocket / HTTP REST)
    // ==========================================
    @MessageMapping("/rails/mode")
    public void changeRailModeWebSocket(RailModeRequestDto dto) {
        requestModeChangeToDevice(dto);
    }

    @PostMapping("/api/rails/mode")
    @ResponseBody
    public ResponseEntity<Void> changeRailModeHttp(@RequestBody RailModeRequestDto dto) {
        requestModeChangeToDevice(dto);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // 3. 하드웨어 제어 결과 처리 (DB 갱신 및 웹 브라우저 통보)
    // ==========================================
    @MessageMapping("/rails/mode/confirm")
    public void handleModeConfirm(RailModeConfirmDto confirmDto) {
        log.info("📩 [하드웨어 제어 결과 수신] 대상 railSeq: {}, 요청 모드: {}번", 
                confirmDto.getRailSeq(), confirmDto.getRailMode());

        if (confirmDto.getRailSeq() != null && confirmDto.getRailSeq() == 0 && confirmDto.getSuccessRails() != null) {
            railService.updateBulkRailMode(confirmDto.getApiKey(), confirmDto.getSuccessRails(), confirmDto.getRailMode());
            log.info("💾 [DB Bulk Update 완료] 대상 난간: {}개", confirmDto.getSuccessRails().size());
        } else {
            railService.updateSingleRailMode(confirmDto.getApiKey(), confirmDto.getRailSeq(), confirmDto.getRailMode());
            log.info("💾 [DB Single Update 완료] 난간 #{} ➔ 모드 {}번", confirmDto.getRailSeq(), confirmDto.getRailMode());
        }

        messagingTemplate.convertAndSend("/topic/rails/mode/result", (Object) confirmDto);
    }

    @MessageMapping("/rails/mode/fail")
    public void failRailModeChange(Map<String, Object> failPayload) {
        log.error("[❌ 하드웨어 제어 실패] 수신 데이터: {}", failPayload);

        String apiKey = (String) failPayload.get("apiKey");
        if (apiKey != null) {
            String dashboardDestination = "/sub/rails/" + apiKey + "/realtime";
            messagingTemplate.convertAndSend(dashboardDestination, (Object) failPayload);
        }
        
        messagingTemplate.convertAndSend("/topic/rails/mode/fail", (Object) failPayload);
    }

    // ==========================================
    // 🎙️ 4. 음성 통화 및 PTT(Push-To-Talk) 고속 라우팅
    // ==========================================

    /**
     * 관제실 통화 시작/종료 제어 (웹 브라우저 -> 백엔드 -> RPi)
     */
    @MessageMapping("/rails/call")
    public void handleCallControl(Map<String, Object> payload) {
        String apiKey = (String) payload.get("apiKey");
        log.info("📞 [통화 제어 명령 중계] apiKey: {}, payload: {}", apiKey, payload);
        if (apiKey != null) {
            messagingTemplate.convertAndSend("/sub/device/" + apiKey + "/call", (Object) payload);
        }
    }

    /**
     * 관제소 PTT 다운링크 (웹 브라우저 PTT 마이크 -> 백엔드 -> RPi 현장 스피커)
     */
    @MessageMapping("/rails/voice/downlink")
    public void handleVoiceDownlink(Map<String, Object> payload) {
        String apiKey = (String) payload.get("apiKey");
        if (apiKey != null) {
            messagingTemplate.convertAndSend("/sub/device/" + apiKey + "/voice/downlink", (Object) payload);
        }
    }

    /**
     * 현장 마이크 음성 업링크 (RPi -> 백엔드 -> 관제소 웹 브라우저 실시간 청음)
     */
    @MessageMapping("/rails/voice/uplink")
    public void handleVoiceUplink(Map<String, Object> payload) {
        String apiKey = (String) payload.get("apiKey");
        if (apiKey != null) {
            messagingTemplate.convertAndSend("/sub/device/" + apiKey + "/voice/uplink", (Object) payload);
        }
        messagingTemplate.convertAndSend("/sub/rails/voice/uplink", (Object) payload);
    }

    /**
     * RPi/하드웨어 통화 응답 확인 (RPi -> 백엔드 -> 웹 브라우저)
     */
    @MessageMapping("/rails/call/confirm")
    public void handleCallConfirm(Map<String, Object> payload) {
        log.info("📞 [통화 상태 변경 확정 수신] payload: {}", payload);
        messagingTemplate.convertAndSend("/topic/rails/call/confirm", (Object) payload);
        
        String apiKey = (String) payload.get("apiKey");
        if (apiKey != null) {
            messagingTemplate.convertAndSend("/sub/device/" + apiKey + "/call/confirm", (Object) payload);
        }
    }

    // ==========================================
    // 🎵 5. 오디오 / 볼륨 / 트랙 제어 라우팅
    // ==========================================

    /**
     * 관제소 오디오 제어 명령 (웹 브라우저 -> RPi)
     */
    @MessageMapping("/device/{apiKey}/audio")
    public void handleAudioControl(@DestinationVariable("apiKey") String apiKey, Map<String, Object> payload) {
        log.info("🎵 [오디오 제어 명령 전송] apiKey: {}, payload: {}", apiKey, payload);
        messagingTemplate.convertAndSend("/sub/device/" + apiKey + "/audio", (Object) payload);
    }

    /**
     * RPi 오디오 제어 응답 확인 (RPi -> 웹 브라우저)
     */
    @MessageMapping("/rails/audio/confirm")
    public void handleAudioConfirm(Map<String, Object> payload) {
        log.info("🎵 [오디오 제어 확정 수신] payload: {}", payload);
        messagingTemplate.convertAndSend("/topic/rails/audio/confirm", (Object) payload);
    }

    // ==========================================
    // ⚙️ 6. 하드웨어 제어 전송 내부 메서드
    // ==========================================
    private void requestModeChangeToDevice(RailModeRequestDto dto) {
        log.info("[🚀 장비 제어 요청 명령 전송] API Key: {}, RailSeq: {}, Target Mode: {}", 
                dto.getApiKey(), dto.getRailSeq(), dto.getRailMode());

        String deviceDestination = "/sub/device/" + dto.getApiKey() + "/mode";
        messagingTemplate.convertAndSend(deviceDestination, (Object) dto);
    }
}