package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.IndexDataSyncRequest;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.service.SyncJobService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
  public List<SyncJobDto> syncIndexInfo() {
    return this.syncJobService.syncIndexInfo();
  }

  /*
  지수 데이터 연동
  : Open API를 통해 지수 데이터를 연동합니다
 */
  @PostMapping("/index-data")
  public SyncJobDto syncIndexData(@RequestBody IndexDataSyncRequest indexDataSyncRequest) {
    //XXX : 구조만 잡아놓음
    return null;
  }

  /*
  연동 작업 목록 조회
  */
  @GetMapping
  public CursorPageResponseSyncJobDto findAll(
      SyncJobParameterRequest syncJobParameterRequest) {
    return this.syncJobService.findAllByParameters(
        syncJobParameterRequest);
  }

}
