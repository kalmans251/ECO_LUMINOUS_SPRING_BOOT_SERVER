package com.ecoluminous.service;

import com.ecoluminous.entity.RailInfo;
import com.ecoluminous.entity.User;
import com.ecoluminous.repository.RailInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RailInfoService {

    private final RailInfoRepository railInfoRepository;

    /**
     * 신규 사용자/API Key 등록 시 1~15번 난간 기본 레코드 자동 생성
     */
    @Transactional
    public void initializeRailsForUser(User user) {
        String apiKey = user.getApiKey();
        BigDecimal defaultLat = new BigDecimal("37.1639214");
        BigDecimal defaultLng = new BigDecimal("126.9602930");

        for (int seq = 1; seq <= 15; seq++) {
            final int currentSeq = seq;
            boolean exists = railInfoRepository.findByApiKeyAndRailSeq(apiKey, currentSeq).isPresent();

            if (!exists) {
                RailInfo rail = RailInfo.builder()
                        .apiKey(apiKey)
                        .user(user)
                        .railSeq(currentSeq)
                        .railMode(0)
                        .latitude(defaultLat.add(new BigDecimal(currentSeq * 0.00003)))
                        .longitude(defaultLng.add(new BigDecimal(currentSeq * 0.00010)))
                        .status("ACTIVE")
                        .build();

                railInfoRepository.save(rail);
            }
        }
    }
}