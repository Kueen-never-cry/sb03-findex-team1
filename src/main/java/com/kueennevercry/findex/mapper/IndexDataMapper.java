package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.entity.IndexData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface IndexDataMapper {

    @Mapping(source = "indexInfo.id", target = "indexInfoId")
    IndexDataDto toDto(IndexData indexData);
}