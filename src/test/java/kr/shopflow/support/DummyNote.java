package kr.shopflow.support;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 노출 범위 시험용 개체(시험 프로필에서만 테이블이 있다). */
@Entity
@Table(name = "dummy_note")
public class DummyNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String body;

    protected DummyNote() {
    }

    public DummyNote(String body) {
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }
}
