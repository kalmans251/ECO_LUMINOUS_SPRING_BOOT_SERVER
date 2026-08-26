package com.ecoluminous.service;

import com.ecoluminous.dto.rail.RailModeRequestDto;
import com.ecoluminous.dto.rail.RailStatusResponseDto;
import com.ecoluminous.entity.RailDataLog;
import com.ecoluminous.entity.RailInfo;
import com.ecoluminous.repository.RailDataLogRepository;
import com.ecoluminous.repository.RailInfoRepository; // 💡 주입받은 레포지토리
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RailService {

    private final RailInfoRepository railInfoRepository;
    private final RailDataLogRepository railDataLogRepository;

    // 1. 특정 API Key(사용자)의 난간 전체 실시간 상태 조회 (대시보드용)
    public List<RailStatusResponseDto> getRailStatusesByApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key가 유효하지 않습니다.");
        }

        List<RailInfo> railList = railInfoRepository.findByApiKeyOrderByRailSeqAsc(apiKey);
        List<RailStatusResponseDto> responseList = new ArrayList<>();

        if (railList == null || railList.isEmpty()) {
            return responseList; // 난간 데이터가 없으면 빈 리스트 [] 반환 (400 에러 방지)
        }

        for (RailInfo rail : railList) {
            RailDataLog latestLog = railDataLogRepository.findFirstByRailInfoOrderByRecordDateDesc(rail)
                    .orElse(null);

            RailStatusResponseDto dto = RailStatusResponseDto.builder()
                    .railInfoId(rail.getId())
                    .railSeq(rail.getRailSeq())
                    .railMode(rail.getRailMode())
                    .status(rail.getCalculatedStatus() != null ? rail.getCalculatedStatus() : rail.getStatus())
                    .lastConnectedAt(rail.getLastConnectedAt())
                    .latestLeftWatt(latestLog != null ? latestLog.getLeftWatt() : 0.0)
                    .latestRightWatt(latestLog != null ? latestLog.getRightWatt() : 0.0)
                    .latestBatteryPct(latestLog != null ? latestLog.getBatteryPct() : 0)
                    .isCharging(latestLog != null && Boolean.TRUE.equals(latestLog.getIsCharging()))
                    .build();

            responseList.add(dto);
        }

        return responseList;
    }

    // 2. 난간 LED 모드 변경 (DB 반영 - DTO 기반)
    @Transactional
    public void updateRailMode(RailModeRequestDto dto) {
        RailInfo railInfo = railInfoRepository.findByApiKeyAndRailSeq(dto.getApiKey(), dto.getRailSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 난간 정보를 찾을 수 없습니다."));

        railInfo.updateRailMode(dto.getRailMode());
    }

    // 3. 난간 LED 모드 단건 조회
    public Integer getRailMode(String apiKey, Integer railSeq) {
        return railInfoRepository.findByApiKeyAndRailSeq(apiKey, railSeq)
                .map(RailInfo::getRailMode)
                .orElse(0);
    }

    // 4. [신규/수정] 전체 제어 시 응답 성공한 난간 리스트만 DB Bulk Update
    @Transactional
    public void updateBulkRailMode(String apiKey, List<Integer> successRails, Integer railMode) {
        if (successRails == null || successRails.isEmpty()) return;

        // 💡 주입된 변수명(railInfoRepository)으로 수정
        int updatedCount = railInfoRepository.updateModeBySeqList(apiKey, successRails, railMode);
        System.out.println("💾 [DB 일괄 업데이트 완료] 성공 대상: " + updatedCount + "개 난간");
    }

    // 5. [신규/수정] 단일 제어 성공 시 DB Update
    @Transactional
    public void updateSingleRailMode(String apiKey, Integer railSeq, Integer railMode) {
        int updatedCount = railInfoRepository.updateSingleMode(apiKey, railSeq, railMode);
        System.out.println("💾 [DB 단일 업데이트 완료] 난간 #" + railSeq + " ➔ 모드 " + railMode + "번 (" + updatedCount + "건)");
    }
}