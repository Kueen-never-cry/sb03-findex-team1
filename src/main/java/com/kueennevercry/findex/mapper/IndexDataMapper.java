package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.entity.IndexData;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface IndexDataMapper {

    IndexDataDto toDto(IndexData indexData);
    IndexData toEntity(IndexDataDto indexDataDto);
}