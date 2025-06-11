package com.kueennevercry.findex.infra.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kueennevercry.findex.config.OpenApiProperties;
import java.io.IOException;
import java.net.URI;
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

  /* FIXME : 더 좋은 방법이 있지 않을까?  */
  private URI buildUrl(IndexInfoApiRequest indexInfoApiRequest) {

    // 1. 자동 인코딩이 필요한 파라미터를 먼저 빌드
    String encodedOtherParams = UriComponentsBuilder.newInstance()
        .queryParam("resultType", "json")
        .queryParam("pageNo", indexInfoApiRequest.getPageNo())
        .queryParam("numOfRows", indexInfoApiRequest.getNumOfRows())
        .queryParam("beginBasDt", indexInfoApiRequest.getBeginBasDt())
        .queryParam("endBasDt", indexInfoApiRequest.getEndBasDt())
        .build()
        .getQuery();

    // 2. 인코딩하지말고, 인코딩이 되어있는 키 값 그대로 써야하므로 분리
    String query =
        "?serviceKey=" + properties.getApiEncodedKey() + "&" + encodedOtherParams;

    // 3. 최종 URI 조립
    String fullUrl = UriComponentsBuilder.newInstance()
        .scheme(properties.getScheme())
        .host(properties.getHost())
        .path(properties.getPath())
        .build(false)
        .toUriString()
        + query;

    System.out.println("11111 " + "URI.create(fullUrl) = " + URI.create(fullUrl));
    return URI.create(fullUrl);
  }
}
