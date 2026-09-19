/* Interactive logic for the mock. Every string, colour, array, dhikr and image comes from
   assets/data.js, which build_assets.py generates out of the repository's real resources
   (values/strings.xml, values-ar/*, res/raw/athkar.json, QiblaUtil, GetClocks, drawables). */

const D = window.APP_DATA;

const state = {
  lang: 'ar',            // 'ar' | 'en'  - the app itself defaults to English
  win: 'morning',        // 'morning' | 'evening' | 'none'  (PrayerWindow simulator)
  wp: 0,                 // index into D.wallpapers
  bgUnlocked: {},        // premium background file -> unlocked for good (PremiumUnlocks)
  clock: { analog: 0, digital: 0, smart: 0 },
  kind: 'analog',
  unlocked: false,       // rewarded unlock, session only - exactly like the app
  tasbeeh: 0,
  tasbeehLock: false,
  sw: { hijri: true, dhikr: true, power: false, badge: true },
  counters: {},          // athkar id -> taps left
  cat: 'all',
  page: 0,               // current position in the Quran reader preview
  night: false,          // the reader's own night theme, not the system one
  wash: 0,               // index into D.washes: the reader's background (QuranTheme.STYLES)
  sajdahSurah: false,    // false = al-Fatiha, true = surat as-Sajdah (carries ayah 32:15)
  sajdahOpen: false,     // the prostration note under the page
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
    page: 'صفحة', of: 'من', night: 'وضع القراءة الليلية', day: 'وضع القراءة النهارية',
    premiumBg: 'خلفيات حصرية (مكافأة)', unlockPermanent: 'فتح دائم بعد الإعلان',
    backdrop: 'الشريط العلوي الزجاجي والخلفية المتدرجة',
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
    page: 'Page', of: 'of', night: 'Night reading theme', day: 'Day reading theme',
    premiumBg: 'Premium backgrounds (rewarded)', unlockPermanent: 'Permanent unlock after the ad',
    backdrop: 'The frosted top bar and the gradient backdrop',
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

/* ─────────── the Quran reader: one surah as a continuous scroll ───────────
   The bordered inline number mirrors Android's AyahNumberSpan visually; it does not rely on
   a font combining U+06DD and digits. The app uses a vertical RecyclerView, while this mock
   groups the same ayahs at real Madani page boundaries. */
function arabicIndic(value) {
  return String(value).replace(/[0-9]/g, (d) => String.fromCharCode(0x0660 + Number(d)));
}
function surahOf(n) { return D.quran.surahs.filter((s) => s.n === n)[0]; }
function ayahsOf(n) { return D.quran.ayahs[n] || []; }
function markKey(n, a) { return n + ':' + a; }
function isMarked(n, a) { return state.marked[markKey(n, a)] === true; }

/* The reader appears on two phones (day and night) and both show the same surah, so the page
   HTML is written into both pagers and the palette into both screens. */
const QURAN_PAGERS = ['quranPages', 'quranNightPage'];
const QURAN_SCREENS = ['quranScreen', 'quranNightScreen'];

/* The reader's palette, in both themes, taken from colors.xml (and the quranNight* tokens added
   for the in-reader toggle). Setting them as CSS variables is what makes one class flip the whole
   page - sheet, ink, ornaments, footer - exactly like QuranTheme.apply() does in the app. */
/* The wash itself, read from D.washes - which build_assets.py reads out of values/colors.xml and
   out of res/drawable/quran_bg_*.xml, so the mock paints the same gradient, lamp light and frame
   the APK draws. One CSS variable set per wash, exactly like QuranTheme.apply(). */
function applyWash(el, wash, night) {
  const P = wash.palette;
  const pairs = [
    ['--quranPaper', P.paper],
    ['--quranSurface', P.surface],
    ['--quranInk', P.ink],
    ['--quranBody', P.body],
    ['--quranMuted', P.muted],
    ['--quranLine', P.line],
    ['--quranGreen', P.green],
    ['--quranGreenDark', P.greenDark],
    ['--quranSoftGreen', P.softGreen],
    ['--quranGold', P.gold],
    ['--quranBgTop', wash.top],
    ['--quranBgBottom', wash.bottom],
    ['--quranBgGlow', wash.glow],
    ['--quranBgFrame', wash.frame || wash.top],
  ];
  pairs.forEach((p) => el.style.setProperty(p[0], p[1]));
  el.classList.toggle('night', night);
}

/* Kept for the night phone beside the reader: it always shows the night wash. */
function applyQuranPalette(el, night) {
  applyWash(el, washOf(night ? 'night' : washKey()), night);
}

function washKey() { return D.washes[state.wash].key; }
function washOf(key) { return D.washes.filter((w) => w.key === key)[0]; }

/* The reader's target: al-Fatiha by default, surat as-Sajdah when the sajdah demo is on, so the
   mihrab marker and the footer chip are both visible without paging through 200 Madani pages. */
function readerTarget() {
  return state.sajdahSurah ? D.quranSajdahPages : D.quranPages;
}

/* The prostration flag of one ayah, straight off the metadata the app reads (QuranAyah.sajdah). */
function sajdahOf(surah, ayah) {
  return (D.quran.sajdahs || []).filter((x) => x.s === surah && x.a === ayah)[0] || null;
}

/* The mihrab SajdahMarkerSpan draws: an arch, a base and the lamp dot inside it. Drawn as markup
   here for the same reason it is drawn from a Path in the app - no font has to carry U+06E9. */
function sajdahMark() {
  return '<span class="sajdahMark"><svg viewBox="0 0 24 24" aria-hidden="true">'
    + '<path d="M7.2 20 L7.2 10.4 Q7.2 4.6 12 2.4 Q16.8 4.6 16.8 10.4 L16.8 20" fill="none" '
    + 'stroke="currentColor" stroke-width="1.7"/>'
    + '<path d="M5.4 20.6 L18.6 20.6" stroke="currentColor" stroke-width="1.7" '
    + 'stroke-linecap="round"/>'
    + '<circle cx="12" cy="14.2" r="1.6" fill="currentColor"/></svg></span>';
}

/* @return one entry per page: its markup plus the ayah range it carries, which is what the
   footer prints (the app's Screen carries the same two numbers). */
function buildQuranPages() {
  const target = readerTarget();
  const n = target.surah;
  const breaks = target.breaks;
  const pages = [];
  let html = '', first = 1;
  ayahsOf(n).forEach((text, i) => {
    const ayah = i + 1;
    const saved = isMarked(n, ayah) ? ' saved' : '';
    const mark = sajdahOf(n, ayah) ? sajdahMark() : '';
    html += '<span class="verse' + saved + '" data-surah="' + n + '" data-ayah="' + ayah + '">'
      + text + '\u00A0<span class="ayahNumber">' + arabicIndic(ayah) + '</span>' + mark + '</span> ';
    if (breaks.indexOf(ayah) >= 0) {
      pages.push({ html: html, first: first, last: ayah,
        number: target.numbers[pages.length] });
      html = '';
      first = ayah + 1;
    }
  });
  if (html) pages.push({ html: html, first: first, last: ayahsOf(n).length });
  return pages;
}

function renderQuranPages() {
  const pages = buildQuranPages();
  const html = pages.map((page, i) => '<div class="quranScrollPage' + (i === state.page ? ' on' : '')
    + '" data-page="' + i + '"><div class="quranSheet"><p>' + page.html
    + '</p></div></div>').join('');
  QURAN_PAGERS.forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = html;
    el.dir = 'rtl';           // a Mushaf page always reads right to left, in both languages
  });
}

