package com.kueennevercry.findex.dto.response;

public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    Boolean enabled
) {

}
