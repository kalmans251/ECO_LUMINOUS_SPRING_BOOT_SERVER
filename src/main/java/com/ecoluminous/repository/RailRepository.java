package com.ecoluminous.repository;

import com.ecoluminous.entity.RailInfo; // 사용하시는 Entity 클래스명
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailRepository extends JpaRepository<RailInfo, Long> {

    /**
     * 💡 [Bulk Update] 성공한 난간 리스트(railSeq IN)에 대해 LED 모드 일괄 변경
     */
    @Modifying
    @Query("UPDATE RailInfo r SET r.railMode = :railMode " +
           "WHERE r.apiKey = :apiKey AND r.railSeq IN :successRails")
    int updateModeBySeqList(@Param("apiKey") String apiKey, 
                            @Param("successRails") List<Integer> successRails, 
                            @Param("railMode") Integer railMode);

    /**
     * 단일 난간 LED 모드 변경
     */
    @Modifying
    @Query("UPDATE RailInfo r SET r.railMode = :railMode " +
           "WHERE r.apiKey = :apiKey AND r.railSeq = :railSeq")
    int updateSingleMode(@Param("apiKey") String apiKey, 
                         @Param("railSeq") Integer railSeq, 
                         @Param("railMode") Integer railMode);
}