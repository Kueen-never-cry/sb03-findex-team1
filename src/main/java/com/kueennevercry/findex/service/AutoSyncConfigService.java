package com.kueennevercry.findex.service;

import com.kueennevercry.findex.config.AutoSyncConfig;
import com.kueennevercry.findex.dto.AutoSyncConfigDto;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.mapper.AutoSyncConfigMapper;
import com.kueennevercry.findex.repository.AutoSyncConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final AutoSyncConfigMapper autoSyncConfigMapper;

  @Transactional
  public AutoSyncConfigDto updateEnabled(Long id, Boolean enabled) {
    AutoSyncConfig config = autoSyncConfigRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("자동 연동 설정을 찾을 수 없습니다. ID: " + id));

    config.setEnabled(enabled);

    IndexInfo index = config.getIndexInfo();
    return autoSyncConfigMapper.toDto(
        config,
        index.getIndexClassification(),
        index.getIndexName()
    );
  }

}
