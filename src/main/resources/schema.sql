-- 기본 public 스키마 대신 사용자 전용 공간에서의 관리를 위한 스키마 생성
CREATE SCHEMA IF NOT EXISTS findex AUTHORIZATION findex;

-- findex 유저 기본 접근 스키마 설정
ALTER ROLE findex SET search_path TO findex;

SELECT *
FROM INDEX_INFO;
SELECT *
FROM INDEX_DATA
LIMIT 100;

-- 테이블 삭제
DROP TABLE IF EXISTS "integration_tasks";
DROP TABLE IF EXISTS "index_data";
DROP TABLE IF EXISTS "auto_sync_config";
DROP TABLE IF EXISTS "index_info";

-- index_info 테이블
CREATE TABLE "index_info"
(
    "id"                   BIGSERIAL PRIMARY KEY,
    "index_classification" VARCHAR(32)                           NOT NULL,
    "index_name"           VARCHAR(100)                          NOT NULL,
    "employed_items_count" INTEGER                               NOT NULL,
    "base_point_in_time"   TIMESTAMPTZ                           NOT NULL,
    "base_index"           DECIMAL(10, 2)                        NOT NULL,
    "source_type"          VARCHAR(32)                           NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    "favorite"             BOOLEAN,
    "created_at"           TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updated_at"           TIMESTAMPTZ
);

-- index_data 테이블
CREATE TABLE "index_data"
(
    "id"                  BIGSERIAL PRIMARY KEY,
    "index_info_id"       BIGSERIAL                             NOT NULL,
    "base_date"           DATE                                  NOT NULL,
    "source_type"         VARCHAR(20)                           NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    "market_price"        FLOAT       DEFAULT 0                 NOT NULL,
    "closing_price"       FLOAT       DEFAULT 0                 NOT NULL,
    "high_price"          FLOAT       DEFAULT 0                 NOT NULL,
    "low_price"           FLOAT       DEFAULT 0                 NOT NULL,
    "versus"              FLOAT       DEFAULT 0                 NOT NULL,
    "fluctuation_rate"    FLOAT       DEFAULT 0                 NOT NULL,
    "trading_quantity"    BIGINT      DEFAULT 0                 NOT NULL,
    "trading_price"       BIGINT      DEFAULT 0                 NOT NULL,
    "market_total_amount" BIGINT      DEFAULT 0                 NOT NULL,
    "created_at"          TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updated_at"          TIMESTAMPTZ,
    FOREIGN KEY ("index_info_id") REFERENCES "index_info" ("id")
);

-- auto_sync_config 테이블
CREATE TABLE "auto_sync_config"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "index_info_id" BIGINT                                NOT NULL UNIQUE,
    "enabled"       BOOLEAN     DEFAULT FALSE             NOT NULL,
    "created_at"    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updated_at"    TIMESTAMPTZ,
    FOREIGN KEY ("index_info_id") REFERENCES "index_info" ("id")
);

-- integration_tasks 테이블
CREATE TABLE "integration_tasks"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "index_info_id" BIGSERIAL,
    "job_type"      VARCHAR(20)                           NOT NULL CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA')),
    "worker"        VARCHAR(255)                          NOT NULL,
    "job_time"      TIMESTAMPTZ                           NOT NULL,
    "result"        VARCHAR(20)                           NOT NULL CHECK (result IN ('SUCCESS', 'FAILED')),
    "target_date"   DATE,
    "created_at"    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY ("index_info_id") REFERENCES "index_info" ("id")
);

COMMENT ON COLUMN "integration_tasks"."index_info_id" IS '지수정보 관리 테이블의 FK';
COMMENT ON COLUMN "integration_tasks"."job_type" IS 'ENUM("INDEX_INFO", "INDEX_DATA")';
COMMENT ON COLUMN "integration_tasks"."worker" IS 'IP 또는 SYSTEM';
COMMENT ON COLUMN "integration_tasks"."result" IS 'ENUM("SUCCESS", "FAILED")';

alter table integration_tasks
    alter column index_info_id drop not null;

--- 더미데이터 INSERT 구문
INSERT INTO index_info (id, index_classification, index_name, employed_items_count,
                        base_point_in_time, base_index, source_type, favorite)
VALUES (49, '테마지수', 'KRX ESG Leaders 150', 150, '2010-01-04', 1000.00, 'OPEN_API', false),
       (53, '테마지수', '코스피 200 ESG 지수', 111, '2012-01-02', 238.70, 'OPEN_API', true),
       (57, '테마지수', 'KRX 게임 TOP 10 지수', 10, '2015-01-02', 1000.00, 'OPEN_API', true),
       (62, '테마지수', 'KRX 전기차 Top 15', 15, '2014-12-30', 1000.00, 'OPEN_API', true),
       (73, '테마지수', 'KRX 부동산리츠인프라 지수', 17, '2015-01-02', 1000.00, 'OPEN_API', false),
       (75, '테마지수', 'KRX 기후변화 솔루션지수', 28, '2016-01-04', 1000.00, 'OPEN_API', false),
       (79, '테마지수', 'KRX FactSet 차세대 에너지 지수', 35, '2019-01-02', 1000.00, 'OPEN_API', true),
       (80, '테마지수', 'KRX FactSet 디지털 헬스케어 지수', 20, '2019-01-02', 1000.00, 'OPEN_API', true),
       (92, '테마지수', '코스피 배당성장 50', 50, '2009-07-01', 1000.00, 'OPEN_API', false),
       (94, '테마지수', 'KRX-IHS Markit 코스피 200 예측 고배당 50 TR', 0, '2012-01-02', 1000.00, 'OPEN_API',
        false);

SELECT *
FROM index_info;

-- 2024-01-01 ~ 2025-06-30 랜덤하게 더미 데이터를 생성해주는 쿼리
INSERT INTO index_data (index_info_id, base_date, source_type,
                        market_price, closing_price, high_price, low_price,
                        versus, fluctuation_rate,
                        trading_quantity, trading_price, market_total_amount)
SELECT ids.index_info_id,
       dates.base_date,
       CASE
           WHEN RANDOM() < 0.5 THEN 'USER'
           ELSE 'OPEN_API'
           END                                           AS source_type,
       FLOOR(900 + (100 * RANDOM()))::float              AS market_price,
       FLOOR(900 + (100 * RANDOM()))::float              AS closing_price,
       FLOOR(900 + (100 * RANDOM()))::float              AS high_price,
       FLOOR(900 + (100 * RANDOM()))::float              AS low_price,
       ROUND((10 * RANDOM())::numeric, 2)                AS versus,
       ROUND((5 * RANDOM())::numeric, 2)                 AS fluctuation_rate,
       FLOOR(90000 + (20000 * RANDOM()))::bigint         AS trading_quantity,
       FLOOR(90000000 + (20000000 * RANDOM()))::bigint   AS trading_price,
       FLOOR(900000000 + (200000000 * RANDOM()))::bigint AS market_total_amount
FROM (SELECT generate_series(DATE '2024-01-01', DATE '2025-06-30',
                             INTERVAL '1 day') AS base_date) AS dates
         CROSS JOIN (SELECT *
                     FROM (VALUES (49),
                                  (53),
                                  (57),
                                  (62),
                                  (73),
                                  (75),
                                  (79),
                                  (80),
                                  (92),
                                  (94)) AS t(index_info_id)) AS ids;

SELECT *
FROM index_data;

SELECT *
FROM integration_tasks;