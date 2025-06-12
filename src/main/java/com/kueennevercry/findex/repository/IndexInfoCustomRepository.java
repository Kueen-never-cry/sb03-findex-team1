package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.request.IndexInfoListRequest;
import com.kueennevercry.findex.entity.IndexInfo;
import java.util.List;

/**
 * IndexInfo 엔티티에 대한 커스텀 Repository 인터페이스
 * 복잡한 동적 쿼리와 커서 기반 페이징을 제공합니다.
 */
public interface IndexInfoCustomRepository {

  /**
   * 커서 기반 페이징으로 지수 정보 목록을 조회합니다.
   *
   * @param request 검색 조건 및 페이징 정보
   * @return 조회된 지수 정보 목록 (최대 size+1개)
   */
  List<IndexInfo> findWithCursorPaging(IndexInfoListRequest request);

  /**
   * 필터 조건에 맞는 전체 데이터 개수를 조회합니다.
   *
   * @param request 검색 조건
   * @return 조건에 맞는 전체 데이터 개수
   */
  Long countWithFilters(IndexInfoListRequest request);
}