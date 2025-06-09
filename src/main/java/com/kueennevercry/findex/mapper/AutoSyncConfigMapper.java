package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.config.AutoSyncConfig;
import com.kueennevercry.findex.dto.AutoSyncConfigDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @Mapping(target = "indexInfoId", expression = "java(config.getIndexInfo().getId())")
  @Mapping(target = "indexClassification", source = "indexClassification")
  @Mapping(target = "indexName", source = "indexName")
  AutoSyncConfigDto toDto(
      AutoSyncConfig config,
      String indexClassification,
      String indexName
  );
}
