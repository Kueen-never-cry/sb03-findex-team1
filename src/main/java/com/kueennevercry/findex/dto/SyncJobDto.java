package com.kueennevercry.findex.dto;

import com.kueennevercry.findex.entity.IntegrationTask;
import com.kueennevercry.findex.entity.IntegrationResultType;
import java.time.Instant;
import lombok.Getter;

/*연동 작업 DTO */
@Getter
public class SyncJobDto {
private int id;
private IntegrationTask jobType;
private int indexInFold;
private Instant targetDate;
private String worker;
private Instant jobTime;
private IntegrationResultType result;
}
