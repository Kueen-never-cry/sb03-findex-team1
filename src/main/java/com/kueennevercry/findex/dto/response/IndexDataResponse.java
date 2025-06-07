package com.kueennevercry.findex.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataResponse(
    @JsonFormat(pattern = "yyyyMMdd")
    @JsonProperty("basDt")
    LocalDate baseDate,
    
    @JsonProperty("clpr")
    BigDecimal closingPrice
) {

}
