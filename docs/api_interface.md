# 맛집 조회 서비스 API interface

<br>

## 공통
* **Base URL**: `/api/v1/search`
* **Content-Type**: `application/json`

<br>

## 1. 맛집 조회
### GET /restaurants

> 맛집 정보를 조회합니다.

<br>

**요청 예시 (Query Parameter)**
| 파라미터 | 타입 | 필수 여부 | 예시 | 설명 |
| ------  | --- | --- | --- | ---- |
| keyword | string | Y | 맛집 | 검색 키워드 (지역 정보 포함 가능) |
| sort | string | N | accuracy / review (default: accuracy) | 정렬 방법 | 
| page | int | N | 1 (default: 1) | 페이지 정보 |

<br>

**응답 예시**
```json
{
    "status": 200,
    "message": "success",
    "data": [
        {
            "title": "빌라 더 다이닝 <b>홍대</b>본점",
            "link": "http://app.catchtable.co.kr/ct/shop/villathedining",
            "category": "음식점>양식",
            "roadAddress": "서울특별시 마포구 동교로30길 16 JnS.Bldg",
            "mapx": "1269249144",
            "mapy": "375601583"
        },
        {
            "title": "츠케루",
            "link": "http://instagram.com/tsukeru_tsukemen",
            "category": "음식점>일식>일본식라면",
            "roadAddress": "서울특별시 마포구 와우산로23길 9 1층 102호",
            "mapx": "1269245307",
            "mapy": "375533814"
        }
    ]
}
```

<br>
<br>

## 2. 인기 키워드 조회
### GET /keywords/popular

> 상위 10개의 인기 키워드 정보를 표시합니다.

<br>

**요청 예시 (Query Parameter)**
| 파라미터 | 타입 | 필수 여부 | 예시 | 설명 |
| ------  | --- | --- | ---- | --- | 
| region | string | N | 강남 | 지역 |

<br>

**응답 예시**
```json
{
    "status": 200,
    "message": "success",
    "data": [
        {
            "keyword": "강남역 맛집",
            "count" : 30  // 조회 수
        },
        {
            "keyword": "홍대 맛집",
            "count" : 25  // 조회 수
        }
        // ...
    ]
}
```