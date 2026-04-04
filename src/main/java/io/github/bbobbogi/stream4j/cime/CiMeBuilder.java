package io.github.bbobbogi.stream4j.cime;

import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;

public class CiMeBuilder extends PlatformApiBuilder<CiMe, CiMeBuilder> {
    String cookie;

    public CiMeBuilder withCookie(String cookie) {
        this.cookie = cookie;
        return self();
    }

    @Override
    public CiMe build() {
        return new CiMe(this);
    }
}
