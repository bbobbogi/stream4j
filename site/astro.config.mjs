// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import sitemap from '@astrojs/sitemap';
import react from '@astrojs/react';

const SITE = 'https://bbobbogi.github.io';
const BASE = '/stream4j';

const META_DESC =
  'stream4j — 치지직, 씨미(CiMe), 숲(SOOP, 구 아프리카TV/아프리카티비), 유튜브(YouTube), 투네이션 5개 스트리밍 플랫폼의 채팅·후원을 통합 처리하는 Java 라이브러리. 마인크래프트, 디스코드, OBS, TTS 연동에 활용.';

// https://astro.build/config
export default defineConfig({
  site: SITE,
  base: BASE,
  trailingSlash: 'ignore',
  output: 'static',
  integrations: [
    react(),
    sitemap(),
    starlight({
      title: 'stream4j',
      description: META_DESC,
      defaultLocale: 'root',
      locales: {
        root: { label: '한국어', lang: 'ko' },
      },
      social: [
        {
          icon: 'github',
          label: 'GitHub',
          href: 'https://github.com/bbobbogi/stream4j',
        },
      ],
      customCss: ['./src/styles/custom.css'],
      head: [
        { tag: 'meta', attrs: { property: 'og:type', content: 'website' } },
        { tag: 'meta', attrs: { property: 'og:locale', content: 'ko_KR' } },
        { tag: 'meta', attrs: { property: 'og:site_name', content: 'stream4j' } },
        { tag: 'meta', attrs: { name: 'twitter:card', content: 'summary_large_image' } },
        {
          tag: 'meta',
          attrs: {
            name: 'keywords',
            content:
              '치지직, 씨미, 숲, 아프리카TV, 아프리카티비, 유튜브, 투네이션, 마인크래프트 후원 연동, 디스코드 봇, OBS 오버레이, TTS, Java 스트리밍, 후원 알림, 채팅 통합, chzzk4j, stream4j',
          },
        },
        {
          tag: 'link',
          attrs: { rel: 'preconnect', href: 'https://cdn.jsdelivr.net' },
        },
        {
          tag: 'link',
          attrs: { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        },
        {
          tag: 'link',
          attrs: {
            rel: 'stylesheet',
            href: 'https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable.min.css',
            crossorigin: '',
          },
        },
        {
          tag: 'link',
          attrs: {
            rel: 'stylesheet',
            href: 'https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600&display=swap',
          },
        },
        {
          tag: 'script',
          attrs: { type: 'application/ld+json' },
          content: JSON.stringify({
            '@context': 'https://schema.org',
            '@type': 'SoftwareSourceCode',
            name: 'stream4j',
            description: META_DESC,
            programmingLanguage: 'Java',
            license: 'https://opensource.org/license/mit/',
            codeRepository: 'https://github.com/bbobbogi/stream4j',
            url: 'https://bbobbogi.github.io/stream4j/',
            inLanguage: 'ko-KR',
            keywords:
              '치지직, 씨미, 숲, 아프리카TV, 유튜브, 투네이션, 마인크래프트 후원, 디스코드 봇, OBS 오버레이, TTS',
          }),
        },
      ],
      sidebar: [
        {
          label: '시작하기',
          items: [
            { label: '소개', link: '/getting-started/' },
            { label: '설치 및 빠른 시작', link: '/quickstart/' },
          ],
        },
        {
          label: '통합 사용 가이드',
          items: [{ label: 'StreamChat 가이드', link: '/usage/' }],
        },
        {
          label: '플랫폼별 가이드',
          items: [
            { label: '치지직 (Chzzk)', link: '/platforms/chzzk/' },
            { label: '씨미 (CiMe)', link: '/platforms/cime/' },
            { label: '숲 (SOOP, 구 아프리카TV)', link: '/platforms/soop/' },
            { label: '유튜브 (YouTube)', link: '/platforms/youtube/' },
            { label: '투네이션 (Toonation)', link: '/platforms/toonation/' },
          ],
        },
        {
          label: '인증',
          items: [{ label: '인증 가이드', link: '/auth/' }],
        },
      ],
    }),
  ],
});
