package com.ecoluminous.repository;

import com.ecoluminous.entity.RailAlarmLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RailAlarmLogRepository extends JpaRepository<RailAlarmLog, Long> {
    // 특정 난간의 안 읽은 알림 목록 조회
    List<RailAlarmLog> findByRailInfoIdAndIsReadFalseOrderByCreatedAtDesc(Long railInfoId);
}