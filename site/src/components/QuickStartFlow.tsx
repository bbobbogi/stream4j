import { Fragment, useState } from 'react';
import { highlightJava } from '../lib/highlightJava';

type TabId = 'java' | 'kotlin' | 'rawid';

interface Annotation {
  lines: number[];
  step: string;
  text: string;
}

const QS_STEPS = [
  { num: '01', label: 'URL 추가', desc: '5개 플랫폼 URL을 Builder에 던져넣기', icon: '→ +' },
  { num: '02', label: 'Listener 등록', desc: 'onChat / onDonation 두 메서드 구현', icon: '↺' },
  { num: '03', label: 'connectAll()', desc: '전부 동시에 WebSocket 연결, 끝', icon: '▶' },
] as const;

const QS_JAVA = `StreamChat chat = new StreamChatBuilder()
        .add("https://chzzk.naver.com/live/924a636224c9203259af46ad7d8b70ca")
        .add("https://ci.me/@lyn")
        .add("https://play.sooplive.co.kr/tjrdbs999/292536969")
        .add("https://www.youtube.com/watch?v=Qv6o6WACJ60")
        .add("https://toon.at/widget/alertbox/abc123")
        .withListener(new StreamChatEventListener() {
            @Override
            public void onDonation(Donation donation) {
                System.out.println("[" + donation.platform() + "] "
                        + donation.nickname() + ": "
                        + donation.formattedAmount());
            }

            @Override
            public void onChat(DonationPlatform platform, String channelId,
                               String nickname, String message) {
                System.out.println("[" + platform + "] " + nickname + ": " + message);
            }
        })
        .build();

chat.connectAll();`;

const QS_KOTLIN = `val chat = StreamChatBuilder()
    .add("https://chzzk.naver.com/live/924a636224c9203259af46ad7d8b70ca")
    .add("https://ci.me/@lyn")
    .add("https://play.sooplive.co.kr/tjrdbs999/292536969")
    .add("https://www.youtube.com/watch?v=Qv6o6WACJ60")
    .add("https://toon.at/widget/alertbox/abc123")
    .withListener(object : StreamChatEventListener {
        override fun onDonation(d: Donation) {
            println("[\${d.platform()}] \${d.nickname()}: \${d.formattedAmount()}")
        }

        override fun onChat(platform: DonationPlatform, channelId: String,
                            nickname: String, message: String) {
            println("[\$platform] \$nickname: \$message")
        }
    })
    .build()

chat.connectAll()`;

const QS_RAWID = `// URL 대신 플랫폼 ID로도 등록 가능
StreamChat chat = new StreamChatBuilder()
        .add("924a636224c9203259af46ad7d8b70ca", DonationPlatform.CHZZK)
        .add("tjrdbs999",                         DonationPlatform.SOOP)
        .add("@lyn",                              DonationPlatform.CIME)
        .add("alertbox_key",                      DonationPlatform.TOONATION)
        .add("@jtbc_news",                        DonationPlatform.YOUTUBE)
        .withListener(listener)
        .build();

chat.connectAll();`;

const QS_ANNOTATIONS: Record<TabId, Annotation[]> = {
  java: [
    { lines: [2, 3, 4, 5, 6], step: '01', text: '5개 URL — Builder가 자동으로 플랫폼 감지' },
    {
      lines: [7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21],
      step: '02',
      text: '단일 Listener로 모든 플랫폼 이벤트 처리',
    },
    { lines: [23], step: '03', text: '전체 연결을 동시에 시작 — 비동기 처리' },
  ],
  kotlin: [
    { lines: [2, 3, 4, 5, 6], step: '01', text: '5개 URL — Builder가 자동으로 플랫폼 감지' },
    {
      lines: [7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17],
      step: '02',
      text: 'Kotlin object — SAM 변환 없이 명시적 구현',
    },
    { lines: [19], step: '03', text: '동일 — Java/Kotlin 어디서든' },
  ],
  rawid: [
    { lines: [3, 4, 5, 6, 7], step: '01', text: 'ID + 플랫폼 enum 명시 — URL 파싱 우회' },
    { lines: [8], step: '02', text: '별도 객체로 분리해서 재사용' },
    { lines: [11], step: '03', text: '동일' },
  ],
};

