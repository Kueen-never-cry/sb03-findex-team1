package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.QIndexData;
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IndexDataCustomRepositoryImpl implements IndexDataCustomRepository {

  private final JPAQueryFactory queryFactory;
  private final QIndexData indexData = QIndexData.indexData;
  private final IndexDataMapper indexDataMapper;


  @Override
  public CursorPageResponse<IndexDataDto> findCursorPage(Long indexInfoId,
      LocalDate from, LocalDate to,
      Long idAfter, String cursor,
      String sortField, String sortDirection, int size) {

    BooleanExpression baseCondition =
        indexData.indexInfo.id.eq(indexInfoId)
            .and(indexData.baseDate.gt(from))
            .and(indexData.baseDate.lt(to));
    BooleanExpression cursorCondition = buildCursorCondition(idAfter, sortField, sortDirection);

    OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(sortField, sortDirection);

    List<IndexData> result = queryFactory
        .selectFrom(indexData)
        .where(baseCondition.and(cursorCondition))
        .orderBy(orderSpecifier)
        .limit(size + 1)
        .fetch();

    boolean hasNext = result.size() > size;
    List<IndexData> content = hasNext ? result.subList(0, size) : result;

    List<IndexDataDto> dtoList = content.stream()
        .map(indexDataMapper::toDto)
        .toList();

    String nextCursor =
        content.isEmpty() ? null : String.valueOf(content.get(content.size() - 1).getId());
    Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).getId();

    Long totalElements = queryFactory
        .select(indexData.count())
        .from(indexData)
        .where(baseCondition)
        .fetchOne();

    return indexDataMapper.toCursorDto(
        dtoList,
        nextCursor,
        nextIdAfter,
        dtoList.size(),
        totalElements,
        hasNext
    );
  }

  private BooleanExpression buildCursorCondition(Long idAfter, String sortField,
      String sortDirection) {
    if (idAfter == null) {
      return null;
    }

    switch (sortField) {
      case "id":
        return sortDirection.equalsIgnoreCase("desc") ? indexData.id.lt(idAfter)
            : indexData.id.gt(idAfter);
      case "baseDate":
        return sortDirection.equalsIgnoreCase("desc") ? indexData.baseDate.lt(
            fetchBaseDate(idAfter))
            : indexData.baseDate.gt(fetchBaseDate(idAfter));
      case "closingPrice":
        return sortDirection.equalsIgnoreCase("desc") ? indexData.closingPrice.lt(
            fetchClosingPrice(idAfter))
            : indexData.closingPrice.gt(fetchClosingPrice(idAfter));
      default:
        throw new IllegalArgumentException("Unsupported sort field: " + sortField);
    }
  }

  private OrderSpecifier<?> buildOrderSpecifier(String sortField, String direction) {
    Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);

    switch (sortField) {
      case "id":
        return dir.isAscending() ? indexData.id.asc() : indexData.id.desc();
      case "baseDate":
        return dir.isAscending() ? indexData.baseDate.asc() : indexData.baseDate.desc();
      case "closingPrice":
        return dir.isAscending() ? indexData.closingPrice.asc() : indexData.closingPrice.desc();
      default:
        throw new IllegalArgumentException("Unsupported sort field: " + sortField);
    }
  }

  private LocalDate fetchBaseDate(Long id) {
    return queryFactory
        .select(indexData.baseDate)
        .from(indexData)
        .where(indexData.id.eq(id))
        .fetchOne();
  }

  private BigDecimal fetchClosingPrice(Long id) {
    return queryFactory
        .select(indexData.closingPrice)
        .from(indexData)
        .where(indexData.id.eq(id))
        .fetchOne();
  }
}

