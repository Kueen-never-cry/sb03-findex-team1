package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.request.IndexInfoListRequest;
import com.kueennevercry.findex.dto.request.IndexInfoUpdateRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.mapper.IndexInfoMapper;
import com.kueennevercry.findex.repository.AutoSyncConfigRepository;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 Transactional(더티체킹, 플러시 방지)
public class IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;
  private final IndexInfoMapper indexInfoMapper;
  private final IndexDataRepository indexDataRepository;
  private final AutoSyncConfigRepository autoSyncConfigRepository;

  /**
   * ID로 지수 정보 조회
   */
  public IndexInfoDto findById(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("지수 정보를 찾을 수 없습니다. ID: " + id));
    return indexInfoMapper.toDto(indexInfo);
  }

  /**
   * 전체 지수 정보 요약 목록 조회 ID, 지수명, 지수 분류명만 포함한 간단한 목록을 반환
   */
  public List<IndexInfoSummaryDto> findAllSummaries() {
    List<IndexInfo> indexInfos = indexInfoRepository.findAllByOrderByIdAsc();
    return indexInfos.stream()
        .map(indexInfoMapper::toSummaryDto)
        .toList();
  }

  /**
   * 커서 기반 페이징으로 지수 정보 목록 조회

   * 
   * 동작 과정:
   * 1. 요청 파라미터 검증 및 기본값 적용
   * 2. Repository에서 size+1개 데이터 조회
   * 3. 다음 페이지 존재 여부 판단
   * 4. 다음 커서 정보 생성

   * 5. 응답 DTO 구성
   * 
   * @param request 페이징 및 필터링 조건
   * @return 커서 페이징 응답 (데이터 목록 + 페이징 메타데이터)
   */
  public CursorPageResponse<IndexInfoDto> findWithCursorPaging(IndexInfoListRequest request) {
    // 1. 요청 파라미터 검증 및 기본값 적용
    // 유효하지 않은 정렬 필드, 방향, 페이지 크기 등을 사전에 검증
    IndexInfoListRequest validatedRequest = validateAndApplyDefaults(request);

    // 2. Repository에서 데이터 조회 (size+1개)
    // +1개를 조회하는 이유: 다음 페이지 존재 여부(hasNext) 판단을 위함
    // 예: 10개 요청 시 11개 조회 → 11개가 나오면 hasNext=true
    List<IndexInfo> indexInfos = indexInfoRepository.findWithCursorPaging(validatedRequest);

    // 3. 다음 페이지 존재 여부 판단
    // 조회된 데이터가 요청한 size보다 많으면 다음 페이지가 존재
    boolean hasNext = indexInfos.size() > validatedRequest.size();

    // 4. 실제 반환할 데이터 (size개만)
    // hasNext 판단을 위해 +1개 조회했으므로, 실제로는 요청한 size만큼만 반환
    List<IndexInfo> actualContent = hasNext
        ? indexInfos.subList(0, validatedRequest.size()) // 마지막 1개 제거
        : indexInfos; // 전체 반환

    // 5. 엔티티를 DTO로 변환
    List<IndexInfoDto> contentDtos = actualContent.stream()
        .map(indexInfoMapper::toDto)
        .toList();

    // 6. 다음 커서 정보 생성
    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !actualContent.isEmpty()) {
      // 현재 페이지의 마지막 항목을 기준으로 다음 페이지 커서 생성
      IndexInfo lastItem = actualContent.get(actualContent.size() - 1);
      nextIdAfter = lastItem.getId(); // 보조 정렬 기준 (ID)
      nextCursor = generateCursor(lastItem, validatedRequest); // 주 정렬 기준 값
    }

    // 7. 전체 개수 계산 (필터 조건에 맞는 전체 데이터 개수)
    Long totalElements = indexInfoRepository.countWithFilters(validatedRequest);

    // 8. 응답 DTO 구성
    return CursorPageResponse.of(contentDtos, nextCursor, nextIdAfter, hasNext, totalElements);
  }

  /**
   * 요청 파라미터 검증 및 기본값 적용
   * 
   * 검증 항목:
   * 1. sortField: "indexClassification" 또는 "indexName"만 허용
   * 2. sortDirection: "asc" 또는 "desc"만 허용
   * 3. size: 1-100 범위 내에서만 허용
   * 
   * 기본값:
   * - sortField: "indexClassification" (분류명 기준 정렬)
   * - sortDirection: "asc" (오름차순)
   * - size: 10 (한 페이지당 10개)
   * 
   * @param request 원본 요청 객체
   * @return 검증되고 기본값이 적용된 요청 객체
   * @throws IllegalArgumentException 유효하지 않은 파라미터가 있는 경우
   */
  private IndexInfoListRequest validateAndApplyDefaults(IndexInfoListRequest request) {
    // 파라미터 검증
    if (!request.isValidSortField()) {
      throw new IllegalArgumentException("유효하지 않은 정렬 필드입니다: " + request.sortField());
    }

    if (!request.isValidSortDirection()) {
      throw new IllegalArgumentException("유효하지 않은 정렬 방향입니다: " + request.sortDirection());
    }

    if (!request.isValidSize()) {
      throw new IllegalArgumentException("페이지 크기는 1-100 사이여야 합니다: " + request.size());
    }

    // 기본값 적용
    return request.withDefaults();
  }

  /**
   * 다음 페이지를 위한 커서 생성

   * 
   * 커서 생성 전략:
   * - 정렬 기준 필드의 값을 커서로 사용
   * - 클라이언트는 이 커서 값과 ID를 함께 전달하여 다음 페이지 요청
   * 
   * 정렬 필드별 커서 값:
   * 1. indexClassification 정렬: 마지막 항목의 분류명 → cursor="KOSPI지수"
   * 2. indexName 정렬: 마지막 항목의 지수명 → cursor="삼성전자"
   * 3. ID 정렬 (기본): 마지막 항목의 ID → cursor="123"
   * 
   * 사용 예시:
   * 현재 페이지 마지막 항목이 {id: 123, indexClassification: "KOSPI지수", indexName: "삼성전자"}인
   * 경우
   * - 분류명 정렬 시: nextCursor="KOSPI지수", nextIdAfter=123
   * - 지수명 정렬 시: nextCursor="삼성전자", nextIdAfter=123
   * 
   * @param lastItem 현재 페이지의 마지막 항목
   * @param request  정렬 정보가 포함된 요청 객체
   * @return 다음 페이지 요청 시 사용할 커서 값

   */
  private String generateCursor(IndexInfo lastItem, IndexInfoListRequest request) {
    String sortField = request.sortField();

    switch (sortField) {
      case "indexClassification":
        // 분류명 기준 정렬 시: 마지막 항목의 분류명을 커서로 사용
        return lastItem.getIndexClassification();
      case "indexName":
        // 지수명 기준 정렬 시: 마지막 항목의 지수명을 커서로 사용
        return lastItem.getIndexName();
      default:
        // ID 정렬 또는 기본값: 마지막 항목의 ID를 문자열로 변환하여 커서로 사용
        return String.valueOf(lastItem.getId());
    }
  }

  /**
   * 지수 정보 등록
   */
  @Transactional // 오버라이드: readOnly = false
  public IndexInfoDto create(IndexInfoCreateRequest request) {
    // 지수명과 지수 분류명 조합 중복 검증
    if (indexInfoRepository.existsByIndexNameAndIndexClassification(
        request.indexName(), request.indexClassification())) {
      throw new RuntimeException("이미 존재하는 지수명과 분류 조합입니다. 지수명: "
          + request.indexName() + ", 분류: " + request.indexClassification());
    }

    // 요청 DTO를 엔티티로 변환 후 저장 (Mapper 사용)
    IndexInfo indexInfo = indexInfoMapper.toEntity(request);

    // sourceType이 null인 경우 기본값 설정 (안전장치)
    if (indexInfo.getSourceType() == null) {
      indexInfo.setSourceType(com.kueennevercry.findex.entity.SourceType.USER);
    }

    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    // 자동 연동 설정 등록
    autoSyncConfigRepository.save(new AutoSyncConfig(savedIndexInfo));

    return indexInfoMapper.toDto(savedIndexInfo);
  }

  /**
   * 지수 정보 수정
   * <p>
   * JPA 더티 체킹(Dirty Checking) 활용 - findById()로 조회한 엔티티는 영속성 컨텍스트에서 관리됨 - 엔티티 필드 변경 시 JPA가 자동으로
   * 변경사항을 감지 - @Transactional 메서드 종료 시점에 자동으로 UPDATE 쿼리 실행 - 따라서 repository.save() 호출이 불필요함
   * <p>
   * 주의사항: - @Transactional 어노테이션이 반드시 필요 - findById()로 조회한 영속 상태의 엔티티여야 함 - 변경된 필드만 UPDATE 쿼리에 포함되어
   * 성능상 유리
   */
  @Transactional // 오버라이드: readOnly = false
  public IndexInfoDto update(Long id, IndexInfoUpdateRequest request) {
    // 1. 기존 지수 정보 조회 (영속성 컨텍스트에 엔티티 저장 + 스냅샷 생성)
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("지수 정보를 찾을 수 없습니다. ID: " + id));

    // 2. 수정 가능한 필드만 업데이트 (null이 아닌 경우에만)
    // 각 setter 호출 시 JPA가 변경사항을 감지함
    if (request.employedItemsCount() != null) {
      indexInfo.setEmployedItemsCount(request.employedItemsCount());
    }
    if (request.basePointInTime() != null) {
      indexInfo.setBasePointInTime(request.basePointInTime());
    }
    if (request.baseIndex() != null) {
      indexInfo.setBaseIndex(request.baseIndex());
    }
    if (request.favorite() != null) {
      indexInfo.setFavorite(request.favorite());
    }

    // 3. 메서드 종료 시 @Transactional에 의해 자동으로 UPDATE 쿼리 실행
    // repository.save() 호출 불필요 (더티 체킹이 자동 처리)
    return indexInfoMapper.toDto(indexInfo);
  }

  /**
   * 지수 정보 삭제
   * <p>
   * 삭제 순서: 1. 지수 정보 존재 여부 확인 2. 연관된 지수 데이터(IndexData) 삭제 3. 연관된 자동 연동 설정(AutoSyncConfig) 삭제 4. 지수
   * 정보(IndexInfo) 삭제
   *
   * @Transactional을 통해 모든 삭제 작업이 원자적으로 실행됨
   */
  @Transactional // 오버라이드: readOnly = false
  public void delete(Long id) {
    // 1. 지수 정보 존재 여부 확인
    if (!indexInfoRepository.existsById(id)) {
      throw new EntityNotFoundException("삭제할 지수 정보를 찾을 수 없습니다. ID: " + id);
    }

    // 2. 연관된 지수 데이터 삭제 (IndexData)
    indexDataRepository.deleteAllByIndexInfoId(id);

    // 3. 연관된 자동 연동 설정 삭제 (AutoSyncConfig)
    autoSyncConfigRepository.deleteByIndexInfo_Id(id);

    // 4. 지수 정보 삭제 (IndexInfo)
    indexInfoRepository.deleteById(id);
  }
}
