package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexInfoDto;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
}
