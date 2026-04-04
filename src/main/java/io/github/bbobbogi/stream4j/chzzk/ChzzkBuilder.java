package io.github.bbobbogi.stream4j.chzzk;

import io.github.bbobbogi.stream4j.chzzk.naver.Naver;
import io.github.bbobbogi.stream4j.common.PlatformApiBuilder;

/**
 * Class for creating instances of {@link Chzzk}.
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
     * @return 현재 {@link ChzzkBuilder} 인스턴스
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
     * @return 현재 {@link ChzzkBuilder} 인스턴스
     */
    public ChzzkBuilder withAuthorization(Naver naver) {
        return withAuthorization(naver.getCookie(Naver.Cookie.NID_AUT), naver.getCookie(Naver.Cookie.NID_SES));
    }

    /**
     * {@link Chzzk} 인스턴스를 생성합니다.
     *
     * @return 생성된 {@link Chzzk} 인스턴스
     */
    @Override
    public Chzzk build() {
        Chzzk chzzk = new Chzzk(this);
        chzzk.isDebug = this.isDebug;
        return chzzk;
    }
}
