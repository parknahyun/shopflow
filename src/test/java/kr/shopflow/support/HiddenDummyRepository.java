package kr.shopflow.support;

import org.springframework.data.repository.CrudRepository;

/** 어노테이션이 없으므로 노출되지 않아야 한다(T012). */
public interface HiddenDummyRepository extends CrudRepository<HiddenDummyNote, Long> {
}
