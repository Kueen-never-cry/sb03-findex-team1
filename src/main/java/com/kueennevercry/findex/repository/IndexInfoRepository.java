package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.request.IndexInfoListRequest;
import com.kueennevercry.findex.entity.IndexInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * IndexInfo 엔티티에 대한 기본 Repository 인터페이스
 * Spring Data JPA의 기본 CRUD 연산을 제공합니다.
 */
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoCustomRepository {

  /**
   * IndexInfoRepository는 JpaRepository를 상속받아 기본적인 CRUD 연산을 제공합니다.
   *
   * JpaRepository -> ListCrudRepository -> CrudRepository 계층 구조를 통해 다음과 같은 메서드들이
   * 자동으로 제공됩니다:
   * - Optional<IndexInfo> findById(Long id): ID로 엔티티 조회 (Optional 반환으로 null 안전성
   * 보장)
   * - List<IndexInfo> findAll(): 모든 엔티티 조회
   * - IndexInfo save(IndexInfo entity): 엔티티 저장/수정
   * - void deleteById(Long id): ID로 엔티티 삭제
   * - boolean existsById(Long id): ID 존재 여부 확인
   */

  /**
   * 지수명과 지수 분류명 조합 중복 검증
   */
  boolean existsByIndexNameAndIndexClassification(String indexName, String indexClassification);

  /**
   * 지수 정보 중 favorite = true인 것을 조회
   */
  List<IndexInfo> findAllByFavoriteTrue();

  Optional<IndexInfo> findByIndexNameAndIndexClassification(String indexName,
      String indexClassification);

  /**
   * 전체 지수 정보 요약 목록 조회 (ID 기준 오름차순 정렬)
   * 요약 정보용으로 ID, 지수명, 지수 분류명만 필요
   */
  List<IndexInfo> findAllByOrderByIdAsc();

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
