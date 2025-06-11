package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;

public interface AutoSyncConfigCustomRepository {

  CursorPageResponse<AutoSyncConfigDto> findAllByParameters(
      Long indexInfoId,
      Boolean enabled,
      Long idAfter,
      String sortField,
      String sortDirection,
      int size
  );
}