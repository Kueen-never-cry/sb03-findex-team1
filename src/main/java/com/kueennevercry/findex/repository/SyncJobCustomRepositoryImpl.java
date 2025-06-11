package com.kueennevercry.findex.repository;


import com.kueennevercry.findex.dto.CursorPageResponseSyncJobDto;
import com.kueennevercry.findex.dto.SyncJobDto;
import com.kueennevercry.findex.dto.request.SyncJobParameterRequest;
import com.kueennevercry.findex.entity.IntegrationTask;
import com.kueennevercry.findex.entity.QIntegrationTask;
import com.kueennevercry.findex.mapper.IntegrationTaskMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

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
  public CursorPageResponseSyncJobDto findAllByParameters(
      SyncJobParameterRequest request) {

    /* 기본 필터링 */
    BooleanBuilder whereCondition = this.createFilterCondition(request);

    /* 기본 sorting */
    OrderSpecifier<?>[] primaryOrder = this.createPrimaryOrder(request.getSortField(),
        request.getSortDirection());

    /* cursor 기반 데이터 */
    BooleanExpression cursorCondition = this.createCursorCondition(request);

    int pageSize = request.getSize() == null ? 10 : request.getSize();

    List<IntegrationTask> integrationTaskList = queryFactory
        .selectFrom(qIntegrationTask)
        .leftJoin(qIntegrationTask.indexInfo)
        .where(whereCondition, cursorCondition)
        .orderBy(primaryOrder)
        .limit(pageSize)
        .fetch();

    /*  CursorPageResponseSyncJobDto 타입으로 변환 */
    Long totalElements = queryFactory
        .select(qIntegrationTask.count())
        .from(qIntegrationTask)
        .where(whereCondition) // cursorCondition 제외
        .fetchOne();

    return this.toCursorPageResponseSyncJobDto(integrationTaskList, request, totalElements);
  }


  private BooleanBuilder createFilterCondition(SyncJobParameterRequest request) {
    BooleanBuilder builder = new BooleanBuilder();
    /* 필터링 */
    if (request.getJobType() != null) {
      builder.and(qIntegrationTask.jobType.eq(request.getJobType()));
    }
    if (request.getWorker() != null) {
      builder.and(qIntegrationTask.worker.eq(request.getWorker()));
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
  private OrderSpecifier<?>[] createPrimaryOrder(String sortField, String sortDirection) {
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

      case "targetDate":  //대상날짜
        orderSpecifierList = new OrderSpecifier[]{
            new OrderSpecifier<>(direction, qIntegrationTask.targetDate),
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
                .and(qIntegrationTask.id.gt(request.getIdAfter()))
            : qIntegrationTask.jobTime.lt(cursorJobTime)
                .and(qIntegrationTask.id.lt(request.getIdAfter()));
        break;
      case "targetDate":   //대상날짜
        LocalDate cursorTargetDate = LocalDate.parse(cursor);
        // cursorTargetDate 값 비교 그리고 id 비교
        cursorCondition = direction == Order.ASC ?
            qIntegrationTask.targetDate.gt(cursorTargetDate)
                .and(qIntegrationTask.id.gt(request.getIdAfter()))
            : qIntegrationTask.targetDate.lt(cursorTargetDate)
                .and(qIntegrationTask.id.lt(request.getIdAfter()));
        break;
      default:
        throw new IllegalStateException("Unexpected closingPrice: " + sortField);
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
    if (cursor == null) {
      return null;
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
      LocalDateTime localDateTime = LocalDateTime.parse(cursor, formatter);
      return localDateTime.toInstant(ZoneOffset.ofHours(9));
    } catch (Exception e) {
      throw new IllegalArgumentException("시간 형식이 잘못된 문자열입니다: " + cursor, e);
    }
  }

  // TODO: 이 메소드가 여기 있는게 맞는지 의문
  public CursorPageResponseSyncJobDto toCursorPageResponseSyncJobDto(
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

      switch (request.getSortField()) {
        case "jobTime" -> nextCursor = last.getJobTime().toString();
        case "targetDate" -> nextCursor = last.getTargetDate().toString();
        default -> throw new IllegalStateException("Unexpected sort field");
      }
    }

    boolean hasNext = integrationTaskList.size() == request.getSize();

    return new CursorPageResponseSyncJobDto(
        syncJobDtoList,
        nextCursor,
        nextIdAfter,
        request.getSize(),
        totalElements,
        hasNext
    );
  }
}
