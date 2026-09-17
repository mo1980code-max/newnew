/* Interactive logic for the mock. Every string, colour, array, dhikr and image comes from
   assets/data.js, which build_assets.py generates out of the repository's real resources
   (values/strings.xml, values-ar/*, res/raw/athkar.json, QiblaUtil, GetClocks, drawables). */

const D = window.APP_DATA;

const state = {
  lang: 'ar',            // 'ar' | 'en'  - the app itself defaults to English
  win: 'morning',        // 'morning' | 'evening' | 'none'  (PrayerWindow simulator)
  wp: 0,                 // index into D.wallpapers
  clock: { analog: 0, digital: 0, smart: 0 },
  kind: 'analog',
  unlocked: false,       // rewarded unlock, session only - exactly like the app
  tasbeeh: 0,
  tasbeehLock: false,
  sw: { hijri: true, dhikr: true, power: false, badge: true },
  counters: {},          // athkar id -> taps left
  cat: 'all',
  surah: 1,              // surah open in the Quran reader
  marked: {},            // "surah:ayah" -> saved, the mock's QuranStore bookmark
};

/* ─────────── i18n: the app's real string tables ─────────── */
const t = (k) => (D[state.lang][k] !== undefined ? D[state.lang][k] : (D.en[k] || k));
const arr = (n) => (state.lang === 'ar' ? (D.arrays_ar[n] || D.arrays_en[n]) : D.arrays_en[n]) || [];
const fmt = (s, ...a) => String(s).replace(/%(\d+)\$[ds]/g, (m, i) => a[+i - 1]);

/* Captions the mock needs but the app has no string for (page chrome, not app UI). */
const CAP = {
  ar: {
    home: 'الشاشة الرئيسية', quran: 'قارئ القرآن — نص متصل', wallpaper: 'الخلفية الحية',
    athkar: 'قارئ الأذكار',
    settings: 'إعدادات توقيت الأذكار', qibla: 'بوصلة القبلة', clocks: 'تصاميم الساعة',
    editor: 'محرّر الساعة', options: 'خيارات الخلفية', tasbeeh: 'السبحة العائمة',
    widgets: 'ودجت الشاشة الرئيسية', gallery: 'معرض الخلفيات الإسلامية',
    reward: 'فتح الساعات الذهبية',
    angle: 'زاوية حساب الفجر', start: 'بداية', end: 'نهاية',
    tasbeehHint: 'انقر الدائرة للعدّ · اضغط مطوّلًا 1.5ث للقفل · صفّر بضغط مطوّل 0.7ث على الزر الذهبي',
    noWindow: 'خارج نافذة الأذكار: الشارة مخفية والقارئ لا يُفتح (كما في التطبيق).',
    all: 'الكل', locked: 'مقفلة', watchToUnlock: 'شاهد إعلانًا لفتحها',
  },
  en: {
    home: 'Home screen', quran: 'Quran reader - one continuous page',
    wallpaper: 'Live wallpaper', athkar: 'Athkar reader',
    settings: 'Athkar timing settings', qibla: 'Qibla compass', clocks: 'Clock designs',
    editor: 'Clock editor', options: 'Wallpaper options', tasbeeh: 'Floating tasbeeh',
    widgets: 'Home-screen widgets', gallery: 'Islamic wallpaper gallery',
    reward: 'Unlocking the golden clocks',
    angle: 'Fajr angle', start: 'start', end: 'end',
    tasbeehHint: 'Tap the circle to count · hold 1.5s to lock · hold the golden button 0.7s to reset',
    noWindow: 'Outside the athkar window: the badge is hidden and the reader does not open, as in the app.',
    all: 'All', locked: 'Locked', watchToUnlock: 'Watch an ad to unlock',
  },
};
const cap = (k) => CAP[state.lang][k] || k;

