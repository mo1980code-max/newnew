/* Headless smoke test for the mock: runs app.js against a minimal DOM shim and asserts that
   what it renders is the repository's real content.

       node tools/preview/smoke.js

   No browser exists in the sandbox, so this is the cheapest way to catch a typo, a missing
   element id or a broken format string before the page is ever looked at.
*/
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const HERE = __dirname;

function makeEl(id) {
  const el = {
    id,
    _cls: new Set(),
    style: {},
    dataset: {},
    textContent: '',
    innerHTML: '',
    childElementCount: 0,
    scrollTop: 0,
    dir: '',
    classList: {
      add: (c) => el._cls.add(c),
      remove: (c) => el._cls.delete(c),
      toggle: (c, on) => {
        if (on === undefined) on = !el._cls.has(c);
        if (on) el._cls.add(c); else el._cls.delete(c);
        return on;
      },
      contains: (c) => el._cls.has(c),
    },
    addEventListener: () => {},
    querySelectorAll: () => [],
    querySelector: () => makeEl('child'),
    closest: () => null,
    scrollIntoView: () => {},
  };
  return el;
}

const els = {};
const sandbox = {
  console,
  navigator: {},
  setInterval: () => 0,
  setTimeout: () => 0,
  clearTimeout: () => {},
  fetch: async () => ({ text: async () => '<svg></svg>' }),
  document: {
    getElementById: (id) => (els[id] = els[id] || makeEl(id)),
    querySelectorAll: () => [],
  },
};
sandbox.window = sandbox;
vm.createContext(sandbox);

vm.runInContext(fs.readFileSync(path.join(HERE, 'assets', 'data.js'), 'utf8'), sandbox);
vm.runInContext(fs.readFileSync(path.join(HERE, 'app.js'), 'utf8'), sandbox);

let failures = 0;
function check(label, ok, detail) {
  console.log((ok ? '  ok   ' : '  FAIL ') + label + (ok ? '' : '  -> ' + detail));
  if (!ok) failures += 1;
}

