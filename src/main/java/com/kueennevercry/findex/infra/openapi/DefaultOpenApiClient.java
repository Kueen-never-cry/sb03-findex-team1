package com.kueennevercry.findex.infra.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kueennevercry.findex.config.OpenApiProperties;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class DefaultOpenApiClient implements OpenApiClient {

  private final RestTemplate restTemplate;
  private final OpenApiProperties properties;
  private final ObjectMapper objectMapper;


  //FIXME : 각 페이지별로 다 가져와야함 총데이터 203849 개 , 최대 페이지 크기 10000 개
  // TODO : 외부응답 500 에러일때 전역 에러 처리
  @Override
  public List<IndexInfoApiResponse> fetchAllIndexData() {
    ResponseEntity<String> response = restTemplate.getForEntity(rootUrl(), String.class);
    try {
      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode items = root.path("response").path("body").path("items").path("item");

      return objectMapper.readerForListOf(IndexInfoApiResponse.class).readValue(items);
    } catch (IOException e) {
      throw new IllegalStateException("외부 API 응답 JSON 파싱 실패", e);
    }
  }

  // TODO : 사용하는곳 없으면 삭제할것
  private List<IndexDataDto> convert(String json) {
    try {
      JsonNode root = objectMapper.readTree(json);
      JsonNode items = root.path("response").path("body").path("items").path("item");
      return objectMapper.readerForListOf(IndexDataDto.class).readValue(items);
    } catch (IOException e) {
      throw new IllegalStateException("외부 API 응답 JSON 파싱 실패", e);
    }
  }


  /* FIXME : 리팩토링할때 buildUrl(String indexCode, String endpoint, LocalDate from, LocalDate to) 메소드와 공통작업은 빼서 다시 구현 */
  private URI rootUrl() {
    try {
      String serviceKey = properties.getApiKey();

      String base = properties.getBaseUrl() + "/getStockMarketIndex"
          + "?serviceKey=" + serviceKey
          + "&resultType=json";
      // + 기호는 인코딩에서 제외되기 때문에 미리 변환하고
      // URI 클래스를 사용하면 URL 전송 할 때 문자열 그대로 날아가는 것이 아닌, 한 번 인코딩을 해서 보내준다
      return new URI(base.replace("+", "%2B"));
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("잘못된 URI 형식입니다: " + e.getInput(), e);
    }
  }

  private URI buildUrl(String indexCode, String endpoint, LocalDate from, LocalDate to)
      throws URISyntaxException {
    String serviceKey = properties.getApiKey();

    String base = properties.getBaseUrl() + endpoint
        // endpoint: [getStockMarketIndex | getBoundMarketIndex | getDerivationProductMarketIndex]
        + "?serviceKey=" + serviceKey
//        + "&idxNm=" + indexCode
        + "&beginBasDt=" + from
        + "&endBasDt=" + to
        + "&resultType=json";

    // + 기호는 인코딩에서 제외되기 때문에 미리 변환하고
    // URI 클래스를 사용하면 URL 전송 할 때 문자열 그대로 날아가는 것이 아닌, 한 번 인코딩을 해서 보내준다
    return new URI(base.replace("+", "%2B"));
  }
}