/* ─────────── icons: Android VectorDrawable -> inline SVG ─────────── */
const svgCache = {};
async function loadIcons() {
  await Promise.all(D.icons.map(async (n) => {
    try { svgCache[n] = await (await fetch('assets/' + n + '.svg')).text(); } catch (e) { /* ignore */ }
  }));
}
function icon(name) {
  if (svgCache[name]) {
    // android:tint recolours every path, so currentColor is the faithful equivalent.
    return '<span class="icon">'
      + svgCache[name].replace(/fill="#[0-9A-Fa-f]{3,8}"/g, 'fill="currentColor"') + '</span>';
  }
  return '<img class="icon" src="assets/' + name + '.png" alt="">';
}
function applyIcons(root) {
  (root || document).querySelectorAll('[data-icon]')
    .forEach((el) => { el.innerHTML = icon(el.dataset.icon); });
}

/* ─────────── time, Hijri and dhikr: the same rules the app uses ─────────── */
function hijriParts(d) {
  try {
    const f = new Intl.DateTimeFormat('en-u-ca-islamic-umalqura-nu-latn',
      { day: 'numeric', month: 'numeric', year: 'numeric' });
    const p = {};
    f.formatToParts(d).forEach((x) => { p[x.type] = parseInt(x.value, 10); });
    return [p.day, p.month - 1, p.year];
  } catch (e) {
    return [1, 0, 1447];
  }
}
function hijriText(d) {
  const [day, mi, year] = hijriParts(d);
  const months = arr('hijri_months');
  return fmt(t('hijri_format'), day, months[mi] || '', year);
}
/* The overlay rotates its dhikr every few hours, so the mock derives the index the same way. */
function dhikrText(d) {
  const list = arr('adhkar');
  if (!list.length) return '';
  return list[Math.floor(d.getHours() / 3) % list.length];
}
function wallpaperUrl() { return 'assets/' + D.wallpapers[state.wp].file; }
function wallpaperName() { return D.wallpapers[state.wp][state.lang]; }
function clockImage() { return 'assets/' + D.clocks[state.kind][state.clock[state.kind]].file; }
function badgeOn() { return state.win !== 'none' && state.sw.badge; }
function badgeLabel() { return t(state.win === 'evening' ? 'athkar_evening_title' : 'athkar_morning_title'); }

/* ─────────── the Quran reader: one continuous page, verse by verse ───────────
   buildQuranPage mirrors QuranReaderActivity.buildPage(): every verse of the surah in a
   single run, each closed by U+06DD plus its number in Arabic-Indic digits. The Basmalah
   opening is the text of 1:1 straight out of the bundled asset - never typed by hand. */
function arabicIndic(value) {
  return String(value).replace(/[0-9]/g, (d) => String.fromCharCode(0x0660 + Number(d)));
}
function surahOf(n) { return D.quran.surahs.filter((s) => s.n === n)[0]; }
function ayahsOf(n) { return D.quran.ayahs[n] || []; }
function markKey(n, a) { return n + ':' + a; }
function isMarked(n, a) { return state.marked[markKey(n, a)] === true; }

function buildQuranPage(surahNumber) {
  const ayahs = ayahsOf(surahNumber);
  const parts = [];
  if (surahNumber !== 1 && surahNumber !== 9) {
    parts.push('<span class="basmalah">' + ayahsOf(1)[0] + '</span>');
  }
  ayahs.forEach((text, i) => {
    const saved = isMarked(surahNumber, i + 1) ? ' saved' : '';
    parts.push('<span class="verse' + saved + '" data-surah="' + surahNumber
      + '" data-ayah="' + (i + 1) + '">' + text + ' <span class="ayahMark">۝'
      + arabicIndic(i + 1) + '</span></span> ');
  });
  return parts.join('');
}

function toggleVerse(surahNumber, ayahNumber) {
  const key = markKey(surahNumber, ayahNumber);
  if (state.marked[key]) delete state.marked[key]; else state.marked[key] = true;
  renderQuran();
  return state.marked[key] === true;
}

