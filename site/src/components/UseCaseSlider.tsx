import { useState, type KeyboardEvent } from 'react';

type VisualKind =
  | 'viewer'
  | 'alert'
  | 'chart'
  | 'minecraft'
  | 'discord'
  | 'overlay'
  | 'tts';

interface UseCase {
  tag: string;
  title: string;
  desc: string;
  visual: VisualKind;
}

const ITEMS: UseCase[] = [
  {
    tag: 'VIEWER',
    title: '통합 채팅 뷰어',
    desc: '여러 플랫폼의 채팅을 한 화면에 모아보는 데스크탑/웹 뷰어',
    visual: 'viewer',
  },
  {
    tag: 'BOT',
    title: '멀티플랫폼 후원 알림 봇',
    desc: '치즈·별풍선·슈퍼챗·빔 후원을 단일 알림으로',
    visual: 'alert',
  },
  {
    tag: 'DASHBOARD',
    title: '방송 관리 대시보드',
    desc: '채팅/후원 데이터를 수집·분석하는 운영 도구',
    visual: 'chart',
  },
  {
    tag: 'GAME',
    title: '마인크래프트 서버 연동',
    desc: '후원 시 보스 소환·아이템 지급·이펙트 발동 자동 트리거',
    visual: 'minecraft',
  },
  {
    tag: 'DISCORD',
    title: '디스코드 봇 연동',
    desc: '치지직·숲·유튜브 채팅과 후원을 디스코드 채널로 실시간 중계',
    visual: 'discord',
  },
  {
    tag: 'OBS',
    title: 'OBS 오버레이 연동',
    desc: '후원·구독·미션 알림을 방송 화면 커스텀 오버레이로 표시',
    visual: 'overlay',
  },
  {
    tag: 'TTS',
    title: 'TTS 음성 알림',
    desc: '후원 메시지를 자동 음성 변환해 방송 중 재생',
    visual: 'tts',
  },
];

function ViewerVis() {
  const rows = [
    { p: 'CHZZK', n: '코드몽키', m: 'ㅋㅋㅋ' },
    { p: 'SOOP', n: '라이브덕후', m: '오늘 화이팅' },
    { p: 'YOUTUBE', n: 'minki_dev', m: '굿굿' },
    { p: 'CIME', n: 'lyn_fan', m: '사랑해요' },
  ];
  return (
    <div className="s4j-vis s4j-vis-viewer">
      {rows.map((r, i) => (
        <div key={i} className="s4j-vis-row" data-pf={r.p}>
          <span className="s4j-vis-pf">[{r.p}]</span>
          <span className="s4j-vis-nick">{r.n}</span>
          <span className="s4j-vis-msg">{r.m}</span>
        </div>
      ))}
    </div>
  );
}

function AlertVis() {
  return (
    <div className="s4j-vis s4j-vis-alert">
      <div className="s4j-alert-card">
        <div className="s4j-alert-amt">₩10,000</div>
        <div className="s4j-alert-meta">
          <span>치즈러버99</span>
          <span className="s4j-alert-pf">CHZZK</span>
        </div>
        <div className="s4j-alert-msg">"오늘 방송 화이팅!"</div>
      </div>
    </div>
  );
}

function ChartVis() {
  const bars = [40, 65, 30, 80, 55, 90, 70, 45, 85, 60, 95, 50];
  return (
    <div className="s4j-vis s4j-vis-chart">
      <div className="s4j-chart-head">
        <span>일별 후원 (KRW)</span>
        <span className="s4j-chart-num">+24.3%</span>
      </div>
      <div className="s4j-chart-bars">
        {bars.map((h, i) => (
          <div key={i} className="bar" style={{ height: `${h}%` }} />
        ))}
      </div>
    </div>
  );
}

function MinecraftVis() {
  return (
    <div className="s4j-vis s4j-vis-mc">
      <div className="s4j-mc-grid">
        {Array.from({ length: 48 }).map((_, i) => (
          <div key={i} className={`s4j-mc-px s4j-mc-px-${i % 4}`} />
        ))}
      </div>
      <div className="s4j-mc-overlay">
        <div className="s4j-mc-line">› onDonation(5000원)</div>
        <div className="s4j-mc-line s4j-mc-cmd">/summon ender_dragon</div>
        <div className="s4j-mc-line s4j-mc-ok">✓ executed</div>
      </div>
    </div>
  );
}

