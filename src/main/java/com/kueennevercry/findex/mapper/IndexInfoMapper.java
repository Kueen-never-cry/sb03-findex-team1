package com.kueennevercry.findex.mapper;

import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.entity.IndexInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

/**
 * IndexInfo 엔티티와 IndexInfoDto 간의 변환 매퍼 변환 책임을 분리하여 단일 책임 원칙(SRP) 준수
 * <p>
 * 주요 기능: Entity ↔ DTO 변환 책임 분리 (단일 책임 원칙) 컴파일 시점에 MapStruct가 자동으로 구현체 생성 같은 이름의
 * 필드는 자동 매핑, null 안전성
 * 보장
 * <p>
 * 변환 방식: Entity → DTO: Entity의 각 필드를 개별 변수로 추출 후 record 생성자 직접 호출 • DTO →
 * Entity: DTO의 각 필드를 개별 변수로
 * 추출 후 Entity의 Builder 패턴 사용
 */
@Component
@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

  IndexInfoDto toDto(IndexInfo indexInfo);

  IndexInfo toEntity(IndexInfoDto indexInfoDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  IndexInfo toEntity(IndexInfoCreateRequest request);

  /**
   * IndexInfo 엔티티를 IndexInfoSummaryDto로 변환
   * 요약 정보만 포함 (ID, 지수명, 지수 분류명)
   */
  IndexInfoSummaryDto toSummaryDto(IndexInfo indexInfo);

}