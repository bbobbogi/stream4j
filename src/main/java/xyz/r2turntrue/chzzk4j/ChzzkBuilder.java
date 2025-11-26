package xyz.r2turntrue.chzzk4j;

import xyz.r2turntrue.chzzk4j.naver.Naver;

/**
 * Class for creating instances of {@link Chzzk}.
 */
public class ChzzkBuilder {
    boolean isAnonymous = false;
    boolean isDebug = false;
    String nidAuth;
    String nidSession;

    /**
     * Creates a new {@link ChzzkBuilder} that not logged in.
     */
    public ChzzkBuilder() {
        this.isAnonymous = true;
    }

    /**
     * 디버그 모드를 활성화합니다.
     *
     * @return 현재 {@link ChzzkBuilder} 인스턴스
     */
    public ChzzkBuilder withDebugMode() {
        isDebug = true;

        return this;
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
    public Chzzk build() {
        Chzzk chzzk = new Chzzk(this);
        chzzk.isDebug = this.isDebug;
        return chzzk;
    }
}