function renderQuran() {
  const surah = surahOf(state.surah) || D.quran.surahs[0];
  document.getElementById('quranTitle').textContent =
    fmt(t('quran_surah_title'), surah.n, state.lang === 'ar' ? surah.arabic : surah.translit);
  const revelation = t(surah.meccan ? 'quran_meccan' : 'quran_medinan');
  document.getElementById('quranMeta').textContent =
    fmt(t('quran_surah_metadata'), surah.ayahs, revelation);
  document.getElementById('quranSheet').innerHTML = buildQuranPage(surah.n);
  const el = document.getElementById('surahName');
  if (el) el.textContent = surah.n + '. ' + (state.lang === 'ar' ? surah.arabic : surah.translit);
}

/* ─────────── rendering ─────────── */
function renderChrome() {
  document.querySelectorAll('[data-i18n]').forEach((el) => { el.textContent = t(el.dataset.i18n); });
  document.querySelectorAll('[data-cap]').forEach((el) => { el.textContent = cap(el.dataset.cap); });
  // The app mirrors its layouts in Arabic and lays them out LTR in English.
  document.querySelectorAll('.screen').forEach((s) => { s.dir = state.lang === 'ar' ? 'rtl' : 'ltr'; });
  document.getElementById('wpName').textContent = wallpaperName();
  document.getElementById('unlockBtn').textContent =
    state.unlocked ? t('premium_unlocked') : (state.lang === 'ar' ? 'شاهد إعلانًا (فتح الجلسة)' : 'Watch an ad (session unlock)');
  applyIcons();
}

function renderBadges() {
  const on = badgeOn(), label = on ? badgeLabel() : '';
  // The athkar tile is permanent; only the chip inside it follows the window.
  const chip = document.getElementById('homeAthkarChip');
  chip.textContent = label;
  chip.classList.toggle('hidden', !on);
  const wall = document.getElementById('wallBadge');
  wall.textContent = label;
  wall.classList.toggle('hidden', !on);
}

function renderWallpaper() {
  ['wallScreen', 'optionsBg', 'tasbeehBg', 'widgetBg'].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.style.backgroundImage = 'url("' + wallpaperUrl() + '")';
  });
  const ed = document.getElementById('editorScreen');
  if (ed) ed.style.backgroundImage = 'url("' + wallpaperUrl() + '")';

  const now = new Date();
  document.getElementById('wallHijri').classList.toggle('hidden', !state.sw.hijri);
  document.getElementById('wallHijri').textContent = hijriText(now);
  document.getElementById('wallDhikr').classList.toggle('hidden', !state.sw.dhikr);
  document.getElementById('wallDhikr').textContent = dhikrText(now);
  document.getElementById('wallClockImg').src = clockImage();

  document.getElementById('widgetImg22').src = clockImage();
  document.getElementById('widgetImg42').src = clockImage();
  document.getElementById('widgetHijri').textContent = hijriText(now);
  document.getElementById('widgetDhikr').textContent = dhikrText(now);
  document.getElementById('editorClockImg').src = clockImage();
}

