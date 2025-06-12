package com.kueennevercry.findex.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "자동 연동 설정 수정 요청")
@Getter
@NoArgsConstructor
public class AutoSyncConfigUpdateRequest {

  private Boolean enabled;
}
