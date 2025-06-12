package com.kueennevercry.findex.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "지수 성과 응답 DTO")
public record IndexPerformanceDto(

    @Schema(description = "지수 정보 ID", example = "1")
    Long indexInfoId,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "전일 대비 등락", example = "50.5")
    BigDecimal versus,

    @Schema(description = "등락률 (%)", example = "1.8")
    BigDecimal fluctuationRate,

    @Schema(description = "현재 지수", example = "2850.75")
    BigDecimal currentPrice,

    @Schema(description = "전일 지수", example = "2800.25")
    BigDecimal beforePrice
) {

  public static IndexPerformanceDto of(IndexInfo info, IndexData current, IndexData before) {
    if (info == null || current == null || before == null) {
      return null;
    }

    BigDecimal currentPrice = current.getClosingPrice();
    BigDecimal beforePrice = before.getClosingPrice();

    if (beforePrice.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    BigDecimal versus = currentPrice.subtract(beforePrice);
    BigDecimal fluctuationRate = versus.divide(beforePrice, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));

    return new IndexPerformanceDto(
        info.getId(),
        info.getIndexClassification(),
        info.getIndexName(),
        versus,
        fluctuationRate,
        currentPrice,
        beforePrice
    );
  }
}

