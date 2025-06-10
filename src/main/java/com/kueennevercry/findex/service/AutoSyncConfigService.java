package com.kueennevercry.findex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.dto.response.AutoSyncConfigDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.entity.AutoSyncConfig;
import com.kueennevercry.findex.entity.IndexInfo;
import com.kueennevercry.findex.mapper.AutoSyncConfigMapper;
import com.kueennevercry.findex.repository.AutoSyncConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final AutoSyncConfigMapper autoSyncConfigMapper;

  @Transactional
  public AutoSyncConfigDto updateEnabled(Long id, Boolean enabled) {
    AutoSyncConfig config = autoSyncConfigRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("자동 연동 설정을 찾을 수 없습니다. ID: " + id));

    config.setEnabled(enabled);

    IndexInfo index = config.getIndexInfo();
    IndexInfoSummaryDto indexInfoDto = new IndexInfoSummaryDto(
        index.getId(),
        index.getIndexName(),
        index.getIndexClassification()
    );
    return autoSyncConfigMapper.toDto(config, indexInfoDto);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<AutoSyncConfigDto> getAutoSyncConfigs(
      Long indexInfoId,
      Boolean enabled,
      String cursor,
      Long idAfter,
      String sortField,
      String sortDirection,
      int size
  ) {
    Long decodedIdAfter =
        idAfter != null ? idAfter : (cursor != null ? decodeCursor(cursor) : null);

    CursorPageResponse<AutoSyncConfigDto> response =
        autoSyncConfigRepository.findAllByParameters(
            indexInfoId, enabled, decodedIdAfter, sortField, sortDirection, size
        );

    String nextCursor = (response.hasNext() && !response.content().isEmpty())
        ? encodeCursor(response.content().get(response.content().size() - 1).id())
        : null;

    return new CursorPageResponse<>(
        response.content(),
        nextCursor,
        response.nextIdAfter(),
        response.size(),
        response.totalElements(),
        response.hasNext()
    );
  }

  private Long decodeCursor(String cursor) {
    try {
      byte[] decoded = Base64.getUrlDecoder().decode(cursor);
      String json = new String(decoded, StandardCharsets.UTF_8);
      JsonNode node = new ObjectMapper().readTree(json);
      return node.get("id").asLong();
    } catch (Exception e) {
      throw new IllegalArgumentException("유효하지 않은 커서 형식입니다: " + cursor, e);
    }
  }

  private String encodeCursor(Long id) {
    try {
      Map<String, Object> map = Map.of("id", id);
      String json = new ObjectMapper().writeValueAsString(map);
      return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("커서를 생성하는 데 실패했습니다. ID: " + id, e);
    }
  }
}