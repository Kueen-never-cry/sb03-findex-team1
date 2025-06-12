package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "지수 데이터 응답 DTO")
public record IndexDataDto(

    @Schema(description = "지수 데이터 ID", example = "1")
    Long id,

    @Schema(description = "지수 정보 ID", example = "1")
    Long indexInfoId,

    @Schema(description = "기준일자", example = "2023-01-01")
    LocalDate baseDate,

    @Schema(description = "지수 생성 방식", example = "OPEN_API")
    SourceType sourceType,

    @Schema(description = "시가", example = "2800.25")
    BigDecimal marketPrice,

    @Schema(description = "종가", example = "2850.75")
    BigDecimal closingPrice,

    @Schema(description = "고가", example = "2870.50")
    BigDecimal highPrice,

    @Schema(description = "저가", example = "2795.30")
    BigDecimal lowPrice,

    @Schema(description = "전일 대비 등락", example = "50.50")
    BigDecimal versus,

    @Schema(description = "등락률", example = "1.8")
    BigDecimal fluctuationRate,

    @Schema(description = "거래량", example = "1250000")
    Long tradingQuantity,

    @Schema(description = "거래대금", example = "3500000000")
    Long tradingPrice,

    @Schema(description = "시가총액", example = "450000000000")
    Long marketTotalAmount) {

}
