package kr.shopflow.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 동시성 시험 공통 도구(T170).
 * 규칙: 스레드 수는 연결 풀 크기를 넘기지 않아도 되고(넘기면 풀 대기로 느려질 뿐 결과는 같다),
 * 출발 래치로 모든 스레드가 동시에 시작하게 하며, 제한 시간이 지나면 시험을 실패시킨다.
 */
public final class ConcurrencyTestSupport {

    private ConcurrencyTestSupport() {
    }

    public static <T> List<T> runConcurrently(int threads, Callable<T> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return task.call();
                }));
            }
            if (!ready.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("스레드가 제한 시간 안에 준비되지 않았다");
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
