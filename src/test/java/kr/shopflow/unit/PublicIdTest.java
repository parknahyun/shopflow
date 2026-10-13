package kr.shopflow.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import kr.shopflow.common.PublicId;
import org.junit.jupiter.api.Test;

/**
 * 외부에 내보내는 식별자는 추측하기 어려운 공개용 값이다(T173, review.md W-07).
 * 개체별 열과 응답 프로젝션 검사는 해당 개체가 생기는 스토리의 테스트에서 이어 한다.
 */
class PublicIdTest {

    @Test
    void 매번_다른_값을_만든다() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            seen.add(PublicId.generate());
        }
        assertThat(seen).hasSize(10_000);
    }

    @Test
    void 열_길이_36자의_무작위_UUID_모양이다() {
        String id = PublicId.generate();
        assertThat(id).hasSize(36).matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }
}
