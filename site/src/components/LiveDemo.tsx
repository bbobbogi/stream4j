import { useEffect, useRef, useState } from 'react';

type EventItem =
  | { id: number; type: 'CHAT'; platform: string; nick: string; msg: string }
  | {
      id: number;
      type: 'DONATION';
      platform: string;
      nick: string;
      msg: string;
      amount: string;
    };

const NICKS = [
  '코딩하는방울',
  '야경러버',
  'minki_dev',
  '치즈러버99',
  '별빛스트리머',
  'lyn_official',
  '삼겹살왕',
  'Soup_Fan',
  'udon_san',
  '코드몽키',
  '라이브러리덕후',
  '옆집개발자',
];

const MESSAGES_CHAT = [
  'ㅋㅋㅋㅋㅋㅋ 미쳤다',
  '오늘도 잘 보고 갑니다',
  '방송 너무 재밌어요',
  '이게 그 라이브러리인가요?',
  '통합 채팅 진짜 편해보임',
  '5개 플랫폼 동시에 ㄷㄷ',
  'Java 11 이상이면 다 됨',
  '재연결 자동이라니 굿',
  '빌더 패턴 깔끔함',
  '후원 모델 통일된거 ㄹㅈㄷ',
];

const MESSAGES_DON = [
  '오늘 방송도 화이팅!',
  '구독 1년차 기념 ❤',
  '라이브러리 잘 쓰고 있어요',
  '응원합니다',
  '저녁 식사 보태주세요',
  '100만 가즈아',
];

const PLATFORMS = ['CHZZK', 'CIME', 'SOOP', 'YOUTUBE', 'TOONATION'] as const;

type PlatformKey = (typeof PLATFORMS)[number];

const CURRENCIES: Record<
  PlatformKey,
  { unit: string; rate: number; prefix?: boolean }
> = {
  CHZZK: { unit: '치즈', rate: 1 },
  SOOP: { unit: '별풍선', rate: 100 },
  YOUTUBE: { unit: '₩', rate: 1, prefix: true },
  CIME: { unit: '빔', rate: 10 },
  TOONATION: { unit: '₩', rate: 1, prefix: true },
};

function pick<T>(arr: readonly T[]): T {
  const idx = Math.floor(Math.random() * arr.length);
  return arr[idx]!;
}

function makeEvent(id: number): EventItem {
  const isDonation = Math.random() < 0.32;
  const platform = pick(PLATFORMS);
  const finalPf: PlatformKey =
    !isDonation && platform === 'TOONATION' ? 'CHZZK' : platform;
  if (isDonation) {
    const cur = CURRENCIES[finalPf];
    const baseAmounts = [1000, 2000, 5000, 10000, 500, 3000] as const;
    const baseAmount = pick(baseAmounts);
    const display = cur.prefix
      ? `${cur.unit}${baseAmount.toLocaleString()}`
      : `${(baseAmount / cur.rate).toLocaleString()}${cur.unit}`;
    return {
      id,
      platform: finalPf,
      type: 'DONATION',
      nick: pick(NICKS),
      msg: pick(MESSAGES_DON),
      amount: display,
    };
  }
  return {
    id,
    platform: finalPf,
    type: 'CHAT',
    nick: pick(NICKS),
    msg: pick(MESSAGES_CHAT),
  };
}

export default function LiveDemo() {
  const [events, setEvents] = useState<EventItem[]>(() =>
    Array.from({ length: 6 }, (_, i) => makeEvent(i)),
  );
  const idRef = useRef(6);
  const pausedRef = useRef(false);
  const [paused, setPaused] = useState(false);

  useEffect(() => {
    let tid: ReturnType<typeof setTimeout> | undefined;
    const tick = () => {
      const delay = 700 + Math.random() * 1400;
      tid = setTimeout(() => {
        if (!pausedRef.current) {
          setEvents((prev) => {
            const next = [makeEvent(idRef.current++), ...prev];
            return next.slice(0, 9);
          });
        }
        tick();
      }, delay);
    };
    tick();
    return () => {
      if (tid) clearTimeout(tid);
    };
  }, []);

  const togglePause = () => {
    pausedRef.current = !pausedRef.current;
    setPaused(pausedRef.current);
  };

  return (
    <div className="s4j-demo">
      <div className="s4j-demo-side">
        <div className="s4j-demo-h">// INPUT — URLs</div>
        <div className="s4j-demo-input">
          <span className="pl">CHZZK</span>
          <span>chzzk.naver.com/live/924a...</span>
        </div>
        <div className="s4j-demo-input">
          <span className="pl">CIME</span>
          <span>ci.me/@lyn</span>
        </div>
        <div className="s4j-demo-input">
          <span className="pl">SOOP</span>
          <span>play.sooplive.co.kr/tjrdbs999</span>
        </div>
        <div className="s4j-demo-input">
          <span className="pl">YOUTUBE</span>
          <span>youtube.com/watch?v=Qv6o...</span>
        </div>
        <div className="s4j-demo-input">
          <span className="pl">TOONATION</span>
          <span>toon.at/widget/alertbox/abc</span>
        </div>
        <div className="s4j-demo-arrow">▼ &nbsp; STREAM4J &nbsp; ▼</div>
        <div className="s4j-demo-h" style={{ marginBottom: 0 }}>
          // OUTPUT — unified events
        </div>
      </div>
      <div className="s4j-demo-stream">
        <div className="s4j-stream-bar">
          <span className="live">
            <span className="dot"></span>LIVE — onChat() / onDonation()
          </span>
          <button onClick={togglePause}>{paused ? '▶ RESUME' : '❚❚ PAUSE'}</button>
        </div>
        <div className="s4j-stream-list">
          {events.map((e) => (
            <div
              key={e.id}
              className="s4j-stream-event"
              data-pf={e.platform}
              data-type={e.type}
            >
              <div className="pf">[{e.platform}]</div>
              <div className="body">
                <span className="nick">{e.nick}:</span>
                <span>{e.msg}</span>
              </div>
              <div className="amt">{e.type === 'DONATION' ? e.amount : ''}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
