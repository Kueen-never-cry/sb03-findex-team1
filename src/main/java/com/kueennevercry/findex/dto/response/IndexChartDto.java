package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.dto.ChartDataPoint;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.entity.IndexInfo;
import java.util.List;

public record IndexChartDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    PeriodType periodType,
    List<ChartDataPoint> dataPoints,
    List<ChartDataPoint> ma5DataPoints,
    List<ChartDataPoint> ma20DataPoints
) {

  public static IndexChartDto from(
      IndexInfo indexInfo,
      PeriodType periodType,
      List<ChartDataPoint> dataPoints,
      List<ChartDataPoint> ma5DataPoints,
      List<ChartDataPoint> ma20DataPoints
  ) {
    return new IndexChartDto(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        periodType,
        dataPoints,
        ma5DataPoints,
        ma20DataPoints
    );
  }
}
