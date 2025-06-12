package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.IndexDataSyncRequest;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

  @Autowired
  private final SyncJobService syncJobService;

  /*
  지수 정보 연동
  : Open API를 통해 지수 정보를 연동합니다
   */
  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfo(HttpServletRequest request) {
    String clientIp = this.getClientIp(request);
    return ResponseEntity.ok(this.syncJobService.syncIndexInfo(clientIp));
  }

  /*
  지수 데이터 연동
  : Open API를 통해 지수 데이터를 연동합니다
 */
  @PostMapping("/index-data")
  public List<SyncJobDto> syncIndexData(@RequestBody @Valid IndexDataSyncRequest request) {
    return this.syncJobService.syncIndexData(request);
  }

  /*
  연동 작업 목록 조회
  */
  @GetMapping
  public ResponseEntity<CursorPageResponseSyncJobDto> findAll(
      SyncJobParameterRequest syncJobParameterRequest) {
    return ResponseEntity.ok(this.syncJobService.findAllByParameters(
        syncJobParameterRequest));

  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
      return ip.split(",")[0];
    }
    ip = request.getHeader("Proxy-Client-IP");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
      return ip;
    }
    ip = request.getHeader("WL-Proxy-Client-IP");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
      return ip;
    }
    return request.getRemoteAddr();
  }

}
