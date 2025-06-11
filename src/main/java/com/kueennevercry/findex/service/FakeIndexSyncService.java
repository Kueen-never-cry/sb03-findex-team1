package com.kueennevercry.findex.service;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FakeIndexSyncService implements IndexSyncService {

  @Override
  public void sync(Long indexInfoId, LocalDate from, LocalDate to) {
    log.info("[FAKE]지수 연동 실행: indexInfoId:{} 기간: {} ~ {}", indexInfoId, from, to);
  }
}
