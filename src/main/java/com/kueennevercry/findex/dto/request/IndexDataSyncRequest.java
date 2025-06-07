package com.kueennevercry.findex.dto.request;

import java.time.LocalDate;

public record IndexDataSyncRequest(
    Long indexInfoId,
    LocalDate startDate,
    LocalDate endDate
) {

}
