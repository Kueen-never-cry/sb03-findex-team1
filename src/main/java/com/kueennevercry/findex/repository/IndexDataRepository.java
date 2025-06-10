package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

  List<IndexData> findAllByIndexInfo_Id(Long indexInfoId);

  List<IndexData> findAllByIndexInfo_IdAndBaseDateBetween(Long indexInfoId, LocalDate from,
      LocalDate to, Sort sort);

  boolean existsByIndexInfoId(Long indexInfoId);

  boolean existsByBaseDate(LocalDate baseDate);

  @Query("""
      SELECT new com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto(
        null,
        new com.kueennevercry.findex.dto.response.IndexPerformanceDto(
          info.id,
          info.indexClassification,
          info.indexName,
          cast((curr.closingPrice - prev.closingPrice) * 1.0f as BigDecimal),
          cast(
            case when prev.closingPrice <> 0
              then ((curr.closingPrice - prev.closingPrice) / prev.closingPrice) * 100.0f
              else 0.0f
            end as BigDecimal
          ),
          cast(curr.closingPrice as BigDecimal),
          cast(prev.closingPrice as BigDecimal)
        )
      )
      FROM IndexData curr
      JOIN IndexData prev ON curr.indexInfo.id = prev.indexInfo.id
      JOIN IndexInfo info ON curr.indexInfo.id = info.id
      WHERE curr.baseDate = :baseDate
        AND prev.baseDate = :beforeBaseDate
        AND (:indexInfoId IS NULL OR info.id = :indexInfoId)
      """)
  List<RankedIndexPerformanceDto> findRankedPerformances(
      @Param("baseDate") LocalDate baseDate,
      @Param("beforeBaseDate") LocalDate beforeBaseDate,
      @Param("indexInfoId") Long indexInfoId
  );


  @Query("SELECT MAX(d.baseDate) FROM IndexData d")
  Optional<LocalDate> findMaxBaseDate();
}
