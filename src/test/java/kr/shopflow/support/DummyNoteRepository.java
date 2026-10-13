package kr.shopflow.support;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/** 어노테이션을 붙였으므로 노출된다. 쓰기 요청은 거절되어야 한다(T012). */
@RepositoryRestResource(path = "dummy-notes")
public interface DummyNoteRepository extends CrudRepository<DummyNote, Long> {
}
