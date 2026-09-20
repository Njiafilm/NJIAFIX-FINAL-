// NJIAFIX server - Developer: Godwin Dotto. Inafanya kazi Termux (simu) na PC (Windows/Linux/Mac, Node 18+).
// Kila jibu linatokana na vipimo/amri halisi.
// Bila data halisi = hakuna hitimisho. Inasikiliza 127.0.0.1 tu (HOST=0.0.0.0 kuruhusu mtandao).
const express = require('express');
const cors = require('cors');
const net = require('net');
const os = require('os');
const dgram = require('dgram');
const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const { execFile } = require('child_process');

const PORT = Number(process.env.PORT) || 5555;
const HOST = process.env.HOST || '127.0.0.1';
const PAYMENTS_FILE = path.join(__dirname, 'payments.jsonl');

const app = express();
app.use(cors());
app.use(express.json());
app.use((q, r, n) => { console.log(q.method, q.url, (q.body && q.body.action) || ''); n(); });

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const run = (cmd, args, timeout = 15000) =>
  new Promise((resolve) =>
    execFile(cmd, args, { timeout }, (err, out, errOut) =>
      resolve({ ok: !err, out: (out || '').trim(), err: ((errOut || (err && err.message)) || '').trim() })
    )
  );
const adb = (...args) => run('adb', args);
const fail = (error) => ({ ok: false, error, message: error });
const NO_DEVICE = 'Hakuna kifaa kilichounganishwa';
const SERIAL_RE = /^[A-Za-z0-9._:\-]+$/;

// ---------- ADB ----------
async function listDevices() {
  const r = await adb('devices', '-l');
  const rows = r.out.split('\n').slice(1).map((l) => l.trim())
    .filter((l) => l && !l.startsWith('emulator-')).map((l) => l.split(/\s+/));
  return { ok: r.ok, err: r.err, rows };
}

async function target(body) {
  const d = await listDevices();
  if (!d.ok) return { error: d.err || 'ADB imeshindwa kuanza' };
  const online = d.rows.filter((r) => r[1] === 'device');
  if (!online.length) {
    if (d.rows.some((r) => r[1] === 'unauthorized'))
      return { error: 'Kifaa kimeonekana lakini hakijaruhusu USB debugging. Kubali dirisha kwenye kifaa.' };
    if (d.rows.some((r) => r[1] === 'offline')) return { error: 'Kifaa kiko offline. Kiunganishe upya.' };
    return { error: NO_DEVICE };
  }
  const want = body && body.serial;
  if (want) {
    if (!SERIAL_RE.test(want) || !online.some((r) => r[0] === want)) return { error: 'Kifaa ' + want + ' hakipo' };
    return { serial: want };
  }
  if (online.length > 1)
    return { error: 'Vifaa zaidi ya kimoja: ' + online.map((r) => r[0]).join(', ') + '. Tuma "serial".' };
  return { serial: online[0][0] };
}

async function storageInfo(serial) {
  const df = await adb('-s', serial, 'shell', 'df', '/data');
  if (!df.ok) return null;
  const m = df.out.split('\n').pop().match(/(\d+)\s+(\d+)\s+(\d+)\s+(\d+)%/);
  return m ? { availKb: Number(m[3]), pct: Number(m[4]) } : null;
}

async function diagnoseDevice(serial) {
  const issues = [], unavailable = [], data = {};
  const bad = { 3: ['BATTERY_OVERHEAT', 'joto kupita kiasi'], 4: ['BATTERY_DEAD', 'imekufa'],
    5: ['BATTERY_OVER_VOLTAGE', 'volti kupita kiasi'], 6: ['BATTERY_FAILURE', 'hitilafu isiyojulikana'],
    7: ['BATTERY_COLD', 'baridi kupita kiasi'] };

  const bat = await adb('-s', serial, 'shell', 'dumpsys', 'battery');
  if (bat.ok && /level:/.test(bat.out)) {
    const b = {};
    for (const m of bat.out.matchAll(/^\s*([\w ]+?):\s*(.+)$/gm)) b[m[1].trim()] = m[2].trim();
    const temp = b.temperature ? Number(b.temperature) / 10 : undefined;
    data.betri = { asilimia: b.level, msimbo_wa_afya: b.health, joto_c: temp };
    if (bad[b.health]) issues.push({ code: bad[b.health][0], description: 'Betri: ' + bad[b.health][1] + ' (msimbo ' + b.health + ')' });
    if (temp !== undefined && temp >= 45) issues.push({ code: 'BATTERY_HOT', description: 'Betri: joto juu (' + temp + '°C)' });
  } else unavailable.push('betri');

  const st = await storageInfo(serial);
  if (st) {
    data.hifadhi_imejaa_asilimia = st.pct;
    if (st.pct >= 90) issues.push({ code: 'STORAGE_FULL', description: 'Hifadhi imejaa ' + st.pct + '%' });
  } else unavailable.push('hifadhi');

  const mem = await adb('-s', serial, 'shell', 'cat', '/proc/meminfo');
  const tot = (mem.out.match(/MemTotal:\s+(\d+)/) || [])[1];
  const av = (mem.out.match(/MemAvailable:\s+(\d+)/) || [])[1];
  if (mem.ok && tot && av) {
    data.ram_huru_asilimia = Math.round((av / tot) * 100);
    if (av / tot < 0.1) issues.push({ code: 'RAM_LOW', description: 'Kumbukumbu huru chini ya 10%' });
  } else unavailable.push('ram');

  if (unavailable.length === 3) issues.push({ code: 'NO_DATA', description: 'Hakuna data halisi iliyosomwa. Hakuna hitimisho.' });
  return { issues, unavailable, data };
}

const reboot = (extra) => async (body) => {
  const t = await target(body);
  if (t.error) return fail(t.error);
  const r = await adb('-s', t.serial, 'reboot', ...extra);
  return r.ok ? { ok: true, output: r.out, message: 'Amri imetumwa kwa ' + t.serial } : fail(r.err);
};