function DiscordVis() {
  const msgs = [
    { p: 'CHZZK', n: '코딩방울', m: '오늘 방송 시작!' },
    { p: 'YOUTUBE', n: 'udon_san', m: '👋 hello' },
    { p: 'SOOP', n: 'Soup_Fan', m: 'ㅋㅋㅋㅋ' },
  ];
  return (
    <div className="s4j-vis s4j-vis-discord">
      <div className="s4j-dc-channel"># stream-relay</div>
      {msgs.map((r, i) => (
        <div key={i} className="s4j-dc-msg">
          <div className="s4j-dc-av" data-pf={r.p}></div>
          <div>
            <div className="s4j-dc-nick">
              {r.n} <span className="s4j-dc-tag">{r.p}</span>
            </div>
            <div className="s4j-dc-text">{r.m}</div>
          </div>
        </div>
      ))}
    </div>
  );
}

function OverlayVis() {
  return (
    <div className="s4j-vis s4j-vis-overlay">
      <div className="s4j-ov-screen">
        <div className="s4j-ov-placeholder">방송 화면</div>
        <div className="s4j-ov-alert">
          <div className="s4j-ov-amt">★ 50,000원</div>
          <div className="s4j-ov-from">별빛스트리머 님의 후원</div>
        </div>
      </div>
    </div>
  );
}

function TtsVis() {
  return (
    <div className="s4j-vis s4j-vis-tts">
      <div className="s4j-tts-quote">"방송 너무 재밌어요"</div>
      <div className="s4j-tts-wave">
        {Array.from({ length: 28 }).map((_, i) => (
          <span
            key={i}
            style={{
              height: `${20 + Math.abs(Math.sin(i * 0.6)) * 80}%`,
              animationDelay: `${i * 60}ms`,
            }}
          />
        ))}
      </div>
      <div className="s4j-tts-meta">▶ playing · 0:03 / 0:05</div>
    </div>
  );
}

function Visual({ kind }: { kind: VisualKind }) {
  switch (kind) {
    case 'viewer':
      return <ViewerVis />;
    case 'alert':
      return <AlertVis />;
    case 'chart':
      return <ChartVis />;
    case 'minecraft':
      return <MinecraftVis />;
    case 'discord':
      return <DiscordVis />;
    case 'overlay':
      return <OverlayVis />;
    case 'tts':
      return <TtsVis />;
  }
}

export default function UseCaseSlider() {
  const [idx, setIdx] = useState(0);
  const total = ITEMS.length;

  const go = (n: number) => {
    const next = (n + total) % total;
    setIdx(next);
  };

  const onKey = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'ArrowRight') {
      e.preventDefault();
      go(idx + 1);
    }
    if (e.key === 'ArrowLeft') {
      e.preventDefault();
      go(idx - 1);
    }
  };

  return (
    <div
      className="s4j-uslide"
      tabIndex={0}
      onKeyDown={onKey}
      role="region"
      aria-label="사용 사례"
    >
      <div className="s4j-uslide-frame">
        <div
          className="s4j-uslide-track"
          style={{ transform: `translateX(-${idx * 100}%)` }}
        >
          {ITEMS.map((u, i) => (
            <div key={u.title} className="s4j-uslide-card" aria-hidden={i !== idx}>
              <div className="s4j-uslide-stage">
                <div style={{ position: 'relative', zIndex: 2, width: '100%', maxWidth: 420 }}>
                  <Visual kind={u.visual} />
                </div>
              </div>
              <div className="s4j-uslide-side">
                <div className="s4j-uslide-tag">
                  {u.tag} / {String(i + 1).padStart(2, '0')}
                </div>
                <h3 className="s4j-uslide-title">{u.title}</h3>
                <p className="s4j-uslide-desc">{u.desc}</p>
                <div className="s4j-uslide-meta">
                  <span>SCENE</span>
                  <span className="s4j-uslide-meta-v">
                    {String(i + 1).padStart(2, '0')} / {String(total).padStart(2, '0')}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="s4j-uslide-nav">
        <button
          className="s4j-uslide-arrow"
          onClick={() => go(idx - 1)}
          aria-label="이전"
        >
          ←
        </button>
        <div className="s4j-uslide-dots">
          {ITEMS.map((u, i) => (
            <button
              key={i}
              className={'s4j-uslide-dot' + (i === idx ? ' is-active' : '')}
              onClick={() => go(i)}
              aria-label={u.title}
            >
              <span className="s4j-uslide-dot-num">{String(i + 1).padStart(2, '0')}</span>
              <span className="s4j-uslide-dot-name">{u.tag}</span>
            </button>
          ))}
        </div>
        <button
          className="s4j-uslide-arrow"
          onClick={() => go(idx + 1)}
          aria-label="다음"
        >
          →
        </button>
      </div>
    </div>
  );
}
