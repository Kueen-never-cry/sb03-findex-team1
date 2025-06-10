package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.ChartPoint;
import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.dto.response.IndexDataResponse;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.SourceType;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.kueennevercry.findex.repository.IndexDataRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import com.kueennevercry.findex.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataServiceImpl implements IndexDataService {

  private final OpenApiClient openApiClient;
  private String STOCK_INDEX_ENDPOINT = "/getStockMarketIndex";

  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;
  private final IndexDataMapper indexDataMapper;

  //------------지수 데이터-----------//
  @Override
  public IndexData create(
      IndexDataCreateRequest request
  ) {
    IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("Index Info not found"));
    LocalDate baseDate = request.baseDate();
    if (indexDataRepository.existsByIndexInfoId(indexInfo.getId())
        && indexDataRepository.existsByBaseDate(baseDate)) {
      throw new IllegalStateException("ERR_BAD_REQUEST");
    }

    IndexData indexData =
        IndexData.builder()
            .indexInfo(indexInfo)
            .baseDate(baseDate)
            .sourceType(SourceType.USER)
            .marketPrice(request.marketPrice())
            .closingPrice(request.closingPrice())
            .highPrice(request.highPrice())
            .lowPrice(request.lowPrice())
            .versus(request.versus())
            .fluctuationRate(request.fluctuationRate())
            .tradingPrice(request.tradingPrice())
            .tradingQuantity(request.tradingQuantity())
            .marketTotalAmount(request.marketTotalAmount())
            .build();

    return indexDataRepository.save(indexData);

  }

  @Override
  public List<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from, LocalDate to,
      String sortBy, String sortDirection) {

    Sort.Direction direction;

    if ("asc".equalsIgnoreCase(sortDirection)) {
      direction = Sort.Direction.ASC;
    } else {
      direction = Sort.Direction.DESC;
    }

    Sort sort = Sort.by(direction, sortBy);

    return indexDataRepository.findAllByIndexInfo_IdAndBaseDateBetween(indexInfoId, from, to, sort)
        .stream()
        .map(indexDataMapper::toDto)
        .toList();
  }

  @Override
  public IndexData update(Long id, IndexDataUpdateRequest request) {
    IndexData indexData = indexDataRepository.findById(id).orElseThrow(NoSuchElementException::new);

    indexData.update(
        request.marketPrice(),
        request.closingPrice(),
        request.highPrice(),
        request.lowPrice(),
        request.versus(),
        request.fluctuationRate(),
        request.tradingQuantity()
    );

    return indexData;
  }

  @Override
  public void delete(Long id) {
    indexDataRepository.deleteById(id);
  }

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
