package com.workbenchlens;

final class WebPage {
    private WebPage() { }

    static final String HTML = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <meta name="theme-color" content="#f1f1f1">
              <title>Workbench Lens</title>
              <style>
                :root {
                  color-scheme: light;
                  --ink: #151515;
                  --muted: #666;
                  --line: #d8d8d8;
                  --paper: #f1f1f1;
                  --card: #fff;
                  --accent: #242424;
                  --soft: #ededed;
                  --shadow: 0 18px 60px #0000000c;
                }

                body[data-lens="kitchen"] {
                  --accent: #735b3e;
                  --soft: #f2ede6;
                }

                body[data-lens="garden"] {
                  --accent: #426b47;
                  --soft: #e9f1e8;
                }

                * { box-sizing: border-box; }

                body {
                  margin: 0;
                  background: var(--paper);
                  color: var(--ink);
                  font: 15px/1.55 Inter, ui-sans-serif, system-ui,
                        -apple-system, "Segoe UI", sans-serif;
                  transition: background .25s;
                }

                button, input, textarea { font: inherit; }
                button { color: inherit; }

                .shell {
                  max-width: 1440px;
                  min-height: 100vh;
                  margin: auto;
                  padding: 0 44px;
                }

                .topbar {
                  height: 76px;
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  border-bottom: 1px solid var(--line);
                }

                .brand {
                  display: flex;
                  align-items: center;
                  gap: 12px;
                  font-size: 15px;
                  font-weight: 750;
                  letter-spacing: -.02em;
                }

                .mark {
                  width: 36px;
                  height: 36px;
                  display: grid;
                  place-items: center;
                  border-radius: 11px;
                  background: var(--accent);
                  color: white;
                  transition: background .25s;
                }

                .mark svg { width: 20px; }

                .top-right {
                  display: flex;
                  align-items: center;
                  gap: 20px;
                  color: var(--muted);
                  font-size: 12px;
                }

                .connection {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                }

                .dot {
                  width: 8px;
                  height: 8px;
                  border-radius: 50%;
                  background: #aaa;
                }

                .hero {
                  max-width: 780px;
                  padding: 48px 0 30px;
                }

                .eyebrow {
                  display: flex;
                  align-items: center;
                  gap: 9px;
                  color: var(--accent);
                  font-size: 11px;
                  font-weight: 750;
                  letter-spacing: .13em;
                  text-transform: uppercase;
                }

                .eyebrow::before {
                  width: 23px;
                  height: 1px;
                  background: currentColor;
                  content: "";
                }

                h1 {
                  margin: 15px 0 12px;
                  font-size: clamp(36px, 5vw, 58px);
                  line-height: 1.03;
                  letter-spacing: -.06em;
                  white-space: pre-line;
                }

                .hero p {
                  max-width: 620px;
                  margin: 0;
                  color: var(--muted);
                  font-size: 16px;
                }

                .card {
                  border: 1px solid var(--line);
                  border-radius: 18px;
                  background: var(--card);
                  box-shadow: var(--shadow);
                }

                .auth-card {
                  max-width: 680px;
                  margin-bottom: 25px;
                  padding: 28px;
                  border-color: #d3d3d3;
                  background: #fff;
                  box-shadow: 0 24px 72px #00000012;
                }

                .auth-card h2 {
                  margin: 0;
                  font-size: 21px;
                  letter-spacing: -.035em;
                }

                .auth-card > p {
                  margin: 6px 0 0;
                  color: var(--muted);
                  font-size: 13px;
                }

                .auth-toggle {
                  display: flex;
                  gap: 9px;
                  margin: 18px 0;
                }

                .auth-card label,
                .label {
                  display: block;
                  margin: 17px 0 7px;
                  font-size: 12px;
                  font-weight: 700;
                }

                .auth-card input,
                .password-input {
                  width: 100%;
                  height: 46px;
                  padding: 0 13px;
                  border: 1px solid #d7d7d7;
                  border-radius: 10px;
                  outline: none;
                  background: #fafafa;
                  color: #151515;
                }

                input:focus, textarea:focus {
                  border-color: #555;
                  box-shadow: 0 0 0 3px #0000000d;
                }

                .btn {
                  min-height: 43px;
                  padding: 0 16px;
                  border: 1px solid #d5d5d5;
                  border-radius: 10px;
                  background: #f6f6f6;
                  color: #171717;
                  font-weight: 650;
                  cursor: pointer;
                  transition: transform .15s, background .15s;
                }

