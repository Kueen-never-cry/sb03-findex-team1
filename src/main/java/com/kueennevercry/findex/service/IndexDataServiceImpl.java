package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.ChartDataPoint;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexChartDto;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
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
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  public CursorPageResponse<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from,
      LocalDate to,
      Long idAfter, String cursor,
      String sortField, String sortDirection, int size) {

    Sort.Direction direction;

    if ("asc".equalsIgnoreCase(sortDirection)) {
      direction = Sort.Direction.ASC;
    } else {
      direction = Sort.Direction.DESC;
    }

    Sort sort = Sort.by(direction, sortField);

    Pageable pageable = PageRequest.of(0, size, sort);

    List<IndexDataDto> dto = indexDataRepository.findAllByIndexInfo_IdAndBaseDateBetween(
            indexInfoId, from, to,
            pageable)
        .stream()
        .map(indexDataMapper::toDto)
        .toList();

    CursorPageResponse<IndexDataDto> cursorDto = indexDataMapper.toCursorDto(
        dto,
        cursor,
        idAfter,
        size,
        (long) dto.size(),
        dto.size() > size
    );

    return cursorDto;
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
      Optional<IndexData> currentData = indexDataRepository
          .findByIndexInfoIdAndBaseDate(index.getId(), baseDate);
      Optional<IndexData> previousData = indexDataRepository
          .findClosestBeforeOrEqual(index.getId(), compareDate);

      if (currentData.isEmpty() || previousData.isEmpty()) {
        continue;
      }

      IndexData current = currentData.get();
      IndexData previous = previousData.get();

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
