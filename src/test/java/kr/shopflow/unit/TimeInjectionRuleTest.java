package kr.shopflow.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * SQL에서 데이터베이스 시계를 쓰지 않는다(T169, review.md W-09).
 * 시각은 TimeProvider에서 받아 파라미터로 넘겨야 시험에서 시간을 고정할 수 있다.
 * 자바 코드의 문자열(질의)만 검사하므로 주석이나 time.now() 호출은 걸리지 않는다.
 * 마이그레이션의 기본값 정의는 검사하지 않는다.
 */
class TimeInjectionRuleTest {

    private static final Pattern DB_CLOCK = Pattern.compile(
            "(?i)\\b(now\\s*\\(\\)|current_timestamp|current_date|localtimestamp|clock_timestamp\\s*\\()");

    /** 텍스트 블록과 일반 문자열 리터럴. */
    private static final Pattern STRING_LITERAL = Pattern.compile("(?s)\"\"\"(.*?)\"\"\"|\"((?:[^\"\\\\\\n]|\\\\.)*)\"");

    @Test
    void 질의에_데이터베이스_시계가_없다() throws IOException {
        Path root = Path.of("src/main/java");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher literal = STRING_LITERAL.matcher(Files.readString(file));
                while (literal.find()) {
                    String text = literal.group(1) != null ? literal.group(1) : literal.group(2);
                    if (DB_CLOCK.matcher(text).find()) {
                        violations.add(file + ": " + text.strip().lines().findFirst().orElse(""));
                    }
                }
            }
        }
        assertThat(violations).as("DB 시계를 쓰는 질의").isEmpty();
    }

    @Test
    void 규칙이_실제로_걸리는지_확인한다() {
        assertThat(DB_CLOCK.matcher("UPDATE x SET at = now() WHERE id = ?").find()).isTrue();
        assertThat(DB_CLOCK.matcher("WHERE expires_at < CURRENT_TIMESTAMP").find()).isTrue();
        assertThat(DB_CLOCK.matcher("WHERE expires_at < ?").find()).isFalse();
    }
}
