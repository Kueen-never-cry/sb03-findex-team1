# 팀명: **Kueen Never Cry**

## **팀원 구성**

안여경 ((https://github.com/yeokyeong) <br>
박진솔 (https://github.com/JinsolPark) <br>
조백선 (https://github.com/bs8841) <br>
조재구 ((https://github.com/NINE-J) <br>
백은호 (https://github.com/BackEunHo)) <br>

## **프로젝트 소개**

- Codeit 백엔드 스프린트 3기 초급 프로젝트 레포지토리입니다.
- 프로젝트 기간: 2025.06.03 ~ 2025.06.13

## **기술 스택 및 사용 도구**

- Backend: Spring Boot,  , Spring Data JPA
- Database: PostgreSQL 17
- API 문서화: Swagger
- 협업 도구: Discord, Notion
- 일정 관리: Github Issues, GitHub Project, Notion 타임라인
- IDE: IntelliJ


## **팀원별 구현 기능 상세**


## **프로젝트 구조**
```
src/
├── main/
│   ├── java/
│   │   └── com/kueennevercry/findex/
│   │
│   │       ├── FindexApplication.java
│   │       │   └─ 메인 애플리케이션 클래스 (@SpringBootApplication)
│   │
│   │       ├── controller/                  ← 웹 요청 (API)을 처리하는 계층
│   │       │   └── Controller.java          ← 테스트용
│   │
│   │       ├── service/                     ← 비즈니스 로직을 담당하는 계층
│   │       │   └── Service.java             ← 테스트용 
│   │
│   │       ├── repository/                  ← DB에 접근하는 계층 (JPA 등)
│   │       │   └── Repository.java          ← 테스트용
│   │
│   │       ├── domain/                      ← Entity 클래스 (DB 테이블과 매핑)
│   │       │   └── Entity.java              ← 테스트용 
│   │
│   │       ├── dto/                         ← 데이터 전달 객체
│   │       │   └── Dto.java                 ← 테스트용
│   │
│   │       ├── config/                      ← 전역 설정 클래스 (CORS, Security 등)
│   │       │   └── Config.java              ← 테스트용
│   │
│   │       └── mapper/                      ← Entity ↔ DTO 간 매핑을 수행
│   │           └── Mapper.java              ← 테스트용 
│
│   └── resources/
│       ├── application.yml                  ← 애플리케이션 설정 파일
│       ├── db.yaml                          ← DB 설정 파일
│       ├── static/                          ← 정적 리소스 (HTML, CSS, JS, 이미지 등)

│
├── test/
│   └── java/
│       └── com/kueennevercry/findex/
│           └── FindexApplicationTests.java     ← 테스트 클래스
│
├── build.gradle                                ← 의존성 및 빌드 설정
└── README.md
```

## **배포 링크**
