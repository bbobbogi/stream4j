package io.github.bbobbogi.stream4j.chzzk;

import io.github.bbobbogi.stream4j.chzzk.naver.Naver;
import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;

/**
 * Class for creating instances of {@link Chzzk}.
 *
 * @since 1.0.0
 */
public class ChzzkBuilder extends PlatformApiBuilder<Chzzk, ChzzkBuilder> {
    boolean isAnonymous = false;
    String nidAuth;
    String nidSession;

    /**
     * Creates a new {@link ChzzkBuilder} that not logged in.
     */
    public ChzzkBuilder() {
        this.isAnonymous = true;
    }

    /**
     * Makes {@link ChzzkBuilder} authorized.
     * To build an instance of {@link Chzzk} that logged in, we must have
     * the values of NID_AUT and NID_SES cookies.<br>
     *
     * You can get that values from developer tools of your browser.<br>
     * In Chrome, you can see the values from
     * {@code Application > Cookies > https://chzzk.naver.com}
     *
     * @param nidAuth The value of NID_AUT cookie
     * @param nidSession The value of NID_SES cookie
     * @return this {@link ChzzkBuilder} instance
     */
    public ChzzkBuilder withAuthorization(String nidAuth, String nidSession) {
        this.nidAuth = nidAuth;
        this.nidSession = nidSession;
        this.isAnonymous = false;

        return this;
    }

    /**
     * Add authorize token (NID_AUT and NID_SES) automatically by {@link Naver}.
     *
     * @param naver The authorized naver object
     * @return this {@link ChzzkBuilder} instance
     */
    public ChzzkBuilder withAuthorization(Naver naver) {
        return withAuthorization(naver.getCookie(Naver.Cookie.NID_AUT), naver.getCookie(Naver.Cookie.NID_SES));
    }

    /**
     * Creates a {@link Chzzk} instance.
     *
     * @return a new {@link Chzzk} instance
     */
    @Override
    public Chzzk build() {
        Chzzk chzzk = new Chzzk(this);
        chzzk.isDebug = this.isDebug;
        return chzzk;
    }
}
