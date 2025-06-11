package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.entity.IndexData;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  IndexDataDto toDto(IndexData indexData);

  default CursorPageResponse<IndexDataDto> toCursorDto(
      List<IndexDataDto> content,
      String nextCursor,
      Long nextIdAfter,
      int size,
      Long totalElements,
      boolean hasNext
  ) {
    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext);
  }

  ;
}