/* app.js boots in an async IIFE: give it a few turns of the event loop. */
setTimeout(() => {
  const D = sandbox.APP_DATA;
  const html = (id) => els[id] ? els[id].innerHTML : '';
  const text = (id) => (els[id] ? els[id].textContent : '');

  console.log('\nathkar reader (the screen that was just redesigned)');
  const morning = D.athkar.filter((i) => i.type === 'morning');
  const blocks = html('athkarList').split('class="athkarBlock').length - 1;
  check('31 morning blocks rendered', blocks === morning.length, blocks + ' != ' + morning.length);
  check('the count badge shows the list size', text('athkarCount') === morning.length + ' ذكرًا',
    JSON.stringify(text('athkarCount')));
  check('the title is the real string', text('athkarTitle') === D.ar.athkar_morning_title,
    JSON.stringify(text('athkarTitle')));
  check('position label formatted (الذكر 1 من 31)',
    html('athkarList').indexOf('الذكر 1 من 31') >= 0, 'not found');
  check('progress formatted (1 / 1)', html('athkarList').indexOf('1 / 1') >= 0, 'not found');
  check('the 100x dhikr renders its count', html('athkarList').indexOf('>100<') >= 0, 'not found');
  check('reward note rendered',
    html('athkarList').indexOf('من قالها حين يصبح أجير من الجن') >= 0, 'not found');
  check('source line rendered',
    html('athkarList').indexOf('[آية الكرسى - البقرة 255]') >= 0, 'not found');
  check('the isti\'adha line break survived', html('athkarList').indexOf('أَعُوذُ بِاللهِ') >= 0,
    'not found');
  check('transliteration hidden in the Arabic UI',
    html('athkarList').indexOf('class="tr hidden"') >= 0, 'not found');
  check('no counter circle left in the markup', html('athkarList').indexOf('class="counter') < 0,
    'a circle is still being rendered');

  console.log('\nthe rest of the mock');
  check('Hijri line formatted from hijri_format', /\d+ .+ \d+ هـ/.test(text('wallHijri')),
    JSON.stringify(text('wallHijri')));
  check('rotating dhikr comes from R.array.adhkar',
    D.arrays_ar.adhkar.indexOf(text('wallDhikr')) >= 0, JSON.stringify(text('wallDhikr')));
  check('qibla bearing from QiblaUtil\'s coordinates',
    text('qiblaBearing').indexOf(String(D.qibla.bearing)) >= 0, JSON.stringify(text('qiblaBearing')));
  check('qibla distance rendered', text('qiblaDistance').indexOf(String(D.qibla.distance)) >= 0,
    JSON.stringify(text('qiblaDistance')));
  check('compass cardinals drawn', html('compassTicks').split('class="lab"').length - 1 === 4,
    html('compassTicks').slice(0, 80));
  // 'class="thumb' would also match the .thumbs grid, so count the design attribute instead.
  const thumbs = html('clockLists').split('data-kind=').length - 1;
  check('all 35 clock designs rendered', thumbs === 35, String(thumbs));
  const locks = html('clockLists').split('class="lock"').length - 1;
  check('9 premium locks (last 3 of each list)', locks === 9, String(locks));
  check('wallpaper gallery shows all 21', html('galleryGrid').split('class="thumb').length - 1 === 21,
    String(html('galleryGrid').split('class="thumb').length - 1));
  check('wallpaper applied to the live-wallpaper screen',
    els.wallScreen.style.backgroundImage.indexOf(D.wallpapers[0].file) >= 0,
    JSON.stringify(els.wallScreen.style.backgroundImage));
  check('the athkar tile carries the golden chip inside the window',
    !els.homeAthkarChip._cls.has('hidden') && text('homeAthkarChip') === D.ar.athkar_morning_title,
    JSON.stringify(text('homeAthkarChip')));
  check('reward state uses the real string', text('unlockState') === D.ar.premium_not_unlocked,
    JSON.stringify(text('unlockState')));

  console.log('\nquran reader (one continuous Mushaf page)');
  const totalAyahs = Object.keys(D.quran.ayahs).reduce((n, k) => n + D.quran.ayahs[k].length, 0);
  check('114 surahs in the metadata', D.quran.surahs.length === 114,
    String(D.quran.surahs.length));
  check('6236 ayahs in the bundled text', totalAyahs === 6236, String(totalAyahs));
  const expectTitle = D.ar.quran_surah_title
    .replace('%1$d', '1').replace('%2$s', D.quran.surahs[0].arabic);
  check('the reader title uses quran_surah_title', text('quranTitle') === expectTitle,
    JSON.stringify(text('quranTitle')));
  check('the surah picker names the open surah',
    text('surahName') === '1. ' + D.quran.surahs[0].arabic, JSON.stringify(text('surahName')));
  const fatiha = html('quranSheet');
  check('all 7 verses of Al-Fatiha are on the page',
    (fatiha.match(/class="verse/g) || []).length === 7,
    String((fatiha.match(/class="verse/g) || []).length));
  const mark7 = String.fromCharCode(0x06DD) + vm.runInContext('arabicIndic(7)', sandbox);
  check('a verse closes with U+06DD and an Arabic-Indic number',
    fatiha.indexOf(mark7) >= 0, 'marker ' + mark7 + ' not found');
  check('Al-Fatiha gets no separate Basmalah line (it is verse 1)',
    fatiha.indexOf('class="basmalah"') < 0, 'a Basmalah line was added');
  const baqara = vm.runInContext('buildQuranPage(2)', sandbox);
  check('surah 2 renders all 286 verses',
    (baqara.match(/class="verse/g) || []).length === 286,
    String((baqara.match(/class="verse/g) || []).length));
  check('surah 2 opens with the Basmalah taken from the asset',
    baqara.indexOf('class="basmalah">' + D.quran.ayahs[1][0]) >= 0, 'no Basmalah line');
  vm.runInContext('state.surah = 2; renderQuran()', sandbox);
  check('tapping a verse saves it', vm.runInContext('toggleVerse(2, 255)', sandbox) === true,
    'toggleVerse returned false');
  check('the saved verse is tinted in place',
    /class="verse saved" data-surah="2" data-ayah="255"/.test(html('quranSheet')),
    html('quranSheet').slice(0, 90));
  vm.runInContext('state.surah = 1; renderQuran()', sandbox);

  console.log(failures ? '\n' + failures + ' CHECK(S) FAILED' : '\nALL CHECKS PASSED');
  process.exit(failures ? 1 : 0);
}, 60);
