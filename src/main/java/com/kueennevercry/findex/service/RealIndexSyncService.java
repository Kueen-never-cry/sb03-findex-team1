package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.request.IndexDataSyncRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class RealIndexSyncService implements IndexSyncService {

  private final IndexDataService indexDataService;

  @Override
  public void sync(Long indexInfoId, LocalDate from, LocalDate to) {
    IndexDataSyncRequest request = new IndexDataSyncRequest(
        List.of(indexInfoId),
        from,
        to
    );

    indexDataService.syncIndexData(request);
  }
}
