package kr.shopflow.common.audit;

import java.util.List;
import java.util.regex.Pattern;
import kr.shopflow.common.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 변경 이력 서비스(T024, FR-016). 결제, 주문, 하위 주문, 재고 확보, 환불의 상태가 바뀔 때마다
 * 한 행을 추가한다. 호출자의 트랜잭션에 참여하므로 상태 변경과 이력이 함께 확정되거나 함께 사라진다.
 *
 * <p>개인정보가 새어 들어가지 않도록 상태와 행위자는 코드 값(영문 대문자와 밑줄, 행위자는 식별자 모양)만 받는다.
 */
@Service
public class AuditService {

    private static final Pattern STATUS_CODE = Pattern.compile("[A-Z][A-Z0-9_]{0,29}");
    private static final Pattern ACTOR = Pattern.compile("[A-Za-z0-9_:.\\-]{1,100}");

    private final AuditLogRepository repository;
    private final TimeProvider time;

    public AuditService(AuditLogRepository repository, TimeProvider time) {
        this.repository = repository;
        this.time = time;
    }

    @Transactional
    public void record(String targetType, long targetId, String fromStatus, String toStatus, String actor) {
        require(STATUS_CODE, targetType, "대상 종류");
        if (fromStatus != null) {
            require(STATUS_CODE, fromStatus, "이전 상태");
        }
        require(STATUS_CODE, toStatus, "이후 상태");
        require(ACTOR, actor, "행위자");
        repository.save(new AuditLog(targetType, targetId, fromStatus, toStatus, actor, time.now()));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> history(String targetType, long targetId) {
        return repository.findByTargetTypeAndTargetIdOrderByIdAsc(targetType, targetId);
    }

    private static void require(Pattern pattern, String value, String name) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(name + "은(는) 코드 값만 담을 수 있습니다(개인정보나 자유 문장은 담지 않는다).");
        }
    }
}
