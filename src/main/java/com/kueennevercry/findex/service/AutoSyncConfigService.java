package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.mapper.AutoSyncConfigMapper;
import com.kueennevercry.findex.repository.AutoSyncConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    IndexInfoSummaryDto indexInfoDto = new IndexInfoSummaryDto(
        index.getId(),
        index.getIndexName(),
        index.getIndexClassification()
    );
    return autoSyncConfigMapper.toDto(config, indexInfoDto);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<AutoSyncConfigDto> getAutoSyncConfigs(
      Long indexInfoId,
      Boolean enabled,
      String cursor,
      Long idAfter,
      String sortField,
      String sortDirection,
      int size
  ) {
    Long decodedIdAfter =
        idAfter != null ? idAfter : (cursor != null ? Long.parseLong(cursor) : null);

    CursorPageResponse<AutoSyncConfigDto> response =
        autoSyncConfigRepository.findAllByParameters(
            indexInfoId, enabled, decodedIdAfter, sortField, sortDirection, size
        );

    String nextCursor = (response.hasNext() && !response.content().isEmpty())
        ? String.valueOf(response.content().get(response.content().size() - 1).id())
        : null;

    return new CursorPageResponse<>(
        response.content(),
        nextCursor,
        response.nextIdAfter(),
        response.size(),
        response.totalElements(),
        response.hasNext()
    );
  }
}