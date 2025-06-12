package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.request.IndexInfoListRequest;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.QIndexInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * IndexInfo 엔티티에 대한 커스텀 Repository 구현체
 * QueryDSL을 사용하여 복잡한 동적 쿼리와 커서 기반 페이징을 구현합니다.
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexInfoCustomRepositoryImpl implements IndexInfoCustomRepository {

  private final JPAQueryFactory queryFactory;
  private static final QIndexInfo indexInfo = QIndexInfo.indexInfo;

  /**
   * 커서 기반 페이징으로 지수 정보 목록을 조회합니다.
   * <p>
   * 커서 기반 페이징 구현 전략:
   * 1. 기본 필터 조건 적용 (indexClassification, indexName, favorite)
   * 2. 커서 조건 적용 (이전 페이지 마지막 항목 기준으로 다음 데이터 조회)
   * 3. 복합 정렬 조건 적용 (주 정렬 필드 + ID 보조 정렬)
   * 4. size+1개 조회하여 다음 페이지 존재 여부 판단
   * 
   * @param request 검색 조건 및 페이징 정보
   * @return 조회된 지수 정보 목록 (최대 size+1개, hasNext 판단용)
   */
  @Override
  public List<IndexInfo> findWithCursorPaging(IndexInfoListRequest request) {
    // 1. 기본 WHERE 조건 구성 (필터링)
    BooleanBuilder whereCondition = buildFilterConditions(request);

    // 2. 커서 조건 적용
    // 이전 페이지의 마지막 항목을 기준으로 다음 데이터를 조회하는 조건 생성
    BooleanExpression cursorCondition = buildCursorCondition(request);
    if (cursorCondition != null) {
      whereCondition.and(cursorCondition);
    }

    // 3. 정렬 조건 구성 (복합 정렬: 주 필드 + ID)
    OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifier(request);

    // 4. 쿼리 실행
    // size+1개를 조회하는 이유: 다음 페이지 존재 여부(hasNext) 판단을 위함
    // 예: size=10이면 11개 조회 → 11개가 나오면 hasNext=true, 10개 이하면 hasNext=false
    return queryFactory
        .selectFrom(indexInfo)
        .where(whereCondition)
        .orderBy(orderSpecifiers) // 복합 정렬 적용
        .limit(request.size() + 1) // 다음 페이지 존재 여부 판단을 위해 +1
        .fetch();
  }

  /**
   * 필터 조건에 맞는 전체 데이터 개수를 조회합니다.
   */
  @Override
  public Long countWithFilters(IndexInfoListRequest request) {
    // findWithCursorPaging과 동일한 필터 조건 적용 (커서 조건은 제외: 현재 페이지 이후의 개수가 아님)
    BooleanBuilder whereCondition = buildFilterConditions(request);

    return queryFactory
        .select(indexInfo.count())
        .from(indexInfo)
        .where(whereCondition)
        .fetchOne();
  }

  /**
   * 공통 필터링 조건을 구성합니다.
   * findWithCursorPaging과 countWithFilters에서 동일한 조건을 사용합니다.
   * 
   * 필터링 조건:
   * 1. indexClassification: 부분 일치, 대소문자 무시
   * 2. indexName: 부분 일치, 대소문자 무시
   * 3. favorite: 정확 일치 (true/false)
   */
  private BooleanBuilder buildFilterConditions(IndexInfoListRequest request) {
    BooleanBuilder whereCondition = new BooleanBuilder();

    // 지수 분류명 필터링 (부분 일치, 대소문자 구분 없음)
    if (StringUtils.hasText(request.indexClassification())) {
      whereCondition.and(
          indexInfo.indexClassification.containsIgnoreCase(request.indexClassification()));
    }

    // 지수명 필터링 (부분 일치, 대소문자 구분 없음)
    if (StringUtils.hasText(request.indexName())) {
      whereCondition.and(indexInfo.indexName.containsIgnoreCase(request.indexName()));
    }

    // 즐겨찾기 필터링
    if (request.favorite() != null) {
      whereCondition.and(indexInfo.favorite.eq(request.favorite()));
    }

    return whereCondition;
  }

  /**
   * 커서 조건을 구성
   * 
   * 커서 기반 페이징의 원리:
   * - 이전 페이지의 마지막 요소를 기준으로 다음 데이터를 조회
   * - 정렬 기준과 동일한 필드로 커서 조건을 구성해야 데이터 누락 방지
   * 
   * 조건 생성 로직:
   * 1. 정렬 필드에 따라 적절한 커서 조건 메서드 호출
   * 2. cursor와 idAfter 모두 필요 (복합 조건)
   * 3. 정렬 방향(asc/desc)에 따라 비교 연산자 변경
   * 
   * @param request 커서 정보가 포함된 요청 객체
   * @return 커서 기반 WHERE 조건 (null이면 첫 페이지)
   */
  private BooleanExpression buildCursorCondition(IndexInfoListRequest request) {
    String sortField = request.sortField();
    String sortDirection = request.sortDirection();
    Long idAfter = request.idAfter();
    String cursor = request.cursor();

    // cursor와 idAfter가 모두 없으면 첫 페이지 요청으로 간주
    if ((cursor == null || cursor.trim().isEmpty()) && (idAfter == null || idAfter <= 0)) {
      return null;
    }

    // 정렬 기준에 따른 커서 조건 생성
    switch (sortField) {
      case "indexClassification":
        return buildClassificationCursorCondition(cursor, idAfter, sortDirection);
      case "indexName":
        return buildIndexNameCursorCondition(cursor, idAfter, sortDirection);
      default:
        // ID 정렬인 경우 단순 ID 비교
        if (idAfter != null && idAfter > 0) {
          return indexInfo.id.gt(idAfter);
        }
        return null;
    }
  }

  /**
   * 분류명 기준 커서 조건 생성
   */
  private BooleanExpression buildClassificationCursorCondition(String cursor, Long idAfter, String sortDirection) {
    if (cursor == null || cursor.trim().isEmpty() || idAfter == null || idAfter <= 0) {
      return null;
    }

    boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

    if (isDesc) {
      // 내림차순: (indexClassification < cursor) OR (indexClassification = cursor AND id
      // > idAfter)
      return indexInfo.indexClassification.lt(cursor)
          .or(indexInfo.indexClassification.eq(cursor).and(indexInfo.id.gt(idAfter)));
    } else {
      // 오름차순: (indexClassification > cursor) OR (indexClassification = cursor AND id
      // > idAfter)
      return indexInfo.indexClassification.gt(cursor)
          .or(indexInfo.indexClassification.eq(cursor).and(indexInfo.id.gt(idAfter)));
    }
  }

  /**
   * 지수명 기준 커서 조건 생성
   */
  private BooleanExpression buildIndexNameCursorCondition(String cursor, Long idAfter, String sortDirection) {
    if (cursor == null || cursor.trim().isEmpty() || idAfter == null || idAfter <= 0) {
      return null;
    }

    boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

    if (isDesc) {
      // 내림차순: (indexName < cursor) OR (indexName = cursor AND id > idAfter)
      return indexInfo.indexName.lt(cursor)
          .or(indexInfo.indexName.eq(cursor).and(indexInfo.id.gt(idAfter)));
    } else {
      // 오름차순: (indexName > cursor) OR (indexName = cursor AND id > idAfter)
      return indexInfo.indexName.gt(cursor)
          .or(indexInfo.indexName.eq(cursor).and(indexInfo.id.gt(idAfter)));
    }
  }

  /**
   * 정렬 조건 구성
   * 
   * 커서 기반 페이징에서는 일관된 정렬 순서가 매우 중요
   * 복합 정렬을 사용하는 이유:
   * 1. 주 정렬 필드: 사용자가 요청한 정렬 기준 (분류명 또는 지수명)
   * 2. 보조 정렬 필드: ID (항상 오름차순)
   * 
   * 보조 정렬이 필요한 이유:
   * - 동일한 분류명/지수명을 가진 데이터들 사이에서 일관된 순서 보장
   * - 커서 조건에서 ID 비교를 통한 정확한 위치 특정
   * - 데이터 추가/삭제 시에도 안정적인 페이징
   * 
   * 생성되는 ORDER BY 예시:
   * - 분류명 오름차순: ORDER BY index_classification ASC, id ASC
   * - 분류명 내림차순: ORDER BY index_classification DESC, id ASC
   * - 지수명 오름차순: ORDER BY index_name ASC, id ASC
   * - 지수명 내림차순: ORDER BY index_name DESC, id ASC
   * 
   * @param request 정렬 정보가 포함된 요청 객체
   * @return 복합 정렬 조건 배열
   */
  private OrderSpecifier<?>[] buildOrderSpecifier(IndexInfoListRequest request) {
    String sortField = request.sortField();
    String sortDirection = request.sortDirection();
    boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

    switch (sortField) {
      case "indexClassification":
        return isDesc
            ? new OrderSpecifier[] { indexInfo.indexClassification.desc(), indexInfo.id.asc() }
            : new OrderSpecifier[] { indexInfo.indexClassification.asc(), indexInfo.id.asc() };

      case "indexName":
        return isDesc
            ? new OrderSpecifier[] { indexInfo.indexName.desc(), indexInfo.id.asc() }
            : new OrderSpecifier[] { indexInfo.indexName.asc(), indexInfo.id.asc() };

      default:
        // ID 정렬 또는 기본 정렬
        return new OrderSpecifier[] { indexInfo.id.asc() };
    }
  }
}