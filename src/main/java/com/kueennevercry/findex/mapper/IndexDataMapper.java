package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.IndexInfoApiResponse;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  IndexDataDto toDto(IndexData indexData);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  IndexInfo toEntity(IndexInfoCreateRequest request);

  void updateEntity(IndexInfoApiResponse response, @MappingTarget IndexData target);

  @Mapping(source = "response.baseDate", target = "baseDate")
  @Mapping(source = "response.marketPrice", target = "marketPrice")
  @Mapping(source = "response.closingPrice", target = "closingPrice")
  @Mapping(source = "response.highPrice", target = "highPrice")
  @Mapping(source = "response.lowPrice", target = "lowPrice")
  @Mapping(source = "response.versus", target = "versus")
  @Mapping(source = "response.fluctuationRate", target = "fluctuationRate")
  @Mapping(source = "response.tradingQuantity", target = "tradingQuantity")
  @Mapping(source = "response.tradingPrice", target = "tradingPrice")
  @Mapping(source = "response.marketTotalAmount", target = "marketTotalAmount")
  @Mapping(source = "indexInfo", target = "indexInfo")
  IndexData toEntity(IndexInfoApiResponse response, IndexInfo indexInfo);

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
}