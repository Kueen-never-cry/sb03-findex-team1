package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.config.AutoSyncConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @Mapping(target = "indexClassification", source = "indexClassification")
  @Mapping(target = "indexName", source = "indexName")
  AutoSyncConfig toDto(
      AutoSyncConfig config,
      String indexClassification,
      String indexName
  );
}
