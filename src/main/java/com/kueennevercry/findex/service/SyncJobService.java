package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.IndexDataSyncRequest;
import com.kueennevercry.findex.dto.request.IndexInfoApiRequest;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.dto.response.IndexInfoApiResponse;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import com.kueennevercry.findex.entity.IntegrationTask;
import com.kueennevercry.findex.entity.SourceType;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.kueennevercry.findex.mapper.IntegrationTaskMapper;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import com.kueennevercry.findex.repository.IntegrationTaskRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncJobService {

  private final IntegrationTaskRepository integrationTaskRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;
  private final IntegrationTaskMapper integrationTaskMapper;
  private final OpenApiClient openApiClient;

  private final IndexDataMapper indexDataMapper;

  private String clientIp = null;

  public CursorPageResponseSyncJobDto findAllByParameters(
      SyncJobParameterRequest syncJobParameterRequest) {

    return integrationTaskRepository.findAllByParameters(syncJobParameterRequest);
  }

  public List<SyncJobDto> syncIndexInfo() {
    IndexInfoApiRequest apiRequestParams = IndexInfoApiRequest.builder().numOfRows(500).pageNo(1)
        .basDt("20250610") // 하루치 데이터를 기준으로 지수정보 카테고리화
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

  // 성공한 것과 실패한 것 모두 기록할 수 있게 Transactional 보단 try-catch + save 패턴으로 사용
  public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String clientIp) {
    this.clientIp = clientIp;
    
    LocalDate from = request.baseDateFrom();
    LocalDate to = request.baseDateTo();

    List<IndexInfo> indexInfos = Optional.ofNullable(request.indexInfoIds())
        .map(indexInfoRepository::findAllById)
        .orElseGet(indexInfoRepository::findAll);

    List<IntegrationTask> integrationTasks = new ArrayList<>();

    for (IndexInfo indexInfo : indexInfos) {
      try {
        List<IndexInfoApiResponse> responses = openApiClient
            .fetchAllIndexDataByNameAndDateRange(indexInfo.getIndexName(), from.toString(),
                to.toString());

        // 날짜별로 그룹화
        Map<LocalDate, List<IndexInfoApiResponse>> byDate =
            responses.stream().collect(Collectors.groupingBy(IndexInfoApiResponse::baseDate));

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
          List<IndexInfoApiResponse> daily = byDate.getOrDefault(date, List.of());

          if (daily.isEmpty()) {
            continue;
          }

          try {
            // 성공
            for (IndexInfoApiResponse response : daily) {
              upsertIndexData(indexInfo, response);
            }
            integrationTasks.add(
                buildIntegrationTaskEntity(indexInfo, date, IntegrationJobType.INDEX_DATA,
                    IntegrationResultType.SUCCESS));

          } catch (Exception e) {
            // 내부적인 오류로 실패
            integrationTasks.add(
                buildIntegrationTaskEntity(indexInfo, date, IntegrationJobType.INDEX_DATA,
                    IntegrationResultType.FAILED));
          }
        }

      } catch (Exception e) {
        // 통신 실패 또는 전체 응답 처리 실패 시 해당 범위 모든 날짜를 실패 처리
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
          integrationTasks.add(
              buildIntegrationTaskEntity(indexInfo, date, IntegrationJobType.INDEX_DATA,
                  IntegrationResultType.FAILED));
        }
      }
    }

    return integrationTaskRepository.saveAll(integrationTasks).stream()
        .map(integrationTaskMapper::toSyncJobDto)
        .toList();
  }


  private void upsertIndexData(IndexInfo indexInfo, IndexInfoApiResponse response) {
    Optional<IndexData> existing = indexDataRepository.findByIndexInfoIdAndBaseDate(
        indexInfo.getId(), response.baseDate());

    if (existing.isPresent()) {
      IndexData data = existing.get();
      data.setIndexInfo(indexInfo); // 수동 지정
      indexDataMapper.updateEntity(response, data);
      indexDataRepository.save(data);
    } else {
      IndexData newData = indexDataMapper.toEntity(response, indexInfo);
      indexDataRepository.save(newData);
    }
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

  // TODO : 가능하면 지수 정보, 지수 데이터 통합하여 사용
  private IntegrationTask buildIntegrationTaskEntity(
      IndexInfo indexInfo,
      LocalDate date,
      IntegrationJobType type,
      IntegrationResultType result
  ) {
    return IntegrationTask.builder()
        .jobType(type)
        .targetDate(date)
        .worker(this.clientIp == null ? "SYSTEM" : this.clientIp)
        .jobTime(Instant.now())
        .result(result)
        .indexInfo(indexInfo)
        .build();
  }

  private List<IntegrationTask> createIntegrationTasks(List<IntegrationTask> integrationTaskList) {
    return integrationTaskRepository.saveAll(integrationTaskList);
  }
}