const ACTIONS = {
  adb_devices: async () => {
    const d = await listDevices();
    if (!d.ok) return fail(d.err || 'ADB imeshindwa kuanza');
    const text = d.rows.length ? d.rows.map((r) => r.join(' ')).join('\n') : NO_DEVICE;
    return { ok: true, devices: d.rows.map((r) => r[0]), output: text, message: text };
  },

  adb_connect: async (body) => {
    const host = String(body.host || '').trim();
    if (!/^[A-Za-z0-9.\-]+(:\d{1,5})?$/.test(host)) return fail('Andika IP sahihi, mfano 192.168.1.20');
    const r = await adb('connect', host.includes(':') ? host : host + ':5555');
    const text = r.out || r.err;
    return { ok: /connected to/.test(r.out), output: text, message: text };
  },

  reboot: reboot([]), reboot_bootloader: reboot(['bootloader']), reboot_recovery: reboot(['recovery']),

  fastboot_devices: async () => {
    const r = await run('fastboot', ['devices'], 10000);
    if (!r.ok && !r.out) return fail(r.err || 'fastboot imeshindwa');
    const text = r.out || 'Hakuna kifaa kwenye fastboot';
    return { ok: true, output: text, message: text };
  },

  adb_info: async (body) => {
    const t = await target(body);
    if (t.error) return fail(t.error);
    const r = await adb('-s', t.serial, 'shell', 'getprop');
    if (!r.ok) return fail(r.err);
    const p = {};
    for (const m of r.out.matchAll(/^\[(.+?)\]: \[(.*)\]$/gm)) p[m[1]] = m[2];
    const info = { serial: t.serial, mtengenezaji: p['ro.product.manufacturer'], modeli: p['ro.product.model'],
      android: p['ro.build.version.release'], sdk: p['ro.build.version.sdk'], cpu: p['ro.product.cpu.abi'],
      ujenzi: p['ro.build.display.id'] };
    const text = Object.entries(info).map(([k, v]) => k + ': ' + (v || 'HAIJAPATIKANA')).join('\n');
    return { ok: true, info, output: text, message: text };
  },

  diagnose: async (body) => {
    const t = await target(body);
    if (t.error) return fail(t.error);
    const d = await diagnoseDevice(t.serial);
    const text = d.issues.length ? d.issues.map((i) => i.description).join('\n')
      : 'Hakuna tatizo lililothibitishwa kwa vipimo vilivyosomwa.';
    return { ok: true, serial: t.serial, ...d, output: text, message: text };
  },

  clear_cache: async (body) => {
    const t = await target(body);
    if (t.error) return fail(t.error);
    const before = await storageInfo(t.serial);
    const r = await adb('-s', t.serial, 'shell', 'pm', 'trim-caches', '999G');
    if (!r.ok || /exception|error|unknown/i.test(r.out)) return fail('Kusafisha cache kumeshindwa: ' + (r.err || r.out));
    const after = await storageInfo(t.serial);
    const mb = (kb) => Math.round(kb / 1024);
    const text = 'Kifaa kimekubali amri ya kusafisha cache.\n' + (before && after
      ? 'Nafasi huru: ' + mb(before.availKb) + ' MB → ' + mb(after.availKb) + ' MB'
      : '(Nafasi huru haikuweza kupimwa)');
    return { ok: true, before, after, output: r.out, message: text };
  },

  toggle_wifi: async (body) => {
    const t = await target(body);
    if (t.error) return fail(t.error);
    const cur = await adb('-s', t.serial, 'shell', 'settings', 'get', 'global', 'wifi_on');
    if (!cur.ok || !/^\d$/.test(cur.out)) return fail('Hali ya Wi-Fi haikuweza kusomwa kwenye kifaa');
    const isOn = cur.out !== '0';
    if (isOn && t.serial.includes(':'))
      return fail('Kifaa kimeunganishwa kupitia Wi-Fi (' + t.serial + '). Kuzima Wi-Fi kungekata muunganisho, sijafanya.');
    const r = await adb('-s', t.serial, 'shell', 'svc', 'wifi', isOn ? 'disable' : 'enable');
    if (!r.ok) return fail('Imeshindwa: ' + (r.err || r.out));
    await sleep(2000);
    const now = await adb('-s', t.serial, 'shell', 'settings', 'get', 'global', 'wifi_on');
    const label = (on) => (on ? 'IMEWASHWA' : 'IMEZIMWA');
    if (!now.ok || !/^\d$/.test(now.out))
      return { ok: false, message: 'Amri imetumwa lakini hali mpya haikuthibitishwa.' };
    const nowOn = now.out !== '0';
    return { ok: nowOn === !isOn, message: 'Wi-Fi ilikuwa ' + label(isOn) + ' → sasa ' + label(nowOn) };
  },

  obd_clear_dtc: async (body) => {
    const host = String(body.host || '').trim(), port = validPort(body.port, 35000);
    if (!isPrivateIPv4(host) || !port) return fail('Andika IP ya adapta ELM327 (mtandao wa ndani).');
    let sess;
    try { sess = await elmSession(host, port); } catch (e) { return fail(e.message); }
    try {
      await sess.send('ATZ', 6000);
      for (const c of ['ATE0', 'ATL0', 'ATS0', 'ATH0', 'ATSP0']) await sess.send(c);
      const first = await readPid(sess, '00', 15000);
      if (!first) return fail('Gari haijibu. Washa ignition (ON), injini izimwe.');
      const r = await sess.send('04', 10000);
      const ok = !!r && hexBytes(r).includes(0x44);
      return ok ? { ok: true, message: 'Codes zimefutwa kwenye kompyuta ya gari. Soma tena kuthibitisha kama hazijarudi.' }
        : fail('Gari haikukubali kufuta (injini izimwe, ignition ON). Jibu: ' + (r ? r.join(' ') : 'hakuna'));
    } finally { sess.close(); }
  },

  clear_passkey: async (body) => {
    const t = await target(body);
    if (t.error) return fail(t.error);
    const r = await adb('-s', t.serial, 'shell', 'cmd', 'lock_settings', 'clear-passkey');
    const out = (r.out + ' ' + r.err).toLowerCase();
    if (!r.ok || /exception|unknown command|no such|error/.test(out))
      return fail('Kifaa hiki hakiungi mkono amri hii kwa njia ya ADB (mara nyingi inahitaji root): ' + (r.err || r.out || 'hakuna jibu'));
    return { ok: true, output: r.out, message: 'Amri imekubaliwa na kifaa. Angalia kwenye kifaa kama lock imeondoka - ADB haiwezi kuthibitisha hilo yenyewe.' };
  },

  factory_reset: async (body) => {
    const t = await target(body);
    if (t.error) return fail(t.error);
    const r = await adb('-s', t.serial, 'shell', 'am', 'broadcast', '-a', 'android.intent.action.MASTER_CLEAR');
    const out = (r.out + ' ' + r.err).toLowerCase();
    if (!r.ok || /security exception|permission denial|error/.test(out))
      return fail('Kifaa kimekataa amri (kawaida app maalum ya Device Admin inahitajika): ' + (r.err || r.out || 'hakuna jibu'));
    return { ok: true, output: r.out, message: 'Amri imetumwa. Kifaa kitajizima/kurudisha upya kikifanikiwa - server haiwezi kuthibitisha baada ya hapo.' };
  },
};
ACTIONS.adb_reboot = ACTIONS.reboot;
ACTIONS.adb_fastboot = ACTIONS.reboot_bootloader;
ACTIONS.adb_recovery = ACTIONS.reboot_recovery;

