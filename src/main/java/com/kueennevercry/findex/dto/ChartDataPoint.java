package com.kueennevercry.findex.dto;

import java.time.LocalDate;

public record ChartDataPoint(
    LocalDate date,
    Double value
) {

}
