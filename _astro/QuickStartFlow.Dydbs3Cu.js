import{t as e}from"./react.CNfqC2zk.js";import{t}from"./jsx-runtime.Cqka64nY.js";var n=e(),r=/\b(new|public|void|return|import|package|String|int|System|Override|implements|extends|null|true|false|throws|throw|try|catch|final|static|private|protected|val|var|object|fun|override)\b/g,i=e=>e.replace(/&/g,`&amp;`).replace(/</g,`&lt;`).replace(/>/g,`&gt;`);function a(e){if(/^\s*<\?xml|<[a-zA-Z][\w-]*>/m.test(e)&&/<\/[a-zA-Z]/.test(e)){let t=i(e);return t=t.replace(/&lt;(\/?)([\w.-]+)(.*?)&gt;/g,(e,t,n,r)=>`<span class="tok-pun">&lt;${t}</span><span class="tok-cls">${n}</span>${r}<span class="tok-pun">&gt;</span>`),t}let t=[],n=(e,n)=>{let r=t.push(`<span class="${e}">${i(n)}</span>`)-1;return`\uE000${String.fromCharCode(57600+r)}\uE001`},a=e;return a=a.replace(/"(?:[^"\\]|\\.)*"/g,e=>n(`tok-str`,e)),a=a.replace(/\/\/[^\n]*/g,e=>n(`tok-cmt`,e)),a=i(a),a=a.replace(/\b(\d+)\b/g,`<span class="tok-num">$1</span>`),a=a.replace(/@\w+/g,e=>`<span class="tok-key">${e}</span>`),a=a.replace(r,`<span class="tok-key">$1</span>`),a=a.replace(/\.([a-zA-Z_][a-zA-Z0-9_]*)(?=\()/g,`.<span class="tok-mtd">$1</span>`),a=a.replace(/\b([A-Z][a-zA-Z0-9_]+)\b/g,e=>e===`API`||e===`URL`||e===`ID`?e:`<span class="tok-cls">${e}</span>`),a=a.replace(/\uE000([\uE100-\uE9FF])\uE001/g,(e,n)=>t[n.charCodeAt(0)-57600]??``),a}var o=t(),s=[{num:`01`,label:`URL 추가`,desc:`5개 플랫폼 URL을 Builder에 던져넣기`,icon:`→ +`},{num:`02`,label:`Listener 등록`,desc:`onChat / onDonation 두 메서드 구현`,icon:`↺`},{num:`03`,label:`connectAll()`,desc:`전부 동시에 WebSocket 연결, 끝`,icon:`▶`}],c=`StreamChat chat = new StreamChatBuilder()
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

chat.connectAll();`,l=`val chat = StreamChatBuilder()
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

chat.connectAll()`,u=`// URL 대신 플랫폼 ID로도 등록 가능
StreamChat chat = new StreamChatBuilder()
        .add("924a636224c9203259af46ad7d8b70ca", DonationPlatform.CHZZK)
        .add("tjrdbs999",                         DonationPlatform.SOOP)
        .add("@lyn",                              DonationPlatform.CIME)
        .add("alertbox_key",                      DonationPlatform.TOONATION)
        .add("@jtbc_news",                        DonationPlatform.YOUTUBE)
        .withListener(listener)
        .build();