app.get('/health', (req, res) => res.json({ ok: true }));

app.get('/api/devices', async (req, res) => {
  const d = await listDevices();
  if (!d.ok) return res.json({ ok: false, devices: [], error: d.err || 'ADB imeshindwa kuanza' });
  res.json({ ok: true, devices: d.rows.filter((r) => r[1] === 'device').map((r) => r[0]),
    other: d.rows.filter((r) => r[1] !== 'device').map((r) => r[0] + ' (' + r[1] + ')') });
});

app.get('/api/diagnose/:serial', async (req, res) => {
  const t = await target({ serial: req.params.serial });
  if (t.error) return res.json({ ok: false, issues: [], error: t.error, message: t.error });
  res.json({ ok: true, serial: t.serial, ...(await diagnoseDevice(t.serial)) });
});

app.post('/execute-fix', async (req, res) => {
  const body = req.body || {};
  if (!Object.prototype.hasOwnProperty.call(ACTIONS, body.action))
    return res.status(400).json({ ok: false, error: 'Amri haijulikani: ' + body.action, zinazojulikana: Object.keys(ACTIONS) });
  try { res.json(await ACTIONS[body.action](body)); }
  catch (e) { res.json(fail(String((e && e.message) || e))); }
});

// ---------- Vifaa vingine (mtandao wa ndani tu) ----------
const isPrivateIPv4 = (ip) => {
  const m = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/.exec(ip);
  if (!m) return false;
  const [a, b, c, d] = m.slice(1).map(Number);
  if ([a, b, c, d].some((x) => x > 255)) return false;
  return a === 10 || a === 127 || (a === 172 && b >= 16 && b <= 31) || (a === 192 && b === 168) || (a === 169 && b === 254);
};

const SERVICES = { 9100: 'RAW (kuchapa)', 631: 'IPP', 515: 'LPD', 80: 'HTTP', 443: 'HTTPS', 8080: 'HTTP-alt', 8000: 'HTTP-alt',
  554: 'RTSP', 8554: 'RTSP', 37777: 'Dahua', 22: 'SSH', 23: 'Telnet', 53: 'DNS', 445: 'SMB', 139: 'NetBIOS',
  135: 'RPC', 3389: 'RDP', 5985: 'WinRM' };

const probe = (ip, port, timeout = 1500) => new Promise((resolve) => {
  const t0 = Date.now();
  const s = new net.Socket();
  let done = false;
  const end = (state) => { if (!done) { done = true; s.destroy(); resolve({ port, state, ms: Date.now() - t0 }); } };
  s.setTimeout(timeout);
  s.once('connect', () => end('open'));
  s.once('timeout', () => end('timeout'));
  s.once('error', (e) => end(e.code === 'ECONNREFUSED' ? 'closed' : 'timeout'));
  s.connect(port, ip);
});

const httpBanner = (ip, port, secure) => new Promise((resolve) => {
  let done = false, buf = '';
  const finish = (v) => { if (!done) { done = true; resolve(v); } };
  const req = (secure ? https : http).request(
    { host: ip, port, path: '/', method: 'GET', timeout: 2500, rejectUnauthorized: false },
    (res) => {
      res.setEncoding('utf8');
      res.on('data', (c) => { buf += c; if (buf.length > 4096) res.destroy(); });
      const end = () => {
        const t = (buf.match(/<title[^>]*>([\s\S]*?)<\/title>/i) || [])[1];
        finish({ status: res.statusCode, server: res.headers.server, title: t ? t.trim().slice(0, 80) : undefined });
      };
      res.on('end', end); res.on('close', end); res.on('error', end);
    });
  req.on('timeout', () => { req.destroy(); finish(null); });
  req.on('error', () => finish(null));
  req.end();
});

