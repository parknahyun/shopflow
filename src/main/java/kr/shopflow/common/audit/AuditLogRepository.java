package kr.shopflow.common.audit;

import java.util.List;
import org.springframework.data.repository.Repository;

/**
 * 변경 이력 저장소(T166 스텁, T024에서 채운다). 추가와 조회만 있다.
 * 수정과 삭제 메서드가 생기지 않도록 CrudRepository를 상속하지 않는다.
 */
public interface AuditLogRepository extends Repository<AuditLog, Long> {

    AuditLog save(AuditLog log);

    List<AuditLog> findByTargetTypeAndTargetIdOrderByIdAsc(String targetType, long targetId);
}
