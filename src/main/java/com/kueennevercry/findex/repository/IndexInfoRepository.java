package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

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
}
