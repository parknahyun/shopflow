package kr.shopflow.unit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 구조 규칙(T011, ADR-0007, ADR-0008). */
class ArchitectureTest {

    private static final String ROOT = "kr.shopflow";
    private static final List<String> CONTEXTS =
            List.of("account", "product", "order", "inventory", "payment", "delivery");

    private static final JavaClasses MAIN = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    @Test
    void 다른_컨텍스트의_저장소를_직접_쓰지_않는다() {
        for (String own : CONTEXTS) {
            List<String> others = new ArrayList<>();
            for (String other : CONTEXTS) {
                if (!other.equals(own)) {
                    others.add(ROOT + "." + other + ".repository..");
                }
            }
            noClasses().that().resideInAPackage(ROOT + "." + own + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(others.toArray(String[]::new))
                    .because(own + " 컨텍스트는 다른 컨텍스트의 저장소를 서비스 인터페이스를 거치지 않고 쓸 수 없다")
                    .allowEmptyShould(true)
                    .check(MAIN);
        }
    }

    @Test
    void 백엔드에_화면_코드가_없다() {
        assertThat(MAIN.stream().map(JavaClass::getPackageName))
                .noneMatch(p -> p.startsWith(ROOT + ".web") || p.startsWith(ROOT + ".client"));
    }

    @Test
    void 컨텍스트_사이에_JPA_연관을_두지_않고_식별자_값만_참조한다() {
        List<String> violations = new ArrayList<>();
        for (JavaClass type : MAIN) {
            String own = contextOf(type.getPackageName());
            if (own == null) {
                continue;
            }
            for (JavaField field : type.getFields()) {
                boolean association = field.isAnnotatedWith(ManyToOne.class) || field.isAnnotatedWith(OneToMany.class)
                        || field.isAnnotatedWith(OneToOne.class) || field.isAnnotatedWith(ManyToMany.class);
                if (!association) {
                    continue;
                }
                String target = contextOf(field.getRawType().getPackageName());
                if (target != null && !target.equals(own)) {
                    violations.add(type.getName() + "." + field.getName());
                }
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void 재고와_배송에는_api_패키지가_없다() {
        assertThat(MAIN.stream().map(JavaClass::getPackageName))
                .noneMatch(p -> p.startsWith(ROOT + ".inventory.api") || p.startsWith(ROOT + ".delivery.api"));
    }

    private static String contextOf(String packageName) {
        for (String context : CONTEXTS) {
            if (packageName.equals(ROOT + "." + context) || packageName.startsWith(ROOT + "." + context + ".")) {
                return context;
            }
        }
        return null;
    }
}
