package kr.shopflow.common;

import java.time.Instant;

/**
 * 시각 공급자. 업무 코드는 현재 시각을 직접 읽지 않고 이 인터페이스로 받는다.
 * SQL에서도 데이터베이스 시계(now())를 쓰지 않고 이 시각을 파라미터로 넘긴다(T169).
 */
public interface TimeProvider {

    Instant now();
}