const TABS: Array<{ id: TabId; label: string }> = [
  { id: 'java', label: 'Java' },
  { id: 'kotlin', label: 'Kotlin' },
  { id: 'rawid', label: 'Raw ID + enum' },
];

export default function QuickStartFlow() {
  const [tab, setTab] = useState<TabId>('java');
  const [hoverStep, setHoverStep] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  const code = tab === 'java' ? QS_JAVA : tab === 'kotlin' ? QS_KOTLIN : QS_RAWID;
  const annotations = QS_ANNOTATIONS[tab];

  const onCopy = () => {
    navigator.clipboard
      .writeText(code)
      .then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1400);
      })
      .catch(() => {});
  };

  const lines = code.split('\n');
  const highlightLines = hoverStep
    ? new Set(annotations.find((a) => a.step === hoverStep)?.lines ?? [])
    : new Set<number>();

  return (
    <div className="s4j-qsflow">
      <div className="s4j-qsflow-steps">
        {QS_STEPS.map((s, i) => (
          <Fragment key={s.num}>
            <div
              className={'s4j-qs-step' + (hoverStep === s.num ? ' is-active' : '')}
              onMouseEnter={() => setHoverStep(s.num)}
              onMouseLeave={() => setHoverStep(null)}
            >
              <div className="s4j-qs-step-num">{s.num}</div>
              <div className="s4j-qs-step-icon">{s.icon}</div>
              <div className="s4j-qs-step-body">
                <div className="s4j-qs-step-label">{s.label}</div>
                <div className="s4j-qs-step-desc">{s.desc}</div>
              </div>
            </div>
            {i < QS_STEPS.length - 1 && (
              <div className="s4j-qs-step-arrow" aria-hidden="true">
                <span className="s4j-qs-arrow-line"></span>
                <span className="s4j-qs-arrow-tip">▶</span>
              </div>
            )}
          </Fragment>
        ))}
      </div>

      <div className="s4j-qsflow-card">
        <div className="s4j-qsflow-head">
          <div className="s4j-qsflow-tabs">
            {TABS.map((t) => (
              <button
                key={t.id}
                className={'s4j-qsflow-tab' + (tab === t.id ? ' is-active' : '')}
                onClick={() => setTab(t.id)}
              >
                {t.label}
              </button>
            ))}
          </div>
          <button className="s4j-qsflow-copy" onClick={onCopy}>
            {copied ? '✓ COPIED' : 'COPY'}
          </button>
        </div>

        <div className="s4j-qsflow-body">
          <div className="s4j-qsflow-codewrap">
            <pre className="s4j-qsflow-code">
              <code>
                {lines.map((line, i) => {
                  const ln = i + 1;
                  const isHl = highlightLines.has(ln);
                  return (
                    <div
                      key={i}
                      className={'s4j-qs-line' + (isHl ? ' is-hl' : '')}
                    >
                      <span className="s4j-qs-lineno">{String(ln).padStart(2, ' ')}</span>
                      <span
                        className="s4j-qs-linecode"
                        dangerouslySetInnerHTML={{
                          __html: highlightJava(line) || '&nbsp;',
                        }}
                      />
                    </div>
                  );
                })}
              </code>
            </pre>
          </div>

          <div className="s4j-qsflow-anno">
            {annotations.map((a) => (
              <div
                key={a.step}
                className={'s4j-qs-anno' + (hoverStep === a.step ? ' is-active' : '')}
                onMouseEnter={() => setHoverStep(a.step)}
                onMouseLeave={() => setHoverStep(null)}
              >
                <div className="s4j-qs-anno-pin">
                  <span className="s4j-qs-anno-num">{a.step}</span>
                  <span className="s4j-qs-anno-arrow">←</span>
                </div>
                <div className="s4j-qs-anno-body">
                  <div className="s4j-qs-anno-lines">
                    L{a.lines[0]}
                    {a.lines.length > 1 ? `–${a.lines[a.lines.length - 1]}` : ''}
                  </div>
                  <div className="s4j-qs-anno-text">{a.text}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
