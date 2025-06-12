package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.service.AutoSyncConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "자동 연동 설정 API", description = "자동 연동 설정 관리 API")
@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {

  private final AutoSyncConfigService autoSyncConfigService;

  @Operation(summary = "자동 연동 설정 수정", description = "자동 연동 설정의 활성화 여부를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "자동 연동 설정 수정 성공"),
      @ApiResponse(responseCode = "404", description = "자동 연동 설정을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PatchMapping("/{id}")
  public ResponseEntity<AutoSyncConfigDto> updateAutoSyncConfig(
      @Parameter(description = "자동 연동 설정 ID") @PathVariable("id") Long id,
      @RequestBody AutoSyncConfigUpdateRequest request
  ) {
    AutoSyncConfigDto updated = autoSyncConfigService.updateEnabled(id, request.getEnabled());
    return ResponseEntity.status(HttpStatus.OK).body(updated);
  }

  @Operation(summary = "자동 연동 설정 목록 조회", description = "자동 연동 설정 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "자동 연동 설정 목록 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public CursorPageResponse<AutoSyncConfigDto> getAutoSyncConfigs(
      @Parameter(description = "지수 정보 ID") @RequestParam(required = false) Long indexInfoId,
      @Parameter(description = "활성화 여부") @RequestParam(required = false) Boolean enabled,
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "이전 페이지 마지막 요소 ID") @RequestParam(required = false) Long idAfter,
      @Parameter(description = "정렬 필드") @RequestParam(defaultValue = "id") String sortFild,
      @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "asc") String sortDirection,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
  ) {
    return autoSyncConfigService.getAutoSyncConfigs(
        indexInfoId, enabled, cursor, idAfter, sortFild, sortDirection, size
    );
  }
}
