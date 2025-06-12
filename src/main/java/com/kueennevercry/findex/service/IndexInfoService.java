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
   * <p>
   * 커서 기반 페이징의 장점: 1. 대용량 데이터에서 일관된 성능 (OFFSET 없이 WHERE 조건만 사용) 2. 실시간 데이터 변경에도 안정적 (중복/누락 방지) 3.
   * 무한 스크롤 UI에 최적화
   * <p>
   * 동작 과정: 1. 요청 파라미터 검증 및 기본값 적용 2. Repository에서 size+1개 데이터 조회 3. 다음 페이지 존재 여부 판단 4. 다음 커서 정보 생성
   * 5. 응답 DTO 구성
   */
  public CursorPageResponse<IndexInfoDto> findWithCursorPaging(IndexInfoListRequest request) {
    // 1. 요청 파라미터 검증 및 기본값 적용
    IndexInfoListRequest validatedRequest = validateAndApplyDefaults(request);

    // 2. Repository에서 데이터 조회 (size+1개)
    List<IndexInfo> indexInfos = indexInfoRepository.findWithCursorPaging(validatedRequest);

    // 3. 다음 페이지 존재 여부 판단
    boolean hasNext = indexInfos.size() > validatedRequest.size();

    // 4. 실제 반환할 데이터 (size개만)
    List<IndexInfo> actualContent = hasNext
        ? indexInfos.subList(0, validatedRequest.size())
        : indexInfos;

    // 5. 엔티티를 DTO로 변환
    List<IndexInfoDto> contentDtos = actualContent.stream()
        .map(indexInfoMapper::toDto)
        .toList();

    // 6. 다음 커서 정보 생성
    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !actualContent.isEmpty()) {
      IndexInfo lastItem = actualContent.get(actualContent.size() - 1);
      nextIdAfter = lastItem.getId();
      nextCursor = generateCursor(lastItem, validatedRequest);
    }

    // 7. 전체 개수 계산 (필터 조건에 맞는 전체 데이터 개수)
    Long totalElements = indexInfoRepository.countWithFilters(validatedRequest);

    // 8. 응답 DTO 구성
    return CursorPageResponse.of(contentDtos, nextCursor, nextIdAfter, hasNext, totalElements);
  }

  /**
   * 요청 파라미터 검증 및 기본값 적용
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
   * <p>
   * 현재는 단순 ID 기반이지만, 복합 정렬이 필요한 경우 Base64로 인코딩된 복합 커서를 생성할 수 있습니다.
   * <p>
   * 예: {"sortField": "indexName", "sortValue": "IT서비스", "id": 123} -> Base64 인코딩 ->
   * "eyJzb3J0RmllbGQiOi..."
   */
  private String generateCursor(IndexInfo lastItem, IndexInfoListRequest request) {
    // 현재는 단순 구현 (ID만 사용)
    // 필요시 복합 정렬을 위한 JSON 기반 커서로 확장 가능
    return "cursor_" + lastItem.getId(); // 단순 예시
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
