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
   * 2. 커서 조건 적용 (idAfter 또는 복합 정렬 기준)
   * 3. 정렬 조건 적용
   * 4. size+1개 조회하여 다음 페이지 존재 여부 판단
   */
  @Override
  public List<IndexInfo> findWithCursorPaging(IndexInfoListRequest request) {
    // 1. 기본 WHERE 조건 구성
    BooleanBuilder whereCondition = buildFilterConditions(request);

    // 2. 커서 조건 적용
    BooleanExpression cursorCondition = buildCursorCondition(request);
    if (cursorCondition != null) {
      whereCondition.and(cursorCondition);
    }

    // 3. 정렬 조건 구성
    OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(request);

    // 4. 쿼리 실행 (size+1개 조회)
    return queryFactory
        .selectFrom(indexInfo)
        .where(whereCondition)
        .orderBy(orderSpecifier)
        .limit(request.size() + 1) // 다음 페이지 존재 여부 판단을 위해 +1
        .fetch();
  }

  /**
   * 필터 조건에 맞는 전체 데이터 개수를 조회합니다.
   */
  @Override
  public Long countWithFilters(IndexInfoListRequest request) {
    // findWithCursorPaging과 동일한 필터 조건 적용 (커서 조건은 제외)
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
   * 커서 조건을 구성합니다.
   * <p>
   * 커서 기반 페이징에서는 이전 페이지의 마지막 요소를 기준으로 다음 데이터를 조회합니다.
   * 예: ID 기준 오름차순 정렬 시, WHERE id > {lastId} 조건 추가
   */
  private BooleanExpression buildCursorCondition(IndexInfoListRequest request) {
    // 단순 ID 기반 커서 (가장 일반적인 방식)
    if (request.idAfter() != null && request.idAfter() > 0) {
      String sortDirection = request.sortDirection();

      // ID 기준 정렬인 경우
      if ("id".equals(request.sortField()) || request.sortField() == null) {
        return "desc".equals(sortDirection)
            ? indexInfo.id.lt(request.idAfter()) // 내림차순: id < lastId
            : indexInfo.id.gt(request.idAfter()); // 오름차순: id > lastId
      }

      // 다른 필드 기준 정렬인 경우, ID를 보조 정렬 기준으로 사용
      // 복잡한 커서 로직이 필요하지만, 여기서는 단순화하여 ID만 사용
      return indexInfo.id.gt(request.idAfter());
    }

    // TODO: 복합 정렬을 위한 cursor 문자열 파싱 로직
    // Base64로 인코딩된 커서 정보를 파싱하여 복합 조건 생성
    if (StringUtils.hasText(request.cursor())) {
      // 현재는 단순 구현, 필요시 확장
      return null;
    }

    return null;
  }

  /**
   * 정렬 조건을 구성합니다.
   * <p>
   * 커서 기반 페이징에서는 일관된 정렬 순서가 매우 중요합니다.
   * 동일한 값을 가진 레코드들의 순서가 바뀌면 데이터 중복이나 누락이 발생할 수 있습니다.
   */
  private OrderSpecifier<?> buildOrderSpecifier(IndexInfoListRequest request) {
    String sortField = request.sortField();
    boolean isDesc = "desc".equals(request.sortDirection());

    // 정렬 필드에 따른 OrderSpecifier 생성
    return switch (sortField) {
      case "indexClassification" -> isDesc
          ? indexInfo.indexClassification.desc()
          : indexInfo.indexClassification.asc();
      case "indexName" -> isDesc
          ? indexInfo.indexName.desc()
          : indexInfo.indexName.asc();
      case "employedItemsCount" -> isDesc
          ? indexInfo.employedItemsCount.desc()
          : indexInfo.employedItemsCount.asc();
      default -> isDesc
          ? indexInfo.indexClassification.desc() // 기본값
          : indexInfo.indexClassification.asc();
    };
  }
}