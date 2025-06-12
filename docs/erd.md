# 데이터베이스 모델링

<br>

## 1. restaurant

| 필드명    | 타입 | 제약 조건 | 설명 |
| ---------| -----| ------- | ---- | 
| `id`     | BIGINT | PK, AUTO_INCREMENT | 맛집 ID |
| `title`  | VARCHAR(200) | NOT NULL, DEFAULT '', FULLTEXT (WITH PARSER ngram) | 맛집 이름 |
| `category` | VARCHAR(100) | NOT NULL, DEFAULT '', FULLTEXT (WITH PARSER ngram) | 업체 분류 정보 |
| `road_address` | VARCHAR(500) | UNIQUE, FULLTEXT (WITH PARSER ngram) | 도로명 주소 |
| `mapx` | INT | NOT NULL | X 좌표 |
| `mapy` | INT | NOT NULL | Y 좌표 | 
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | 생성 시각 | 
| `updated_at` | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 갱신 시각 | 

<br>

## 2. popular keyword
| 필드명 | 타입 | 제약 조건 | 설명 |
| ------| ------| --------| --------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 인기 키워드 ID | 
| `keyword` | VARCHAR(200) | UNIQUE | 검색 키워드 | 
| `count`| INT | NOT NULL, DEFAULT 1 | 조회 수 | 
| `region` | VARCHAR(100) | NOT NULL, DEFAULT '' | 지역 |

<br>