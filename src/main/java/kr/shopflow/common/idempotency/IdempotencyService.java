package kr.shopflow.common.idempotency;

import java.sql.Timestamp;
import java.time.Instant;
import kr.shopflow.common.TimeProvider;
import kr.shopflow.common.error.ErrorCode;
import kr.shopflow.common.error.UnprocessableException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 멱등키 서비스(T023, T172, ADR-0002, ADR-0009 B6).
 *
 * <p>(범위, 키)는 유일하다. 같은 키에 같은 내용이 오면 저장된 결과를 그대로 돌려주고, 내용이 다르면 거절한다(422).
 * 처리 중인 키에는 점유 만료 시각이 있어, 서버가 도중에 죽어도 점유 시간이 지나면 다른 요청이 이어받는다.
 * 키는 시도마다 새로 만들고, 실패한 결과도 저장한다.
 *
 * <p>모든 동작은 호출자의 트랜잭션과 별개로 즉시 확정한다(호출자가 롤백해도 "처리 중" 표시는 남아야
 * 점유 만료로 회수할 수 있기 때문이다). 시각은 {@link TimeProvider}에서 받아 질의에 넘긴다.
 */
@Service
public class IdempotencyService {

    /** 요청 시작 결과. */
    public enum Outcome {
        /** 처음 보는 키. 호출자가 처리를 진행한다. */
        STARTED,
        /** 이미 끝난 키. 저장된 결과를 그대로 돌려준다. */
        REPLAY,
        /** 다른 요청이 처리 중이고 점유 시간이 아직 남아 있다. */
        IN_PROGRESS,
        /** 이전 처리가 점유 시간을 넘겨 멈췄다. 이 요청이 이어받았으니 외부 상태를 조회해 마무리한다. */
        CLAIM_EXPIRED
    }

    /** 시작 결과. {@code result}는 REPLAY일 때만 있다. */
    public record Begin(Outcome outcome, String result) {
    }

    private static final String PROCESSING = "PROCESSING";

    private final JdbcTemplate jdbc;
    private final TimeProvider time;
    private final IdempotencyProperties properties;

    public IdempotencyService(JdbcTemplate jdbc, TimeProvider time, IdempotencyProperties properties) {
        this.jdbc = jdbc;
        this.time = time;
        this.properties = properties;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Begin begin(String scope, String key, String requestHash) {
        Instant now = time.now();
        int inserted = jdbc.update("""
                INSERT INTO idempotency_key (scope, idem_key, request_hash, status, claim_expires_at, expires_at, created_at)
                VALUES (?, ?, ?, 'PROCESSING', ?, ?, ?)
                ON CONFLICT (scope, idem_key) DO NOTHING
                """, scope, key, requestHash, ts(now.plus(properties.claimDuration())),
                ts(now.plus(properties.ttl())), ts(now));
        if (inserted == 1) {
            return new Begin(Outcome.STARTED, null);
        }

        Row row = jdbc.queryForObject("""
                SELECT request_hash, status, CAST(result AS text) AS result, claim_expires_at
                FROM idempotency_key WHERE scope = ? AND idem_key = ?
                """, (rs, i) -> new Row(rs.getString("request_hash"), rs.getString("status"),
                rs.getString("result"), rs.getTimestamp("claim_expires_at")), scope, key);

        if (!row.requestHash().equals(requestHash)) {
            throw new UnprocessableException(ErrorCode.IDEMPOTENCY_KEY_REUSED,
                    ErrorCode.IDEMPOTENCY_KEY_REUSED.defaultMessage());
        }
        if (!PROCESSING.equals(row.status())) {
            return new Begin(Outcome.REPLAY, row.result());
        }
        if (row.claimExpiresAt() != null && row.claimExpiresAt().toInstant().isAfter(now)) {
            return new Begin(Outcome.IN_PROGRESS, null);
        }

        int taken = jdbc.update("""
                UPDATE idempotency_key SET claim_expires_at = ?
                WHERE scope = ? AND idem_key = ? AND status = 'PROCESSING' AND (claim_expires_at IS NULL OR claim_expires_at <= ?)
                """, ts(now.plus(properties.claimDuration())), scope, key, ts(now));
        return new Begin(taken == 1 ? Outcome.CLAIM_EXPIRED : Outcome.IN_PROGRESS, null);
    }

    /** 처리를 끝내고 결과(JSON)를 저장한다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String scope, String key, String resultJson) {
        finish(scope, key, "COMPLETED", resultJson);
    }

    /** 처리를 실패로 끝내고 실패 결과(JSON)를 저장한다. 재시도는 새 키로 한다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String scope, String key, String resultJson) {
        finish(scope, key, "FAILED", resultJson);
    }

    /** 보관 기간이 지난 끝난 키를 지운다(정리 작업이 부른다). 처리 중인 키는 지우지 않는다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteExpired() {
        return jdbc.update("DELETE FROM idempotency_key WHERE status <> 'PROCESSING' AND expires_at < ?",
                ts(time.now()));
    }

    private void finish(String scope, String key, String status, String resultJson) {
        Instant now = time.now();
        int updated = jdbc.update("""
                UPDATE idempotency_key
                SET status = ?, result = CAST(? AS jsonb), claim_expires_at = NULL, expires_at = ?
                WHERE scope = ? AND idem_key = ? AND status = 'PROCESSING'
                """, status, resultJson, ts(now.plus(properties.ttl())), scope, key);
        if (updated != 1) {
            throw new IllegalStateException("처리 중인 멱등키가 아니어서 결과를 저장할 수 없습니다.");
        }
    }

    private static Timestamp ts(Instant instant) {
        return Timestamp.from(instant);
    }

    private record Row(String requestHash, String status, String result, Timestamp claimExpiresAt) {
    }
}
