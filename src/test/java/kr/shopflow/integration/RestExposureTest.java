package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.shopflow.support.DummyNote;
import kr.shopflow.support.DummyNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 스프링 데이터 REST 노출 범위(T012, ADR-0008). 읽기만 열고 쓰기는 모두 막는다.
 * 실제 리소스가 생기기 전에는 시험용 리소스(dummy-notes)로 같은 규칙을 확인하고,
 * 실제 리소스는 각 스토리의 통합 점검(T055, T108)과 T142에서 다시 돌린다.
 */
@AutoConfigureMockMvc
@WithMockUser(roles = "CUSTOMER")
class RestExposureTest extends IntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    DummyNoteRepository notes;

    long noteId;

    @BeforeEach
    void seed() {
        notes.deleteAll();
        noteId = notes.save(new DummyNote("읽기 전용")).getId();
    }

    @Test
    void 어노테이션을_붙인_리포지토리의_읽기는_허용한다() throws Exception {
        mvc.perform(get("/api/dummy-notes")).andExpect(status().isOk());
        mvc.perform(get("/api/dummy-notes/" + noteId)).andExpect(status().isOk());
    }

    @Test
    void 일반_POST는_거절한다() throws Exception {
        mvc.perform(post("/api/dummy-notes").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"body\":\"새 글\"}")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void PUT_PATCH_DELETE는_거절한다() throws Exception {
        String url = "/api/dummy-notes/" + noteId;
        mvc.perform(put(url).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"바꿈\"}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(patch(url).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"바꿈\"}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(delete(url).with(csrf())).andExpect(status().isMethodNotAllowed());
        assertThat(notes.findById(noteId)).get().extracting(DummyNote::getBody).isEqualTo("읽기 전용");
    }

    @Test
    void 어노테이션이_없는_리포지토리는_노출하지_않는다() throws Exception {
        String root = mvc.perform(get("/api")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat(root).contains("dummy-notes");
        assertThat(root).doesNotContain("hiddenDummies").doesNotContain("hidden-dummies");
    }

    @Test
    void 재고_배송_멱등키_변경이력_환불은_목록에_없다() throws Exception {
        String root = mvc.perform(get("/api")).andReturn().getResponse().getContentAsString().toLowerCase();
        for (String forbidden : new String[] {"stock", "deliver", "idempot", "audit", "refund"}) {
            assertThat(root).as("노출되면 안 되는 리소스: " + forbidden).doesNotContain(forbidden);
        }
    }

    @Test
    void 내부_식별자는_응답에_나오지_않는다() throws Exception {
        String body = mvc.perform(get("/api/dummy-notes/" + noteId)).andReturn().getResponse()
                .getContentAsString();
        assertThat(body).doesNotContain("\"id\"");
    }
}
