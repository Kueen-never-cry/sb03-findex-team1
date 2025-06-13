package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

  List<IndexData> findAllByIndexInfo_IdAndBaseDateBetween(Long indexInfoId, LocalDate from,
      LocalDate to, Sort sort);

  Optional<IndexData> findByIndexInfo_IdAndBaseDate(Long indexInfoId, LocalDate baseDate);

  boolean existsByIndexInfoId(Long indexInfoId);

  boolean existsByBaseDate(LocalDate baseDate);

  @Query("SELECT MIN(d.baseDate) FROM IndexData d WHERE d.indexInfo.id = :indexInfoId")
  Optional<LocalDate> findMinBaseDateByIndexIfoId(@Param("indexInfoId") Long indexInfoId);

  @Query("SELECT MAX(d.baseDate) FROM IndexData d")
  Optional<LocalDate> findMaxBaseDate();

  @Query("SELECT MAX(d.baseDate) FROM IndexData d WHERE d.indexInfo.id = :indexInfoId")
  Optional<LocalDate> findMaxBaseDateByIndexInfoId(@Param("indexInfoId") Long indexInfoId);

  @Query("""
      SELECT d FROM IndexData d
      WHERE d.indexInfo.id = :indexInfoId
        AND d.baseDate <= :baseDate
      ORDER BY d.baseDate DESC
      """)
  List<IndexData> findLatestBeforeOrEqualBaseDate(
      @Param("indexInfoId") Long indexInfoId,
      @Param("baseDate") LocalDate baseDate,
      Pageable pageable
  );

  default Optional<IndexData> findByIndexInfoIdAndBaseDateOnlyDateMatch(
      Long indexInfoId, LocalDate baseDate
  ) {
    return findLatestBeforeOrEqualBaseDate(indexInfoId, baseDate, PageRequest.of(0, 1))
        .stream().findFirst();
  }

  Optional<IndexData> findTopByIndexInfoIdOrderByBaseDateDesc(Long indexInfoId);

  void deleteAllByIndexInfoId(Long indexInfoId);
}
