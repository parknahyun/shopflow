package kr.shopflow.common;

import java.util.UUID;

/**
 * 외부에 내보내는 공개용 식별자(T174). 내부 식별자(순번)는 화면과 API 응답에 나가지 않고,
 * 추측하기 어려운 무작위 값(UUID v4)만 나간다.
 */
public final class PublicId {

    private PublicId() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