function quranPageCount() { return readerTarget().breaks.length; }
function quranPage() { return buildQuranPages()[state.page]; }

function flipPage(delta) {
  const count = quranPageCount();
  state.page = Math.max(0, Math.min(state.page + delta, count - 1));
  renderQuran();
}

/* The three deep washes are the app's isNightStyle(): emerald, night and midnight. */
function isNightWash(index) {
  return ['green', 'night', 'midnight'].indexOf(D.washes[index].key) >= 0;
}

/* Paints the wash that is selected right now onto both phones. Nothing else touches the
   palette, so a wash switch never depends on the night flag being consistent. */
function applyWashes() {
  QURAN_SCREENS.forEach((id, i) => {
    const el = document.getElementById(id);
    if (!el) return;
    // Only the second phone is the night-reading figure, so it stays dark whatever the reader
    // picked; the first phone follows the wash exactly like the app's own reader.
    const dark = state.night || i > 0;
    applyWash(el, i > 0 ? washOf('night') : D.washes[state.wash], dark);
  });
  document.querySelectorAll('[data-icon="ic_quran_night"]').forEach((el, i) => {
    el.classList.toggle('nightOn', state.night || i > 0);
  });
  const label = document.getElementById('quranNightState');
  if (label) label.textContent = state.night ? cap('night') : cap('day');
}

