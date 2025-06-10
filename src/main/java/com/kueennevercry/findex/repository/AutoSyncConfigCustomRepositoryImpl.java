package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.entity.QAutoSyncConfig;
import com.kueennevercry.findex.entity.QIndexInfo;
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
        .selectFrom(config)
        .leftJoin(config.indexInfo, index).fetchJoin()
        .where(
            indexInfoIdEq(indexInfoId, index),
            enabledEq(enabled, config),
            idAfterGt(idAfter, config)
        );

    if ("enabled".equals(sortField)) {
      query.orderBy("desc".equals(sortDirection) ? config.enabled.desc() : config.enabled.asc());
    } else {
      query.orderBy("desc".equals(sortDirection) ? index.indexName.desc() : index.indexName.asc());
    }

    List<AutoSyncConfig> results = query
        .limit(size + 1)
        .fetch();

    boolean hasNext = results.size() > size;
    if (hasNext) {
      results.remove(size);
    }

    List<AutoSyncConfigDto> dtoList = results.stream()
        .map(cfg -> new AutoSyncConfigDto(
            cfg.getId(),
            cfg.getIndexInfo().getId(),
            cfg.getIndexInfo().getIndexName(),
            cfg.getIndexInfo().getIndexClassification(),
            cfg.isEnabled()
        ))
        .toList();

    Long nextIdAfter = dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).id();

    return new CursorPageResponse<>(dtoList, null, nextIdAfter, size, dtoList.size(), hasNext);
  }

  private BooleanExpression indexInfoIdEq(Long indexInfoId, QIndexInfo index) {
    return indexInfoId != null ? index.id.eq(indexInfoId) : null;
  }

  private BooleanExpression enabledEq(Boolean enabled, QAutoSyncConfig config) {
    return enabled != null ? config.enabled.eq(enabled) : null;
  }

  private BooleanExpression idAfterGt(Long idAfter, QAutoSyncConfig config) {
    return idAfter != null ? config.id.gt(idAfter) : null;
  }
}
