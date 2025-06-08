package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.request.IndexDataCreateDto;
import com.kueennevercry.findex.dto.request.IndexDataUpdateDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.entity.SourceType;
import com.kueennevercry.findex.mapper.IndexDataMapper;
import com.kueennevercry.findex.repository.IndexDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataServiceImpl implements IndexDataService {

    private final IndexDataRepository indexDataRepository;
    private final IndexDataMapper indexDataMapper;

    //------------지수 데이터-----------//
    @Override
    public IndexData create(
            IndexDataCreateDto request
    ) {
        IndexInfo indexInfo = request.indexInfo();
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
    public List<IndexDataDto> findAllByIndexInfoId(Long indexInfoId) {

        if (indexInfoId == null) {
            indexInfoId = 3L;
        }

        return indexDataRepository.findAllByIndexInfo_Id(indexInfoId).stream()
                .map(indexDataMapper::toDto)
                .toList();
    }

    @Override
    public List<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from, LocalDate to, String sortBy, String sortDirection) {

        if (from == null) {
            from = LocalDate.of(1900, 1, 1);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        Sort.Direction direction;

        if ("asc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);

        return indexDataRepository.findAllByIndexInfo_IdAndBaseDateBetween(indexInfoId, from, to, sort).stream()
                .map(indexDataMapper::toDto)
                .toList();
    }

    @Override
    public IndexData update(Long id, IndexDataUpdateDto request) {
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

}
