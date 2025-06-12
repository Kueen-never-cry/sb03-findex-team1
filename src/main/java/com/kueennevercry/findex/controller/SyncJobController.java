package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.IndexDataSyncRequest;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.service.SyncJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
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
  @Operation(summary = "지수 정보 연동", description = "Open API를 통해 지수 정보를 연동합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 연동 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfo(HttpServletRequest request) {
    String clientIp = this.getClientIp(request);
    return ResponseEntity.ok(this.syncJobService.syncIndexInfo(clientIp));
  }

  /*
  지수 데이터 연동
  : Open API를 통해 지수 데이터를 연동합니다
 */
  @Operation(summary = "지수 데이터 연동", description = "Open API를 통해 지수 데이터를 연동합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 데이터 연동 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/index-data")
  public List<SyncJobDto> syncIndexData(
      @RequestBody @Valid IndexDataSyncRequest request,
      HttpServletRequest httpServletRequest
  ) {
    String clientIp = getClientIp(httpServletRequest);
    return this.syncJobService.syncIndexData(request, clientIp);
  }

  /*
  연동 작업 목록 조회
  */
  @Operation(summary = "연동 작업 목록 조회", description = "연동 작업 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "연동 작업 목록 조회 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseSyncJobDto> findAll(
      @ParameterObject SyncJobParameterRequest syncJobParameterRequest) {
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
