package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.entity.QAutoSyncConfig;
import com.kueennevercry.findex.entity.QIndexInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AutoSyncConfigCustomRepositoryImpl implements AutoSyncConfigCustomRepository {

  private final JPAQueryFactory queryFactory;

  public AutoSyncConfigCustomRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public CursorPageResponse<AutoSyncConfigDto> findAllByParameters(
      Long indexInfoId,
      Boolean enabled,
      Long idAfter,
      String sortField,
      String sortDirection,
      int size
  ) {
    QAutoSyncConfig config = QAutoSyncConfig.autoSyncConfig;
    QIndexInfo index = QIndexInfo.indexInfo;

    var query = queryFactory
        .select(Projections.constructor(AutoSyncConfigDto.class,
            config.id,
            index.id,
            index.indexName,
            index.indexClassification,
            config.enabled
        ))
        .from(config)
        .join(config.indexInfo, index)
        .where(
            indexInfoIdEq(indexInfoId, index),
            enabledEq(enabled, config),
            idAfterGt(idAfter, index)
        );

    if ("enabled".equals(sortField)) {
      query.orderBy("desc".equals(sortDirection) ? config.enabled.desc() : config.enabled.asc());
    } else if ("id".equals(sortField)) {
      query.orderBy("desc".equals(sortDirection) ? index.id.desc() : index.id.asc());
    } else {
      query.orderBy("desc".equals(sortDirection) ? index.indexName.desc() : index.indexName.asc());
    }

    List<AutoSyncConfigDto> results = query
        .limit(size + 1)
        .fetch();

    boolean hasNext = results.size() > size;
    if (hasNext) {
      results.remove(size);
    }

    Long nextIdAfter = results.isEmpty() ? null : results.get(results.size() - 1).indexInfoId();

    Long countResult = queryFactory
        .select(config.count())
        .from(config)
        .join(config.indexInfo, index)
        .where(indexInfoIdEq(indexInfoId, index))
        .fetchOne();

    long totalElements = countResult != null ? countResult : 0L;

    return new CursorPageResponse<>(results, null, nextIdAfter, size, totalElements, hasNext);
  }

  private BooleanExpression indexInfoIdEq(Long indexInfoId, QIndexInfo index) {
    return indexInfoId != null ? index.id.eq(indexInfoId) : null;
  }

  private BooleanExpression enabledEq(Boolean enabled, QAutoSyncConfig config) {
    return enabled != null ? config.enabled.eq(enabled) : null;
  }

  private BooleanExpression idAfterGt(Long idAfter, QIndexInfo index) {
    return idAfter != null ? index.id.gt(idAfter) : null;
  }
}
