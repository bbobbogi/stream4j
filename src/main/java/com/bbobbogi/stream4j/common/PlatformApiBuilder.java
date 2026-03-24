package com.bbobbogi.stream4j.common;

/**
 * 플랫폼 API 빌더의 공통 추상 클래스입니다.
 *
 * @param <T> 빌드할 플랫폼 API 타입
 * @param <B> 빌더 자기 자신 타입 (fluent chaining용)
 */
public abstract class PlatformApiBuilder<T, B extends PlatformApiBuilder<T, B>> {

    protected boolean isDebug = false;

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    public B withDebugMode() {
        this.isDebug = true;
        return self();
    }

    public boolean isDebugEnabled() {
        return isDebug;
    }

    public abstract T build();
}
