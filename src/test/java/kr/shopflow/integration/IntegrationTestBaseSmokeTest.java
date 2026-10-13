package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** 공통 기반 클래스가 포스트그레스큐엘에 연결된 애플리케이션을 띄우는지 확인한다(T008). */
class IntegrationTestBaseSmokeTest extends IntegrationTestBase {

    @Autowired
    DataSource dataSource;

    @Test
    void 포스트그레스큐엘에_연결된_상태로_애플리케이션이_뜬다() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        }
    }
}
