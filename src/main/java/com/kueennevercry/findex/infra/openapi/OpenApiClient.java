package com.kueennevercry.findex.infra.openapi;

import com.kueennevercry.findex.dto.request.IndexInfoApiRequest;
import com.kueennevercry.findex.dto.response.IndexInfoApiResponse;
import java.util.List;

public interface OpenApiClient {

  List<IndexInfoApiResponse> fetchAllIndexData(IndexInfoApiRequest indexInfoApiRequest);

  List<IndexInfoApiResponse> fetchAllIndexDataByNameAndDateRange(String indexName, String beginDate,
      String endDate);
}
