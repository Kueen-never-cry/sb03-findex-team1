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
import java.util.Objects;
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

  public List<SyncJobDto> syncIndexInfo(String clientIp) {
    this.clientIp = clientIp;
    // 1. openApi에서 가져온 외부 데이터
    List<IndexInfoApiResponse> openApiDataList = this.fetchIndexInfoFromOpenApi();
    List<IntegrationTask> integrationTaskList = new ArrayList<>();

    //2. indexInfo 생성 or 조회
    for (IndexInfoApiResponse indexInfoFromApi : openApiDataList) {
      IndexInfo indexInfo = null;
      try {
        // DB에서 있는 데이터 가져와서 다르면 업데이트, 없으면 DB에 생성해서 가져옴
        Optional<IndexInfo> optional = indexInfoRepository.findByIndexNameAndIndexClassification(
            indexInfoFromApi.indexName(), indexInfoFromApi.indexClassification());
        if (optional.isPresent()) {
          indexInfo = optional.get();
          this.updateIndexInfo(indexInfo, indexInfoFromApi);
        } else {
          indexInfo = this.saveIndexInfo(indexInfoFromApi);
        }

        integrationTaskList.add(
            this.buildIntegrationTaskEntity(indexInfo, null, IntegrationJobType.INDEX_INFO,
                IntegrationResultType.SUCCESS)
        );
      } catch (Exception e) {
        // 실패해도, integration task 데이터 넣음
        integrationTaskList.add(
            this.buildIntegrationTaskEntity(indexInfo, null, IntegrationJobType.INDEX_INFO,
                IntegrationResultType.FAILED)
        );
      }
    }

    // 3. integrationTask DB 저장
    return this.saveIntegrationTasks(integrationTaskList)
        .stream()
        .map(integrationTaskMapper::toSyncJobDto)
        .toList();
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
            .fetchAllIndexDataByNameAndDateRange(indexInfo.getIndexName(), from,
                to);

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

  /*private methods*/

  private void updateIndexInfo(IndexInfo existed, IndexInfoApiResponse indexInfoFromApi) {
    boolean updated = false;
    if (!Objects.equals(existed.getEmployedItemsCount(), indexInfoFromApi.employedItemsCount())) {
      existed.setEmployedItemsCount(indexInfoFromApi.employedItemsCount());
      updated = true;
    }
    if (!Objects.equals(existed.getBasePointInTime(), indexInfoFromApi.basePointInTime())) {
      existed.setBasePointInTime(indexInfoFromApi.basePointInTime());
      updated = true;
    }
    if (!Objects.equals(existed.getBaseIndex(), indexInfoFromApi.baseIndex())) {
      existed.setBaseIndex(indexInfoFromApi.baseIndex());
      updated = true;
    }
    if (!existed.getSourceType().equals(SourceType.OPEN_API)) {
      existed.setSourceType(SourceType.OPEN_API);
      updated = true;
    }
    if (updated) {
      indexInfoRepository.save(existed);
      indexInfoRepository.flush();
    }
  }

  private List<IndexInfoApiResponse> fetchIndexInfoFromOpenApi() {
    try {
      IndexInfoApiRequest apiRequestParams = IndexInfoApiRequest.builder().numOfRows(500).pageNo(1)
          .basDt("20250610") // 하루치 데이터를 기준으로 지수정보 카테고리화
          .build();
      return openApiClient.fetchAllIndexData(apiRequestParams);
    } catch (Exception e) {
      IntegrationTask createdIntegrationTask = this.buildIntegrationTaskEntity(null, null,
          IntegrationJobType.INDEX_INFO,
          IntegrationResultType.FAILED);
      this.saveIntegrationTasks(List.of(createdIntegrationTask));
      throw new RuntimeException("OPEN Api의 데이터를 가져올 수 없습니다.");
    }
  }

  private void upsertIndexData(IndexInfo indexInfo, IndexInfoApiResponse response) {
    Optional<IndexData> existing = indexDataRepository.findByIndexInfo_IdAndBaseDate(
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

  private IndexInfo saveIndexInfo(IndexInfoApiResponse indexInfoFromApi) {
    IndexInfo newIndexInfo = IndexInfo.builder()
        .indexClassification(indexInfoFromApi.indexClassification())
        .indexName(indexInfoFromApi.indexName())
        .employedItemsCount(indexInfoFromApi.employedItemsCount())
        .basePointInTime(indexInfoFromApi.basePointInTime())
        .baseIndex(indexInfoFromApi.baseIndex()).sourceType(SourceType.OPEN_API).favorite(false)
        .build();

    return indexInfoRepository.save(newIndexInfo);
  }

  private List<IntegrationTask> saveIntegrationTasks(List<IntegrationTask> integrationTaskList) {
    return integrationTaskRepository.saveAllAndFlush(integrationTaskList);
  }
}
