package com.kueennevercry.findex.infra.openapi;

import java.util.List;

public interface OpenApiClient {

  List<IndexInfoApiResponse> fetchAllIndexData(IndexInfoApiRequest indexInfoApiRequest);
}
