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
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.kueennevercry.findex.repository.IndexDataCustomRepository;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.repository.IndexInfoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataServiceImpl implements IndexDataService {

  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;
  private final IndexDataCustomRepository indexDataCustomRepository;
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

    CursorPageResponse<IndexDataDto> dto = indexDataCustomRepository.findCursorPage(
        indexInfoId, from, to, idAfter, cursor,
        sortField, sortDirection, size);

    return dto;
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
  public IndexChartDto getChart(Long indexInfoId, PeriodType periodType) {
    IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
        .orElseThrow(() -> new IllegalStateException("지수 정보가 없습니다."));

    LocalDate endDate = indexDataRepository.findMaxBaseDateByIndexInfoId(indexInfoId)
        .orElseThrow(() -> new IllegalStateException("지수 데이터가 없습니다."));
    LocalDate startDate = calculateStartDate(endDate, periodType);

    List<IndexData> indexDataList = indexDataRepository
        .findAllByIndexInfo_IdAndBaseDateBetweenOrderByBaseDateAsc(indexInfoId, startDate, endDate);

    List<ChartDataPoint> points = indexDataList.stream()
        .map(d -> new ChartDataPoint(d.getBaseDate(), d.getClosingPrice()))
        .toList();

    return new IndexChartDto(
        indexInfoId,
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        periodType,
        points,
        calculateMovingAverage(points, 5),
        calculateMovingAverage(points, 20)
    );
  }

  @Override
  public List<RankedIndexPerformanceDto> getPerformanceRanking(
      Long indexInfoId,
      PeriodType periodType,
      int limit
  ) {
    LocalDate baseDate = indexDataRepository.findMaxBaseDate()
        .orElseThrow(() -> new IllegalStateException("지수 데이터가 없습니다."));
    LocalDate beforeBaseDate = calculateStartDate(baseDate, periodType);

    List<RankedIndexPerformanceDto> raw = indexDataRepository.findRankedPerformances(
        baseDate, beforeBaseDate, indexInfoId
    );

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

    LocalDate baseDate = indexDataRepository.findMaxBaseDate()
        .orElseThrow(() -> new IllegalStateException("지수 데이터가 없습니다."));

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

  @Override
  public List<String[]> getExportableIndexData(Long indexInfoId, LocalDate startDate,
      LocalDate endDate, Sort sort) {
    LocalDate now = LocalDate.now();

    if (endDate == null) {
      endDate = now;
    }

    if (startDate == null) {
      startDate = indexDataRepository.findMinBaseDateByIndexIfoId(indexInfoId)
          .orElseThrow(() -> new IllegalStateException("지수 데이터가 없습니다."));
    }

    List<IndexData> dataList = indexDataRepository.findAllByIndexInfo_IdAndBaseDateBetween(
        indexInfoId, startDate, endDate, sort);

    if (dataList.size() > 20_000) {
      log.warn("지수 데이터 CSV Export 경고: {}건 대량의 데이터가 요청되었습니다.", dataList.size());
    }

    List<String[]> rows = new ArrayList<>();

    rows.add(new String[]{
        "기준일자", "시가", "종가", "고가", "저가",
        "전일대비등락", "등락률", "거래량", "거래대금", "시가총액"
    });

    for (IndexData data : dataList) {
      rows.add(new String[]{
          data.getBaseDate().toString(),
          String.valueOf(data.getMarketPrice()),
          String.valueOf(data.getClosingPrice()),
          String.valueOf(data.getHighPrice()),
          String.valueOf(data.getLowPrice()),
          String.valueOf(data.getVersus()),
          String.valueOf(data.getFluctuationRate()),
          String.valueOf(data.getTradingQuantity()),
          String.valueOf(data.getTradingPrice()),
          String.valueOf(data.getMarketTotalAmount())
      });
    }

    return rows;
  }

  private List<ChartDataPoint> calculateMovingAverage(List<ChartDataPoint> points, int windowSize) {
    List<ChartDataPoint> result = new ArrayList<>();

    for (int i = 0; i <= points.size() - windowSize; i++) {
      BigDecimal sum = BigDecimal.ZERO;

      for (int j = 0; j < windowSize; j++) {
        sum = sum.add(points.get(i + j).value());
      }

      BigDecimal average = sum.divide(BigDecimal.valueOf(windowSize), 4, RoundingMode.HALF_UP);
      result.add(new ChartDataPoint(points.get(i).date(), average));
    }

    return result;
  }

  private LocalDate calculateStartDate(LocalDate baseDate, PeriodType type) {
    return switch (type) {
      case DAILY -> baseDate.minusDays(1);
      case WEEKLY -> baseDate.minusWeeks(1);
      case MONTHLY -> baseDate.minusMonths(1);
      case QUARTERLY -> baseDate.minusMonths(3);
      case YEARLY -> baseDate.minusYears(1);
    };
  }
}
