package com.ecoluminous.scheduler;

import com.ecoluminous.entity.RailDailySummary;
import com.ecoluminous.entity.RailInfo;
import com.ecoluminous.repository.RailDailySummaryRepository;
import com.ecoluminous.repository.RailDataLogRepository;
import com.ecoluminous.repository.RailInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySummaryScheduler {

    private final RailDataLogRepository railDataLogRepository;
    private final RailDailySummaryRepository railDailySummaryRepository;
    private final RailInfoRepository railInfoRepository;
    private final SimpMessagingTemplate messagingTemplate; // 💡 웹소켓 실시간 메시지 전송용

    // 매일 자정(00:00:00)에 실행되어 어제 하루 치 데이터 요약 및 저장
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void calculateDailySummary() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startDate = yesterday.atStartOfDay();       // 어제 00:00:00
        LocalDateTime endDate = LocalDate.now().atStartOfDay();  // 오늘 00:00:00 (어제 데이터 범위 미만 조건 적용)

        List<Object[]> summaryResults = railDataLogRepository.findDailySummaryData(startDate, endDate);
        List<RailDailySummary> summariesToSave = new ArrayList<>();

        for (Object[] result : summaryResults) {
            Long railInfoId = (Long) result[0];
            
            Double totalLeftWh = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
            Double totalRightWh = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;
            Integer minBatteryPct = result[3] != null ? ((Number) result[3]).intValue() : 0;
            Integer maxBatteryPct = result[4] != null ? ((Number) result[4]).intValue() : 0;
            Integer dataCount = result[5] != null ? ((Number) result[5]).intValue() : 0;

            RailInfo railInfo = railInfoRepository.findById(railInfoId).orElse(null);

            if (railInfo != null) {
                RailDailySummary summary = RailDailySummary.builder()
                        .railInfo(railInfo)
                        .summaryDate(yesterday)
                        .totalLeftWh(totalLeftWh)
                        .totalRightWh(totalRightWh)
                        .minBatteryPct(minBatteryPct)
                        .maxBatteryPct(maxBatteryPct)
                        .dataCount(dataCount)
                        .build();

                summariesToSave.add(summary);
            }
        }

        if (!summariesToSave.isEmpty()) {
            railDailySummaryRepository.saveAll(summariesToSave);
            log.info("{}건의 일일 집계 데이터가 성공적으로 저장되었습니다.", summariesToSave.size());
        }
    }

    // 10분마다 실행되는 난간 헬스체크 스케줄러
    // 마지막 수신 시각이 20분 이상 지난 ACTIVE 난간을 OFFLINE으로 자동 변경 + 실시간 대시보드 전파
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void checkDeviceHealth() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(20);

        List<RailInfo> inactiveRails = railInfoRepository.findByStatusAndLastConnectedAtBefore("ACTIVE", threshold);

        for (RailInfo rail : inactiveRails) {
            // 1) DB 상태 변경
            rail.updateStatus("OFFLINE");

            // 2) 💡 대시보드로 실시간 OFFLINE 상태 웹소켓 전파
            String destination = "/sub/rails/" + rail.getApiKey() + "/realtime";

            Map<String, Object> offlinePayload = new HashMap<>();
            offlinePayload.put("apiKey", rail.getApiKey());
            offlinePayload.put("railSeq", rail.getRailSeq());
            offlinePayload.put("status", "OFFLINE");

            messagingTemplate.convertAndSend((Object) destination, offlinePayload);
        }

        if (!inactiveRails.isEmpty()) {
            log.info("총 {}개의 난간 상태가 통신 미수신으로 인해 OFFLINE으로 변경 및 대시보드 전파되었습니다.", inactiveRails.size());
        }
    }
}