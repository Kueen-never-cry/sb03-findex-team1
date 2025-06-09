package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.ChartPoint;
import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.dto.response.IndexDataResponse;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.SourceType;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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

  //------------ 대시보드 -----------//
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

  @Override
  public List<IndexPerformanceDto> getFavoritePerformances(PeriodType periodType) {
    List<IndexInfo> favorites = indexInfoRepository.findAllByFavoriteTrue();

    LocalDate baseDate = indexDataRepository.findLatestBaseDateAcrossAll();
    if (baseDate == null) {
      return List.of();
    }

    LocalDate compareDate = calculateStartDate(baseDate, periodType);

    List<IndexPerformanceDto> results = new ArrayList<>();

    for (IndexInfo index : favorites) {
      Optional<IndexData> currentOpt = indexDataRepository
          .findByIndexInfoIdAndBaseDate(index.getId(), baseDate);
      Optional<IndexData> previousOpt = indexDataRepository
          .findClosestBeforeOrEqual(index.getId(), compareDate);

      if (currentOpt.isEmpty() || previousOpt.isEmpty()) {
        continue;
      }

      IndexData current = currentOpt.get();
      IndexData previous = previousOpt.get();

      BigDecimal currentPrice = current.getClosingPrice();
      BigDecimal beforePrice = previous.getClosingPrice();
      BigDecimal versus = currentPrice.subtract(beforePrice);
      BigDecimal fluctuationRate = beforePrice.compareTo(BigDecimal.ZERO) == 0
          ? BigDecimal.ZERO
          : versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

      results.add(new IndexPerformanceDto(
          index.getId(),
          index.getIndexClassification(),
          index.getIndexName(),
          versus,
          fluctuationRate,
          currentPrice,
          beforePrice
      ));
    }

    return results;
  }

  private LocalDate calculateStartDate(LocalDate baseDate, PeriodType type) {
    return switch (type) {
      case DAILY -> baseDate.minusDays(1);
      case WEEKLY -> baseDate.minusWeeks(1);
      case MONTHLY -> baseDate.minusMonths(1);
      default -> baseDate;
    };
  }
}
