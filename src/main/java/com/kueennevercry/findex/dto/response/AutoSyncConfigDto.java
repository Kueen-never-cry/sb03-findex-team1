package com.kueennevercry.findex.dto.response;

public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexName,
    String indexClassification,
    Boolean enabled
) {

}
