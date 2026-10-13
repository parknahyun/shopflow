package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import kr.shopflow.common.audit.AuditLog;
import kr.shopflow.common.audit.AuditLogRepository;
import kr.shopflow.common.audit.AuditService;
import kr.shopflow.support.MutableTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;

/** 변경 이력: 추가만 하고 수정과 삭제를 하지 않는다. 개인정보와 카드 정보는 담지 않는다(T016, FR-016). */
class AuditLogTest extends IntegrationTestBase {

    @Autowired
    AuditService audit;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    MutableTimeProvider time;

    @AfterEach
    void resetTime() {
        time.reset();
    }

    @Test
    void 상태가_바뀔_때마다_한_행씩_추가한다() {
        time.fixAt(Instant.parse("2026-10-12T01:00:00Z"));
        audit.record("ORDER", 9001L, null, "PENDING_PAYMENT", "account:1");
        time.fixAt(Instant.parse("2026-10-12T01:05:00Z"));
        audit.record("ORDER", 9001L, "PENDING_PAYMENT", "CONFIRMED", "system");

        List<AuditLog> history = audit.history("ORDER", 9001L);
        assertThat(history).hasSize(2);
    }

    @Test
    void 첫_기록은_이전_상태가_없다() {
        audit.record("PAYMENT", 9002L, null, "READY", "system");
        assertThat(jdbc.queryForObject(
                "SELECT from_status FROM audit_log WHERE target_type = 'PAYMENT' AND target_id = 9002", String.class))
                .isNull();
    }

    @Test
    void 발생_시각은_시각_공급자에서_받는다() {
        Instant fixed = Instant.parse("2026-10-12T03:00:00Z");
        time.fixAt(fixed);
        audit.record("SUB_ORDER", 9003L, "BEFORE_SHIPMENT", "SHIPPING", "account:7");
        Instant stored = jdbc.queryForObject(
                "SELECT occurred_at FROM audit_log WHERE target_type = 'SUB_ORDER' AND target_id = 9003",
                java.sql.Timestamp.class).toInstant();
        assertThat(stored).isEqualTo(fixed);
    }

    @Test
    void 데이터베이스가_수정을_막는다() {
        audit.record("ORDER", 9004L, null, "PENDING_PAYMENT", "system");
        assertThatThrownBy(() -> jdbc.update("UPDATE audit_log SET to_status = 'X' WHERE target_id = 9004"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 데이터베이스가_삭제를_막는다() {
        audit.record("ORDER", 9005L, null, "PENDING_PAYMENT", "system");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM audit_log WHERE target_id = 9005"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 저장소에는_수정과_삭제_메서드가_없다() {
        assertThat(CrudRepository.class.isAssignableFrom(AuditLogRepository.class)).isFalse();
        for (var method : AuditLogRepository.class.getMethods()) {
            assertThat(method.getName().toLowerCase()).doesNotContain("delete").doesNotContain("update");
        }
    }

    @Test
    void 이메일이나_공백이_든_자유_문장은_담지_않는다() {
        assertThatThrownBy(() -> audit.record("ORDER", 9006L, null, "hong@example.com", "system"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> audit.record("ORDER", 9006L, null, "CONFIRMED", "홍길동 010-1234-5678"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> audit.record("ORDER", 9006L, null, "고객이 취소함", "system"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