const rtspProbe = (ip, port) => new Promise((resolve) => {
  let done = false, buf = '';
  const s = new net.Socket();
  const end = (v) => { if (!done) { done = true; s.destroy(); resolve(v); } };
  s.setTimeout(2500);
  s.once('timeout', () => end(null));
  s.once('error', () => end(null));
  s.on('data', (d) => { buf += d.toString('latin1'); if (buf.includes('\n')) end(buf.split('\n')[0].trim()); });
  s.connect(port, ip, () => s.write('OPTIONS rtsp://' + ip + ':' + port + '/ RTSP/1.0\r\nCSeq: 1\r\n\r\n'));
});

const webLines = async (ip, open) => {
  const out = [];
  for (const r of open.filter((x) => [80, 8080, 8000, 8001, 8008, 443].includes(x.port))) {
    const b = await httpBanner(ip, r.port, r.port === 443);
    out.push(b ? 'Web ' + r.port + ': HTTP ' + b.status + (b.server ? ', server: ' + b.server : '') + (b.title ? ', kichwa: ' + b.title : '')
      : 'Web ' + r.port + ': imefunguka lakini haikujibu HTTP');
  }
  return out;
};

async function checkHost(ip, ports, extra) {
  if (!isPrivateIPv4(ip)) return fail('Andika IP ya mtandao wa ndani, mfano 192.168.1.20');
  const results = await Promise.all(ports.map((p) => probe(ip, p)));
  const open = results.filter((r) => r.state === 'open');
  const closed = results.filter((r) => r.state === 'closed');
  const lines = ['IP: ' + ip];
  if (!open.length && !closed.length) {
    lines.push('Hakuna jibu kwenye port zote zilizojaribiwa (' + ports.join(', ') + ').');
    lines.push('Inawezekana: kimezimwa, IP si sahihi, kiko mtandao mwingine, au firewall. Hii si uthibitisho wa hitilafu ya kifaa.');
    return { ok: false, alive: false, ports: results, message: lines.join('\n') };
  }
  const ms = Math.min(...[...open, ...closed].map((r) => r.ms));
  lines.push('Kifaa kinajibu (TCP ~' + ms + ' ms)');
  lines.push(open.length ? 'Port wazi: ' + open.map((r) => r.port + ' ' + (SERVICES[r.port] || '')).join(', ')
    : 'Hakuna port wazi kati ya zilizojaribiwa.');
  if (extra) lines.push(...(await extra(ip, open)));
  return { ok: true, alive: true, ports: results, message: lines.join('\n') };
}

const KINDS = {
  '/api/diagnose/printer/network/:ip': [[9100, 631, 515, 80, 443], async (ip, open) => {
    const svc = open.filter((r) => [9100, 631, 515].includes(r.port));
    return [svc.length ? 'Huduma ya kuchapa inapatikana (' + svc.map((r) => r.port).join(', ') + ')'
      : 'Huduma ya kuchapa (9100/631/515) haijafunguka', ...(await webLines(ip, open)), ...(await snmpPrinterLines(ip))];
  }],
  '/api/diagnose/camera/:ip': [[554, 8554, 80, 443, 8000, 8080, 37777], async (ip, open) => {
    const out = [];
    for (const r of open.filter((x) => x.port === 554 || x.port === 8554)) {
      const l = await rtspProbe(ip, r.port);
      out.push(l ? 'RTSP ' + r.port + ' inajibu: ' + l : 'RTSP ' + r.port + ': imefunguka lakini haikujibu');
    }
    return [...out, ...(await webLines(ip, open))];
  }],
  '/api/diagnose/router/:ip': [[80, 443, 8080, 22, 23, 53], async (ip, open) => webLines(ip, open)],
  '/api/diagnose/pc/:ip': [[445, 139, 135, 3389, 22, 5985, 80], null],
  '/api/diagnose/tv/:ip': [[8001, 8002, 3000, 3001, 8008, 8009, 9197, 55000, 80], async (ip, open) => webLines(ip, open)],
};
for (const [route, [ports, extra]] of Object.entries(KINDS)) {
  app.get(route, async (req, res) => {
    try { res.json(await checkHost(String(req.params.ip || '').trim(), ports, extra)); }
    catch (e) { res.json(fail(String((e && e.message) || e))); }
  });
}


// ---------- SNMP (printer / photocopier) ----------
const SNMP_PORT = Number(process.env.SNMP_PORT) || 161;
const SNMP_COMMUNITY = process.env.SNMP_COMMUNITY || 'public';
const berLen = (n) => (n < 128 ? Buffer.from([n]) : n < 256 ? Buffer.from([0x81, n]) : Buffer.from([0x82, n >> 8, n & 255]));
const tlv = (tag, body) => Buffer.concat([Buffer.from([tag]), berLen(body.length), body]);
const berInt = (n) => {
  const b = [];
  do { b.unshift(n & 255); n = Math.floor(n / 256); } while (n > 0);
  if (b[0] & 0x80) b.unshift(0);
  return tlv(0x02, Buffer.from(b));
};
const berOid = (oid) => {
  const p = oid.split('.').map(Number);
  const out = [p[0] * 40 + p[1]];
  for (const n0 of p.slice(2)) {
    let n = n0; const st = [n & 127]; n = Math.floor(n / 128);
    while (n > 0) { st.unshift((n & 127) | 128); n = Math.floor(n / 128); }
    out.push(...st);
  }
  return tlv(0x06, Buffer.from(out));
};
const snmpGetPacket = (community, oids, reqId) =>
  tlv(0x30, Buffer.concat([
    berInt(1), tlv(0x04, Buffer.from(community)),
    tlv(0xa0, Buffer.concat([berInt(reqId), berInt(0), berInt(0),
      tlv(0x30, Buffer.concat(oids.map((o) => tlv(0x30, Buffer.concat([berOid(o), tlv(0x05, Buffer.alloc(0))])))))])),
  ]));
