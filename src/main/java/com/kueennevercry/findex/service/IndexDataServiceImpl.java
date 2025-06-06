package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.ChartPoint;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.dto.response.IndexDataResponse;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexDataServiceImpl implements IndexDataService {

  private final OpenApiClient openApiClient;
  private String STOCK_INDEX_ENDPOINT = "/getStockMarketIndex";

  @Override
  public IndexChartResponse getChart(Long indexInfoId, PeriodType periodType)
      throws IOException, URISyntaxException {
    // 임의의 값으로 테스트
    String indexCode = "KRX 300 소재";
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusMonths(6);

    List<IndexDataResponse> rawData = openApiClient.fetchIndexData(indexCode, STOCK_INDEX_ENDPOINT,
        startDate,
        endDate);

    List<ChartPoint> dataPoints = rawData.stream()
        .map(data -> new ChartPoint(data.baseDate(), data.closingPrice()))
        .sorted(Comparator.comparing(ChartPoint::date).reversed())
        .toList();

    List<ChartPoint> ma5 = calculateMovingAverage(dataPoints, 5);
    List<ChartPoint> ma20 = calculateMovingAverage(dataPoints, 20);

    return new IndexChartResponse(
        indexInfoId,
        "KRX 시리즈",
        "KRX 300 소재",
        periodType,
        dataPoints,
        ma5,
        ma20
    );
  }

  private List<ChartPoint> calculateMovingAverage(List<ChartPoint> points, int windowSize) {
    List<ChartPoint> result = new ArrayList<>();
    for (int i = 0; i <= points.size() - windowSize; i++) {
      BigDecimal sum = BigDecimal.ZERO;
      for (int j = 0; j < windowSize; j++) {
        sum = sum.add(points.get(i + j).value());
      }
      BigDecimal average = sum.divide(BigDecimal.valueOf(windowSize), RoundingMode.HALF_UP);
      result.add(new ChartPoint(points.get(i).date(), average));
    }
    return result;
  }
}