/* One place changes the wash, exactly like QuranReaderActivity.applyBackground(style): the
   palette is swapped on the live element and the page keeps its place. */
function setWash(index) {
  state.wash = index;
  state.night = isNightWash(index);
  // A full re-render, the way the app repaints every surface from the one QuranTheme instance;
  // renderQuran() ends in applyWashes(), so nothing here re-derives the night flag.
  renderQuran();
}

/* The night button is a shortcut to the night wash, as QuranTheme.nextNightStyle() is. */
function setNight(on) {
  const target = D.washes.filter((w) => w.key === (on ? 'night' : 'paper'))[0];
  setWash(Math.max(0, D.washes.indexOf(target)));
}

function toggleVerse(surahNumber, ayahNumber) {
  const key = markKey(surahNumber, ayahNumber);
  if (state.marked[key]) delete state.marked[key]; else state.marked[key] = true;
  renderQuran();
  return state.marked[key] === true;
}

/* The six washes as swatches, each painted with its own quran_bg_*.xml gradient, the way
   QuranReaderDialogs lists them. Tapping one is what the app's dialog row does. */
function renderWashRow() {
  const row = document.getElementById('quranWashRow');
  if (!row) return;
  row.innerHTML = D.washes.map((w, i) => '<button class="washSwatch'
    + (i === state.wash ? ' on' : '') + '" data-wash="' + i + '" title="' + t(w.name)
    + '" style="background:linear-gradient(' + w.top + ',' + w.bottom + ');'
    + 'border-color:' + (w.frame || w.top) + '"><span style="color:' + w.palette.gold
    + '">&#10003;</span></button>').join('');
  const label = document.getElementById('quranWashName');
  if (label) label.textContent = t(D.washes[state.wash].name);
}

/* The footer's gold chip: present only while the open page carries one of the fifteen. */
function renderSajdahChip() {
  const target = readerTarget();
  const open = quranPage();
  const here = open ? (D.quran.sajdahs || []).filter((x) => x.s === target.surah
    && x.a >= open.first && x.a <= open.last) : [];
  const hit = here[0] || null;
  const chip = document.getElementById('quranSajdah');
  if (chip) {
    chip.classList.toggle('hidden', !hit);
    chip.textContent = t('quran_sajdah_chip');
  }
  const note = document.getElementById('quranSajdahNote');
  if (note) {
    const show = Boolean(hit) && state.sajdahOpen;
    note.classList.toggle('hidden', !show);
    if (show) {
      const surah = surahOf(hit.s);
      note.innerHTML = '<b>' + fmt(t('quran_sajdah_title'), hit.no) + '</b>'
        + '<span>' + fmt(t('quran_sajdah_ayah'), hit.s, hit.a, hit.page) + ' · '
        + (hit.oblig ? t('quran_sajdah_obligatory') : t('quran_sajdah_recommended')) + ' · '
        + surah.arabic + '</span>'
        + '<span>' + t('quran_sajdah_howto') + '</span>';
    }
  }
}

