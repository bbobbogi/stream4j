import{j as s}from"./jsx-runtime.u17CrQMm.js";import{r as d}from"./index.D9mrT8mP.js";const w=/\b(new|public|void|return|import|package|String|int|System|Override|implements|extends|null|true|false|throws|throw|try|catch|final|static|private|protected|val|var|object|fun|override)\b/g,h=n=>n.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");function f(n){if(/^\s*<\?xml|<[a-zA-Z][\w-]*>/m.test(n)&&/<\/[a-zA-Z]/.test(n)){let a=h(n);return a=a.replace(/&lt;(\/?)([\w.-]+)(.*?)&gt;/g,(i,c,p,m)=>`<span class="tok-pun">&lt;${c}</span><span class="tok-cls">${p}</span>${m}<span class="tok-pun">&gt;</span>`),a}const l=[],o=(a,i)=>{const c=l.push(`<span class="${a}">${h(i)}</span>`)-1;return`${String.fromCharCode(57600+c)}`};let t=n;return t=t.replace(/"(?:[^"\\]|\\.)*"/g,a=>o("tok-str",a)),t=t.replace(/\/\/[^\n]*/g,a=>o("tok-cmt",a)),t=h(t),t=t.replace(/\b(\d+)\b/g,'<span class="tok-num">$1</span>'),t=t.replace(/@\w+/g,a=>`<span class="tok-key">${a}</span>`),t=t.replace(w,'<span class="tok-key">$1</span>'),t=t.replace(/\.([a-zA-Z_][a-zA-Z0-9_]*)(?=\()/g,'.<span class="tok-mtd">$1</span>'),t=t.replace(/\b([A-Z][a-zA-Z0-9_]+)\b/g,a=>a==="API"||a==="URL"||a==="ID"?a:`<span class="tok-cls">${a}</span>`),t=t.replace(/\uE000([\uE100-\uE9FF])\uE001/g,(a,i)=>l[i.charCodeAt(0)-57600]??""),t}const v=[{num:"01",label:"URL 추가",desc:"5개 플랫폼 URL을 Builder에 던져넣기",icon:"→ +"},{num:"02",label:"Listener 등록",desc:"onChat / onDonation 두 메서드 구현",icon:"↺"},{num:"03",label:"connectAll()",desc:"전부 동시에 WebSocket 연결, 끝",icon:"▶"}],g=`StreamChat chat = new StreamChatBuilder()
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

chat.connectAll();`,S=`val chat = StreamChatBuilder()
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
            println("[$platform] $nickname: $message")
        }
    })
    .build()

chat.connectAll()`,k=`// URL 대신 플랫폼 ID로도 등록 가능
StreamChat chat = new StreamChatBuilder()
        .add("924a636224c9203259af46ad7d8b70ca", DonationPlatform.CHZZK)
        .add("tjrdbs999",                         DonationPlatform.SOOP)
        .add("@lyn",                              DonationPlatform.CIME)
        .add("alertbox_key",                      DonationPlatform.TOONATION)
        .add("@jtbc_news",                        DonationPlatform.YOUTUBE)
        .withListener(listener)
        .build();

chat.connectAll();`,N={java:[{lines:[2,3,4,5,6],step:"01",text:"5개 URL — Builder가 자동으로 플랫폼 감지"},{lines:[7,8,9,10,11,12,13,14,15,16,17,18,19,20,21],step:"02",text:"단일 Listener로 모든 플랫폼 이벤트 처리"},{lines:[23],step:"03",text:"전체 연결을 동시에 시작 — 비동기 처리"}],kotlin:[{lines:[2,3,4,5,6],step:"01",text:"5개 URL — Builder가 자동으로 플랫폼 감지"},{lines:[7,8,9,10,11,12,13,14,15,16,17],step:"02",text:"Kotlin object — SAM 변환 없이 명시적 구현"},{lines:[19],step:"03",text:"동일 — Java/Kotlin 어디서든"}],rawid:[{lines:[3,4,5,6,7],step:"01",text:"ID + 플랫폼 enum 명시 — URL 파싱 우회"},{lines:[8],step:"02",text:"별도 객체로 분리해서 재사용"},{lines:[11],step:"03",text:"동일"}]},q=[{id:"java",label:"Java"},{id:"kotlin",label:"Kotlin"},{id:"rawid",label:"Raw ID + enum"}];function L(){const[n,j]=d.useState("java"),[l,o]=d.useState(null),[t,a]=d.useState(!1),i=n==="java"?g:n==="kotlin"?S:k,c=N[n],p=()=>{navigator.clipboard.writeText(i).then(()=>{a(!0),setTimeout(()=>a(!1),1400)}).catch(()=>{})},m=i.split(`
`),x=l?new Set(c.find(e=>e.step===l)?.lines??[]):new Set;return s.jsxs("div",{className:"s4j-qsflow",children:[s.jsx("div",{className:"s4j-qsflow-steps",children:v.map((e,r)=>s.jsxs(d.Fragment,{children:[s.jsxs("div",{className:"s4j-qs-step"+(l===e.num?" is-active":""),onMouseEnter:()=>o(e.num),onMouseLeave:()=>o(null),children:[s.jsx("div",{className:"s4j-qs-step-num",children:e.num}),s.jsx("div",{className:"s4j-qs-step-icon",children:e.icon}),s.jsxs("div",{className:"s4j-qs-step-body",children:[s.jsx("div",{className:"s4j-qs-step-label",children:e.label}),s.jsx("div",{className:"s4j-qs-step-desc",children:e.desc})]})]}),r<v.length-1&&s.jsxs("div",{className:"s4j-qs-step-arrow","aria-hidden":"true",children:[s.jsx("span",{className:"s4j-qs-arrow-line"}),s.jsx("span",{className:"s4j-qs-arrow-tip",children:"▶"})]})]},e.num))}),s.jsxs("div",{className:"s4j-qsflow-card",children:[s.jsxs("div",{className:"s4j-qsflow-head",children:[s.jsx("div",{className:"s4j-qsflow-tabs",children:q.map(e=>s.jsx("button",{className:"s4j-qsflow-tab"+(n===e.id?" is-active":""),onClick:()=>j(e.id),children:e.label},e.id))}),s.jsx("button",{className:"s4j-qsflow-copy",onClick:p,children:t?"✓ COPIED":"COPY"})]}),s.jsxs("div",{className:"s4j-qsflow-body",children:[s.jsx("div",{className:"s4j-qsflow-codewrap",children:s.jsx("pre",{className:"s4j-qsflow-code",children:s.jsx("code",{children:m.map((e,r)=>{const u=r+1,b=x.has(u);return s.jsxs("div",{className:"s4j-qs-line"+(b?" is-hl":""),children:[s.jsx("span",{className:"s4j-qs-lineno",children:String(u).padStart(2," ")}),s.jsx("span",{className:"s4j-qs-linecode",dangerouslySetInnerHTML:{__html:f(e)||"&nbsp;"}})]},r)})})})}),s.jsx("div",{className:"s4j-qsflow-anno",children:c.map(e=>s.jsxs("div",{className:"s4j-qs-anno"+(l===e.step?" is-active":""),onMouseEnter:()=>o(e.step),onMouseLeave:()=>o(null),children:[s.jsxs("div",{className:"s4j-qs-anno-pin",children:[s.jsx("span",{className:"s4j-qs-anno-num",children:e.step}),s.jsx("span",{className:"s4j-qs-anno-arrow",children:"←"})]}),s.jsxs("div",{className:"s4j-qs-anno-body",children:[s.jsxs("div",{className:"s4j-qs-anno-lines",children:["L",e.lines[0],e.lines.length>1?`–${e.lines[e.lines.length-1]}`:""]}),s.jsx("div",{className:"s4j-qs-anno-text",children:e.text})]})]},e.step))})]})]})]})}export{L as default};
