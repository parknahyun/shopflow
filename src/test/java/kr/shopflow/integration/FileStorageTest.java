package kr.shopflow.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import kr.shopflow.common.storage.FileStoragePort.Area;
import kr.shopflow.common.storage.FileStoragePort.StoredFile;
import kr.shopflow.common.storage.InvalidFileException;
import kr.shopflow.common.storage.LocalDiskFileStorage;
import kr.shopflow.common.storage.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 파일은 서버가 정한 이름으로 저장하고 허용 형식과 크기를 검사한다(T017).
 * 입점 서류는 비공개 영역, 상품 사진은 공개 영역에 둔다. 상한값은 설정값이다(T168).
 */
class FileStorageTest {

    private static final byte[] PDF = "%PDF-1.7\n내용".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

    @TempDir
    Path root;

    LocalDiskFileStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalDiskFileStorage(new StorageProperties(root, Set.of("application/pdf", "image/png"), 1024,
                Set.of("image/png", "image/jpeg"), 2048));
    }

    private static ByteArrayInputStream stream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    @Test
    void 서버가_정한_이름으로_저장하고_원래_파일_이름을_쓰지_않는다() throws IOException {
        StoredFile saved = storage.store(Area.PRIVATE, "사업자등록증 최종본.pdf", "application/pdf", stream(PDF));
        assertThat(saved.key()).doesNotContain("사업자등록증").doesNotContain("최종본").endsWith(".pdf");
        assertThat(saved.size()).isEqualTo(PDF.length);
        try (var in = storage.open(Area.PRIVATE, saved.key())) {
            assertThat(in.readAllBytes()).isEqualTo(PDF);
        }
    }

    @Test
    void 같은_이름으로_올려도_서로_다른_이름으로_저장한다() throws IOException {
        StoredFile first = storage.store(Area.PRIVATE, "a.pdf", "application/pdf", stream(PDF));
        StoredFile second = storage.store(Area.PRIVATE, "a.pdf", "application/pdf", stream(PDF));
        assertThat(first.key()).isNotEqualTo(second.key());
    }

    @Test
    void 경로를_빠져나가는_파일_이름도_저장_위치에_영향을_주지_않는다() throws IOException {
        StoredFile saved = storage.store(Area.PRIVATE, "../../etc/passwd.pdf", "application/pdf", stream(PDF));
        assertThat(Files.exists(root.resolve("private").resolve(saved.key()))).isTrue();
        assertThat(saved.key()).doesNotContain("..").doesNotContain("/").doesNotContain("\\");
    }

    @Test
    void 비공개와_공개_영역은_다른_위치에_둔다() throws IOException {
        StoredFile document = storage.store(Area.PRIVATE, "a.png", "image/png", stream(PNG));
        StoredFile photo = storage.store(Area.PUBLIC, "b.png", "image/png", stream(PNG));
        assertThat(Files.exists(root.resolve("private").resolve(document.key()))).isTrue();
        assertThat(Files.exists(root.resolve("public").resolve(photo.key()))).isTrue();
        assertThat(Files.exists(root.resolve("public").resolve(document.key()))).isFalse();
    }

    @Test
    void 허용되지_않는_형식은_거절한다() {
        assertThatThrownBy(() -> storage.store(Area.PRIVATE, "a.exe", "application/x-msdownload", stream(PDF)))
                .isInstanceOf(InvalidFileException.class);
        assertThatThrownBy(() -> storage.store(Area.PRIVATE, "a.html", "text/html", stream(PDF)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 영역마다_허용_형식이_다르다() {
        // 서류 영역은 jpeg를 허용하지 않고, 사진 영역은 pdf를 허용하지 않는다(위 설정 기준).
        assertThatThrownBy(() -> storage.store(Area.PRIVATE, "a.jpg", "image/jpeg", stream(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})))
                .isInstanceOf(InvalidFileException.class);
        assertThatThrownBy(() -> storage.store(Area.PUBLIC, "a.pdf", "application/pdf", stream(PDF)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 형식을_속인_파일은_거절한다() {
        byte[] script = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> storage.store(Area.PRIVATE, "a.pdf", "application/pdf", stream(script)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 크기_상한을_넘는_파일은_거절하고_저장하지_않는다() throws IOException {
        byte[] big = new byte[2000];
        System.arraycopy(PDF, 0, big, 0, PDF.length);
        assertThatThrownBy(() -> storage.store(Area.PRIVATE, "big.pdf", "application/pdf", stream(big)))
                .isInstanceOf(InvalidFileException.class);
        try (var files = Files.list(root.resolve("private"))) {
            assertThat(files.count()).isZero();
        }
    }

    @Test
    void 빈_파일은_거절한다() {
        assertThatThrownBy(() -> storage.store(Area.PRIVATE, "a.pdf", "application/pdf", stream(new byte[0])))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 없는_파일이나_경로를_벗어나는_열기는_거절한다() {
        assertThatThrownBy(() -> storage.open(Area.PRIVATE, "../public/x.png")).isInstanceOf(InvalidFileException.class);
        assertThatThrownBy(() -> storage.open(Area.PRIVATE, "nothing.pdf")).isInstanceOf(IOException.class);
    }
}
