package com.ecoluminous.repository;

import com.ecoluminous.entity.RailDataLog;
import com.ecoluminous.entity.RailInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RailDataLogRepository extends JpaRepository<RailDataLog, Long> {

    // 1. 특정 기간(하루) 동안 난간별 발전량합, 배터리 최저/최고, 수신 건수(COUNT) 집계 (기존 메서드)
    @Query("SELECT r.railInfo.id, " +
           "COALESCE(SUM(r.leftWatt), 0) / 6.0, " +   // 10분 W -> Wh 변환
           "COALESCE(SUM(r.rightWatt), 0) / 6.0, " +
           "MIN(r.batteryPct), " +
           "MAX(r.batteryPct), " +
           "COUNT(r) " +                               // 하루 수신 데이터 개수 카운트
           "FROM RailDataLog r " +
           "WHERE r.recordDate >= :startDate AND r.recordDate < :endDate " +
           "GROUP BY r.railInfo.id")
    List<Object[]> findDailySummaryData(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    // 💡 2. 특정 난간(RailInfo)의 가장 최신 센서 로그 1건 조회 (새로 추가)
    Optional<RailDataLog> findFirstByRailInfoOrderByRecordDateDesc(RailInfo railInfo);
}