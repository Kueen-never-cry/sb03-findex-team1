package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.IndexInfoDto;
import com.kueennevercry.findex.service.IndexInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  /**
   * 특정 ID의 지수 정보 조회
   * GET /api/index-infos/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> getIndexInfo(@PathVariable Long id) {
    IndexInfoDto indexInfo = indexInfoService.findById(id);
    return ResponseEntity.ok(indexInfo);
  }
}
