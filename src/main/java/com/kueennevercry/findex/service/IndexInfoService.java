package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.dto.request.IndexInfoUpdateRequest;
import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.mapper.IndexInfoMapper;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.repository.AutoSyncConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

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
   * 전체 지수 정보 요약 목록 조회
   * ID, 지수명, 지수 분류명만 포함한 간단한 목록을 반환
   */
  public List<IndexInfoSummaryDto> findAllSummaries() {
    List<IndexInfo> indexInfos = indexInfoRepository.findAllByOrderByIdAsc();
    return indexInfos.stream()
        .map(indexInfoMapper::toSummaryDto)
        .toList();
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
    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    return indexInfoMapper.toDto(savedIndexInfo);
  }

  /**
   * 지수 정보 수정
   *
   * JPA 더티 체킹(Dirty Checking) 활용
   * - findById()로 조회한 엔티티는 영속성 컨텍스트에서 관리됨
   * - 엔티티 필드 변경 시 JPA가 자동으로 변경사항을 감지
   * - @Transactional 메서드 종료 시점에 자동으로 UPDATE 쿼리 실행
   * - 따라서 repository.save() 호출이 불필요함
   *
   * 주의사항:
   * - @Transactional 어노테이션이 반드시 필요
   * - findById()로 조회한 영속 상태의 엔티티여야 함
   * - 변경된 필드만 UPDATE 쿼리에 포함되어 성능상 유리
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
   * 
   * 삭제 순서:
   * 1. 지수 정보 존재 여부 확인
   * 2. 연관된 지수 데이터(IndexData) 삭제
   * 3. 연관된 자동 연동 설정(AutoSyncConfig) 삭제
   * 4. 지수 정보(IndexInfo) 삭제
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
