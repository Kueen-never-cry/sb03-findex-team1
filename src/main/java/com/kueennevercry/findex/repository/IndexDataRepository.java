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
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

  List<IndexData> findAllByIndexInfo_IdAndBaseDateBetween(Long indexInfoId, LocalDate from,
      LocalDate to, Sort sort);

  boolean existsByIndexInfoId(Long indexInfoId);

  boolean existsByBaseDate(LocalDate baseDate);

  @Query("SELECT MAX(d.baseDate) FROM IndexData d")
  LocalDate findLatestBaseDateAcrossAll();

  @Query("SELECT d FROM IndexData d WHERE d.indexInfo.id = :indexInfoId AND d.baseDate = :baseDate")
  Optional<IndexData> findByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

  @Query("""
          SELECT d FROM IndexData d
          WHERE d.indexInfo.id = :indexInfoId AND d.baseDate <= :targetDate
          ORDER BY d.baseDate DESC
      """)
  List<IndexData> findClosestBeforeOrEqualRaw(Long indexInfoId, LocalDate targetDate,
      Pageable pageable);

  default Optional<IndexData> findClosestBeforeOrEqual(Long indexInfoId, LocalDate targetDate) {
    return findClosestBeforeOrEqualRaw(indexInfoId, targetDate, PageRequest.of(0, 1)).stream()
        .findFirst();
  }
}