const readTlv = (buf, off) => {
  const tag = buf[off]; let len = buf[off + 1], hdr = 2;
  if (len & 0x80) { const nb = len & 0x7f; len = 0; for (let i = 0; i < nb; i++) len = len * 256 + buf[off + 2 + i]; hdr = 2 + nb; }
  return { tag, start: off + hdr, end: off + hdr + len };
};
const decodeOid = (b) => {
  const out = [Math.floor(b[0] / 40), b[0] % 40]; let v = 0;
  for (let i = 1; i < b.length; i++) { v = v * 128 + (b[i] & 127); if (!(b[i] & 128)) { out.push(v); v = 0; } }
  return out.join('.');
};
const berValue = (tag, b) => {
  if (tag === 0x04) return Buffer.from(b);
  if (tag === 0x02) { let v = 0; for (const x of b) v = v * 256 + x; if (b.length && (b[0] & 0x80)) v -= Math.pow(2, 8 * b.length); return v; }
  if (tag === 0x41 || tag === 0x42 || tag === 0x43 || tag === 0x46) { let v = 0; for (const x of b) v = v * 256 + x; return v; }
  if (tag === 0x06) return decodeOid(b);
  return null; // null / noSuchObject / noSuchInstance / endOfMibView
};
function parseSnmpResponse(buf) {
  try {
    const top = readTlv(buf, 0);
    let o = readTlv(buf, top.start);
    o = readTlv(buf, o.end);
    const pdu = readTlv(buf, o.end);
    let f = readTlv(buf, pdu.start);
    f = readTlv(buf, f.end);
    const errStatus = berValue(0x02, buf.subarray(f.start, f.end));
    f = readTlv(buf, f.end);
    const vbl = readTlv(buf, f.end);
    const values = {};
    let p = vbl.start;
    while (p < vbl.end) {
      const vb = readTlv(buf, p);
      const name = readTlv(buf, vb.start);
      const val = readTlv(buf, name.end);
      values[decodeOid(buf.subarray(name.start, name.end))] = berValue(val.tag, buf.subarray(val.start, val.end));
      p = vb.end;
    }
    return { errStatus, values };
  } catch (e) { return null; }
}
const snmpGet = (ip, oids, timeout = 2000) => new Promise((resolve) => {
  const s = dgram.createSocket('udp4');
  const reqId = Math.floor(Math.random() * 0x7fffffff) + 1;
  let done = false, t = null;
  const end = (v) => { if (!done) { done = true; clearTimeout(t); try { s.close(); } catch (e) { /* tayari imefungwa */ } resolve(v); } };
  t = setTimeout(() => end(null), timeout);
  s.on('error', () => end(null));
  s.on('message', (m) => { const r = parseSnmpResponse(m); end(r && r.errStatus === 0 ? r.values : null); });
  s.send(snmpGetPacket(SNMP_COMMUNITY, oids, reqId), SNMP_PORT, ip, (err) => { if (err) end(null); });
});

const PR = { descr: '1.3.6.1.2.1.1.1.0', status: '1.3.6.1.2.1.25.3.5.1.1.1', errs: '1.3.6.1.2.1.25.3.5.1.2.1', pages: '1.3.6.1.2.1.43.10.2.1.4.1.1' };
const SUP = '1.3.6.1.2.1.43.11.1.1.';
const PRINTER_STATUS = { 1: 'nyingine', 2: 'haijulikani', 3: 'tayari (idle)', 4: 'inachapa', 5: 'inapasha joto' };
const PRINTER_ERRS = [[0, 0x80, 'karatasi inakaribia kwisha'], [0, 0x40, 'karatasi imeisha'], [0, 0x20, 'toner/wino inakaribia kwisha'],
  [0, 0x10, 'toner/wino imeisha'], [0, 0x08, 'mlango uko wazi'], [0, 0x04, 'karatasi imekwama (jam)'], [0, 0x02, 'printer offline'],
  [0, 0x01, 'inahitaji service'], [1, 0x80, 'trei ya kuingiza haipo'], [1, 0x40, 'trei ya kutoka haipo'], [1, 0x20, 'cartridge haipo'],
  [1, 0x10, 'trei ya kutoka karibu imejaa'], [1, 0x08, 'trei ya kutoka imejaa'], [1, 0x04, 'trei ya kuingiza tupu'],
  [1, 0x02, 'matengenezo ya kinga yamechelewa']];
const txt = (b) => (Buffer.isBuffer(b) ? b.toString('utf8').replace(/[^\x20-\x7e]/g, '').slice(0, 80) : String(b));

async function snmpPrinterLines(ip) {
  const g = await snmpGet(ip, Object.values(PR));
  if (!g) return ['SNMP haikujibu (imezimwa kwenye printer, au community si "' + SNMP_COMMUNITY + '").'];
  const lines = [];
  if (g[PR.descr] != null) lines.push('Mfano: ' + txt(g[PR.descr]));
  if (g[PR.status] != null) lines.push('Hali: ' + (PRINTER_STATUS[g[PR.status]] || g[PR.status]));
  if (Buffer.isBuffer(g[PR.errs])) {
    const e = g[PR.errs];
    const on = PRINTER_ERRS.filter(([i, m]) => e.length > i && (e[i] & m)).map((x) => x[2]);
    lines.push(on.length ? 'Hitilafu zinazotangazwa na printer: ' + on.join(', ') : 'Printer haitangazi hitilafu yoyote');
  }
  if (g[PR.pages] != null) lines.push('Kurasa zilizochapishwa (jumla): ' + g[PR.pages]);
  const oids = [];
  for (let n = 1; n <= 6; n++) oids.push(SUP + '6.1.' + n, SUP + '8.1.' + n, SUP + '9.1.' + n);
  const sp = await snmpGet(ip, oids);
  if (sp) {
    for (let n = 1; n <= 6; n++) {
      const d = sp[SUP + '6.1.' + n], mx = sp[SUP + '8.1.' + n], lv = sp[SUP + '9.1.' + n];
      if (d == null) continue;
      let level;
      if (typeof lv === 'number' && lv >= 0 && typeof mx === 'number' && mx > 0) level = Math.round((lv * 100) / mx) + '%';
      else if (lv === -3) level = 'kimebaki kiasi (asilimia haijulikani)';
      else level = 'kiwango hakijulikani';
      lines.push('Toner/wino: ' + txt(d) + ' - ' + level);
    }
  }
  return lines;
}