function renderQuran() {
  const target = readerTarget();
  const surah = surahOf(target.surah);
  document.getElementById('quranTitle').textContent =
    fmt(t('quran_surah_title'), surah.n, state.lang === 'ar' ? surah.arabic : surah.translit);
  const revelation = t(surah.meccan ? 'quran_meccan' : 'quran_medinan');
  document.getElementById('quranMeta').textContent =
    fmt(t('quran_surah_metadata'), surah.ayahs, revelation);
  renderQuranPages();

  const count = quranPageCount();
  state.page = Math.max(0, Math.min(state.page, count - 1));
  const open = quranPage();
  const range = document.getElementById('quranRange');
  if (range && open) range.textContent = fmt(t('quran_page_number'), open.number, 604);
  renderWashRow();
  renderSajdahChip();
  applyWashes();
  // The chip inside the page footer and the read-out beside the demo's page stepper show the
  // same numbers; both are updated from the one place.
  const label = cap('page') + ' ' + arabicIndic(state.page + 1) + ' ' + cap('of') + ' '
    + arabicIndic(count);
  ['quranPageChip', 'quranPageIndicator'].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.textContent = label;
  });
  const prev = document.getElementById('quranPrev'), next = document.getElementById('quranNext');
  if (prev) { prev.disabled = state.page === 0; prev.classList.toggle('off', state.page === 0); }
  if (next) {
    next.disabled = state.page >= count - 1;
    next.classList.toggle('off', state.page >= count - 1);
  }
}

/* ─────────── rendering ─────────── */
/* The top bar: the same six tiles the layout declares, frosted with the colours the app's
   bg_glass_tile.xml uses, so the mock shows the real translucency rather than a flat grey. */
function renderGlassBar() {
  const G = D.glass;
  document.querySelectorAll('.glassTile').forEach((tile) => {
    tile.style.background = 'linear-gradient(' + G.glassTileTop + ', ' + G.glassTileBottom + ')';
    tile.style.borderColor = G.glassTileRim;
    tile.style.color = G.glassIconIvory;
    tile.style.boxShadow = '0 6px 14px #0006, 0 1px 0 #ffffff1f inset, 0 1px 0 '
      + G.glassTileGold + ' inset';
  });
  const home = document.getElementById('homeScreen');
  if (home) {
    home.style.background = 'radial-gradient(120% 60% at 50% 0%, ' + G.homeBackdropTop
      + ', ' + G.homeBackdropBottom + ')';
  }
}

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
    ['all', cap('all')], ['premium', t('cat_premium')], ['kaaba', t('cat_kaaba')],
    ['madina', t('cat_madina')], ['aqsa', t('cat_aqsa')], ['mosque', t('cat_mosques')],
  ];
  const cats = document.getElementById('galleryCats');
  cats.innerHTML = groups.map((g) => '<div class="cat' + (state.cat === g[0] ? ' on' : '') + '" data-cat="' + g[0] + '">' + g[1] + '</div>').join('');
  cats.querySelectorAll('.cat').forEach((c) => {
    c.addEventListener('click', () => { state.cat = c.dataset.cat; renderGallery(); });
  });

  const shown = D.wallpapers
    .map((w, i) => ({ w, i }))
    .filter((o) => state.cat === 'all' || (o.w.premium && state.cat === 'premium')
      || o.w.file.indexOf('wp_' + state.cat) === 0
      || (state.cat === 'mosque' && o.w.file.indexOf('wp_min') === 0));
  const grid = document.getElementById('galleryGrid');
  grid.innerHTML = shown.map((o) => {
    const locked = o.w.premium && !bgUnlocked(o.w.file);
    return '<div class="thumb' + (o.i === state.wp ? ' sel' : '')
      + (o.w.premium ? ' premium' : '') + (locked ? ' locked' : '') + '" data-wp="' + o.i + '">'
      + '<img src="assets/' + o.w.file + '" alt="">'
      + '<div class="cap">' + o.w[state.lang] + '</div>'
      + (locked ? '<div class="lockBadge">' + icon('ic_lock_gold')
          + '<small>' + t('premium_bg_badge') + '</small></div>' : '')
      + '</div>';
  }).join('');
  applyIcons(grid);
  grid.querySelectorAll('.thumb').forEach((th) => {
    th.addEventListener('click', () => { openWallpaper(D.wallpapers[parseInt(th.dataset.wp, 10)]); });
  });
}

function bgUnlocked(file) { return state.bgUnlocked[file] === true; }

/* The tap on a wallpaper tile, exactly as WallpaperActivity routes it through
   PremiumBackgroundHelper.onBackgroundClick(): a locked premium item opens the rewarded
   explanation and changes nothing else; anything else is applied straight away.
   @return true when the background was applied. */
