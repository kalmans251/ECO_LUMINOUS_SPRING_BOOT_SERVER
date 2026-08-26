package com.ecoluminous.repository;

import com.ecoluminous.entity.RailInfo; // 👈 entity 패키지의 RailInfo
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RailInfoRepository extends JpaRepository<RailInfo, Long> {

    List<RailInfo> findByApiKeyOrderByRailSeqAsc(String apiKey);
    Optional<RailInfo> findByApiKeyAndRailSeq(String apiKey, Integer railSeq);
    List<RailInfo> findByStatusAndLastConnectedAtBefore(String status, LocalDateTime threshold);

    /**
     * 1. Bulk Update (전체 제어 성공 목록 변경)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RailInfo r SET r.railMode = :railMode " +
           "WHERE r.apiKey = :apiKey AND r.railSeq IN :successRails")
    int updateModeBySeqList(@Param("apiKey") String apiKey, 
                            @Param("successRails") List<Integer> successRails, 
                            @Param("railMode") Integer railMode);

    /**
     * 2. Single Update (단일 제어 변경) - 💡 여기도 Rail -> RailInfo로 수정!
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RailInfo r SET r.railMode = :railMode " +
           "WHERE r.apiKey = :apiKey AND r.railSeq = :railSeq")
    int updateSingleMode(@Param("apiKey") String apiKey, 
                         @Param("railSeq") Integer railSeq, 
                         @Param("railMode") Integer railMode);
}