function renderAthkar() {
  const title = document.getElementById('athkarTitle');
  const count = document.getElementById('athkarCount');
  const list = document.getElementById('athkarList');
  if (state.win === 'none') {
    // Exactly what the app does outside a window: the badge is hidden and the reader closes.
    title.textContent = t('athkar_morning_title');
    count.textContent = '';
    list.innerHTML = '<div class="athkarEmpty">' + cap('noWindow') + '</div>';
    return;
  }
  const type = state.win === 'evening' ? 'evening' : 'morning';
  const items = D.athkar.filter((i) => i.type === type);
  title.textContent = badgeLabel();
  count.textContent = fmt(t('athkar_count'), items.length);
  list.innerHTML = items.map((it, n) => {
    const left = state.counters[it.id] === undefined ? it.repeat : state.counters[it.id];
    const done = left <= 0;
    const ref = state.lang === 'ar' ? it.reference_ar : it.reference_en;
    const virtue = state.lang === 'ar' ? it.virtue_ar : it.virtue_en;
    return '<div class="athkarBlock' + (done ? ' done' : '') + '" data-id="' + it.id + '">'
      + '<div class="athkarHead">'
      + '<span class="idx">' + fmt(t('athkar_position'), n + 1, items.length) + '</span>'
      + '<span class="prog">' + fmt(t('athkar_progress'), Math.max(left, 0), it.repeat) + '</span>'
      + '<span class="fin' + (done ? '' : ' hidden') + '">' + t('athkar_completed') + '</span>'
      + '</div>'
      + '<div class="athkarRow">'
      + '<span class="num">' + Math.max(left, 0) + '</span>'
      + '<span class="ar">' + it.arabic_text + '</span>'
      + '</div>'
      + '<div class="tr' + (state.lang === 'ar' ? ' hidden' : '') + '">' + it.transliteration + '</div>'
      + '<div class="en">' + it.english_text + '</div>'
      + (virtue ? '<div class="virtue">' + virtue + '</div>' : '')
      + (ref ? '<div class="ref">' + ref + '</div>' : '')
      + '</div><div class="hairline"></div>';
  }).join('');

  list.querySelectorAll('.athkarBlock').forEach((b) => {
    b.addEventListener('click', () => {
      const id = parseInt(b.dataset.id, 10);
      const item = items.find((i) => i.id === id);
      const cur = state.counters[id] === undefined ? item.repeat : state.counters[id];
      if (cur <= 0) return;
      const left = cur - 1;
      state.counters[id] = left;
      if (navigator.vibrate) navigator.vibrate(12);
      /* Patch this one block in place: a full re-render would throw the reader back to the
         top of a list that is now 31 blocks long. */
      b.classList.toggle('done', left <= 0);
      b.querySelector('.num').textContent = String(left);
      b.querySelector('.prog').textContent = fmt(t('athkar_progress'), left, item.repeat);
      b.querySelector('.fin').classList.toggle('hidden', left > 0);
    });
  });
}

function renderQibla() {
  const letters = arr('compass_cardinals');
  const dial = document.getElementById('compassTicks');
  if (!dial.childElementCount) {
    let html = '<div class="ring"></div><div class="ring2"></div>';
    for (let deg = 0; deg < 360; deg += 15) {
      const cardinal = deg % 90 === 0;
      html += '<div class="tick' + (cardinal ? ' cardinal' : '') + '" style="transform:translate(-50%,0) rotate(' + deg + 'deg)"></div>';
      if (cardinal) {
        html += '<div class="lab" style="transform:rotate(' + deg + 'deg)">'
          + '<span style="transform:translate(-50%,-50%) rotate(' + (-deg) + 'deg)">'
          + (letters[deg / 90] || '') + '</span></div>';
      }
    }
    dial.innerHTML = html;
  } else {
    dial.querySelectorAll('.lab span').forEach((s, i) => { s.textContent = letters[i] || ''; });
  }
  // The dial is drawn with north up and the needle points at the Qibla bearing, so the whole
  // dial rotates by -bearing in the app; here the needle carries the bearing instead.
  document.getElementById('needle').style.transform = 'translate(-50%,0) rotate(' + D.qibla.bearing + 'deg)';
  document.getElementById('qiblaCity').textContent = arr('qibla_cities')[0] || '';
  document.getElementById('qiblaBearing').textContent = fmt(t('qibla_bearing'), D.qibla.bearing);
  document.getElementById('qiblaDistance').textContent = fmt(t('qibla_distance'), D.qibla.distance);
}

const KIND_TITLE = { analog: 'title_analog_clock', digital: 'title_digital_clock', smart: 'title_smart_clock' };

