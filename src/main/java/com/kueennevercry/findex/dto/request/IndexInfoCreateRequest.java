package com.kueennevercry.findex.dto.request;

import com.kueennevercry.findex.common.SourceType;
import com.kueennevercry.findex.entity.IndexInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoCreateRequest {

  private String indexClassification;
  private String indexName;
  private Integer employedItemsCount;
  private LocalDate basePointInTime;
  private Float baseIndex;
  private SourceType sourceType;

  @Builder.Default
  private Boolean favorite = false; // 기본값 false

  /**
   * 요청 DTO를 엔티티로 변환
   */
  public IndexInfo toEntity() {
    return IndexInfo.builder()
        .indexClassification(this.indexClassification)
        .indexName(this.indexName)
        .employedItemsCount(this.employedItemsCount)
        .basePointInTime(this.basePointInTime)
        .baseIndex(this.baseIndex)
        .sourceType(this.sourceType)
        .favorite(this.favorite)
        .build();
  }
}