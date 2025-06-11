package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import com.kueennevercry.findex.entity.IntegrationTask;
import com.kueennevercry.findex.entity.SourceType;
import com.kueennevercry.findex.infra.openapi.IndexInfoApiRequest;
import com.kueennevercry.findex.infra.openapi.IndexInfoApiResponse;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import com.kueennevercry.findex.mapper.IntegrationTaskMapper;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import com.kueennevercry.findex.repository.IntegrationTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobService {

  private final IntegrationTaskRepository integrationTaskRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IntegrationTaskMapper integrationTaskMapper;
  private final OpenApiClient openApiClient;

  public CursorPageResponseSyncJobDto findAllByParameters(
      SyncJobParameterRequest syncJobParameterRequest) {

    return integrationTaskRepository.findAllByParameters(syncJobParameterRequest);
  }

  public List<SyncJobDto> syncIndexInfo() {

    IndexInfoApiRequest apiRequestParams = IndexInfoApiRequest.builder().pageNo(1).numOfRows(5)
        .build();
    // 1. 순수 openApi에서 가져온  데이터
    List<IndexInfoApiResponse> indexDataList = openApiClient.fetchAllIndexData(apiRequestParams);

    //2. indexInfo에 이미 데이터 있으면 바로 integrationTask 데이터 삽입, 없으면 indexInfo 데이터 생성 후 integrationTask 데이터 삽입
    List<IntegrationTask> integrationTaskList = new ArrayList<>();
    indexDataList.forEach(indexInfoFromApi -> {
      Optional<IndexInfo> indexInfo = indexInfoRepository.findByIndexNameAndIndexClassification(
          indexInfoFromApi.indexName(), indexInfoFromApi.indexClassification());
      if (indexInfo.isPresent()) {
        integrationTaskList.add(this.buildIntegrationTaskEntity(indexInfo.get()));
      } else {
        IndexInfo createdIndexInfo = this.createIndexInfo(indexInfoFromApi);
        integrationTaskList.add(this.buildIntegrationTaskEntity(createdIndexInfo));
      }
    });
    List<IntegrationTask> createdIntegrationTaskList = this.createIntegrationTasks(
        integrationTaskList);

    return createdIntegrationTaskList.stream().map(integrationTaskMapper::toSyncJobDto).toList();
  }

  private IndexInfo createIndexInfo(IndexInfoApiResponse indexInfoFromApi) {
    IndexInfo newIndexInfo = IndexInfo.builder()
        .indexClassification(indexInfoFromApi.indexClassification())
        .indexName(indexInfoFromApi.indexName())
        .employedItemsCount(indexInfoFromApi.employedItemsCount())
        .basePointInTime(indexInfoFromApi.basePointInTime())
        .baseIndex(indexInfoFromApi.baseIndex()).sourceType(SourceType.OPEN_API).favorite(false)
        .build();

    return indexInfoRepository.save(newIndexInfo);
  }

  private IntegrationTask buildIntegrationTaskEntity(IndexInfo indexInfo) {
    return IntegrationTask.builder()
        .jobType(IntegrationJobType.INDEX_INFO)
        .targetDate(null)
        .worker(SourceType.OPEN_API.name())
        .jobTime(Instant.now())
        .result(IntegrationResultType.SUCCESS)  // TODO : success를 default로 두는게 맞나? -> 추후 수정
        .indexInfo(indexInfo)
        .build();
  }

  private List<IntegrationTask> createIntegrationTasks(List<IntegrationTask> integrationTaskList) {
    return integrationTaskRepository.saveAll(integrationTaskList);
  }

}
