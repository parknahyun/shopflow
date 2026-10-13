package kr.shopflow.support;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 비노출 시험용 개체. {@link DummyNote}와 같은 개체를 쓰면 스프링 데이터 REST가 개체당 하나만 고르는
 * 탓에 노출 여부가 실행마다 달라질 수 있어, 별도 개체와 테이블을 쓴다.
 */
@Entity
@Table(name = "hidden_dummy_note")
public class HiddenDummyNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected HiddenDummyNote() {
    }

    public Long getId() {
        return id;
    }
}
