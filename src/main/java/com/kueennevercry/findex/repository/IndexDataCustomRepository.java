package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import java.time.LocalDate;

public interface IndexDataCustomRepository {

  CursorPageResponse<IndexDataDto> findCursorPage(Long indexInfoId,
      LocalDate from, LocalDate to,
      Long idAfter, String cursor,
      String sortField, String sortDirection, int size);
}
