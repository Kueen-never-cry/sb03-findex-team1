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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
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

  private static final int MA5DATA_NUM = 5;
  private static final int MA20DATA_NUM = 20;

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
  @Transactional(readOnly = true)
  public IndexChartDto getChart(Long indexInfoId, PeriodType periodType) {
    // 1. 지수 정보 로드
    IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
        .orElseThrow(() -> new NoSuchElementException("지수 정보를 찾을 수 없습니다."));

    // 2. 날짜 범위 계산
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = calculateStartDate(endDate, periodType);

    // 3. 데이터 조회 (baseDate 오름차순 정렬)
    List<IndexData> indexDataList = indexDataRepository.findAllByIndexInfo_IdAndBaseDateBetween(
        indexInfoId, startDate, endDate, Sort.by(Sort.Direction.ASC, "baseDate"));

    // 4. ChartDataPoint 변환
    List<ChartDataPoint> pricePoints = indexDataList.stream()
        .map(data -> new ChartDataPoint(
            data.getBaseDate(),
            data.getClosingPrice()
        ))
        .toList();

    // 5. 이동 평균선 계산
    List<ChartDataPoint> ma5 = calculateMovingAverageStrict(pricePoints, MA5DATA_NUM);
    List<ChartDataPoint> ma20 = calculateMovingAverageStrict(pricePoints, MA20DATA_NUM);

    // 6. DTO 응답 생성
    return IndexChartDto.from(indexInfo, periodType, pricePoints, ma5, ma20);
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
  @Transactional(readOnly = true)
  public List<IndexPerformanceDto> getFavoritePerformances(PeriodType period) {
    List<IndexInfo> favorites = indexInfoRepository.findAllByFavoriteTrue();

    return favorites.stream()
        .map(indexInfo -> {

          IndexData current = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(
              indexInfo.getId()).orElse(null);

          IndexData past = indexDataRepository.findByIndexInfoIdAndBaseDateOnlyDateMatch(
                  indexInfo.getId(),
                  calculateStartDate(period))
              .orElse(null);

          return IndexPerformanceDto.of(indexInfo, current, past);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
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

  private List<ChartDataPoint> calculateMovingAverageStrict(List<ChartDataPoint> prices,
      int window) {
    List<ChartDataPoint> sorted = prices.stream()
        .sorted(Comparator.comparing(ChartDataPoint::date))
        .toList();

    Deque<BigDecimal> windowValues = new ArrayDeque<>(window);
    BigDecimal sum = BigDecimal.ZERO;
    List<ChartDataPoint> result = new ArrayList<>();

    for (ChartDataPoint point : sorted) {
      BigDecimal value = point.value();
      windowValues.addLast(value);
      sum = sum.add(value);

      if (windowValues.size() > window) {
        sum = sum.subtract(windowValues.removeFirst());
      }

      BigDecimal avg = (windowValues.size() == window)
          ? sum.divide(BigDecimal.valueOf(window), 2, RoundingMode.HALF_UP)
          : null;

      result.add(new ChartDataPoint(point.date(), avg));
    }

    return result;
  }

  private LocalDate calculateStartDate(PeriodType type) {
    return calculateStartDate(LocalDate.now(), type);
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
