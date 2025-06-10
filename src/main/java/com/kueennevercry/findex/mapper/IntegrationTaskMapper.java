package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.entity.IntegrationTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IntegrationTaskMapper {

  @Mapping(target = "indexInfoId", source = "integrationTask.indexInfo.id")
  SyncJobDto toSyncJobDto(IntegrationTask integrationTask);


}
