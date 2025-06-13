package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.entity.AutoSyncConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @Mapping(target = "id", source = "config.id")
  @Mapping(target = "indexInfoId", source = "indexInfo.id")
  @Mapping(target = "indexName", source = "indexInfo.indexName")
  @Mapping(target = "indexClassification", source = "indexInfo.indexClassification")
  AutoSyncConfigDto toDto(
      AutoSyncConfig config,
      IndexInfoSummaryDto indexInfo
  );
}
