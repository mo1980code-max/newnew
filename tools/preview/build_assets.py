#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Builds the interactive HTML mock's assets straight out of the Android resources.

    python3 tools/preview/build_assets.py

Everything the mock shows comes from the repository: the English/Arabic string tables, every
string-array, res/raw/athkar.json, the colours, the bundled wallpapers, the real clock
thumbnails, the vector icons (converted to SVG) and a Qibla bearing/distance computed with the
same formula QiblaUtil uses. Nothing is typed by hand, so the mock cannot drift from the app.

Generated output lands in tools/preview/assets/ which is git-ignored: it is ~3 MB of copied
wallpapers and thumbnails, reproducible with this script in about a second.
"""
import glob
import json
import math
import os
import re
import shutil
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
MAIN = os.path.join(ROOT, 'app', 'src', 'main')
RES = os.path.join(MAIN, 'res')
JAVA = os.path.join(MAIN, 'java', 'org', 'Allah_Clock_Live_Wallpaper')
ASSETS = os.path.join(HERE, 'assets')
A = '{http://schemas.android.com/apk/res/android}'

# The generated directory is the only thing wiped: the mock's own sources are hand-written.
if os.path.isdir(ASSETS):
    shutil.rmtree(ASSETS)
os.makedirs(ASSETS)

data = {}


# ══════════════════════════════════ strings ══════════════════════════════════
def read_strings(path):
    out = {}
    if not os.path.exists(path):
        return out
    for node in ET.parse(path).getroot().findall('string'):
        name = node.get('name')
        text = (node.text or '')
        # Android escapes ' and " with a backslash; the browser must not see the backslash.
        out[name] = text.replace("\\'", "'").replace('\\"', '"').strip()
    return out


data['en'] = read_strings(os.path.join(RES, 'values', 'strings.xml'))
data['ar'] = read_strings(os.path.join(RES, 'values-ar', 'strings.xml'))
# Any key missing from values-ar falls back to English in the app, so the mock does the same.
for key, value in data['en'].items():
    data['ar'].setdefault(key, value)
print('strings: en=%d ar=%d' % (len(data['en']), len(data['ar'])))


# ══════════════════════════════════ arrays ══════════════════════════════════
def read_arrays(folder):
    out = {}
    for path in sorted(glob.glob(os.path.join(RES, folder, '*.xml'))):
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for node in root.findall('string-array'):
            items = [(i.text or '').strip() for i in node.findall('item')]
            out[node.get('name')] = items
    return out


data['arrays_en'] = read_arrays('values')
data['arrays_ar'] = read_arrays('values-ar')
# adhkar (the wallpaper overlay rotation) is intentionally Arabic in both languages.
for name, items in data['arrays_en'].items():
    data['arrays_ar'].setdefault(name, items)
print('arrays: %s' % ', '.join('%s=%d' % (k, len(v)) for k, v in sorted(data['arrays_en'].items())))


# ══════════════════════════════════ colours ══════════════════════════════════
colors = {}
for node in ET.parse(os.path.join(RES, 'values', 'colors.xml')).getroot().findall('color'):
    colors[node.get('name')] = (node.text or '').strip()
data['colors'] = colors
print('colours: %d' % len(colors))


# ══════════════════════════════════ athkar ══════════════════════════════════
athkar = json.load(open(os.path.join(RES, 'raw', 'athkar.json'), encoding='utf-8'))
data['athkar'] = athkar['athkar'] if isinstance(athkar, dict) else athkar
morning = sum(1 for i in data['athkar'] if i['type'] == 'morning')
print('athkar: %d items (%d morning / %d evening)'
      % (len(data['athkar']), morning, len(data['athkar']) - morning))


# ══════════════════════════════════ quran ══════════════════════════════════
# The reader shows the official Uthmanic text (Hafs reading) verbatim, so the mock loads the
# same two assets the app bundles: the edition text and the repository's companion metadata
# (surah names, the 604 Madani pages, the 30 juzs).
ASSETS_DIR = os.path.join(MAIN, 'assets')
edition = json.load(open(os.path.join(ASSETS_DIR, 'quran.json'), encoding='utf-8'))
metadata = json.load(open(os.path.join(ASSETS_DIR, 'quran_info.json'), encoding='utf-8'))


def display_surah_name(official):
    """Mirror of QuranText.displaySurahName(): drop the leading "سورة" and the diacritics."""
    value = official.strip()
    prefix = 'سورة'
    letters, cursor = 0, 0
    while cursor < len(value) and letters < len(prefix):
        char = value[cursor]
        if 0x064B <= ord(char) <= 0x065F or 0x06D6 <= ord(char) <= 0x06ED:
            cursor += 1
            continue
        if char != prefix[letters]:
            break
        letters += 1
        cursor += 1
    if letters == len(prefix):
        value = value[cursor:].strip()
    if not value:
        return official.strip()
    plain = ''.join(c for c in value
                    if not (0x064B <= ord(c) <= 0x065F or 0x06D6 <= ord(c) <= 0x06ED
                            or c in '\u0670\u0640'))
    return plain.strip() or official.strip()


surahs = []
for chapter in metadata['chapters']:
    surahs.append({
        'n': chapter['chapter'],
        'ayahs': len(chapter['verses']),
        'arabic': display_surah_name(chapter['arabicname']),
        'translit': chapter['name'],
        'meaning': chapter['englishname'],
        'meccan': chapter['revelation'] == 'Mecca',
    })

ayahs = {}
for row in edition['quran']:
    ayahs.setdefault(row['chapter'], []).append(row['text'])

assert len(surahs) == 114, 'the Quran metadata must hold 114 surahs'
assert sum(len(v) for v in ayahs.values()) == 6236, 'the Quran text must hold 6236 ayahs'
data['quran'] = {'surahs': surahs, 'ayahs': ayahs}
print('quran: %d surahs, %d ayahs (%d KB of text)'
      % (len(surahs), sum(len(v) for v in ayahs.values()),
         os.path.getsize(os.path.join(ASSETS_DIR, 'quran.json')) // 1024))


# ══════════════════════════════ the reader's page breaks ══════════════════════════════
# QuranText.withEndGlyph closes every verse with U+06DD followed by the verse number in
# Arabic-Indic digits; the continuous column breaks its visual pages there. The mock mirrors
# that rule, so it never has to guess where a page ends.
PAGE_SEPARATOR = chr(0x06DD)


def arabic_indic(value):
    return ''.join(chr(0x0660 + int(digit)) for digit in str(value))


surah_by_number = {s['n']: s for s in surahs}
TARGET_SURAH, TARGET_AYAH = 1, 1      # Al-Fatiha: its Basmalah is verse 1, so there is no opening
run, page_breaks = '', []
for index, text in enumerate(ayahs[TARGET_SURAH]):
    mark = ' ' + PAGE_SEPARATOR + arabic_indic(index + 1) + ' '
    run += text + mark
    # The page may break right after the marker, never inside the verse that owns it. Searching
    # from the previous break proves the string only occurs at the marker and not in the text.
    found = run.find(mark, page_breaks[-1] if page_breaks else 0)
    assert found == len(run) - len(mark), (
        'the marker of verse %d is not where withEndGlyph() puts it' % (index + 1))
    page_breaks.append(len(run))
assert run.count(PAGE_SEPARATOR) == len(page_breaks) == surah_by_number[TARGET_SURAH]['ayahs']
data['quranPages'] = {'surah': TARGET_SURAH, 'firstAyah': TARGET_AYAH, 'breaks': page_breaks}
print('quran reader: surah %d, %d marked verses, breaks at %s'
      % (TARGET_SURAH, len(page_breaks), page_breaks[:8]))


# ══════════════════════════════════ qibla ══════════════════════════════════
KAABA_LAT, KAABA_LON = 21.422487, 39.826206
coords = re.search(r'CITY_COORDS\s*=\s*\{(.*?)\};',
                   open(os.path.join(JAVA, 'utils', 'QiblaUtil.java'), encoding='utf-8').read(),
                   re.S).group(1)
pairs = [tuple(float(n) for n in p.split(',')) for p in re.findall(r'\{([^}]*)\}', coords)]
cities = data['arrays_en']['qibla_cities']
assert len(pairs) == len(cities), 'city names and coordinates disagree: %d vs %d' % (
    len(pairs), len(cities))


def qibla(lat, lon):
    """QiblaUtil's bearing (initial great-circle bearing to the Kaaba) and distance in km."""
    phi1, phi2 = math.radians(lat), math.radians(KAABA_LAT)
    dlon = math.radians(KAABA_LON - lon)
    y = math.sin(dlon)
    x = math.cos(phi1) * math.tan(phi2) - math.sin(phi1) * math.cos(dlon)
    bearing = (math.degrees(math.atan2(y, x)) + 360.0) % 360.0
    dphi = math.radians(KAABA_LAT - lat)
    dl = dlon
    h = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dl / 2) ** 2
    return round(bearing), round(6371.0 * 2 * math.atan2(math.sqrt(h), math.sqrt(1 - h)))


