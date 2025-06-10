package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.entity.IndexInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long> {

  boolean existsByIndexInfo(IndexInfo indexInfo);

  Optional<AutoSyncConfig> findByIndexInfo(IndexInfo indexInfo);

  @Query("SELECT c FROM AutoSyncConfig c JOIN FETCH c.indexInfo WHERE c.id = :id")
  Optional<AutoSyncConfig> findById(@Param("id") Long id);
}
