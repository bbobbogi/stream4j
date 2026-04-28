const KEYWORDS =
  /\b(new|public|void|return|import|package|String|int|System|Override|implements|extends|null|true|false|throws|throw|try|catch|final|static|private|protected|val|var|object|fun|override)\b/g;

const escape = (s: string): string =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

export function highlightJava(code: string): string {
  const looksLikeXml =
    /^\s*<\?xml|<[a-zA-Z][\w-]*>/m.test(code) && /<\/[a-zA-Z]/.test(code);

  if (looksLikeXml) {
    let h = escape(code);
    h = h.replace(
      /&lt;(\/?)([\w.-]+)(.*?)&gt;/g,
      (_match: string, slash: string, name: string, rest: string) =>
        `<span class="tok-pun">&lt;${slash}</span><span class="tok-cls">${name}</span>${rest}<span class="tok-pun">&gt;</span>`,
    );
    return h;
  }

  const protectedTokens: string[] = [];
  const stub = (cls: string, text: string): string => {
    const idx =
      protectedTokens.push(`<span class="${cls}">${escape(text)}</span>`) - 1;
    return `\uE000${String.fromCharCode(0xe100 + idx)}\uE001`;
  };

  let working = code;
  working = working.replace(/"(?:[^"\\]|\\.)*"/g, (m: string) => stub('tok-str', m));
  working = working.replace(/\/\/[^\n]*/g, (m: string) => stub('tok-cmt', m));

  working = escape(working);

  working = working.replace(/\b(\d+)\b/g, '<span class="tok-num">$1</span>');
  working = working.replace(/@\w+/g, (m: string) => `<span class="tok-key">${m}</span>`);
  working = working.replace(KEYWORDS, '<span class="tok-key">$1</span>');
  working = working.replace(
    /\.([a-zA-Z_][a-zA-Z0-9_]*)(?=\()/g,
    '.<span class="tok-mtd">$1</span>',
  );
  working = working.replace(/\b([A-Z][a-zA-Z0-9_]+)\b/g, (m: string) => {
    if (m === 'API' || m === 'URL' || m === 'ID') return m;
    return `<span class="tok-cls">${m}</span>`;
  });

  working = working.replace(/\uE000([\uE100-\uE9FF])\uE001/g, (_match: string, ch: string) => {
    return protectedTokens[ch.charCodeAt(0) - 0xe100] ?? '';
  });
  return working;
}
