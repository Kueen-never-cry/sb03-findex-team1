package com.kueennevercry.findex.scheduler;

import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.repository.AutoSyncConfigRepository;
import com.kueennevercry.findex.repository.IndexDataRepository;
import com.kueennevercry.findex.service.IndexSyncService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexAutoSyncScheduler {

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexSyncService indexSyncService;
  private final IndexDataRepository indexDataRepository;

  @Scheduled(cron = "${batch.index-sync.cron:0 0 0 * * *}")
  public void syncAllEnabledIndices() {
    log.info("[배치 시작] 자동 연동 설정된 지수 목록 연동");

    List<AutoSyncConfig> configs = autoSyncConfigRepository.findAllByEnabledTrue();
    LocalDate today = LocalDate.now();

    for (AutoSyncConfig config : configs) {
      Long indexInfoId = config.getIndexInfo().getId();

      LocalDate fromDate = indexDataRepository.findMaxBaseDateByIndexInfoId(indexInfoId)
          .orElse(today.minusDays(7));

      LocalDate toDate = today;

      log.info("연동 대상: ID:{}, 지수명:{}, from:{}, to:{}",
          indexInfoId, config.getIndexInfo().getIndexName(), fromDate, toDate);
      indexSyncService.sync(indexInfoId, fromDate, toDate);
    }
    log.info("[배치 종료] 자동 연동 완료");
  }
}