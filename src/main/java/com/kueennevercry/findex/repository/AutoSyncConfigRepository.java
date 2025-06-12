package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.entity.IndexInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long>,
    AutoSyncConfigCustomRepository {

  boolean existsByIndexInfo_Id(Long indexInfoId);

  Optional<AutoSyncConfig> findByIndexInfo(IndexInfo indexInfo);

  @Query("SELECT c FROM AutoSyncConfig c JOIN FETCH c.indexInfo WHERE c.id = :id")
  Optional<AutoSyncConfig> findById(@Param("id") Long id);

  @Query("SELECT c FROM AutoSyncConfig c JOIN FETCH c.indexInfo WHERE c.enabled = true")
  List<AutoSyncConfig> findAllByEnabledTrue();

  /**
   * 특정 지수 정보에 연관된 자동 연동 설정 삭제 IndexInfo 삭제 시 연관 데이터 정리용
   */
  void deleteByIndexInfo_Id(Long indexInfoId);
}
