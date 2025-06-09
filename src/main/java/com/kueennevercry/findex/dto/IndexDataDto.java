package com.kueennevercry.findex.dto;

import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.entity.SourceType;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record IndexDataDto(
    Long id,
    IndexInfoDto indexInfoDto,
    LocalDate baseDate,
    SourceType sourceType,
    Double marketPrice,
    Double closingPrice,
    Double highPrice,
    Double lowPrice,
    Double versus,
    Double fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount,
    Instant createdAt,
    Instant updatedAt

) {

}
