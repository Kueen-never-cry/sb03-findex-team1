package com.kueennevercry.findex.infra.openapi;

import com.kueennevercry.findex.dto.request.IndexInfoApiRequest;
import com.kueennevercry.findex.dto.response.IndexInfoApiResponse;
import java.time.LocalDate;
import java.util.List;

public interface OpenApiClient {

  List<IndexInfoApiResponse> fetchAllIndexData(IndexInfoApiRequest indexInfoApiRequest);

  List<IndexInfoApiResponse> fetchAllIndexDataByNameAndDateRange(String indexName,
      LocalDate beginDate,
      LocalDate endDate);
}
