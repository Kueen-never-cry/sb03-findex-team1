package com.kueennevercry.findex.dto;

public record AutoSyncConfig(
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    Boolean enabled
) {}