// ---------- Gari (OBD-II kupitia adapta ya ELM327 Wi-Fi) ----------
const elmClean = (out, cmd) => out.replace(/>/g, '').split(/[\r\n]+/).map((l) => l.trim())
  .filter((l) => l && l.toUpperCase() !== cmd.toUpperCase() && !/^SEARCHING/i.test(l));

function elmSession(host, port) {
  return new Promise((resolve, reject) => {
    const s = new net.Socket();
    let buf = '', waiter = null, settled = false;
    const t = setTimeout(() => {
      s.destroy();
      if (!settled) { settled = true; reject(new Error('Adapta ELM327 haijibu kwenye ' + host + ':' + port + ' (simu iko kwenye Wi-Fi ya adapta?)')); }
    }, 4000);
    s.on('error', (e) => {
      clearTimeout(t);
      if (!settled) { settled = true; reject(new Error('Adapta ELM327 haipatikani (' + (e.code || e.message) + ')')); }
    });
    s.on('data', (d) => {
      buf += d.toString('latin1');
      if (waiter && buf.includes('>')) { const w = waiter; waiter = null; const out = buf; buf = ''; w(out); }
    });
    s.connect(port, host, () => {
      clearTimeout(t); settled = true;
      resolve({
        send: (cmd, timeout = 8000) => new Promise((res) => {
          buf = '';
          const to = setTimeout(() => { waiter = null; res(null); }, timeout);
          waiter = (out) => { clearTimeout(to); res(elmClean(out, cmd)); };
          s.write(cmd + '\r');
        }),
        close: () => s.destroy(),
      });
    });
  });
}

const hexBytes = (lines) => {
  const out = [];
  for (let l of lines) {
    l = l.replace(/^\d:/, '').replace(/\s+/g, '');
    if (/^[0-9A-Fa-f]{3}$/.test(l) || !/^([0-9A-Fa-f]{2})+$/.test(l)) continue;
    for (let i = 0; i < l.length; i += 2) out.push(parseInt(l.slice(i, i + 2), 16));
  }
  return out;
};
const readPid = async (sess, pid, timeout) => {
  const r = await sess.send('01' + pid, timeout);
  if (!r) return null;
  const b = hexBytes(r), want = parseInt(pid, 16);
  for (let i = 0; i + 1 < b.length; i++) if (b[i] === 0x41 && b[i + 1] === want) return b.slice(i + 2);
  return null;
};
const dtcName = (b1, b2) => 'PCBU'[b1 >> 6] + ((b1 >> 4) & 3) + (b1 & 15).toString(16).toUpperCase() + b2.toString(16).toUpperCase().padStart(2, '0');
const DTC_SYSTEM = { P: 'injini/gearbox', C: 'chassis', B: 'mwili', U: 'mtandao wa ECU' };
function parseDtcs(lines, can) {
  const b = hexBytes(lines), i = b.indexOf(0x43);
  if (i < 0) return null;
  let p = b.slice(i + 1), n = 3;
  if (can) { n = p[0]; p = p.slice(1); }
  const codes = [];
  for (let k = 0; k < n && 2 * k + 1 < p.length; k++) {
    if (p[2 * k] === 0 && p[2 * k + 1] === 0) continue;
    codes.push(dtcName(p[2 * k], p[2 * k + 1]));
  }
  return codes;
}
async function readVin(sess) {
  const r = await sess.send('0902', 10000);
  if (!r) return null;
  const b = hexBytes(r);
  for (let i = 0; i + 2 < b.length; i++) {
    if (b[i] === 0x49 && b[i + 1] === 0x02) {
      const v = String.fromCharCode(...b.slice(i + 3, i + 20));
      return /^[A-HJ-NPR-Z0-9]{17}$/.test(v) ? v : null;
    }
  }
  return null;
}
const LIVE_PIDS = [
  ['0C', 'Kasi ya injini', 'rpm', (a, b) => Math.round((a * 256 + b) / 4)],
  ['0D', 'Kasi ya gari', 'km/h', (a) => a],
  ['05', 'Joto la maji ya injini', '°C', (a) => a - 40],
  ['04', 'Mzigo wa injini', '%', (a) => Math.round((a * 100) / 255)],
  ['0F', 'Joto la hewa ya kuingia', '°C', (a) => a - 40],
  ['11', 'Throttle', '%', (a) => Math.round((a * 100) / 255)],
  ['2F', 'Kiwango cha mafuta', '%', (a) => Math.round((a * 100) / 255)],
  ['42', 'Volti ya kompyuta ya gari', 'V', (a, b) => ((a * 256 + b) / 1000).toFixed(2)],
];
const validPort = (p, dflt) => { const n = Number(p || dflt); return Number.isInteger(n) && n > 0 && n < 65536 ? n : null; };

