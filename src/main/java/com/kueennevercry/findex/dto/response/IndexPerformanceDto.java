package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record IndexPerformanceDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    BigDecimal currentPrice,
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

