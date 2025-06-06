package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.dto.ChartPoint;
import com.kueennevercry.findex.dto.PeriodType;
import java.util.List;

public record IndexChartResponse(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    PeriodType periodType,
    List<ChartPoint> dataPoints,
    List<ChartPoint> ma5DataPoints,
    List<ChartPoint> ma20DataPoints
) {

}
