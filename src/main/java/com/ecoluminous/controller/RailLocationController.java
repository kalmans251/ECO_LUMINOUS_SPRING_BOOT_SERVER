package com.ecoluminous.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecoluminous.dto.rail.RailLocationDto;
import com.ecoluminous.entity.RailInfo;
import com.ecoluminous.entity.User;
import com.ecoluminous.repository.RailInfoRepository;
import com.ecoluminous.repository.UserRepository; // User 조회용 (선택)

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RailLocationController {

    private final RailInfoRepository railInfoRepository;
    private final UserRepository userRepository; // 선택 사항
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @MessageMapping("/rails/location/save")
    public void saveRailLocation(@Payload RailLocationDto dto) {
        BigDecimal lat = (dto.getLat() != null) ? BigDecimal.valueOf(dto.getLat()) : null;
        BigDecimal lng = (dto.getLng() != null) ? BigDecimal.valueOf(dto.getLng()) : null;

        // 동적으로 들어온 dto.getApiKey() 및 dto.getRailSeq() 조건으로 조회 및 자동 생성
        RailInfo railInfo = railInfoRepository.findByApiKeyAndRailSeq(dto.getApiKey(), dto.getRailSeq())
                .orElseGet(() -> {
                    // apiKey에 해당하는 User 엔티티가 있다면 함께 매핑
                    User user = userRepository.findByApiKey(dto.getApiKey()).orElse(null);

                    return RailInfo.builder()
                            .apiKey(dto.getApiKey())
                            .railSeq(dto.getRailSeq())
                            .user(user)
                            .railMode(0)
                            .status("ACTIVE")
                            .build();
                });

        // 위치 업데이트 및 저장
        railInfo.updateLocation(lat, lng);
        railInfoRepository.save(railInfo);

        messagingTemplate.convertAndSend("/topic/rails/location/result", dto);
    }
    
 // ★ 웹페이지 최초 로드 시 DB에 저장된 난간 위치 목록 반환 REST API
    @GetMapping("/api/rails/locations")
    @ResponseBody
    public ResponseEntity<List<RailLocationDto>> getSavedLocations(@RequestParam("apiKey") String apiKey) {
        List<RailInfo> list = railInfoRepository.findByApiKeyOrderByRailSeqAsc(apiKey);
        
        List<RailLocationDto> dtoList = list.stream().map(rail -> new RailLocationDto(
                rail.getApiKey(),
                rail.getRailSeq(),
                rail.getLatitude() != null ? rail.getLatitude().doubleValue() : null,
                rail.getLongitude() != null ? rail.getLongitude().doubleValue() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}