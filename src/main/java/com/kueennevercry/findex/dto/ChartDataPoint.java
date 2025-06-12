package com.kueennevercry.findex.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataPoint(
    LocalDate date,
    BigDecimal value
) {

}