function renderClocks() {
  const box = document.getElementById('clockLists');
  box.innerHTML = ['analog', 'digital', 'smart'].map((kind) => {
    const items = D.clocks[kind];
    return '<div class="listTitle">' + t(KIND_TITLE[kind]) + ' · ' + items.length + '</div>'
      + '<div class="thumbs">' + items.map((c, i) => {
        const locked = c.premium && !state.unlocked;
        const sel = state.kind === kind && state.clock[kind] === i;
        return '<div class="thumb' + (sel ? ' sel' : '') + (locked ? '' : ' unlocked') + '"'
          + ' data-kind="' + kind + '" data-i="' + i + '">'
          + '<img src="assets/' + c.file + '" alt="">'
          + (locked ? '<div class="lock">' + icon('ic_lock_gold') + '<small>' + cap('watchToUnlock') + '</small></div>' : '')
          + '</div>';
      }).join('') + '</div>';
  }).join('');
  applyIcons(box);

  box.querySelectorAll('.thumb').forEach((th) => {
    th.addEventListener('click', () => {
      const kind = th.dataset.kind, i = parseInt(th.dataset.i, 10);
      const item = D.clocks[kind][i];
      if (item.premium && !state.unlocked) {
        document.getElementById('unlockState').textContent =
          (state.lang === 'ar' ? 'الساعة مقفلة — ' : 'This clock is locked — ') + cap('watchToUnlock');
        document.getElementById('ph-reward').scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
      }
      state.kind = kind;
      state.clock[kind] = i;
      renderClocks();
      renderWallpaper();
    });
  });
}

function renderGallery() {
  const groups = [
    ['all', cap('all')], ['kaaba', t('cat_kaaba')], ['madina', t('cat_madina')],
    ['aqsa', t('cat_aqsa')], ['mosque', t('cat_mosques')],
  ];
  const cats = document.getElementById('galleryCats');
  cats.innerHTML = groups.map((g) => '<div class="cat' + (state.cat === g[0] ? ' on' : '') + '" data-cat="' + g[0] + '">' + g[1] + '</div>').join('');
  cats.querySelectorAll('.cat').forEach((c) => {
    c.addEventListener('click', () => { state.cat = c.dataset.cat; renderGallery(); });
  });

  const shown = D.wallpapers
    .map((w, i) => ({ w, i }))
    .filter((o) => state.cat === 'all' || o.w.file.indexOf('wp_' + state.cat) === 0
      || (state.cat === 'mosque' && o.w.file.indexOf('wp_min') === 0));
  const grid = document.getElementById('galleryGrid');
  grid.innerHTML = shown.map((o) => '<div class="thumb' + (o.i === state.wp ? ' sel' : '') + '" data-wp="' + o.i + '">'
    + '<img src="assets/' + o.w.file + '" alt="">'
    + '<div class="cap">' + o.w[state.lang] + '</div></div>').join('');
  grid.querySelectorAll('.thumb').forEach((th) => {
    th.addEventListener('click', () => {
      state.wp = parseInt(th.dataset.wp, 10);
      renderGallery();
      renderChrome();
      renderWallpaper();
    });
  });
}

function renderOptions() {
  document.querySelectorAll('.sw').forEach((sw) => {
    const key = sw.dataset.sw;
    sw.classList.toggle('on', !!state.sw[key]);
    sw.onclick = () => {
      state.sw[key] = !state.sw[key];
      renderOptions();
      renderBadges();
      renderWallpaper();
    };
  });
}

function renderTasbeeh() {
  document.getElementById('tasbeehCount').textContent = String(state.tasbeeh);
  document.getElementById('tasbeehLock').classList.toggle('hidden', !state.tasbeehLock);
}

function renderReward() {
  document.getElementById('unlockState').textContent =
    state.unlocked ? t('premium_unlocked') : t('premium_not_unlocked');
}

function renderAll() {
  renderChrome();
  renderBadges();
  renderWallpaper();
  renderQuran();
  renderAthkar();
  renderQibla();
  renderClocks();
  renderGallery();
  renderOptions();
  renderTasbeeh();
  renderReward();
}