data['qiblaCities'] = [{'lat': p[0], 'lon': p[1]} for p in pairs]
data['qibla'] = dict(zip(('bearing', 'distance'), qibla(*pairs[0])))
print('qibla %s: %s' % (cities[0], data['qibla']))


# ══════════════════════════════════ wallpapers ══════════════════════════════════
CAPTIONS = {
    'premium': ('Premium', 'حصري'),
    'kaaba': ('Kaaba, Makkah', 'الكعبة المشرّفة'),
    'aqsa': ('Al-Aqsa, Jerusalem', 'المسجد الأقصى'),
    'madina': ('Prophet\'s Mosque, Madinah', 'المسجد النبوي'),
    'mosque': ('Mosque', 'مسجد'),
    'min': ('Minaret', 'مئذنة'),
}
# Which backgrounds sit behind the rewarded ad. Read out of WallpaperCatalog so the mock can
# never disagree with the app about what is locked.
catalog = open(os.path.join(JAVA, 'utils', 'WallpaperCatalog.java'), encoding='utf-8').read()
premium_block = re.search(r'PREMIUM\s*=\s*\{(.*?)\}', catalog, re.S).group(1)
premium_names = set(re.findall(r'R\.drawable\.(wp_\w+)', premium_block))
assert premium_names, 'WallpaperCatalog.PREMIUM is empty - the reward gate would be open'

