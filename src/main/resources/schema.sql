-- 기본 public 스키마 대신 사용자 전용 공간에서의 관리를 위한 스키마 생성
CREATE SCHEMA IF NOT EXISTS findex AUTHORIZATION findex;

-- findex 유저 기본 접근 스키마 설정
ALTER ROLE findex SET search_path TO findex;

SELECT * FROM INDEX_INFO;
SELECT * FROM INDEX_DATA;

-- 테이블 삭제
DROP TABLE IF EXISTS "integration_tasks" CASCADE;
DROP TABLE IF EXISTS "index_data" CASCADE;
DROP TABLE IF EXISTS "auto_sync_config" CASCADE;
DROP TABLE IF EXISTS "index_info" CASCADE;

-- index_info 지수 정보 관리 테이블
CREATE TABLE "index_info" (
                              "id" BIGSERIAL PRIMARY KEY,
                              "index_classification" VARCHAR(32) NOT NULL,
                              "index_name" VARCHAR(100) NOT NULL,
                              "employed_items_count" INTEGER NOT NULL,
                              "base_point_in_time" TIMESTAMP NOT NULL,
                              "base_index" DECIMAL(10,2) NOT NULL,
                              "source_type" VARCHAR(32) NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
                              "favorite" BOOLEAN,
                              "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              "updated_at" TIMESTAMP
);

-- index_data 지수 데이터 테이블
CREATE TABLE "index_data" (
                              "id" BIGSERIAL PRIMARY KEY,
                              "index_info_id" BIGINT NOT NULL,
                              "base_date" DATE NOT NULL,
                              "source_type" VARCHAR(20) NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
                              "market_price" DECIMAL(10,2) DEFAULT 0 NOT NULL,
                              "closing_price" DECIMAL(10,2) DEFAULT 0 NOT NULL,
                              "high_price" DECIMAL(10,2) DEFAULT 0 NOT NULL,
                              "low_price" DECIMAL(10,2) DEFAULT 0 NOT NULL,
                              "versus" DECIMAL(10,2) DEFAULT 0 NOT NULL,
                              "fluctuation_rate" DECIMAL(10,2) DEFAULT 0 NOT NULL,
                              "trading_quantity" BIGINT DEFAULT 0 NOT NULL,
                              "trading_price" BIGINT DEFAULT 0 NOT NULL,
                              "market_total_amount" BIGINT DEFAULT 0 NOT NULL,
                              "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              "updated_at" TIMESTAMP,
                              FOREIGN KEY ("index_info_id") REFERENCES "index_info"("id")
);

-- auto_sync_config 자동 연동 관리 테이블
CREATE TABLE "auto_sync_config" (
                                    "id" BIGSERIAL PRIMARY KEY,
                                    "index_info_id" BIGINT NOT NULL,
                                    "enabled" BOOLEAN DEFAULT FALSE NOT NULL,
                                    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    "updated_at" TIMESTAMP,
                                    FOREIGN KEY ("index_info_id") REFERENCES "index_info"("id")
);

-- integration_tasks 테이블
CREATE TABLE "integration_tasks" (
                                     "id" BIGSERIAL PRIMARY KEY,
                                     "index_info_id" BIGINT NOT NULL,
                                     "job_type" VARCHAR(20) NOT NULL CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA')),
                                     "worker" VARCHAR(255) NOT NULL,
                                     "job_time" TIMESTAMP NOT NULL,
                                     "result" VARCHAR(20) NOT NULL CHECK (result IN ('SUCCESS', 'FAILED')),
                                     "target_date" DATE,
                                     "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                     FOREIGN KEY ("index_info_id") REFERENCES "index_info"("id")
);

COMMENT ON COLUMN "integration_tasks"."index_info_id" IS '지수정보 관리 테이블의 FK';
COMMENT ON COLUMN "integration_tasks"."job_type" IS 'ENUM("INDEX_INFO", "INDEX_DATA")';
COMMENT ON COLUMN "integration_tasks"."worker" IS 'IP 또는 SYSTEM';
COMMENT ON COLUMN "integration_tasks"."result" IS 'ENUM("SUCCESS", "FAILED")';


--- 더미데이터 INSERT 구문
INSERT INTO index_info (id, index_classification, index_name, employed_items_count, base_point_in_time, base_index, source_type, favorite)
VALUES
    (49, '테마지수', 'KRX ESG Leaders 150', 150, '2010-01-04', 1000.00, 'OPEN_API', false),
    (53, '테마지수', '코스피 200 ESG 지수', 111, '2012-01-02', 238.70, 'OPEN_API', false),
    (57, '테마지수', 'KRX 게임 TOP 10 지수', 10, '2015-01-02', 1000.00, 'OPEN_API', false),
    (62, '테마지수', 'KRX 전기차 Top 15', 15, '2014-12-30', 1000.00, 'OPEN_API', false),
    (73, '테마지수', 'KRX 부동산리츠인프라 지수', 17, '2015-01-02', 1000.00, 'OPEN_API', false),
    (75, '테마지수', 'KRX 기후변화 솔루션지수', 28, '2016-01-04', 1000.00, 'OPEN_API', false),
    (79, '테마지수', 'KRX FactSet 차세대 에너지 지수', 35, '2019-01-02', 1000.00, 'OPEN_API', false),
    (80, '테마지수', 'KRX FactSet 디지털 헬스케어 지수', 20, '2019-01-02', 1000.00, 'OPEN_API', false),
    (92, '테마지수', '코스피 배당성장 50', 50, '2009-07-01', 1000.00, 'OPEN_API', false),
    (94, '테마지수', 'KRX-IHS Markit 코스피 200 예측 고배당 50 TR', 0, '2012-01-02', 1000.00, 'OPEN_API', false);

INSERT INTO index_data (id, index_info_id, base_date, source_type, market_price, closing_price, high_price, low_price, versus, fluctuation_rate, trading_quantity, trading_price, market_total_amount)
VALUES
    (1575, 57, '2025-05-30', 'OPEN_API', 1162.13, 1158.64, 1167.21, 1155.59, -8.95, -0.77, 9681182, 555516802241, 1162),
    (1576, 57, '2025-05-29', 'OPEN_API', 1153.46, 1167.59, 1174.71, 1148.13, 19.25, 1.68, 8598631, 475677869676, 1153),
    (1577, 92, '2025-05-28', 'OPEN_API', 1114.56, 1148.34, 1150.49, 1113.83, 39.13, 3.53, 9528971, 507786182527, 1114),
    (1578, 92, '2025-05-27', 'OPEN_API', 1112.10, 1109.21, 1124.55, 1104.57, -15.35, -1.36, 6877881, 332042816155, 1112);

INSERT INTO integration_tasks (id, index_info_id, job_type, worker, job_time, result, target_date)
VALUES
    (12346, 92, 'INDEX_INFO', '121.171.82.217', CURRENT_TIMESTAMP, 'SUCCESS', '2025-01-03'),
    (44328, 57, 'INDEX_DATA', '1.229.169.180', CURRENT_TIMESTAMP, 'SUCCESS', '2023-01-02');

INSERT INTO auto_sync_config (index_info_id, enabled)
VALUES
    (92, true),
    (53, true),
    (57, false);

SELECT * FROM integration_tasks