function openWallpaper(item) {
  if (item.premium && !bgUnlocked(item.file)) {
    pendingBackground = item.file;
    renderReward();
    const phone = document.getElementById('ph-reward');
    if (phone && phone.scrollIntoView) {
      phone.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    return false;
  }
  applyBackground(item);
  return true;
}

/* The reward callback: the only place a background is ever unlocked, in the mock as in the app,
   where PremiumUnlocks.unlock() runs inside AdMob's onRewardEarned(). */
function grantPendingBackground() {
  if (!pendingBackground) return false;
  state.bgUnlocked[pendingBackground] = true;
  const item = D.wallpapers.filter((w) => w.file === pendingBackground)[0];
  if (item) applyBackground(item); else renderGallery();
  renderReward();
  return true;
}

function applyBackground(item) {
  state.wp = D.wallpapers.indexOf(item);
  renderGallery();
  renderChrome();
  renderWallpaper();
}

let pendingBackground = null;

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
  // The reward phone shows the background gate, which is the flow this release adds: the dialog
  // is dialog_reward_unlock.xml, its copy comes from the premium_bg_* strings, and #rewardWatch
  // is the rewarded ad itself.
  const state_ = document.getElementById('unlockState');
  if (pendingBackground) {
    state_.textContent = (bgUnlocked(pendingBackground) ? t('premium_bg_unlocked') + ' · ' : '')
      + cap('unlockPermanent');
  } else {
    state_.textContent = cap('locked') + ' — ' + cap('watchToUnlock');
  }
}

function renderAll() {
  renderGlassBar();
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
    if (grantPendingBackground()) {
      return;
    }
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

  // Turning the page: the same position change shown by the continuous reader preview.
  document.getElementById('prevPage').addEventListener('click', () => flipPage(-1));
  document.getElementById('nextPage').addEventListener('click', () => flipPage(1));
  const inPagePrev = document.getElementById('quranPrev');
  const inPageNext = document.getElementById('quranNext');
  if (inPagePrev) inPagePrev.addEventListener('click', () => flipPage(-1));
  if (inPageNext) inPageNext.addEventListener('click', () => flipPage(1));
  document.querySelectorAll('[data-icon="ic_quran_night"]').forEach((el) => {
    el.addEventListener('click', () => setNight(!state.night));
  });
  const nightBtn = document.getElementById('nightToggle');
  if (nightBtn) nightBtn.addEventListener('click', () => setNight(!state.night));

  QURAN_PAGERS.forEach((id) => {
    const host = document.getElementById(id);
    if (!host) return;
    host.addEventListener('click', (e) => {
      const verse = e.target.closest('.verse');
      if (!verse) return;
      toggleVerse(Number(verse.dataset.surah), Number(verse.dataset.ayah));
    });
  });
  // The reading wash: the picker the app opens from ic_quran_background, as a swatch row.
  const washRow = document.getElementById('quranWashRow');
  if (washRow) washRow.addEventListener('click', (e) => {
    const btn = e.target.closest ? e.target.closest('.washSwatch') : null;
    if (!btn) return;
    setWash(parseInt(btn.dataset.wash, 10));
  });
  const washBtn = document.getElementById('quranWashBtn');
  if (washBtn) washBtn.addEventListener('click', () => {
    // Cycling is the mock's shortcut for the dialog: paper -> parchment -> ... -> midnight.
    setWash((state.wash + 1) % D.washes.length);
  });
  // The sajdah demo: surat as-Sajdah carries 32:15, one of the four obligatory prostrations.
  const sajdahToggle = document.getElementById('quranSajdahToggle');
  if (sajdahToggle) sajdahToggle.addEventListener('click', () => {
    state.sajdahSurah = !state.sajdahSurah;
    state.page = 0;
    state.sajdahOpen = false;
    renderQuran();
  });
  const sajdahChip = document.getElementById('quranSajdah');
  if (sajdahChip) sajdahChip.addEventListener('click', () => {
    state.sajdahOpen = !state.sajdahOpen;
    renderQuran();
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