/* ─────────── long press, the way the tasbeeh service measures it ─────────── */
function longPress(el, ms, fn) {
  let timer = null;
  const start = (e) => {
    timer = setTimeout(() => { fn(); timer = null; e.preventDefault(); }, ms);
  };
  const cancel = () => { if (timer) { clearTimeout(timer); timer = null; } };
  el.addEventListener('mousedown', start);
  el.addEventListener('touchstart', start, { passive: true });
  ['mouseup', 'mouseleave', 'touchend', 'touchcancel'].forEach((ev) => el.addEventListener(ev, cancel));
}

/* ─────────── wiring ─────────── */
function wire() {
  document.querySelectorAll('#langSeg button').forEach((b) => {
    b.addEventListener('click', () => {
      state.lang = b.dataset.lang;
      document.querySelectorAll('#langSeg button').forEach((x) => x.classList.toggle('on', x === b));
      document.getElementById('athkarList').scrollTop = 0;
      document.getElementById('quranScroll').scrollTop = 0;
      renderAll();
    });
  });

  document.querySelectorAll('#winSeg button').forEach((b) => {
    b.addEventListener('click', () => {
      state.win = b.dataset.win;
      document.querySelectorAll('#winSeg button').forEach((x) => x.classList.toggle('on', x === b));
      renderBadges();
      renderAthkar();
    });
  });

  const step = (d) => {
    state.wp = (state.wp + d + D.wallpapers.length) % D.wallpapers.length;
    renderGallery();
    renderChrome();
    renderWallpaper();
  };
  document.getElementById('nextWp').addEventListener('click', () => step(1));
  document.getElementById('prevWp').addEventListener('click', () => step(-1));

  document.getElementById('unlockBtn').addEventListener('click', () => {
    state.unlocked = !state.unlocked;
    renderChrome();
    renderClocks();
    renderReward();
  });
  document.getElementById('rewardWatch').addEventListener('click', () => {
    state.unlocked = true;
    renderChrome();
    renderClocks();
    renderReward();
  });

  const circle = document.getElementById('tasbeehCircle');
  circle.addEventListener('click', () => {
    if (state.tasbeehLock) return;
    state.tasbeeh += 1;
    if (navigator.vibrate) navigator.vibrate(12);
    renderTasbeeh();
  });
  longPress(circle, 1500, () => { state.tasbeehLock = !state.tasbeehLock; renderTasbeeh(); });
  longPress(document.getElementById('tasbeehReset'), 700, () => {
    state.tasbeeh = 0;
    renderTasbeeh();
  });

  const step_surah = (d) => {
    state.surah = ((state.surah - 1 + d + D.quran.surahs.length) % D.quran.surahs.length) + 1;
    renderQuran();
    document.getElementById('quranScroll').scrollTop = 0;
  };
  document.getElementById('prevSurah').addEventListener('click', () => step_surah(1));
  document.getElementById('nextSurah').addEventListener('click', () => step_surah(-1));

  document.getElementById('quranSheet').addEventListener('click', (e) => {
    const verse = e.target.closest('.verse');
    if (!verse) return;
    toggleVerse(Number(verse.dataset.surah), Number(verse.dataset.ayah));
  });
  document.getElementById('quranBack').addEventListener('click', () => {
    document.getElementById('ph-home').scrollIntoView({ behavior: 'smooth', block: 'center' });
  });

  document.getElementById('athkarPage').addEventListener('click', (e) => {
    if (e.target.closest('.gear')) document.getElementById('ph-settings').scrollIntoView({ behavior: 'smooth', block: 'center' });
    if (e.target.closest('.x')) document.getElementById('ph-home').scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
}

(async function boot() {
  await loadIcons();
  wire();
  renderAll();
  // The clock faces and the Hijri line are live in the app, so keep them live here too.
  setInterval(() => { renderWallpaper(); }, 30000);
})();
