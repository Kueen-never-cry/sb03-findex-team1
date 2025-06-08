package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.config.AutoSyncConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long> {
  boolean existsByIndexInfoId(long indexInfoId);
  Optional<AutoSyncConfig> findByIndexInfoId(long indexInfoId);
}
