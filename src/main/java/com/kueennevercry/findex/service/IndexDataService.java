package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.request.IndexDataCreateDto;
import com.kueennevercry.findex.dto.request.IndexDataUpdateDto;
import com.kueennevercry.findex.entity.IndexData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

public interface IndexDataService {

  // 지수 데이터
  IndexData create(IndexDataCreateDto request);
  List<IndexDataDto> findAllByIndexInfoId(Long indexInfoId);
  List<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from, LocalDate to, String sortBy, String sortDirection);
  IndexData update(Long id, IndexDataUpdateDto request);
  void delete(Long id);
}
