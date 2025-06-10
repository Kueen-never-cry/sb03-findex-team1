package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.ChartDataPoint;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartDto;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.SourceType;
import com.kueennevercry.findex.infra.openapi.OpenApiClient;
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
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
  public IndexChartDto getChart(Long indexInfoId, PeriodType periodType)
      throws IOException, URISyntaxException {
    // FIXME : develop 브랜치 안정화 후 재구님이 추가 예정
    return null;
  }

  @Override
  public List<RankedIndexPerformanceDto> getPerformanceRanking(Long indexInfoId, String periodType,
      int limit) {
    LocalDate baseDate = indexDataRepository.findMaxBaseDate()
        .orElseThrow(() -> new IllegalStateException("지수 데이터가 없습니다."));
    LocalDate beforeBaseDate = calculateStartDate(baseDate,
        PeriodType.valueOf(periodType.toUpperCase()));

    List<RankedIndexPerformanceDto> raw = indexDataRepository.findRankedPerformances(
        baseDate, beforeBaseDate, indexInfoId
    );

    // 정렬 + 랭킹 부여
    List<RankedIndexPerformanceDto> sorted = raw.stream()
        .sorted(Comparator.comparing(
            (RankedIndexPerformanceDto dto) -> dto.performance().fluctuationRate()).reversed())
        .limit(limit)
        .toList();

    return IntStream.range(0, sorted.size())
        .mapToObj(i -> new RankedIndexPerformanceDto(i + 1, sorted.get(i).performance()))
        .toList();
  }

  private List<ChartDataPoint> calculateMovingAverage(List<ChartDataPoint> points, int windowSize) {
    List<ChartDataPoint> result = new ArrayList<>();
    for (int i = 0; i <= points.size() - windowSize; i++) {
      double sum = 0.0;
      for (int j = 0; j < windowSize; j++) {
        sum += points.get(i + j).value();
      }
      double average = sum / windowSize;
      result.add(new ChartDataPoint(points.get(i).date(), average));
    }
    return result;
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
