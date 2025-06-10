package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import com.kueennevercry.findex.repository.SyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobService {

  @Autowired
  private final SyncJobRepository syncJobRepository;
  private final OpenApiClient openApiClient;
  // FIXME : 어차피 이번 프로젝트에서는 /getStockMarketIndex 만 사용하므로 'application.yaml' > 'base-url' 환경변수에 아예 넣어서 사용할것.
  private String STOCK_INDEX_ENDPOINT = "/getStockMarketIndex";

  public CursorPageResponseSyncJobDto findAllByParameters(
      SyncJobParameterRequest syncJobParameterRequest) {

    return syncJobRepository.findAllByParameters(syncJobParameterRequest);
  }
}
