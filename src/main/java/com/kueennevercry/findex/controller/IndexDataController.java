package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.request.IndexDataCreateDto;
import com.kueennevercry.findex.dto.request.IndexDataUpdateDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

    private final IndexDataService indexDataService;

    //----------- 지수 데이터 --------------//
    @GetMapping("/{indexInfoId}")
    public ResponseEntity<List<IndexDataDto>> findByIndexInfoIdAndBaseDateRange(
            @PathVariable Long indexInfoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "baseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
            ) {
        return ResponseEntity.ok(indexDataService.findAllByBaseDateBetween(indexInfoId, from, to, sortBy, sortDirection));
    }

    @PostMapping
    public ResponseEntity<IndexData> create(
            @RequestBody IndexDataCreateDto dto
    ) {
        IndexData indexData = indexDataService.create(dto);

        return ResponseEntity.ok(indexData);
    }

    @PatchMapping("{id}")
    public ResponseEntity<IndexData> update(
            @PathVariable Long id,
            @RequestBody IndexDataUpdateDto dto
    ) {
        IndexData indexData = indexDataService.update(id, dto);

        return ResponseEntity.ok(indexData);
    }

    @DeleteMapping("{id}")
    public void delete(
            @PathVariable Long id
    ) {
        indexDataService.delete(id);
    }

}

