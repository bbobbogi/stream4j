package io.github.bbobbogi.stream4j.cime;

import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;

/**
 * Builder for {@link CiMe} API clients.
 *
 * <p>Cookie authentication is optional and disabled by default.
 *
 * @since 1.0.0
 */
public class CiMeBuilder extends PlatformApiBuilder<CiMe, CiMeBuilder> {
    String cookie;

    /**
     * Configures a Cookie header for API requests.
     * Default is no cookie.
     *
     * @param cookie raw Cookie header value
     * @return this builder for chaining
     */
    public CiMeBuilder withCookie(String cookie) {
        this.cookie = cookie;
        return self();
    }

    /**
     * Builds a {@link CiMe} API client.
     *
     * @return a configured CiMe client
     */
    @Override
    public CiMe build() {
        return new CiMe(this);
    }
}
