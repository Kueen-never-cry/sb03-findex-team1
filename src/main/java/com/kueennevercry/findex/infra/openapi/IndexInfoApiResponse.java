package com.kueennevercry.findex.infra.openapi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/* <response sample>
*  {
      "basDt": "20250609",
      "idxNm": "IT 서비스",
      "idxCsf": "KOSPI시리즈",
      "epyItmsCnt": "26",
      "clpr": "1135.98",
      "vs": "53.19",
      "fltRt": "4.91",
      "mkp": "1097.63",
      "hipr": "1137.77",
      "lopr": "1096.96",
      "trqu": "22556897",
      "trPrc": "1133714965954",
      "lstgMrktTotAmt": "111236334273686",
      "lsYrEdVsFltRg": "108",
      "lsYrEdVsFltRt": "10.5",
      "yrWRcrdHgst": "1158.04",
      "yrWRcrdHgstDt": "20250206",
      "yrWRcrdLwst": "0",
      "yrWRcrdLwstDt": "20250610",
      "basPntm": "20240701",
      "basIdx": "1000"
    }
* */
public record IndexInfoApiResponse
    (
        // 지수 정보
        @JsonProperty("idxCsf")
        String indexClassification,  // 지수 분류

        @JsonProperty("idxNm")
        String indexName,  // 지수명

        @JsonProperty("epyItmsCnt")
        int employedItemsCount,  // 채용 종목 수

        @JsonProperty("basPntm")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate basePointInTime,  // 기준 시점

        @JsonProperty("basIdx")
        BigDecimal baseIndex,  // 기준 지수

        //지수 데이터 정보
        @JsonProperty("basDt")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate baseDate,  // 기준일자

        @JsonProperty("clpr")
        BigDecimal closingPrice,  // 종가

        @JsonProperty("vs")
        BigDecimal versus,  // 전일 대비 등락

        @JsonProperty("fltRt")
        BigDecimal fluctuationRate,  // 전일 대비 등락률 (%)

        @JsonProperty("mkp")
        BigDecimal marketPrice,  // 시가

        @JsonProperty("hipr")
        BigDecimal highPrice,  // 고가

        @JsonProperty("lopr")
        BigDecimal lowPrice,  // 저가

        @JsonProperty("trqu")
        Long tradingQuantity,  // 거래량

        @JsonProperty("trPrc")
        Long tradingPrice,  // 거래대금

        @JsonProperty("lstgMrktTotAmt")
        Long marketTotalAmount,  // 상장 시가총액

        // 아래 데이터는 현재 프로젝트에서 사용 X
        @JsonProperty("lsYrEdVsFltRg")
        double changeAmountFromLastYearEnd,  // 전년도 말 대비 등락 금액

        @JsonProperty("lsYrEdVsFltRt")
        double changeRateFromLastYearEnd,  // 전년도 말 대비 등락률 (%)

        @JsonProperty("yrWRcrdHgst")
        double yearlyHigh,  // 52주 최고가

        @JsonProperty("yrWRcrdHgstDt")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate yearlyHighDate,  // 52주 최고가 달성일

        @JsonProperty("yrWRcrdLwst")
        double yearlyLow,  // 52주 최저가

        @JsonProperty("yrWRcrdLwstDt")
        @JsonFormat(pattern = "yyyyMMdd")
        LocalDate yearlyLowDate  // 52주 최저가 달성일
    ) {

}
