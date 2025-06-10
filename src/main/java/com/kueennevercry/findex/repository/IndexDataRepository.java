package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.entity.IndexData;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

    List<IndexData> findAllByIndexInfo_IdAndBaseDateBetween(Long indexInfoId, LocalDate from, LocalDate to, Sort sort);
    boolean existsByIndexInfoId(Long indexInfoId);
    boolean existsByBaseDate(LocalDate baseDate);
}
