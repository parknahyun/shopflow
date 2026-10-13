package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 인증과 역할 규칙(T013, FR-002a, 헌법 원칙 II).
 * 기반 단계에서는 시험용 더미 경로로 확인하고, 실제 경로에서는 T055, T108에서 다시 돌린다.
 */
@AutoConfigureMockMvc
class SecurityRulesTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void 로그인하지_않은_요청은_401로_거절하고_한글_안내를_준다() throws Exception {
        mvc.perform(get("/dummy/customer")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 고객은_고객_기능을_쓴다() throws Exception {
        mvc.perform(get("/dummy/customer")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 고객_계정은_판매_기능을_쓸_수_없다() throws Exception {
        mvc.perform(get("/dummy/seller")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void 판매자_계정은_장바구니와_주문_기능을_쓸_수_없다() throws Exception {
        mvc.perform(get("/dummy/customer")).andExpect(status().isForbidden());
        mvc.perform(post("/dummy/customer/orders").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void 판매자는_운영자_기능을_쓸_수_없다() throws Exception {
        mvc.perform(get("/dummy/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 운영자는_운영자_기능을_쓴다() throws Exception {
        mvc.perform(get("/dummy/admin")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void 위조_방어_토큰이_없는_상태_변경_요청은_거절한다() throws Exception {
        mvc.perform(post("/dummy/customer/orders")).andExpect(status().isForbidden());
        mvc.perform(post("/dummy/customer/orders").with(csrf())).andExpect(status().isOk());
    }

    @Test
    void 비밀번호는_솔트가_적용된_해시로_저장한다() {
        String raw = "비밀번호-Pass123!";
        String first = passwordEncoder.encode(raw);
        String second = passwordEncoder.encode(raw);
        assertThat(first).isNotEqualTo(raw).isNotEqualTo(second);
        assertThat(passwordEncoder.matches(raw, first)).isTrue();
        assertThat(passwordEncoder.matches("다른 비밀번호", first)).isFalse();
    }
}
