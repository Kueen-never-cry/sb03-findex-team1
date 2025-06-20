package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.service.AutoSyncConfigService;
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

@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {

  private final AutoSyncConfigService autoSyncConfigService;

  @PatchMapping("/{id}")
  public ResponseEntity<AutoSyncConfigDto> updateAutoSyncConfig(
      @PathVariable("id") Long id,
      @RequestBody AutoSyncConfigUpdateRequest request
  ) {
    AutoSyncConfigDto updated = autoSyncConfigService.updateEnabled(id, request.getEnabled());
    return ResponseEntity.status(HttpStatus.OK).body(updated);
  }

  @GetMapping
  public CursorPageResponse<AutoSyncConfigDto> getAutoSyncConfigs(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(defaultValue = "id") String sortFild,
      @RequestParam(defaultValue = "asc") String sortDirection,
      @RequestParam(defaultValue = "10") int size
  ) {
    return autoSyncConfigService.getAutoSyncConfigs(
        indexInfoId, enabled, cursor, idAfter, sortFild, sortDirection, size
    );
  }
}
