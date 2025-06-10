package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.entity.IndexInfo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

  IndexInfoDto toDto(IndexInfo indexInfo);

  IndexInfo toEntity(IndexInfoDto indexInfoDto);

}