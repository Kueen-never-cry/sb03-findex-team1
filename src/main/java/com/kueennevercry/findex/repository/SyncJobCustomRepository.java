package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;

public interface SyncJobCustomRepository {

  CursorPageResponse<SyncJobDto> findAllByParameters(
      SyncJobParameterRequest syncJobParameterRequest);
}