async function obdScan(host, port) {
  let sess;
  try { sess = await elmSession(host, port); } catch (e) { return fail(e.message); }
  try {
    const z = await sess.send('ATZ', 6000);
    if (!z) return fail('Adapta imeunganishwa lakini haijibu amri za ELM327.');
    for (const c of ['ATE0', 'ATL0', 'ATS0', 'ATH0', 'ATSP0']) await sess.send(c);
    const first = await readPid(sess, '00', 15000);
    if (!first) return fail('Gari haijibu. Washa ignition (ON) na hakikisha adapta imeingia vizuri kwenye OBD port.');
    const dpn = await sess.send('ATDPN');
    const proto = dpn && dpn[0] ? parseInt(String(dpn[0]).replace(/^A/i, ''), 16) : NaN;
    const can = proto >= 6;
    const dp = await sess.send('ATDP');
    const lines = ['Adapta: ' + z.join(' '), 'Protocol: ' + (dp ? dp.join(' ') : 'haijulikani')];
    const issues = [], live = {};
    for (const [pid, name, unit, fn] of LIVE_PIDS) {
      const d = await readPid(sess, pid);
      if (d && d.length >= 1) { live[name] = fn(d[0], d[1]); lines.push(name + ': ' + live[name] + ' ' + unit); }
    }
    if (live['Joto la maji ya injini'] >= 105) issues.push('Joto la injini juu (' + live['Joto la maji ya injini'] + '°C)');
    if (live['Volti ya kompyuta ya gari'] && Number(live['Volti ya kompyuta ya gari']) < 11.8) issues.push('Volti ya chini (' + live['Volti ya kompyuta ya gari'] + ' V)');
    const st = await readPid(sess, '01');
    let mil = null;
    if (st && st.length) { mil = !!(st[0] & 0x80); lines.push('Taa ya injini (MIL): ' + (mil ? 'IMEWAKA' : 'imezimwa') + ' - codes zilizotangazwa: ' + (st[0] & 0x7f)); }
    const dr = await sess.send('03', 10000);
    const codes = dr ? parseDtcs(dr, can) : null;
    if (codes === null) lines.push('Codes (DTC): hazikusomeka');
    else if (!codes.length) lines.push('Codes (DTC): hakuna zilizohifadhiwa');
    else { lines.push('Codes (DTC): ' + codes.map((c) => c + ' (' + DTC_SYSTEM[c[0]] + ')').join(', ')); issues.push(...codes.map((c) => 'DTC ' + c)); }
    const vin = can ? await readVin(sess) : null;
    if (vin) lines.push('VIN: ' + vin);
    return { ok: true, protocol: dp ? dp.join(' ') : null, mil, dtcs: codes || [], live, vin, issues, message: lines.join('\n') };
  } finally { sess.close(); }
}

// ---------- Umeme (Modbus TCP) ----------
const MODBUS_ERR = { 1: 'Illegal Function', 2: 'Illegal Data Address', 3: 'Illegal Data Value', 4: 'Slave Device Failure', 6: 'Slave Busy', 11: 'Gateway Target Failed' };
const modbusRead = (host, port, unit, fn, start, count) => new Promise((resolve) => {
  const s = new net.Socket();
  let buf = Buffer.alloc(0), done = false;
  const end = (v) => { if (!done) { done = true; s.destroy(); resolve(v); } };
  s.setTimeout(3000);
  s.once('timeout', () => end({ error: 'Hakuna jibu (timeout)' }));
  s.on('error', (e) => end({ error: 'Muunganisho umeshindwa: ' + (e.code || e.message) }));
  s.on('data', (d) => {
    buf = Buffer.concat([buf, d]);
    if (buf.length < 9) return;
    const len = buf.readUInt16BE(4);
    if (buf.length < 6 + len) return;
    const f = buf[7];
    if (f & 0x80) return end({ error: 'Kifaa kimekataa: ' + (MODBUS_ERR[buf[8]] || 'msimbo ' + buf[8]) });
    const regs = [];
    for (let i = 0; i + 1 < buf[8]; i += 2) regs.push(buf.readUInt16BE(9 + i));
    end({ regs });
  });
  const req = Buffer.alloc(12);
  req.writeUInt16BE(Math.floor(Math.random() * 65535), 0); req.writeUInt16BE(0, 2); req.writeUInt16BE(6, 4);
  req[6] = unit; req[7] = fn; req.writeUInt16BE(start, 8); req.writeUInt16BE(count, 10);
  s.connect(port, host, () => s.write(req));
});

// ---------- Kompyuta/simu inayoendesha server hii ----------
async function localInfo() {
  const issues = [], lines = [];
  const cpus = os.cpus() || [];
  lines.push('Mfumo: ' + os.platform() + ' ' + os.release() + ' (' + os.arch() + '), Node ' + process.version);
  lines.push('CPU: ' + (cpus[0] ? cpus[0].model.trim() : 'haijulikani') + ' x' + cpus.length);
  const total = os.totalmem(), free = os.freemem();
  const rp = Math.round((free * 100) / total);
  lines.push('RAM: ' + Math.round(total / 1048576) + ' MB, huru ' + rp + '%');
  if (rp < 10) issues.push({ code: 'RAM_LOW', description: 'RAM huru chini ya 10%' });
  if (process.platform !== 'win32') lines.push('Load avg (1/5/15 dk): ' + os.loadavg().map((x) => x.toFixed(2)).join(' / '));
  try {
    const st = await fs.promises.statfs(os.homedir());
    const tot = st.blocks * st.bsize, av = st.bavail * st.bsize;
    const used = Math.round(((tot - av) * 100) / tot);
    lines.push('Hifadhi: ' + Math.round(tot / 1073741824) + ' GB, imejaa ' + used + '%');
    if (used >= 90) issues.push({ code: 'STORAGE_FULL', description: 'Hifadhi imejaa ' + used + '%' });
  } catch (e) { lines.push('Hifadhi: haikuweza kusomwa'); }
  lines.push('Imewaka kwa: ' + (os.uptime() / 3600).toFixed(1) + ' masaa');
  return { ok: true, issues, message: lines.join('\n') };
}

