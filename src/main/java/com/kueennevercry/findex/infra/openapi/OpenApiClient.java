package com.kueennevercry.findex.infra.openapi;

import com.kueennevercry.findex.dto.response.IndexDataResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

public interface OpenApiClient {

  List<IndexDataResponse> fetchIndexData(String indexCode, String endpoint, LocalDate from,
      LocalDate to)
      throws IOException, URISyntaxException;
}
