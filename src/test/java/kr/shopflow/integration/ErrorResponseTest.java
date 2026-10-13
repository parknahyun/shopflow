package kr.shopflow.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 오류 응답 규칙(T018). 권한 없는 접근은 존재 여부를 알리지 않도록 404,
 * 허용되지 않는 상태 변경은 409, 같은 멱등키에 다른 내용은 422이며 안내 문구는 한글이다.
 * 기반 단계에서는 시험용 더미 경로로 확인하고, 실제 경로에서는 T055, T108에서 다시 돌린다.
 */
@AutoConfigureMockMvc
class ErrorResponseTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 다른_고객의_주문은_404로_응답하고_존재_여부를_알리지_않는다() throws Exception {
        mvc.perform(get("/dummy/orders/some-order")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("찾을 수 없습니다."));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void 허용되지_않는_상태_변경은_409로_응답한다() throws Exception {
        mvc.perform(post("/dummy/sub-orders/transition").with(csrf())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NOT_ALLOWED_TRANSITION"))
                .andExpect(jsonPath("$.message").value(Matchers.matchesRegex(".*[가-힣].*")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 같은_멱등키에_다른_내용은_422로_응답한다() throws Exception {
        String key = UUID.randomUUID().toString();
        mvc.perform(post("/dummy/idempotent").with(csrf()).header("Idempotency-Key", key)
                .contentType(MediaType.TEXT_PLAIN).content("내용 A")).andExpect(status().isOk());
        mvc.perform(post("/dummy/idempotent").with(csrf()).header("Idempotency-Key", key)
                .contentType(MediaType.TEXT_PLAIN).content("내용 B")).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"))
                .andExpect(jsonPath("$.message").value(Matchers.matchesRegex(".*[가-힣].*")));
    }
}
