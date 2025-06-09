package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;

public interface SyncJobCustomRepository {

  CursorPageResponseSyncJobDto findAllByParameters(SyncJobParameterRequest syncJobParameterRequest);
}
