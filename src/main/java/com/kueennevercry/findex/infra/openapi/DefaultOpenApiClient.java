package com.kueennevercry.findex.infra.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kueennevercry.findex.config.OpenApiProperties;
import com.kueennevercry.findex.dto.request.IndexInfoApiRequest;
import com.kueennevercry.findex.dto.response.IndexInfoApiResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class DefaultOpenApiClient implements OpenApiClient {

  private final RestTemplate restTemplate;
  private final OpenApiProperties properties;
  private final ObjectMapper objectMapper;

  // TODO : 외부응답 500 에러일때 전역 에러 처리
  @Override
  public List<IndexInfoApiResponse> fetchAllIndexData(IndexInfoApiRequest indexInfoApiRequest) {
    ResponseEntity<String> response = restTemplate.getForEntity(buildUrl(indexInfoApiRequest),
        String.class);
    try {
      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode items = root.path("response").path("body").path("items").path("item");

      return objectMapper.readerForListOf(IndexInfoApiResponse.class).readValue(items);
    } catch (IOException e) {
      throw new IllegalStateException("외부 API 응답 JSON 파싱 실패", e);
    }
  }

  @Override
  public List<IndexInfoApiResponse> fetchAllIndexDataByNameAndDateRange(
      String indexName, LocalDate beginDate, LocalDate endDate) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    String beginBasDt = beginDate.format(formatter);
    String endBasDt = endDate.format(formatter);

    int page = 1;
    int numOfRows = 1000;
    List<IndexInfoApiResponse> result = new ArrayList<>();

    while (true) {
      IndexInfoApiRequest request = IndexInfoApiRequest.builder()
          .pageNo(page)
          .numOfRows(numOfRows)
          .idxNm(indexName)
          .beginBasDt(beginBasDt)
          .endBasDt(endBasDt)
          .build();

      URI uri = buildUrl(request);

      ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

      try {
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode items = root.path("response").path("body").path("items").path("item");

        if (items.isMissingNode() || items.isEmpty()) {
          break;
        }

        List<IndexInfoApiResponse> pageData = objectMapper
            .readerForListOf(IndexInfoApiResponse.class)
            .readValue(items);

        result.addAll(pageData);
        page++;
      } catch (IOException e) {
        throw new IllegalStateException("외부 API 응답 JSON 파싱 실패", e);
      }
    }

    return result;
  }

  private URI buildUrl(IndexInfoApiRequest indexInfoApiRequest) {
    String encodedIdxNm = indexInfoApiRequest.getIdxNm() == null ? null
        : URLEncoder.encode(indexInfoApiRequest.getIdxNm(), StandardCharsets.UTF_8)
            .replace("+", "%20");  // 공백 -> + -> %20으로 치환

    return UriComponentsBuilder.newInstance()
        .scheme(properties.getScheme())
        .host(properties.getHost())
        .path(properties.getPath())
        .queryParam("serviceKey", properties.getApiEncodedKey())
        .queryParam("resultType", "json")
        .queryParam("pageNo", indexInfoApiRequest.getPageNo())
        .queryParam("numOfRows", indexInfoApiRequest.getNumOfRows())
        .queryParam("idxNm", encodedIdxNm)
        .queryParam("beginBasDt", indexInfoApiRequest.getBeginBasDt())
        .queryParam("endBasDt", indexInfoApiRequest.getEndBasDt())
        .queryParam("basDt", indexInfoApiRequest.getBasDt())
        .build(true)
        .toUri();
  }
}
