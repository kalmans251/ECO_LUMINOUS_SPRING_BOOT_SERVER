package com.ecoluminous.repository;

import com.ecoluminous.entity.RailDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface RailDailySummaryRepository extends JpaRepository<RailDailySummary, Long> {
    Optional<RailDailySummary> findByRailInfoIdAndSummaryDate(Long railInfoId, LocalDate summaryDate);
}