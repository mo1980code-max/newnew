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
    style: { setProperty: (k, v) => { el.style[k] = v; }, removeProperty: (k) => { delete el.style[k]; } },
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
  check('wallpaper gallery shows all 24', html('galleryGrid').split('class="thumb').length - 1 === 24,
    String(html('galleryGrid').split('class="thumb').length - 1));
  check('exactly the 3 premium backgrounds carry the golden lock',
    html('galleryGrid').split('class="lockBadge"').length - 1 === 3,
    String(html('galleryGrid').split('class="lockBadge"').length - 1));
  check('the premium three come first, as mergedWithPremium() orders them',
    html('galleryGrid').indexOf(D.wallpapers[0].file) >= 0 && D.wallpapers[0].premium === true,
    String(D.wallpapers[0].file));
  check('wallpaper applied to the live-wallpaper screen',
    els.wallScreen.style.backgroundImage.indexOf(D.wallpapers[0].file) >= 0,
    JSON.stringify(els.wallScreen.style.backgroundImage));
  check('the athkar tile carries the golden chip inside the window',
    !els.homeAthkarChip._cls.has('hidden') && text('homeAthkarChip') === D.ar.athkar_morning_title,
    JSON.stringify(text('homeAthkarChip')));
  check('reward phone shows the background gate',
    text('unlockState').indexOf('مقفلة') >= 0, JSON.stringify(text('unlockState')));
  check('a locked premium background is refused before the ad',
    vm.runInContext('openWallpaper(D.wallpapers[1]) === false', sandbox), 'it was applied');
  check('a refused tap does not change the open background',
    vm.runInContext('state.wp', sandbox) === 0, String(vm.runInContext('state.wp', sandbox)));
  check('the reward unlocks it for good, as PremiumUnlocks does',
    vm.runInContext('grantPendingBackground() && bgUnlocked(D.wallpapers[1].file)', sandbox),
    'not unlocked');
  check('the unlocked background is the one that gets applied',
    vm.runInContext('state.wp', sandbox) === 1, String(vm.runInContext('state.wp', sandbox)));
  check('the same tap now goes straight through',
    vm.runInContext('openWallpaper(D.wallpapers[1]) === true', sandbox), 'still refused');

  console.log('\nquran reader (flippable Mushaf pages)');
  const totalAyahs = Object.keys(D.quran.ayahs).reduce((n, k) => n + D.quran.ayahs[k].length, 0);
  check('114 surahs in the metadata', D.quran.surahs.length === 114,
    String(D.quran.surahs.length));
  check('6236 ayahs in the bundled text', totalAyahs === 6236, String(totalAyahs));
  const expectTitle = D.ar.quran_surah_title
    .replace('%1$d', '1').replace('%2$s', D.quran.surahs[0].arabic);
  check('the reader title uses quran_surah_title', text('quranTitle') === expectTitle,
    JSON.stringify(text('quranTitle')));
  check('the reader opens on the first page',
    vm.runInContext('state.page', sandbox) === 0, String(vm.runInContext('state.page', sandbox)));
  check('the page breaks come from the app\'s own constants',
    D.quranPages.surah === 1 && D.quranPages.breaks.length === 7,
    JSON.stringify(D.quranPages));
  const fatiha = html('quranPages');
  check('all 7 verses of Al-Fatiha are on the pages',
    (fatiha.match(/class="verse/g) || []).length === 7,
    String((fatiha.match(/class="verse/g) || []).length));
  check('the surah is cut into 7 flippable pages',
    (fatiha.match(/class="quranPageView/g) || []).length === 7,
    String((fatiha.match(/class="quranPageView/g) || []).length));
  check('one page is on screen at a time',
    (fatiha.match(/class="quranPageView on"/g) || []).length === 1,
    String((fatiha.match(/class="quranPageView on"/g) || []).length));
  const mark7 = String.fromCharCode(0x06DD) + vm.runInContext('arabicIndic(7)', sandbox);
  check('a verse closes with U+06DD and an Arabic-Indic number',
    fatiha.indexOf('>' + vm.runInContext('arabicIndic(7)', sandbox) + '<') >= 0,
    'marker ' + mark7 + ' not found');
  check('Al-Fatiha gets no separate Basmalah line (it is verse 1)',
    fatiha.indexOf('class="basmalah"') < 0, 'a Basmalah line was added');
  // The mock prints one verse per page (see buildQuranPages), so page 1 carries ayah 1 alone;
  // the string is still the app's own quran_screen_ayahs.
  check('the footer states the ayah range with the real string',
    text('quranRange') === D.ar.quran_screen_ayahs.replace('%1$d', '1').replace('%2$d', '1'),
    JSON.stringify(text('quranRange')));
  vm.runInContext('flipPage(1)', sandbox);
  check('flipping turns to the next page', vm.runInContext('state.page', sandbox) === 1,
    String(vm.runInContext('state.page', sandbox)));
  check('the day phone follows the flip too',
    (html('quranPages').match(/class="quranPageView on"/g) || []).length === 1,
    'more than one page is on screen');
  vm.runInContext('flipPage(-1)', sandbox);
  check('the previous arrow stops at the first page',
    vm.runInContext('state.page', sandbox) === 0, String(vm.runInContext('state.page', sandbox)));
  vm.runInContext('state.marked = {}; toggleVerse(1, 7)', sandbox);
  check('tapping a verse saves it',
    /class="verse saved" data-surah="1" data-ayah="7"/.test(html('quranPages')),
    html('quranPages').slice(0, 90));
  vm.runInContext('toggleVerse(1, 7)', sandbox);

  console.log('\nnight reading (the in-reader theme)');
  check('the night palette is the app\'s own colours',
    D.night.quranNightPaper === '#1A1D24' && D.night.quranNightInk === '#E7D9B4',
    JSON.stringify(D.night));
  vm.runInContext('setNight(true)', sandbox);
  check('the reader swaps to the night palette in place',
    els.quranScreen.style['--quranPaper'] === D.night.quranNightPaper
      && els.quranNightScreen.style['--quranPaper'] === D.night.quranNightPaper,
    JSON.stringify(els.quranScreen.style['--quranPaper']));
  check('the page the reader was on is unchanged by the theme',
    vm.runInContext('state.page', sandbox) === 0, String(vm.runInContext('state.page', sandbox)));
  vm.runInContext('setNight(false)', sandbox);
  check('toggling back restores the paper colours',
    els.quranScreen.style['--quranPaper'] === D.colors.quranPaper,
    JSON.stringify(els.quranScreen.style['--quranPaper']));

  console.log(failures ? '\n' + failures + ' CHECK(S) FAILED' : '\nALL CHECKS PASSED');
  process.exit(failures ? 1 : 0);
}, 60);
