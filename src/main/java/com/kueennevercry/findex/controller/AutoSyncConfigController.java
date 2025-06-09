package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.kueennevercry.findex.service.AutoSyncConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