wallpapers = []
for path in sorted(glob.glob(os.path.join(RES, 'drawable-nodpi', 'wp_*.jpg'))):
    name = os.path.basename(path)
    shutil.copy(path, os.path.join(ASSETS, name))
    parts = name[3:-4].rsplit('_', 1)
    key, index = parts[0], int(parts[1]) if len(parts) > 1 and parts[1].isdigit() else 1
    en, ar = CAPTIONS.get(key, (key.title(), key))
    wallpapers.append({'file': name, 'en': '%s %d' % (en, index), 'ar': '%s %d' % (ar, index),
                       'premium': name[:-4] in premium_names})
data['wallpapers'] = wallpapers
# Premium first, exactly like WallpaperActivity.mergedWithPremium().
data['wallpapers'].sort(key=lambda w: (not w['premium'], w['file']))
print('wallpapers copied: %d (%d premium)'
      % (len(wallpapers), sum(1 for w in wallpapers if w['premium'])))


# ══════════════════════════════════ clock designs ══════════════════════════════════
def clock_list(method, thumb_re):
    src = open(os.path.join(JAVA, 'utils', 'GetClocks.java'), encoding='utf-8').read()
    body = src.split(method, 1)[1].split('return arrayList;', 1)[0]
    return re.findall(thumb_re, body)


def copy_thumbs(names, prefix):
    """The last three of every list carry the golden lock (the app's premium rule)."""
    out = []
    for i, name in enumerate(names):
        found = glob.glob(os.path.join(RES, 'drawable*', '%s.png' % name))
        if not found:
            print('  ! missing thumbnail %s.png' % name)
            continue
        shutil.copy(found[0], os.path.join(ASSETS, '%s.png' % name))
        out.append({'file': '%s.png' % name, 'premium': i >= len(names) - 3, 'index': i + 1})
    print('%s: %d designs (%d premium)' % (prefix, len(out), sum(1 for o in out if o['premium'])))
    return out


