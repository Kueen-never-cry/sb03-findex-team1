package com.kueennevercry.findex.infra.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kueennevercry.findex.config.OpenApiProperties;
import com.kueennevercry.findex.dto.response.IndexDataResponse;
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

  @Override
  public List<IndexDataResponse> fetchIndexData(String indexCode, String endpoint, LocalDate from,
      LocalDate to)
      throws IOException, URISyntaxException {
    URI url = buildUrl(indexCode, endpoint, from, to);
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    return convert(response.getBody());
  }

  private List<IndexDataResponse> convert(String json) throws IOException {
    JsonNode root = objectMapper.readTree(json);
    JsonNode items = root.path("response").path("body").path("items").path("item");

    return objectMapper.readerForListOf(IndexDataResponse.class).readValue(items);
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
