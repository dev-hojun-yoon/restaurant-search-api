package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
class TestcontainersConfiguration {

	// public static final MySQLContainer<?> MYSQL_CONTAINER;

	// static {
	// 	MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
	// 		.withDatabaseName("hhplus")
	// 		.withUsername("application")
	// 		.withPassword("application");
	// 	MYSQL_CONTAINER.start();

	// 	System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
	// 	System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
	// 	System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
	// }

	// // 동적으로 spring 에 datasource 설정을 반영
	// @DynamicPropertySource
	// public static void overrideDatasourceProperties(DynamicPropertyRegistry registry) {
	// 	registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
	// 	registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
	// 	registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
	// }

	// // 마지막 소멸 단계
	// // 스프링 컨테이너에서 객체 (Bean) 을 제거하기 전에 해야할 작업에 대해 정의
	// @PreDestroy
	// public void preDestroy() {
	// 	if (MYSQL_CONTAINER.isRunning()) {
	// 		MYSQL_CONTAINER.stop();
	// 	}
	// }
}