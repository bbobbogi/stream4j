package io.github.bbobbogi.stream4j.common;

/**
 * Base abstract builder for platform API clients.
 *
 * @param <T> platform API type produced by {@link #build()}
 * @param <B> concrete builder self type for fluent chaining
 * @since 1.0.0
 */
public abstract class PlatformApiBuilder<T, B extends PlatformApiBuilder<T, B>> {

    protected boolean isDebug = false;

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    /**
     * Enables debug mode for the platform client being built.
     *
     * @return this builder for chaining
     */
    public B withDebugMode() {
        this.isDebug = true;
        return self();
    }

    /**
     * Returns whether debug mode is enabled.
     *
     * @return {@code true} when debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return isDebug;
    }

    /**
     * Builds the platform API client.
     *
     * @return configured platform API instance
     */
    public abstract T build();
}