data['clocks'] = {
    'analog': copy_thumbs(['clock_bg_%d' % n for n in range(1, 15)], 'analog'),
    'digital': copy_thumbs(clock_list('getTextClocks()', r'R\.drawable\.(digital_thumb_\d+)'), 'digital'),
    'smart': copy_thumbs(clock_list('getSmartClocks()', r'R\.drawable\.(smart_thumb_\d+)'), 'smart'),
}

# The home screen's two tiles use these rasters.
for raster in ('big_', 'clock_logo', 'wallpaper_logo', 'star'):
    found = glob.glob(os.path.join(RES, 'drawable*', '%s.png' % raster))
    if found:
        shutil.copy(found[0], os.path.join(ASSETS, '%s.png' % raster))
        data.setdefault('rasters', []).append('%s.png' % raster)
print('rasters: %s' % data.get('rasters'))


# ══════════════════════════════════ vector icons -> svg ══════════════════════════════════
def vector_to_svg(path):
    root = ET.parse(path).getroot()
    if root.tag != 'vector':
        return None
    vb = '%s %s' % (root.get(A + 'viewportWidth', '24'), root.get(A + 'viewportHeight', '24'))
    body = []

    def walk(node, opacity):
        for child in node:
            tag = child.tag.split('}')[-1]
            if tag == 'group':
                walk(child, opacity)
            elif tag == 'path':
                d = child.get(A + 'pathData')
                if not d:
                    continue
                fill = child.get(A + 'fillColor')
                alpha = child.get(A + 'fillAlpha')
                # android:tint recolours every path, so currentColor is the faithful equivalent
                # and the mock can ink an icon for a light or a dark surface with CSS alone.
                attr = 'fill="%s"' % ('none' if not fill else 'currentColor')
                a = alpha or opacity
                if a and fill:
                    attr += ' fill-opacity="%s"' % a
                body.append('<path d="%s" %s/>' % (d, attr))

    walk(root, None)
    if not body:
        return None
    return ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="%s" width="24" height="24">%s</svg>'
            % (vb, ''.join(body)))


icons = []
for path in sorted(glob.glob(os.path.join(RES, 'drawable', 'ic_*.xml'))):
    svg = vector_to_svg(path)
    if svg is None:
        continue
    name = os.path.basename(path)[:-4]
    open(os.path.join(ASSETS, '%s.svg' % name), 'w', encoding='utf-8').write(svg)
    icons.append(name)
data['icons'] = icons
print('icons -> svg: %d' % len(icons))

# Shapes the mock redraws in CSS (badge pill, paper chip) - radii straight out of the XML.
shapes = {}
for path in sorted(glob.glob(os.path.join(RES, 'drawable', 'bg_*.xml'))):
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        continue
    solid = root.find('solid')
    corners = root.find('corners')
    stroke = root.find('stroke')
    shapes[os.path.basename(path)[:-4]] = {
        'color': solid.get(A + 'color') if solid is not None else None,
        'radius': corners.get(A + 'radius') if corners is not None else None,
        'stroke': stroke.get(A + 'color') if stroke is not None else None,
    }
data['shapes'] = shapes
print('shapes: %d' % len(shapes))

# The night palette and the glass tokens, read from QuranTheme and the colour table so the mock's
# dark page and frosted bar are the app's own values.
data['night'] = {}
# Explicit reads: QuranTheme resolves each field from exactly one R.color.
for res in ('quranNightPaper', 'quranNightSurface', 'quranNightInk', 'quranNightBody',
            'quranNightMuted', 'quranNightLine', 'quranNightGreen', 'quranNightGold'):
    data['night'][res] = colors.get(res)
assert all(data['night'].values()), 'a night colour is missing from values/colors.xml'
data['glass'] = {k: colors.get(k) for k in (
    'glassTileTop', 'glassTileBottom', 'glassTileRim', 'glassTileGold', 'glassIconIvory',
    'homeBackdropTop', 'homeBackdropBottom')}
print('night palette + glass tokens read from colors.xml')

with open(os.path.join(ASSETS, 'data.js'), 'w', encoding='utf-8') as fh:
    fh.write('window.APP_DATA = ')
    json.dump(data, fh, ensure_ascii=False, indent=1)
    fh.write(';\n')
print('data.js written (%d KB)' % (os.path.getsize(os.path.join(ASSETS, 'data.js')) // 1024))
