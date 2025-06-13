# 팀명: 👑Kueen Never Cry - FINDEX

---

![image](https://github.com/user-attachments/assets/3809fb80-0a64-4f5c-b5ab-f8532db6a09b)

## 프로젝트 소개

> Codeit 백엔드 스프린트 3기 초급 프로젝트 레포지토리입니다.
>
> 프로젝트 기간: 2025.06.03 ~ 2025.06.13

## 팀원 구성

<table>
  <thead>
    <tr>
      <th style="width: 20%;">안여경</th>
      <th style="width: 20%;">박진솔</th>
      <th style="width: 20%;">조재구</th>
      <th style="width: 20%;">백은호</th>
      <th style="width: 20%;">조백선</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/7ba5969a-b53b-499d-bc8a-258ea71d808f" style="width: 100%; height: auto;"></td>
      <td><img src="https://github.com/user-attachments/assets/fc9c3047-91e4-4f44-906a-eceeddabea13" style="width: 100%; height: auto;"></td>
      <td><img src="https://github.com/user-attachments/assets/87e33773-2aba-49ba-8f67-8ef69c90f334" style="width: 100%; height: auto;"></td>
      <td><img src="https://github.com/user-attachments/assets/8679eb68-29fd-481e-ab59-4faefbb5612a" style="width: 100%; height: auto;"></td>
      <td><img src="https://github.com/user-attachments/assets/661de91f-8f3b-4af2-a9b5-b9f2b9aa02de" style="width: 100%; height: auto;"></td>
    </tr>
    <tr>
      <td>Leader / PM / BE</td>
      <td>BE / QA</td>
      <td>BE / QA</td>
      <td>DevOps / BE / QA</td>
      <td>DevOps / BE / QA</td>
    </tr>
    <tr>
      <td>OpenAPI 연동, 연동 정보, 지수 정보</td>
      <td>문서 작성, 지수 데이터 일괄, 지수 정보</td>
      <td>OpenAPI 연동, 대시보드, 지수 데이터</td>
      <td>GitHub 구성, 지수 정보 일괄</td>
      <td>Swagger API 명세, OpenAPI 연동, 자동 배치</td>
    </tr>
    <tr>
      <td><a href="https://github.com/yeokyeong">yeokyeong</a></td>
      <td><a href="https://github.com/JinsolPark">JinsolPark</a></td>
      <td><a href="https://github.com/NINE-J">NINE-J</a></td>
      <td><a href="https://github.com/BackEunHo">BackEunHo</a></td>
      <td><a href="https://github.com/bs8841">bs8841</a></td>
    </tr>
  </tbody>
</table>

## 기술 스택 및 사용 도구

| 항목       | 사용 도구 / 기술                                                                                                                                                                                                                                                                                                                                                                                 |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Backend  | <img src="https://img.shields.io/badge/SpringBoot-6DB33F.svg?&logo=SpringBoot&logoColor=white"> <img src="https://img.shields.io/badge/DataJPA-333333.svg?labelColor=6DB33F&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-333333.svg?labelColor=088CD0&logoColor=white"> <img src="https://img.shields.io/badge/OpenCSV-333333.svg?labelColor=000000&logoColor=white"> |
| Database | <img src="https://img.shields.io/badge/PostgreSQL-17.5-333333.svg?labelColor=4169E1&logo=PostgreSQL&logoColor=white">                                                                                                                                                                                                                                                                      |
| API 문서화  | <img src="https://img.shields.io/badge/swagger-000.svg?&logo=swagger&logoColor=white">                                                                                                                                                                                                                                                                                                     |
| 협업 도구    | <img src="https://img.shields.io/badge/Discord-5865F2.svg?&logo=discord&logoColor=white"> <img src="https://img.shields.io/badge/Notion-000000.svg?&logo=Notion&logoColor=white">                                                                                                                                                                                                          |
| 일정 관리    | <img src="https://img.shields.io/badge/GitHub-Issues-333333.svg?&logo=GitHub&labelColor=000000&logoColor=white"> <img src="https://img.shields.io/badge/GitHub-Projects-333333.svg?&logo=GitHub&labelColor=000000&logoColor=white"> <img src="https://img.shields.io/badge/Notion-Timeline-333333.svg?&logo=Notion&labelColor=000000&logoColor=white">                                     |
| IDE      | <img src="https://img.shields.io/badge/intellijidea-000000.svg?&logo=intellijidea&logoColor=white">                                                                                                                                                                                                                                                                                        |

## 주요 기능

- [x] 지수 정보 CRUD
- [x] 지수 데이터 CRUD 및 Export
- [x] 외부 OpenAPI 연동
- [x] 자동 연동 배치 설정
- [x] 연동 작업 이력 관리
- [x] Swagger 기반 API 문서 자동화

## 프로젝트 구조

```
src
└── main
   ├── java
   │   └── com.kueennevercry.findex
   │       ├── FindexApplication.java
   │       │   - Spring Boot 애플리케이션의 진입점 (main 메서드 포함)
   │       │
   │       ├── config
   │       │   ├── OpenApiProperties.java
   │       │   │   - application 설정에서 Open API 관련 설정을 불러오는 설정 클래스
   │       │   ├── QueryDSLConfig.java
   │       │   │   - QueryDSL 사용을 위한 JPAQueryFactory 설정 클래스
   │       │   └── RestTemplateConfig.java
   │       │       - 외부 API 호출을 위한 RestTemplate Bean 설정 클래스
   │       │
   │       ├── controller
   │       │   ├── AutoSyncConfigController.java           - 자동 연동 설정 CRUD API
   │       │   ├── IndexInfoController.java                - 지수 정보 관련 REST API (등록, 조회, 수정, 삭제)
   │       │   ├── IndexDataController.java                - 지수 데이터 관련 REST API (등록, 조회, 수정, 삭제, export)
   │       │   └── SyncJobController.java                  - 수동 연동 실행 API (지수 정보, 지수 데이터 동기화 요청)
   │       │
   │       ├── dto
   │       │   ├── request                                 - 클라이언트에서 서버로의 요청 데이터 구조
   │       │   │   ├── AutoSyncConfigUpdateRequest.java    - 자동 연동 설정 수정 요청 DTO
   │       │   │   ├── IndexDataCreateRequest.java         - 지수 데이터 생성 요청 DTO
   │       │   │   ├── IndexDataUpdateRequest.java         - 지수 데이터 수정 요청 DTO
   │       │   │   ├── IndexDataSyncRequest.java           - 지수 데이터 동기화 요청 DTO
   │       │   │   ├── IndexInfoApiRequest.java            - Open API 연동 시 파라미터 DTO
   │       │   │   ├── IndexInfoCreateRequest.java         - 지수 정보 생성 요청 DTO
   │       │   │   ├── IndexInfoListRequest.java           - 지수 정보 목록 요청 DTO (검색, 정렬)
   │       │   │   ├── IndexInfoUpdateRequest.java         - 지수 정보 수정 요청 DTO
   │       │   │   └── SyncJobParameterRequest.java        - 연동 작업 실행을 위한 파라미터 DTO
   │       │   │
   │       │   ├── response                                - 서버에서 클라이언트로의 응답 데이터 구조
   │       │   │   ├── AutoSyncConfigDto.java              - 자동 연동 설정 응답 DTO
   │       │   │   ├── CursorPageResponse.java             - 커서 기반 페이지네이션 응답 공통 DTO
   │       │   │   ├── IndexChartDto.java                  - 차트 데이터 응답 DTO
   │       │   │   ├── IndexDataDto.java                   - 지수 데이터 응답 DTO
   │       │   │   ├── IndexInfoApiResponse.java           - Open API 기반 지수 정보 응답 DTO
   │       │   │   ├── IndexInfoDto.java                   - 지수 정보 응답 DTO
   │       │   │   ├── IndexPerformanceDto.java            - 개별 지수 성과 응답 DTO
   │       │   │   └── RankedIndexPerformanceDto.java      - 지수 랭킹 성과 응답 DTO
   │       │   │
   │       │   ├── ChartDataPoint.java                     - 차트 렌더링용 시계열 점 단위 데이터 구조
   │       │   ├── IndexInfoSummaryDto.java                - 지수 요약 정보 DTO (간략 목록용)
   │       │   ├── PeriodType.java                         - 일/주/월 등 기간 구분용 Enum
   │       │   └── SyncJobDto.java                         - 연동 작업 이력 응답 DTO
   │       │
   │       ├── entity
   │       │   ├── AutoSyncConfig.java                     - 자동 연동 설정 도메인
   │       │   ├── IndexData.java                          - 지수 데이터 도메인
   │       │   ├── IndexInfo.java                          - 지수 정보 도메인
   │       │   ├── IntegrationJobType.java                 - 연동 작업 유형 (지수 정보/데이터 등)
   │       │   ├── IntegrationResultType.java              - 연동 결과 (SUCCESS/FAILED)
   │       │   ├── IntegrationTask.java                    - 연동 이력 기록 도메인
   │       │   └── SourceType.java                         - 데이터 소스 타입 (USER, OPEN_API)
   │       │
   │       ├── exception
   │       │   └── GlobalExceptionHandler.java             - 커스텀 예외에 대한 전역 처리 클래스 (@RestControllerAdvice)
   │       │
   │       ├── infra
   │       │   └── openapi
   │       │       ├── OpenApiClient.java                  - Open API 호출 인터페이스
   │       │       └── DefaultOpenApiClient.java           - RestTemplate 기반 실제 호출 구현체
   │       │
   │       ├── mapper
   │       │   ├── AutoSyncConfigMapper.java               - 자동 연동 설정 엔티티 <-> DTO 변환
   │       │   ├── IndexDataMapper.java                    - 지수 데이터 엔티티 <-> DTO 변환
   │       │   ├── IndexInfoMapper.java                    - 지수 정보 엔티티 <-> DTO 변환
   │       │   └── IntegrationTaskMapper.java              - 연동 작업 엔티티 <-> DTO 변환
   │       │
   │       ├── repository
   │       │   ├── AutoSyncConfigRepository.java           - JPA 기반 자동 연동 설정 Repository
   │       │   ├── AutoSyncConfigCustomRepository.java     - 커스텀 자동 연동 설정 Repository 인터페이스
   │       │   ├── AutoSyncConfigCustomRepositoryImpl.java - Custom Repository 구현체
   │       │   ├── IndexDataRepository.java
   │       │   │   - 지수 데이터 엔티티용 기본 JPA Repository (CRUD 및 메서드 이름 기반 쿼리)
   │       │   ├── IndexDataCustomRepository.java
   │       │   │   - 지수 데이터 커스텀 쿼리 인터페이스 (QueryDSL 기반 복잡한 조건 처리)
   │       │   ├── IndexDataCustomRepositoryImpl.java
   │       │   │   - IndexDataCustomRepository 구현체 (실제 동적 쿼리 로직 포함)
   │       │   ├── IndexInfoRepository.java
   │       │   │   - 지수 정보 엔티티용 기본 JPA Repository
   │       │   ├── IndexInfoCustomRepository.java
   │       │   │   - 지수 정보 커스텀 쿼리 인터페이스
   │       │   ├── IndexInfoCustomRepositoryImpl.java
   │       │   │   - IndexInfoCustomRepository 구현체 (조건 기반 지수 검색 등)
   │       │   ├── IntegrationTaskRepository.java
   │       │   │   - 연동 작업 이력에 대한 기본 Repository (최근 연동 여부 확인 등)
   │       │   ├── SyncJobCustomRepository.java
   │       │   │   - 수동 연동 작업 관련 커스텀 쿼리 인터페이스
   │       │   └── SyncJobCustomRepositoryImpl.java
   │       │       - SyncJobCustomRepository 구현체 (동적 조건 기반 연동 이력 필터링 등)
   │       │
   │       ├── scheduler
   │       │   └── IndexAutoSyncScheduler.java             - 자동 연동을 주기적으로 실행하는 스케줄러 (@Scheduled)
   │       │
   │       └── service
   │           ├── AutoSyncConfigService.java              - 자동 연동 설정 관련 서비스 로직
   │           ├── IndexDataService.java                   - 지수 데이터 서비스 인터페이스
   │           ├── IndexDataServiceImpl.java               - 지수 데이터 서비스 구현체
   │           ├── IndexInfoService.java                   - 지수 정보 서비스 로직
   │           ├── IndexSyncService.java                   - 지수 동기화용 서비스 인터페이스
   │           ├── RealIndexSyncService.java               - IndexSyncService의 실제 구현체
   │           └── SyncJobService.java                     - 연동 실행 및 이력 기록 서비스
   │
   └── resources
       ├── static/                                         - 정적 리소스 (빌드된 프론트엔드 번들 파일)
       ├── api-docs.json                                   - Swagger 기반 OpenAPI 명세 파일
       ├── application.yaml                                - Spring Boot 기본 설정 파일
       ├── db.yaml(ignored)                                - 데이터베이스 로컬 오버라이드 설정 (환경별 차등)
       └── schema.sql                                      - 프로젝트의 전체 DB 테이블 구조 정의 SQL 스크립트
```
