package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.request.IndexInfoUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.service.IndexInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  /**
   * 특정 ID의 지수 정보 조회 GET /api/index-infos/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> getIndexInfo(@PathVariable Long id) {
    IndexInfoDto indexInfo = indexInfoService.findById(id);
    return ResponseEntity.ok(indexInfo);
  }

  /**
   * 지수 정보 등록 POST /api/index-infos
   */
  @PostMapping
  public ResponseEntity<IndexInfoDto> createIndexInfo(@RequestBody IndexInfoCreateRequest request) {
    IndexInfoDto createdIndexInfo = indexInfoService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdIndexInfo);
  }

  /**
   * 지수 정보 수정 PATCH /api/index-infos/{id}
   */
  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> updateIndexInfo(
      @PathVariable Long id,
      @RequestBody IndexInfoUpdateRequest request) {

    // 요청 데이터가 모두 null인 경우 체크
    if (request.employedItemsCount() == null &&
        request.basePointInTime() == null &&
        request.baseIndex() == null &&
        request.favorite() == null) {
      throw new IllegalArgumentException("수정할 데이터가 없습니다.");
    }

    IndexInfoDto updatedIndexInfo = indexInfoService.update(id, request);
    return ResponseEntity.ok(updatedIndexInfo);
  }
}
