package com.kueennevercry.findex.repository;


import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.entity.IntegrationTask;
import com.kueennevercry.findex.entity.QIntegrationTask;
import com.kueennevercry.findex.mapper.IntegrationTaskMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class SyncJobCustomRepositoryImpl implements SyncJobCustomRepository {

  private final JPAQueryFactory queryFactory;
  private final IntegrationTaskMapper integrationTaskMapper;
  private static final QIntegrationTask qIntegrationTask = QIntegrationTask.integrationTask;


  public SyncJobCustomRepositoryImpl(JPAQueryFactory queryFactory,
      IntegrationTaskMapper integrationTaskMapper
  ) {
    this.queryFactory = queryFactory;
    this.integrationTaskMapper = integrationTaskMapper;
  }

  @Override
  public CursorPageResponse<SyncJobDto> findAllByParameters(
      SyncJobParameterRequest request) {

    /* 기본 필터링 */
    BooleanBuilder whereCondition = this.createFilterCondition(request);

    /* 필수 쿼리 */
    JPAQuery<IntegrationTask> query = queryFactory
        .selectFrom(qIntegrationTask)
        .leftJoin(qIntegrationTask.indexInfo)
        .where(whereCondition);

    /* cursor 기반 데이터 */
    BooleanExpression cursorCondition = this.createCursorCondition(request);
    if (cursorCondition != null) {
      query.where(cursorCondition);
    }

    /* sorting */
    OrderSpecifier<?>[] orderCondition = this.createOrderCondition(request.getSortField(),
        request.getSortDirection());
    if (orderCondition != null) {
      query.orderBy(orderCondition);
    }

    int pageSize = request.getSize() == null ? 10 : request.getSize();

    List<IntegrationTask> integrationTaskList = query
        .limit(pageSize)
        .fetch();

    Long totalElements = queryFactory
        .select(qIntegrationTask.count())
        .from(qIntegrationTask)
        .where(whereCondition) // cursorCondition 제외
        .fetchOne();

    return this.toCursorPageResponse(integrationTaskList, request, totalElements);
  }


  private BooleanBuilder createFilterCondition(SyncJobParameterRequest request) {
    BooleanBuilder builder = new BooleanBuilder();
    /* 필터링 */
    if (request.getJobType() != null) {
      builder.and(qIntegrationTask.jobType.eq(request.getJobType()));
    }
    if (request.getWorker() != null) {
      builder.and(qIntegrationTask.worker.containsIgnoreCase(request.getWorker()));
    }
    if (request.getStatus() != null) {
      builder.and(qIntegrationTask.result.eq(request.getStatus()));
    }
    if (request.getIndexInfoId() != null) {
      builder.and(qIntegrationTask.indexInfo.id.eq(request.getIndexInfoId()));
    }
    if (request.getJobTimeFrom() != null && request.getJobTimeTo() != null) {
      builder.and(qIntegrationTask.jobTime.between(
          this.localDateTimeToInstant(request.getJobTimeFrom()),
          this.localDateTimeToInstant(request.getJobTimeTo())
      ));
    }
    if (request.getBaseDateFrom() != null && request.getBaseDateTo() != null) {
      builder.and(qIntegrationTask.targetDate.between(
          request.getBaseDateFrom(),
          request.getBaseDateTo()
      ));
    }

    return builder;
  }

  /* 참고) Sorting 로직 설명
   * 1. sortField( "jobTime", "targetDate") 와 sortDirection 으로 order를 설정한다.
   * 2.  (이후에 id값으로도 필터링하므로) id 값으로도 다시 한번 order 설정한다.
   * */
  private OrderSpecifier<?>[] createOrderCondition(String sortField, String sortDirection) {
    if (sortField == null || sortDirection == null) {
      return null;
    }

    OrderSpecifier<?>[] orderSpecifierList;
    Order direction = Objects.equals(sortDirection, "desc") ? Order.DESC : Order.ASC;
    switch (sortField) {
      //FIXME :  "jobTime" ,"targetDate" -> enum으로 변경
      //작업일시
      case "jobTime":      //작업일시
        orderSpecifierList = new OrderSpecifier[]{
            new OrderSpecifier<>(direction, qIntegrationTask.jobTime),
            new OrderSpecifier<>(direction, qIntegrationTask.id)};
        break;

      case
          "targetDate":  //대상날짜 targetDate가 null 이면 가상의 값('9999-12-31')을 넣음 : null 값을 제일 아래로 빼기 위한 목적
        orderSpecifierList = new OrderSpecifier[]{
            new OrderSpecifier<>(direction,
                qIntegrationTask.targetDate.coalesce(LocalDate.parse("9999-12-31"))),
            new OrderSpecifier<>(direction, qIntegrationTask.id)};
        break;
      default:
        throw new IllegalStateException("정렬 필드가 잘못되었습니다: " + sortField);
    }
    ;
    return orderSpecifierList;
  }

  /* 참고) Cursor 로직 설명
   * : sortField에 따라 Cursor 값이 다르게 들어옴
   * case 1) "jobTime" 이면 LocalDateTime 들어옴
   * case 2) "targetDate" 이면 LocalDate 들어옴
   * */
  private BooleanExpression createCursorCondition(SyncJobParameterRequest request) {
    String sortField = request.getSortField();
    Long idAfter = request.getIdAfter();
    String cursor = request.getCursor();

    if (idAfter == null || sortField == null || cursor == null) {
      return null;
    }

    Order direction = Objects.equals(request.getSortDirection(), "desc") ? Order.DESC : Order.ASC;

    BooleanExpression cursorCondition;
    switch (sortField) {
      //FIXME :  "jobTime" ,"targetDate" -> enum으로 변경
      case "jobTime":      //작업일시
        Instant cursorJobTime = this.stringToInstant(cursor);
        // cursorJobTime 값 비교 그리고 id 비교
        cursorCondition = direction == Order.ASC ?
            qIntegrationTask.jobTime.gt(cursorJobTime)
                .or(qIntegrationTask.id.gt(request.getIdAfter())
                    .and(qIntegrationTask.id.gt(idAfter)))

            : qIntegrationTask.jobTime.lt(cursorJobTime)
                .or(qIntegrationTask.id.lt(request.getIdAfter())
                    .and(qIntegrationTask.id.lt(idAfter)));
        break;
      case "targetDate":   //대상날짜
        LocalDate cursorTargetDate = LocalDate.parse(cursor);
        // cursorTargetDate 값 비교 그리고 id 비교
        cursorCondition = direction == Order.ASC ?
            qIntegrationTask.targetDate.isNotNull()
                .and(qIntegrationTask.targetDate.gt(cursorTargetDate)
                    .or(qIntegrationTask.targetDate.eq(cursorTargetDate)
                        .and(qIntegrationTask.id.gt(idAfter))))
            : qIntegrationTask.targetDate.isNotNull()
                .and(qIntegrationTask.targetDate.lt(cursorTargetDate)
                    .or(qIntegrationTask.targetDate.eq(cursorTargetDate)
                        .and(qIntegrationTask.id.lt(idAfter))));
        break;
      default:
        throw new IllegalStateException("Unexpected sortField: " + sortField);
    }

    return cursorCondition;
  }

  /* LocalDateTime -> Instant로 변경 */
  private Instant localDateTimeToInstant(LocalDateTime jobTime) {
    if (jobTime == null) {
      return null;
    }
    // UTC + 9 = 한국시간
    return jobTime.toInstant(ZoneOffset.ofHours(9));
  }


  /* String -> Instant로 변경 */
  private Instant stringToInstant(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(cursor);
    } catch (DateTimeParseException e) {

      try {
        LocalDateTime local = LocalDateTime.parse(cursor,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
        return local.atZone(ZoneId.systemDefault()).toInstant(); // 시스템 시간대 기준으로 Instant 변환
      } catch (DateTimeParseException ex) {
        throw new IllegalArgumentException("지원하지 않는 시간 형식입니다: " + cursor);
      }
    }
  }

  private LocalDateTime instantToLocalDateTime(Instant cursor) {
    if (cursor == null) {
      return null;
    }
    return LocalDateTime.ofInstant(cursor, ZoneId.systemDefault());
  }

  private CursorPageResponse<SyncJobDto> toCursorPageResponse(
      List<IntegrationTask> integrationTaskList,
      SyncJobParameterRequest request,
      Long totalElements
  ) {
    List<SyncJobDto> syncJobDtoList = integrationTaskList.stream()
        .map(integrationTaskMapper::toSyncJobDto).toList();

    String nextCursor = null;
    Long nextIdAfter = null;

    if (!integrationTaskList.isEmpty()) {
      IntegrationTask last = integrationTaskList.get(integrationTaskList.size() - 1);
      nextIdAfter = last.getId();
      nextCursor = last.getJobTime().toString(); // 기본값으로 jobTime 넘겨줌

      // FIXME : 이상함, 마지막 아이템이 jobTime을 가지고 있지 않으면 null 보냄 -> 다음 페이지 없음?
      if (request.getSortField() != null) {
        switch (request.getSortField()) {
          case "jobTime" ->
              nextCursor = last.getJobTime() != null ? last.getJobTime().toString() : null;
          case "targetDate" ->
              nextCursor = last.getTargetDate() != null ? last.getTargetDate().toString() : null;
          default -> throw new IllegalStateException("Unexpected sort field");
        }
      }
    }

    boolean hasNext = integrationTaskList.size() == request.getSize();

    return new CursorPageResponse<>(
        syncJobDtoList,
        nextCursor,
        nextIdAfter,
        request.getSize(),
        totalElements,
        hasNext
    );
  }
}
