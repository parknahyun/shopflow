package kr.shopflow.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 통합 테스트 공통 기반 클래스.
 * 테스트컨테이너스로 포스트그레스큐엘을 한 번만 띄우고, 플라이웨이 마이그레이션은
 * 애플리케이션이 시작될 때 자동으로 적용된다(운영과 같은 종류의 데이터베이스를 쓴다).
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
