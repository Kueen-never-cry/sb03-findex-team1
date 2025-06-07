package com.kueennevercry.findex.dto;

import com.kueennevercry.findex.entity.IndexInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoDto {

  private Long id;
  private String indexClassification;
  private String indexName;
  private Integer employedItemsCount;
  private LocalDate basePointInTime;
  private Float baseIndex;
  private IndexInfo.SourceType sourceType;
  private Boolean favorite;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static IndexInfoDto from(IndexInfo indexInfo) {
    return IndexInfoDto.builder()
        .id(indexInfo.getId())
        .indexClassification(indexInfo.getIndexClassification())
        .indexName(indexInfo.getIndexName())
        .employedItemsCount(indexInfo.getEmployedItemsCount())
        .basePointInTime(indexInfo.getBasePointInTime())
        .baseIndex(indexInfo.getBaseIndex())
        .sourceType(indexInfo.getSourceType())
        .favorite(indexInfo.getFavorite())
        .createdAt(indexInfo.getCreatedAt())
        .updatedAt(indexInfo.getUpdatedAt())
        .build();
  }
}