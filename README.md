## 프로젝트 소개
맛집 조회 API 서비스를 Spring Boot 를 기반으로 구현하였습니다. 

외부 Open API (kakao, naver) 를 이용한 맛집 검색 서비스이며, 키워드를 이용해 검색을 할 수 있습니다.
인기 키워드 기능을 제공해서 사람들이 현재 관심 있는 맛집이 무엇인지 알 수 있습니다.

## 설계사항
docs 에 정리가 되어 있으며, 다음 문서들을 정리하였습니다.

* API Interface
* ERD 데이터베이스 모델링
* Flow Chart
* Infra 구조도
* 서비스 요구사항 정의
* 시퀀스 다이어그램

## 구현 / 고려 내용
* 특정 외부 API 가 동작하지 않았을 때, 다른 API 로 전환하고 2개의 외부 API 모두 동작하지 않아도
기능이 문제 없도록 구현하였습니다.
* 인기 키워드 저장, 조회 로직이 검색 성능에 영향을 끼치지 않도로 구현 (비동기, redis)
* PopularKeywordRepository 인터페이스를 domain 에 두고, 외부와 관련된 infrastructure 에서 이를 구현 (의존성 역전 원칙)
* 외부 API client 를 추상화하여 외부 API 종류 확장될 경우에 대응
* 클린 아키텍처 적용
* 분산락 및 리액티브 방식 활용
* Kafka Pub/Sub 을 이용한 비동기 이벤트 발행 (외부 데이터 플랫폼에 메시지를 발행하는 상황을 가정)
* Docker compose 를 이용해서 필요 리소스 로컬 세팅 (Kafka, MySQL, Redis)
* 단위 테스트 및 E2E 테스트 코드 작성


## 알게 된 내용
* @SpringBootTest: 통합 테스트용 (컨텍스트 로딩까지 포함) 으로 무겁고 느림 // @ExtendWith 는 순수 객체 간의 단위 테스트에 적합
* Service 레이어에서 usecase 단위로 트랜잭션 관리, repository 에서는 개별 쿼리 수준의 책임만 가져야 함
* Restaurant (Domain) 은 비즈니스 로직 중심이며, RestaurantEntity (infrastructure) 는 영속성 중심임. => Restaurant 는 DB를 모르고 순수한 비즈니스 로직만 담당해서 DB 스키마 변경이 도메인에 영향을 주지 않는 이점이 있음
* 동일한 Service 파일 내에 @Transactional 트랜잭션 메소드를 호출하면 오류가 발생 => Spring 은 AOP 를 사용해, 트랜잭션을 구현하고 있고, 트랜잭션 설정 시 내부적으로 생성되는 프록시가 있어 동일한 파일 내에서는 트랜잭션 전파가 안되는 문제가 발생할 수 있음 => 트랜잭션 전용 파일을 생성하여 해결 // @Transactional 뿐 아니라 @Cacheable 같은 AOP 어노테이션도 동일한 특성임 // 이벤트 기반으로 설계하거나 // @Async + ComputableFuture 조합으로도 가능
* 책임 연쇄 패턴 (Chain of Responsibility)
  * 클라이언트 요청에 대한 세세한 처리를 하나의 객체가 다 처리하는 게 아닌, 여러 처리 객체로 나누고, 사슬처럼 연결해 집합 안에서 연쇄적으로 처리하는 행동 패턴
## Getting Started

