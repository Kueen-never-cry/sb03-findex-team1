package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexInfoDto;
import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 Transactional(더티체킹, 플러시 방지)
public class IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;

  /**
   * ID로 지수 정보 조회
   */
  public IndexInfoDto findById(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("지수 정보를 찾을 수 없습니다. ID: " + id));
    return IndexInfoDto.from(indexInfo);
  }

  /**
   * 지수 정보 등록
   */
  @Transactional // 오버라이드: readOnly = false
  public IndexInfoDto create(IndexInfoCreateRequest request) {
    // 지수명과 지수 분류명 조합 중복 검증
    if (indexInfoRepository.existsByIndexNameAndIndexClassification(
        request.getIndexName(), request.getIndexClassification())) {
      throw new RuntimeException("이미 존재하는 지수명과 분류 조합입니다. 지수명: "
          + request.getIndexName() + ", 분류: " + request.getIndexClassification());
    }

    // 요청 DTO를 엔티티로 변환 후 저장
    IndexInfo indexInfo = request.toEntity();
    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    return IndexInfoDto.from(savedIndexInfo);
  }
}