// ---------- USB (kutambua simu za button / hali za flash - server ikiwa PC) ----------
const USB_KNOWN = {
  '05c6:9008': 'Qualcomm EDL (9008) - hali ya emergency download',
  '04e8:685d': 'Samsung Download Mode (Odin)',
  '0e8d:0003': 'MediaTek Preloader (MTK)',
  '1782:4d00': 'Spreadtrum/Unisoc (SPD) - hali ya diag/download',
};
async function usbList() {
  let out = '', fmt = null;
  if (process.platform === 'linux' && !process.env.PREFIX) {
    const r = await run('lsusb', []);
    if (!r.ok && !r.out) return { error: 'lsusb haipo/imeshindwa: ' + (r.err || '') };
    out = r.out; fmt = 'lsusb';
  } else if (process.platform === 'win32') {
    const r = await run('powershell', ['-NoProfile', '-Command',
      "Get-CimInstance Win32_PnPEntity | Where-Object { $_.DeviceID -like 'USB\\VID_*' } | ForEach-Object { $_.DeviceID + ' | ' + $_.Name }"], 20000);
    if (!r.ok && !r.out) return { error: 'PowerShell imeshindwa: ' + (r.err || '') };
    out = r.out; fmt = 'win';
  } else {
    return { error: 'Server hii (' + os.platform() + (process.env.PREFIX ? '/Termux' : '') + ') haisomi USB. Endesha server kwenye PC (Windows/Linux) kutambua vifaa vya USB.' };
  }
  const devs = [];
  for (const l of out.split('\n')) {
    const m = fmt === 'lsusb' ? /ID ([0-9a-f]{4}):([0-9a-f]{4})\s*(.*)$/i.exec(l) : /VID_([0-9A-F]{4})&PID_([0-9A-F]{4})[^|]*\|\s*(.*)$/i.exec(l);
    if (!m) continue;
    const id = (m[1] + ':' + m[2]).toLowerCase(), name = (m[3] || '').trim();
    if (/root hub/i.test(name)) continue;
    if (!devs.some((d) => d.id === id && d.name === name)) devs.push({ id, name, mode: USB_KNOWN[id] || null });
  }
  return { devs };
}

app.get('/api/diagnose/local', async (req, res) => {
  try { res.json(await localInfo()); } catch (e) { res.json(fail(String((e && e.message) || e))); }
});

app.get('/api/usb', async (req, res) => {
  try {
    const r = await usbList();
    if (r.error) return res.json(fail(r.error));
    const text = r.devs.length ? r.devs.map((d) => d.id + ' ' + d.name + (d.mode ? '  => ' + d.mode : '')).join('\n')
      : 'Hakuna kifaa cha USB kilichotambuliwa (zaidi ya hubs).';
    res.json({ ok: true, devices: r.devs, message: text });
  } catch (e) { res.json(fail(String((e && e.message) || e))); }
});

app.get('/api/diagnose/obd/:ip', async (req, res) => {
  const ip = String(req.params.ip || '').trim(), port = validPort(req.query && req.query.port, 35000);
  if (!isPrivateIPv4(ip) || !port) return res.json(fail('Andika IP ya adapta ELM327 (mfano 192.168.0.10). Simu iunganishwe kwenye Wi-Fi ya adapta.'));
  try { res.json(await obdScan(ip, port)); } catch (e) { res.json(fail(String((e && e.message) || e))); }
});

app.get('/api/diagnose/modbus/:ip', async (req, res) => {
  const ip = String(req.params.ip || '').trim(), q = req.query || {};
  const port = validPort(q.port, 502), unit = Number(q.unit || 1), fn = Number(q.fn || 3), start = Number(q.start || 0), count = Number(q.count || 10);
  if (!isPrivateIPv4(ip) || !port) return res.json(fail('Andika IP ya kifaa cha Modbus (mtandao wa ndani).'));
  if (!(unit >= 0 && unit <= 247) || ![3, 4].includes(fn) || !(start >= 0 && start <= 65535) || !(count >= 1 && count <= 60))
    return res.json(fail('Vigezo si sahihi (unit 0-247, fn 3|4, start 0-65535, count 1-60).'));
  const r = await modbusRead(ip, port, unit, fn, start, count);
  if (r.error) return res.json(fail(r.error));
  const text = 'Modbus ' + ip + ':' + port + ' unit ' + unit + ' (' + (fn === 3 ? 'holding' : 'input') + ' registers)\n'
    + r.regs.map((v, i) => '[' + (start + i) + '] = ' + v).join('\n')
    + '\n(Maana ya kila register inategemea mtengenezaji wa kifaa.)';
  res.json({ ok: true, registers: r.regs, message: text });
});

// ---------- Malipo (rekodi tu, si uthibitisho) ----------
app.post('/api/payment/confirm', (req, res) => {
  const b = req.body || {};
  const company = String(b.company_number || '').trim();
  const ref = String(b.payment_ref || '').trim();
  const phone = b.customer_phone ? String(b.customer_phone).trim() : null;
  if (!company || ref.length < 5) return res.json(fail('Weka kampuni + kumbukumbu (herufi 5+)'));
  let old = '';
  try { old = fs.readFileSync(PAYMENTS_FILE, 'utf8'); } catch (e) { /* faili bado halipo */ }
  const dup = old.split('\n').filter(Boolean).some((l) => {
    try { const r = JSON.parse(l); return r.company === company && r.ref === ref; } catch (e) { return false; }
  });
  if (dup) return res.json(fail('Kumbukumbu hii ya malipo ilishaingizwa tayari.'));
  const rec = { id: Date.now().toString(36), at: new Date().toISOString(), company, ref, phone };
  fs.appendFileSync(PAYMENTS_FILE, JSON.stringify(rec) + '\n');
  res.json({ ok: true, verified: false, id: rec.id,
    message: 'Imerekodiwa (namba ' + rec.id + '). HAIJATHIBITISHWA na mtoa huduma wa malipo; server hii haiwezi kuthibitisha.' });
});

if (HOST !== '127.0.0.1') console.log('ONYO: server inasikiliza ' + HOST + ' - mtu yeyote kwenye mtandao huo anaweza kuitumia. Tumia mtandao unaoamini tu.');
app.listen(PORT, HOST, () => console.log('NJIAFIX server: ' + HOST + ':' + PORT + ' | Developer: Godwin Dotto'));