chat.connectAll();`,d={java:[{lines:[2,3,4,5,6],step:`01`,text:`5개 URL — Builder가 자동으로 플랫폼 감지`},{lines:[7,8,9,10,11,12,13,14,15,16,17,18,19,20,21],step:`02`,text:`단일 Listener로 모든 플랫폼 이벤트 처리`},{lines:[23],step:`03`,text:`전체 연결을 동시에 시작 — 비동기 처리`}],kotlin:[{lines:[2,3,4,5,6],step:`01`,text:`5개 URL — Builder가 자동으로 플랫폼 감지`},{lines:[7,8,9,10,11,12,13,14,15,16,17],step:`02`,text:`Kotlin object — SAM 변환 없이 명시적 구현`},{lines:[19],step:`03`,text:`동일 — Java/Kotlin 어디서든`}],rawid:[{lines:[3,4,5,6,7],step:`01`,text:`ID + 플랫폼 enum 명시 — URL 파싱 우회`},{lines:[8],step:`02`,text:`별도 객체로 분리해서 재사용`},{lines:[11],step:`03`,text:`동일`}]},f=[{id:`java`,label:`Java`},{id:`kotlin`,label:`Kotlin`},{id:`rawid`,label:`Raw ID + enum`}];function p(){let[e,t]=(0,n.useState)(`java`),[r,i]=(0,n.useState)(null),[p,m]=(0,n.useState)(!1),h=e===`java`?c:e===`kotlin`?l:u,g=d[e],_=()=>{navigator.clipboard.writeText(h).then(()=>{m(!0),setTimeout(()=>m(!1),1400)}).catch(()=>{})},v=h.split(`
`),y=r?new Set(g.find(e=>e.step===r)?.lines??[]):new Set;return(0,o.jsxs)(`div`,{className:`s4j-qsflow`,children:[(0,o.jsx)(`div`,{className:`s4j-qsflow-steps`,children:s.map((e,t)=>(0,o.jsxs)(n.Fragment,{children:[(0,o.jsxs)(`div`,{className:`s4j-qs-step`+(r===e.num?` is-active`:``),onMouseEnter:()=>i(e.num),onMouseLeave:()=>i(null),children:[(0,o.jsx)(`div`,{className:`s4j-qs-step-num`,children:e.num}),(0,o.jsx)(`div`,{className:`s4j-qs-step-icon`,children:e.icon}),(0,o.jsxs)(`div`,{className:`s4j-qs-step-body`,children:[(0,o.jsx)(`div`,{className:`s4j-qs-step-label`,children:e.label}),(0,o.jsx)(`div`,{className:`s4j-qs-step-desc`,children:e.desc})]})]}),t<s.length-1&&(0,o.jsxs)(`div`,{className:`s4j-qs-step-arrow`,"aria-hidden":`true`,children:[(0,o.jsx)(`span`,{className:`s4j-qs-arrow-line`}),(0,o.jsx)(`span`,{className:`s4j-qs-arrow-tip`,children:`▶`})]})]},e.num))}),(0,o.jsxs)(`div`,{className:`s4j-qsflow-card`,children:[(0,o.jsxs)(`div`,{className:`s4j-qsflow-head`,children:[(0,o.jsx)(`div`,{className:`s4j-qsflow-tabs`,children:f.map(n=>(0,o.jsx)(`button`,{className:`s4j-qsflow-tab`+(e===n.id?` is-active`:``),onClick:()=>t(n.id),children:n.label},n.id))}),(0,o.jsx)(`button`,{className:`s4j-qsflow-copy`,onClick:_,children:p?`✓ COPIED`:`COPY`})]}),(0,o.jsxs)(`div`,{className:`s4j-qsflow-body`,children:[(0,o.jsx)(`div`,{className:`s4j-qsflow-codewrap`,children:(0,o.jsx)(`pre`,{className:`s4j-qsflow-code`,children:(0,o.jsx)(`code`,{children:v.map((e,t)=>{let n=t+1;return(0,o.jsxs)(`div`,{className:`s4j-qs-line`+(y.has(n)?` is-hl`:``),children:[(0,o.jsx)(`span`,{className:`s4j-qs-lineno`,children:String(n).padStart(2,` `)}),(0,o.jsx)(`span`,{className:`s4j-qs-linecode`,dangerouslySetInnerHTML:{__html:a(e)||`&nbsp;`}})]},t)})})})}),(0,o.jsx)(`div`,{className:`s4j-qsflow-anno`,children:g.map(e=>(0,o.jsxs)(`div`,{className:`s4j-qs-anno`+(r===e.step?` is-active`:``),onMouseEnter:()=>i(e.step),onMouseLeave:()=>i(null),children:[(0,o.jsxs)(`div`,{className:`s4j-qs-anno-pin`,children:[(0,o.jsx)(`span`,{className:`s4j-qs-anno-num`,children:e.step}),(0,o.jsx)(`span`,{className:`s4j-qs-anno-arrow`,children:`←`})]}),(0,o.jsxs)(`div`,{className:`s4j-qs-anno-body`,children:[(0,o.jsxs)(`div`,{className:`s4j-qs-anno-lines`,children:[`L`,e.lines[0],e.lines.length>1?`–${e.lines[e.lines.length-1]}`:``]}),(0,o.jsx)(`div`,{className:`s4j-qs-anno-text`,children:e.text})]})]},e.step))})]})]})]})}export{p as default};