                .btn:hover { background: #eaeaea; }
                .btn:active { transform: scale(.98); }

                .btn-dark {
                  border-color: #202020;
                  background: #202020;
                  color: white;
                }

                .btn-dark:hover { background: #3a3a3a; }
                .auth-submit { margin-top: 15px; }

                .status {
                  min-height: 20px;
                  margin-top: 9px;
                  color: var(--muted);
                  font-size: 12px;
                  white-space: pre-wrap;
                }

                .status.error { color: #a53728; }
                [hidden] { display: none !important; }

                .lens-switch {
                  display: flex;
                  gap: 8px;
                  overflow-x: auto;
                  margin: 0 0 18px;
                  padding: 5px;
                  border: 1px solid var(--line);
                  border-radius: 15px;
                  background: #fff;
                  box-shadow: var(--shadow);
                }

                .lens-switch button {
                  flex: 1;
                  min-width: max-content;
                  padding: 12px 18px;
                  border: 0;
                  border-radius: 11px;
                  background: transparent;
                  color: #666;
                  font-weight: 700;
                  cursor: pointer;
                  transition: background .2s, color .2s;
                }

                .lens-switch button[aria-pressed="true"] {
                  background: var(--accent);
                  color: #fff;
                }

                .lens-switch button:disabled {
                  opacity: .5;
                  cursor: wait;
                }

                .workspace {
                  display: grid;
                  grid-template-columns: minmax(0, 1.7fr)
                                         minmax(290px, .82fr);
                  gap: 20px;
                  padding-bottom: 45px;
                }

                .main-card { padding: 24px; }

                .card-head {
                  display: flex;
                  align-items: flex-start;
                  justify-content: space-between;
                  gap: 15px;
                }

                .card-title {
                  margin: 0;
                  font-size: 19px;
                  letter-spacing: -.03em;
                }

                .card-desc {
                  margin: 5px 0 0;
                  color: var(--muted);
                  font-size: 13px;
                }

                .step {
                  padding: 5px 10px;
                  border-radius: 99px;
                  background: var(--soft);
                  color: var(--accent);
                  font-size: 10px;
                  font-weight: 750;
                  white-space: nowrap;
                }

                .upload {
                  position: relative;
                  min-height: 220px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  overflow: hidden;
                  margin-top: 21px;
                  border: 1px dashed #bdbdbd;
                  border-radius: 14px;
                  background: #f8f8f8;
                  text-align: center;
                  transition: background .2s, border-color .2s;
                }

                .upload.drag {
                  border-color: var(--accent);
                  background: var(--soft);
                }

                .upload input {
                  position: absolute;
                  inset: 0;
                  width: 100%;
                  height: 100%;
                  opacity: 0;
                  cursor: pointer;
                }

                .upload-inner {
                  padding: 24px;
                  pointer-events: none;
                }

                .upload-icon {
                  width: 44px;
                  height: 44px;
                  display: grid;
                  place-items: center;
                  margin: 0 auto 12px;
                  border: 1px solid var(--line);
                  border-radius: 13px;
                  background: #fff;
                  color: var(--accent);
                }

                .upload-icon svg { width: 21px; }
                .upload-title { font-weight: 700; }

                .upload-sub {
                  margin-top: 3px;
                  color: var(--muted);
                  font-size: 12px;
                }

                .file-name {
                  margin-top: 8px;
                  color: var(--accent);
                  font-size: 12px;
                  font-weight: 700;
                }

                .preview {
                  display: none;
                  max-width: 100%;
                  max-height: 280px;
                  object-fit: contain;
                }

                .upload.has-image .upload-inner { display: none; }
                .upload.has-image .preview { display: block; }

                .question {
                  width: 100%;
                  min-height: 92px;
                  padding: 13px;
                  resize: vertical;
                  border: 1px solid var(--line);
                  border-radius: 10px;
                  outline: none;
                  background: white;
                  line-height: 1.5;
                }

                .controls {
                  display: grid;
                  grid-template-columns: minmax(180px, 1fr) auto;
                  gap: 11px;
                  align-items: end;
                  margin-top: 18px;
                }

                .controls .label { margin-top: 0; }

                .submit {
                  width: 100%;
                  min-height: 48px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  gap: 9px;
                  margin-top: 14px;
                  border: 0;
                  border-radius: 11px;
                  background: var(--accent);
                  color: #fff;
                  font-weight: 700;
                  cursor: pointer;
                  transition: filter .2s, transform .15s;
                }

                .submit:hover { filter: brightness(1.15); }
                .submit:active { transform: scale(.99); }
                .submit:disabled { opacity: .55; cursor: wait; }
                .submit svg { width: 18px; }
                .submit svg[hidden] { display: none; }

                .spinner {
                  width: 15px;
                  height: 15px;
                  border: 2px solid #ffffff66;
                  border-top-color: #fff;
                  border-radius: 50%;
                  animation: spin .7s linear infinite;
                }

                @keyframes spin { to { transform: rotate(360deg); } }

                .results {
                  display: none;
                  margin-top: 22px;
                  padding-top: 20px;
                  border-top: 1px solid var(--line);
                }

                .results.visible { display: block; }

                .result-heading {
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  gap: 10px;
                  margin-bottom: 13px;
                }

                .result-heading h3 { margin: 0; font-size: 16px; }

                .badge {
                  padding: 5px 8px;
                  border-radius: 7px;
                  background: var(--soft);
                  color: var(--accent);
                  font-size: 10px;
                  font-weight: 750;
                  letter-spacing: .04em;
                  text-transform: uppercase;
                }

                .result-block {
                  margin-top: 10px;
                  padding: 14px;
                  border: 1px solid var(--line);
                  border-radius: 11px;
                }

                .result-label {
                  margin-bottom: 6px;
                  color: var(--muted);
                  font-size: 10px;
                  font-weight: 750;
                  letter-spacing: .1em;
                  text-transform: uppercase;
                }

                .result-text {
                  font-size: 13px;
                  white-space: pre-wrap;
                }

                .aside {
                  display: flex;
                  flex-direction: column;
                  gap: 15px;
                }

                .side-card { padding: 21px; }
                .side-card h2 { margin: 0; font-size: 15px; }

                .side-card p {
                  margin: 6px 0 0;
                  color: var(--muted);
                  font-size: 12px;
                }

                .trust-row {
                  display: flex;
                  gap: 11px;
                  padding: 13px 0;
                  border-bottom: 1px solid #ededed;
                }

                .trust-row:last-child {
                  padding-bottom: 0;
                  border-bottom: 0;
                }

                .trust-icon {
                  width: 29px;
                  height: 29px;
                  flex: 0 0 29px;
                  display: grid;
                  place-items: center;
                  border-radius: 9px;
                  background: var(--soft);
                  color: var(--accent);
                  font-size: 13px;
                  font-weight: 800;
                }

                .trust-row strong {
                  display: block;
                  font-size: 12px;
                }

                .trust-row span {
                  display: block;
                  margin-top: 2px;
                  color: var(--muted);
                  font-size: 11px;
                }

                .catalogue { margin-top: 13px; }

                .tool {
                  padding: 11px 0;
                  border-bottom: 1px solid #ededed;
                }

                .tool:last-child {
                  padding-bottom: 0;
                  border: 0;
                }

                .tool-name { font-size: 12px; font-weight: 700; }

                .tool-meta {
                  display: flex;
                  justify-content: space-between;
                  gap: 10px;
                  margin-top: 2px;
                  color: var(--muted);
                  font-size: 11px;
                }

                .tool-detail {
                  margin-top: 5px;
                  color: #555;
                  font-size: 11px;
                  white-space: pre-wrap;
                }

                .empty {
                  padding: 18px 0 3px;
                  color: var(--muted);
                  font-size: 12px;
                }

                .side-note {
                  border-color: #d8d8d8;
                  background: #f0f0f0;
                  box-shadow: none;
                }

                .side-note p { line-height: 1.65; }

                .footer {
                  display: flex;
                  justify-content: space-between;
                  gap: 18px;
                  padding: 17px 0 24px;
                  border-top: 1px solid var(--line);
                  color: var(--muted);
                  font-size: 11px;
                }

                @media (max-width: 850px) {
                  .shell { padding: 0 24px; }
                  .workspace {
                    grid-template-columns: minmax(0, 1.3fr)
                                           minmax(250px, .8fr);
                    gap: 14px;
                  }
                  .main-card { padding: 19px; }
                  .side-card { padding: 17px; }
                }

                @media (max-width: 650px) {
                  .shell { padding: 0 16px; }
                  .topbar { height: 64px; }
                  .top-right .local-label { display: none; }
                  .hero { padding: 38px 0 25px; }
                  .hero p { font-size: 14px; }
                  .workspace { grid-template-columns: 1fr; }
                  .main-card { padding: 17px; }
                  .controls { grid-template-columns: 1fr; }
                  .controls .btn { width: 100%; }
                  .footer { flex-direction: column; gap: 4px; }
                }
              </style>
            </head>

            <body data-lens="workbench">
              <div class="shell">
                <header class="topbar">
                  <div class="brand">
                    <span class="mark">
                      <svg viewBox="0 0 24 24" fill="none"
                           stroke="currentColor" stroke-width="1.7">
                        <path d="M14.5 6.5 17.5 9.5M4 20l3.5-.7L19 7.8a2.12 2.12 0 0 0-3-3L4.5 16.3 4 20Z"/>
                        <path d="m13 5 6 6M4 20h16"/>
                      </svg>
                    </span>
                    <span id="brandName">Workbench Lens</span>
                  </div>

                  <div class="top-right">
                    <span class="local-label">Private local workspace</span>
                    <span class="connection">
                      <i class="dot" id="connectionDot"></i>
                      <span id="connectionLabel">Oracle not connected</span>
                    </span>
                  </div>
                </header>

                <main>
                  <section class="hero">
                    <div class="eyebrow" id="heroEyebrow">
                      Your workshop, in focus
                    </div>
                    <h1 id="heroTitle">Know your tools.
            Work with confidence.</h1>
                    <p id="heroIntro">
                      Explore workshop tools with help from local AI.
                    </p>
                  </section>

                  <section class="card auth-card" id="authPanel">
                    <h2>Workbench Lens account</h2>
                    <p>
                      Register or sign in. Your account password is separate
                      from your Oracle catalogue password.
                    </p>

                    <div class="auth-toggle">
                      <button class="btn" id="showLogin" type="button">
                        Log in
                      </button>
                      <button class="btn" id="showRegister" type="button">
                        Register
                      </button>
                    </div>

                    <div id="authFields">
                      <label for="authUsername">Username or email</label>
                      <input id="authUsername" autocomplete="username">

                      <div id="emailWrap" hidden>
                        <label for="authEmail">Email</label>
                        <input id="authEmail" type="email"
                               autocomplete="email">
                      </div>

                      <label for="authPassword">Account password</label>
                      <input id="authPassword" type="password"
                             autocomplete="current-password">

                      <button class="btn btn-dark auth-submit"
                              id="authSubmit" type="button">
                        Log in
                      </button>

                      <div class="status" id="authStatus">
                        Use your Workbench Lens account.
                      </div>
                    </div>

                    <section id="adminPanel" hidden>
                      <h3>Registered users (admin only)</h3>
                      <div id="userList"></div>
                    </section>
                  </section>

                  <div id="appWorkspace" hidden>
                    <nav class="lens-switch" aria-label="Choose a lens">
                      <button type="button" data-lens="workbench"
                              aria-pressed="true">Workbench</button>
                      <button type="button" data-lens="kitchen"
                              aria-pressed="false">Kitchen Counter</button>
                      <button type="button" data-lens="garden"
                              aria-pressed="false">Garden</button>
                    </nav>

                    <div class="workspace">
                      <section class="card main-card">
                        <div class="card-head">
                          <div>
                            <h2 class="card-title" id="askTitle">
                              Ask about a workshop tool
                            </h2>
                            <p class="card-desc" id="askDescription">
                              Add a photo and ask about an item in this lens.
                            </p>
                          </div>
                          <span class="step">IMAGE + QUESTION</span>
                        </div>

                        <label class="upload" id="dropzone">
                          <input id="imageInput" type="file"
                                 accept="image/png,image/jpeg,image/gif,image/webp">
                          <div class="upload-inner">
                            <div class="upload-icon">
                              <svg viewBox="0 0 24 24" fill="none"
                                   stroke="currentColor" stroke-width="1.6">
                                <rect x="3" y="4" width="18" height="16" rx="3"/>
                                <circle cx="8.5" cy="9" r="1.5"/>
                                <path d="m21 15-5-5L5 20"/>
                              </svg>
                            </div>
                            <div class="upload-title">
                              Drop an item photo here, or browse
                            </div>
                            <div class="upload-sub">
                              PNG, JPG, GIF or WebP · up to 8 MB
                            </div>
                            <div class="file-name" id="fileName"></div>
                          </div>
                          <img class="preview" id="preview"
                               alt="Selected item preview">
                        </label>

                        <label class="label" for="question">
                          What would you like to know?
                        </label>
                        <textarea id="question" class="question"
                                  maxlength="4000"
                                  placeholder="What is this workshop tool used for?"></textarea>

                        <div class="controls">
                          <div>
                            <label class="label" for="password">
                              Oracle catalogue password
                            </label>
                            <input id="password" class="password-input"
                                   type="password"
                                   autocomplete="current-password"
                                   placeholder="Used for this local session">
                          </div>
                          <button class="btn" id="connect" type="button">
                            Connect catalogue
                          </button>
                        </div>

                        <button class="submit" id="ask"
                                type="button" disabled>
                          <svg id="askIcon" viewBox="0 0 24 24" fill="none"
                               stroke="currentColor" stroke-width="1.7">
                            <path d="m14 4 6 6m-3-9-9 9m0 0-4 8 8-4m-4-4 7 7"/>
                          </svg>
                          <span class="spinner" id="askSpinner" hidden></span>
                          <span id="askButtonLabel">Ask Workbench Lens</span>
                        </button>

                        <div class="status" id="status">
                          Connect your catalogue to get started.
                        </div>

                        <div class="results" id="results">
                          <div class="result-heading">
                            <h3 id="responseTitle">Workbench Lens response</h3>
                            <span class="badge">AI RESPONSE · UNVERIFIED</span>
                          </div>
                          <div class="result-block">
                            <div class="result-label">
                              What the image may show
                            </div>
                            <div class="result-text" id="description"></div>
                          </div>
                          <div class="result-block">
                            <div class="result-label">Answer</div>
                            <div class="result-text" id="answer"></div>
                          </div>
                        </div>
                      </section>

                      <aside class="aside">
                        <section class="card side-card">
                          <h2>Know what’s verified</h2>
                          <p>
                            AI suggestions and reviewed catalogue information
                            stay clearly distinct.
                          </p>
                          <div class="trust-row">
                            <div class="trust-icon">~</div>
                            <div>
                              <strong>AI image suggestions</strong>
                              <span>Unverified until checked.</span>
                            </div>
                          </div>
                          <div class="trust-row">
                            <div class="trust-icon">✓</div>
                            <div>
                              <strong>Reviewed catalogue</strong>
                              <span>Oracle entries show their saved status.</span>
                            </div>
                          </div>
                          <div class="trust-row">
                            <div class="trust-icon">i</div>
                            <div>
                              <strong>Safety first</strong>
                              <span>Check the maker’s instructions.</span>
                            </div>
                          </div>
                        </section>

                        <section class="card side-card">
                          <h2 id="catalogueTitle">
                            Your workshop catalogue
                          </h2>
                          <p id="catalogueCount">
                            Connect Oracle to view its tools.
                          </p>
                          <div class="catalogue" id="catalogue">
                            <div class="empty">
                              Your catalogue appears here after connecting.
                            </div>
                          </div>
                        </section>

                        <section class="card side-card side-note">
                          <h2>Made to stay on your machine</h2>
                          <p>
                            Your photo is sent to local Ollama. Oracle access
                            is made by this app server; passwords are held only
                            in the current local session.
                          </p>
                        </section>
                      </aside>
                    </div>
                  </div>
                </main>

                <footer class="footer">
                  <span id="footerBrand">Workbench Lens</span>
                  <span>
                    AI-generated information is a starting point, not a
                    verified guide.
                  </span>
                </footer>
              </div>

              <script>
                const input = document.getElementById('imageInput');
                const drop = document.getElementById('dropzone');
                const preview = document.getElementById('preview');
                const statusEl = document.getElementById('status');
                const askBtn = document.getElementById('ask');
                const connectBtn = document.getElementById('connect');
                const authStatus = document.getElementById('authStatus');
                const appWorkspace =
                  document.getElementById('appWorkspace');

                let imageData = '';
                let connected = false;
                let allTools = [];
                let activeLens = 'workbench';
                let authMode = 'login';
                let isAsking = false;

                const lensOrder = [
                  'workbench', 'kitchen', 'garden'
                ];

                const lensInfo = {
                  workbench: {
                    title: 'Workbench Lens',
                    hero: 'Know your tools.\\nWork with confidence.',
                    eyebrow: 'Your workshop, in focus',
                    intro: 'Explore workshop tools with help from local AI.',
                    askTitle: 'Ask about a workshop tool',
                    catalogueTitle: 'Your workshop catalogue',
                    questionPlaceholder:
                      'What is this workshop tool used for?'
                  },
                  kitchen: {
                    title: 'Kitchen Counter Lens',
                    hero: 'Know your kitchen tools.\\nCook with confidence.',
                    eyebrow: 'Your kitchen, in focus',
                    intro:
                      'Explore saved kitchen tools and their food-safe use.',
                    askTitle: 'Ask about a kitchen tool',
                    catalogueTitle: 'Your kitchen catalogue',
                    questionPlaceholder:
                      'What is this kitchen tool used for?'
                  },
                  garden: {
                    title: 'Garden Lens',
                    hero: 'Know your garden tools.\\nGrow with confidence.',
                    eyebrow: 'Your garden, in focus',
                    intro:
                      'Explore saved garden tools and their intended tasks.',
                    askTitle: 'Ask about a garden tool',
                    catalogueTitle: 'Your garden catalogue',
                    questionPlaceholder:
                      'What is this garden tool used for?'
                  }
                };

                function setStatus(message, error = false) {
                  statusEl.textContent = message;
                  statusEl.className =
                    'status' + (error ? ' error' : '');
                }

                function groupsForLens(lens) {
                  return {
                    workbench: [
                      'Electrical tools',
                      'Handheld tools',
                      'Waterworks tools',
                      'Measuring / miscellaneous / dangerous'
                    ],
                    kitchen: ['Kitchen tools'],
                    garden: ['Garden tools']
                  }[lens];
                }

                function toolsForLens(tools, lens) {
                  const groups = groupsForLens(lens);

                  return tools.filter(tool =>
                    groups.includes(tool.group)
                    || (lens === 'workbench'
                      && ['Hand tool', 'Power tool', 'Measuring tool']
                        .includes(tool.category))
                  );
                }

                function renderTools(tools) {
                  const host = document.getElementById('catalogue');
                  host.replaceChildren();

                  document.getElementById('catalogueCount').textContent =
                    tools.length
                    + (tools.length === 1 ? ' tool' : ' tools')
                    + ' in ' + lensInfo[activeLens].title;

                  if (!tools.length) {
                    host.innerHTML =
                      '<div class="empty">No saved tools in this lens catalogue yet.</div>';
                    return;
                  }

                  for (const tool of tools) {
                    const row = document.createElement('div');
                    row.className = 'tool';

                    const name = document.createElement('div');
                    name.className = 'tool-name';
                    name.textContent = tool.name;

                    const meta = document.createElement('div');
                    meta.className = 'tool-meta';

                    const category = document.createElement('span');
                    category.textContent =
                      (tool.group || 'Unassigned')
                      + ' · ' + tool.category;

                    const state = document.createElement('span');
                    state.textContent = tool.status || 'UNVERIFIED';

                    meta.append(category, state);
                    row.append(name, meta);

                    if (tool.purpose) {
                      const purpose = document.createElement('div');
                      purpose.className = 'tool-detail';
                      purpose.textContent =
                        'Purpose and details: ' + tool.purpose;
                      row.append(purpose);
                    }

                    if (tool.safetyNotes) {
                      const safety = document.createElement('div');
                      safety.className = 'tool-detail';
                      safety.textContent =
                        'Safety and care: ' + tool.safetyNotes;
                      row.append(safety);
                    }

                    host.append(row);
                  }
                }

                function refreshCatalogue() {
                  if (!connected) {
                    document.getElementById('catalogueCount')
                      .textContent =
                      'Connect Oracle to view this lens catalogue.';
                    document.getElementById('catalogue').innerHTML =
                      '<div class="empty">Connect Oracle to see saved records.</div>';
                    return;
                  }

                  renderTools(toolsForLens(allTools, activeLens));
                }

                function setLens(lens) {
                  if (!lensInfo[lens] || isAsking) return;

                  activeLens = lens;
                  const info = lensInfo[lens];

                  document.body.dataset.lens = lens;
                  document.title =
                    info.title + ' — Workbench Lens';

                  document.getElementById('brandName').textContent =
                    info.title;
                  document.getElementById('heroTitle').textContent =
                    info.hero;
                  document.getElementById('heroEyebrow').textContent =
                    info.eyebrow;
                  document.getElementById('heroIntro').textContent =
                    info.intro;
                  document.getElementById('askTitle').textContent =
                    info.askTitle;
                  document.getElementById('catalogueTitle').textContent =
                    info.catalogueTitle;
                  document.getElementById('question').placeholder =
                    info.questionPlaceholder;
                  document.getElementById('askButtonLabel').textContent =
                    'Ask ' + info.title;
                  document.getElementById('responseTitle').textContent =
                    info.title + ' response';
                  document.getElementById('footerBrand').textContent =
                    info.title;

                  document.querySelectorAll('.lens-switch button')
                    .forEach(button => {
                      button.setAttribute(
                        'aria-pressed',
                        String(button.dataset.lens === lens));
                    });

                  document.getElementById('results')
                    .classList.remove('visible');
                  refreshCatalogue();
                }

                document.querySelectorAll('.lens-switch button')
                  .forEach(button => {
                    button.addEventListener('click', () =>
                      setLens(button.dataset.lens));
                  });

                document.getElementById('showLogin').onclick = () => {
                  authMode = 'login';
                  document.getElementById('emailWrap').hidden = true;
                  document.getElementById('authSubmit').textContent =
                    'Log in';
                  document.getElementById('authPassword').autocomplete =
                    'current-password';
                  authStatus.textContent =
                    'Use your Workbench Lens account.';
                };

                document.getElementById('showRegister').onclick = () => {
                  authMode = 'register';
                  document.getElementById('emailWrap').hidden = false;
                  document.getElementById('authSubmit').textContent =
                    'Create account';
                  document.getElementById('authPassword').autocomplete =
                    'new-password';
                  authStatus.textContent =
                    'Choose a username, email, and password.';
                };

                document.getElementById('authSubmit').onclick =
                  async () => {
                    const username = document
                      .getElementById('authUsername')
                      .value.trim();
                    const password = document
                      .getElementById('authPassword').value;

                    authStatus.textContent = 'Working…';

                    try {
                      const payload = { username, password };

                      if (authMode === 'register') {
                        payload.email = document
                          .getElementById('authEmail')
                          .value.trim();
                      }

                      const response = await fetch(
                        '/api/' + (authMode === 'register'
                          ? 'register' : 'login'),
                        {
                          method: 'POST',
                          headers: {
                            'Content-Type': 'application/json'
                          },
                          body: JSON.stringify(payload)
                        });

                      const data = await response.json();

                      if (!response.ok) {
                        throw new Error(data.error
                          || 'The account request failed.');
                      }

                      document.getElementById('authPassword').value = '';
                      document.getElementById('appWorkspace').hidden = false;
                      document.getElementById('authUsername').disabled = true;
                      document.getElementById('authPassword').disabled = true;
                      document.getElementById('authSubmit').hidden = true;

                      authStatus.textContent =
                        'Signed in as ' + data.username
                        + ' (' + data.role + '). Connect Oracle below.';

                      if (data.role === 'ADMIN') {
                        document.getElementById('adminPanel').hidden = false;

                        const usersResponse =
                          await fetch('/api/admin/users');
                        const usersData = await usersResponse.json();

                        if (usersResponse.ok) {
                          document.getElementById('userList')
                            .textContent = usersData.users.map(user =>
                              user.username + ' · ' + user.email
                              + ' · ' + user.role).join('\\n');
                        }
                      }

                      setLens(activeLens);
                    } catch (error) {
                      authStatus.textContent = error.message;
                    }
                  };

                function fileSelected(file) {
                  if (!file) return;

                  const allowedTypes = [
                    'image/png',
                    'image/jpeg',
                    'image/gif',
                    'image/webp'
                  ];

                  if (!allowedTypes.includes(file.type)
                      || file.size > 8 * 1024 * 1024) {
                    setStatus(
                      'Choose a PNG, JPEG, GIF, or WebP image up to 8 MB.',
                      true);
                    return;
                  }

                  const reader = new FileReader();

                  reader.onload = () => {
                    imageData =
                      String(reader.result).split(',')[1];
                    preview.src = reader.result;
                    drop.classList.add('has-image');
                    document.getElementById('fileName').textContent =
                      file.name;
                    askBtn.disabled = !connected;
                  };

                  reader.readAsDataURL(file);
                }

                input.addEventListener('change', () =>
                  fileSelected(input.files[0]));

                for (const eventName of ['dragenter', 'dragover']) {
                  drop.addEventListener(eventName, event => {
                    event.preventDefault();
                    drop.classList.add('drag');
                  });
                }

                for (const eventName of ['dragleave', 'drop']) {
                  drop.addEventListener(eventName, event => {
                    event.preventDefault();
                    drop.classList.remove('drag');
                  });
                }

                drop.addEventListener('drop', event =>
                  fileSelected(event.dataTransfer.files[0]));

                connectBtn.addEventListener('click', async () => {
                  const password =
                    document.getElementById('password').value;

                  if (!password) {
                    setStatus(
                      'Enter your Oracle password to connect.',
                      true);
                    return;
                  }

                  connectBtn.disabled = true;
                  setStatus('Connecting to the Oracle catalogue…');

                  try {
                    const response = await fetch('/api/connect', {
                      method: 'POST',
                      headers: {
                        'Content-Type': 'application/json'
                      },
                      body: JSON.stringify({ password })
                    });

                    const data = await response.json();

                    if (!response.ok) {
                      throw new Error(
                        data.error || 'Connection failed.');
                    }

                    connected = true;
                    allTools = data.tools;
                    document.getElementById('password').value = '';
                    document.getElementById('connectionDot')
                      .style.background = '#438b5c';
                    document.getElementById('connectionLabel')
                      .textContent = 'Oracle connected';
                    connectBtn.textContent = 'Connected';
                    askBtn.disabled = !imageData;

                    refreshCatalogue();
                    setStatus(
                      'Catalogue connected. Choose a lens and add a photo.');
                  } catch (error) {
                    connected = false;
                    setStatus(error.message, true);
                  } finally {
                    connectBtn.disabled = false;
                  }
                });

                askBtn.addEventListener('click', async () => {
                  const question =
                    document.getElementById('question')
                      .value.trim();

                  if (!imageData) {
                    setStatus('Add a photo first.', true);
                    return;
                  }

                  if (!question) {
                    setStatus('Write a question about the item first.', true);
                    return;
                  }

                  isAsking = true;
                  askBtn.disabled = true;
                  document.querySelectorAll('.lens-switch button')
                    .forEach(button => button.disabled = true);
                  document.getElementById('askIcon').hidden = true;
                  document.getElementById('askSpinner').hidden = false;
                  document.getElementById('askButtonLabel')
                    .textContent = 'Checking this lens catalogue…';

                  setStatus(
                    'Checking the image against the selected lens catalogue…');
                  document.getElementById('results')
                    .classList.remove('visible');

                  try {
                    const response = await fetch('/api/ask', {
                      method: 'POST',
                      headers: {
                        'Content-Type': 'application/json'
                      },
                      body: JSON.stringify({
                        question,
                        image: imageData,
                        lens: activeLens
                      })
                    });

                    const data = await response.json();

                    if (!response.ok) {
                      throw new Error(data.error
                        || 'The request could not be completed.');
                    }

                    document.getElementById('description').textContent =
                      data.description;
                    document.getElementById('answer').textContent =
                      data.answer;
                    document.getElementById('results')
                      .classList.add('visible');
                    renderTools(toolsForLens(
                      data.catalogue, activeLens));

                    if (data.matched) {
                      setStatus(
                        'Matched to this lens catalogue. AI guidance is unverified.');
                    } else if (data.outOfScope) {
                      setStatus(
                        'This image does not match the selected lens.');
                    } else {
                      setStatus(
                        'No matching saved item was found in this lens.');
                    }
                  } catch (error) {
                    setStatus(error.message, true);
                  } finally {
                    isAsking = false;
                    document.querySelectorAll('.lens-switch button')
                      .forEach(button => button.disabled = false);
                    askBtn.disabled = !connected;
                    document.getElementById('askIcon').hidden = false;
                    document.getElementById('askSpinner').hidden = true;
                    document.getElementById('askButtonLabel')
                      .textContent = 'Ask ' + lensInfo[activeLens].title;
                  }
                });

                let swipeStartX = null;
                let swipeStartY = null;

                appWorkspace.addEventListener('touchstart', event => {
                  if (event.target.closest(
                      'input, textarea, button, label')) {
                    return;
                  }

                  const touch = event.changedTouches[0];
                  swipeStartX = touch.clientX;
                  swipeStartY = touch.clientY;
                }, { passive: true });

                appWorkspace.addEventListener('touchend', event => {
                  if (swipeStartX === null || swipeStartY === null) {
                    return;
                  }

                  const touch = event.changedTouches[0];
                  const dx = touch.clientX - swipeStartX;
                  const dy = touch.clientY - swipeStartY;

                  swipeStartX = null;
                  swipeStartY = null;

                  if (Math.abs(dx) < 75
                      || Math.abs(dx) < Math.abs(dy)) {
                    return;
                  }

                  const index = lensOrder.indexOf(activeLens);
                  const nextIndex = dx < 0
                    ? Math.min(index + 1, lensOrder.length - 1)
                    : Math.max(index - 1, 0);

                  setLens(lensOrder[nextIndex]);
                }, { passive: true });

                setLens('workbench');
              </script>
            </body>
            </html>
            """;
}