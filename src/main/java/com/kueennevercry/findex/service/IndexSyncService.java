package com.kueennevercry.findex.service;

import java.time.LocalDate;

public interface IndexSyncService {

  void sync(Long indexInfoId, LocalDate from, LocalDate to);
}
