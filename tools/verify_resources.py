#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Static resource verifier for the app module.

    python3 tools/verify_resources.py

There is no JDK in the sandbox, so nothing here compiles the app. What it does instead is
check, across the whole resource tree, the classes of mistake that a build would otherwise
catch - and that a hand-edited bilingual app makes often:

  1. every res XML parses;
  2. values-ar carries every default string, with the same format arguments, and every
     string-array with the same number of items (an array may stay default-only on purpose,
     like the Arabic adhkar shown in both languages);
  3. apostrophes are escaped the way aapt2 demands;
  4. every R.something referenced from Java is actually declared somewhere;
  5. every @type/name referenced from a layout or drawable resolves;
  6. every findViewById(R.id.x) has a matching @+id/x in some layout;
  7. Java braces balance (a crude syntax sniff, but it catches a bad edit fast);
  8. the bundled Quran assets are byte-identical to the reviewed upstream repository (git blob
     SHAs pinned), carry all 114 surahs / 6,236 ayahs, and their 604 Madani pages and 30 juzs
     cover every ayah exactly once.

Exit code is non-zero when anything fails, so it can gate a commit.
"""
import glob
import hashlib
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
MAIN = os.path.join(ROOT, 'app', 'src', 'main')
RES = os.path.join(MAIN, 'res')
JAVA = os.path.join(MAIN, 'java')
A = '{http://schemas.android.com/apk/res/android}'

errors = []
notes = []


def err(msg):
    errors.append(msg)


# ══════════════════════════ 1. every resource XML parses ══════════════════════════
xml_files = sorted(glob.glob(os.path.join(RES, '**', '*.xml'), recursive=True))
parsed = {}
for path in xml_files:
    try:
        parsed[path] = ET.parse(path).getroot()
    except ET.ParseError as e:
        err('%s does not parse: %s' % (os.path.relpath(path, ROOT), e))
print('resource xml files: %d (%d parsed)' % (len(xml_files), len(parsed)))


# ══════════════════════════ collect declared resources ══════════════════════════
declared = {'string': set(), 'color': set(), 'dimen': set(), 'style': set(), 'plurals': set(),
            'integer': set(), 'bool': set(), 'attr': set(), 'array': set(), 'id': set(),
            'string_array': {}}
FORMAT_RE = re.compile(r'%(\d+)\$([ds])')

for path, root in parsed.items():
    folder = os.path.basename(os.path.dirname(path))
    if not folder.startswith('values'):
        continue
    for node in root:
        tag = node.tag.split('}')[-1]
        name = node.get('name')
        if tag == 'string' and name:
            declared['string'].add(name)
            if folder == 'values':
                declared.setdefault('_strings_en', {})[name] = node
        elif tag in ('color', 'dimen', 'integer', 'bool', 'attr') and name:
            declared[tag if tag != 'attr' else 'attr'].add(name)
        elif tag == 'style' and name:
            declared['style'].add(name)
        elif tag == 'plurals' and name:
            declared['plurals'].add(name)
        elif tag in ('string-array', 'integer-array', 'array') and name:
            declared['array'].add(name)
            declared['string_array'].setdefault(folder, {})[name] = len(node.findall('item'))

# drawable / layout / raw / font / mipmap / xml / anim families
for kind in ('drawable', 'layout', 'raw', 'font', 'mipmap', 'xml', 'anim', 'color'):
    names = set()
    for path in glob.glob(os.path.join(RES, kind + '*', '*')):
        if os.path.isdir(path):
            continue
        base = os.path.basename(path)
        # any file counts (athkar.json in res/raw is content, not a bitmap): only the
        # extension and a .9 patch suffix are dropped
        names.add(re.sub(r'(\.9)?\.[A-Za-z0-9]+$', '', base))
    declared[kind + '_files'] = names

# ids declared by layouts (and by <item type="id">)
for path, root in parsed.items():
    for node in root.iter():
        for attr, value in node.attrib.items():
            if attr.endswith('id') and isinstance(value, str) and value.startswith('@+id/'):
                declared['id'].add(value[5:])
        if node.tag == 'item' and node.get('type') == 'id' and node.get('name'):
            declared['id'].add(node.get('name'))

print('declared: %d strings, %d ids, %d arrays, %d drawables, %d layouts'
      % (len(declared['string']), len(declared['id']), len(declared['array']),
         len(declared['drawable_files']), len(declared['layout_files'])))


# ══════════════════════════ 2/3. values-ar parity, formats, apostrophes ══════════════════════════
def strings_of(folder):
    out = {}
    for path, root in parsed.items():
        if os.path.basename(os.path.dirname(path)) != folder:
            continue
        for node in root.findall('string'):
            out[node.get('name')] = (node.text or '')
    return out


en = strings_of('values')
ar = strings_of('values-ar')
missing_ar = sorted(set(en) - set(ar))
extra_ar = sorted(set(ar) - set(en))
if missing_ar:
    err('values-ar is missing %d string(s): %s' % (len(missing_ar), ', '.join(missing_ar[:8])))
if extra_ar:
    err('values-ar declares %d string(s) with no default: %s' % (len(extra_ar), ', '.join(extra_ar[:8])))

for name in sorted(set(en) & set(ar)):
    a, b = sorted(FORMAT_RE.findall(en[name])), sorted(FORMAT_RE.findall(ar[name]))
    if a != b:
        err('format args differ for %s: en=%s ar=%s' % (name, a, b))

APOS = re.compile(r"(?<!\\)'")
for folder, table in (('values', en), ('values-ar', ar)):
    for name, text in sorted(table.items()):
        stripped = text.strip()
        if stripped.startswith('"') and stripped.endswith('"'):
            continue  # a fully quoted string may contain a bare apostrophe
        if APOS.search(text):
            err("%s/%s has an unescaped apostrophe: %r" % (folder, name, text[:60]))

en_arrays = declared['string_array'].get('values', {})
ar_arrays = declared['string_array'].get('values-ar', {})
for name, count in sorted(en_arrays.items()):
    if name in ar_arrays and ar_arrays[name] != count:
        err('array %s has %d items in values but %d in values-ar'
            % (name, count, ar_arrays[name]))
for name in sorted(set(ar_arrays) - set(en_arrays)):
    err('array %s exists only in values-ar (no default fallback)' % name)
notes.append('arrays default-only on purpose: %s'
             % ', '.join(sorted(set(en_arrays) - set(ar_arrays))) or 'arrays: all localised')
print('strings: en=%d ar=%d | arrays: en=%d ar=%d'
      % (len(en), len(ar), len(en_arrays), len(ar_arrays)))


# ══════════════════════════ 4. R.* references from Java ══════════════════════════
# The lookbehind keeps framework references (android.R.id.content) out of the check.
R_REF = re.compile(r'(?<!android\.)(?<![\w.])R\.(string|color|dimen|style|plurals|integer|bool'
                   r'|attr|array|id|drawable|layout|raw|font|mipmap|xml|anim)'
                   r'\.([A-Za-z_][A-Za-z0-9_]*)')
FAMILY = {'drawable': 'drawable_files', 'layout': 'layout_files', 'raw': 'raw_files',
          'font': 'font_files', 'mipmap': 'mipmap_files', 'xml': 'xml_files',
          'anim': 'anim_files', 'color': 'color'}
java_files = sorted(glob.glob(os.path.join(JAVA, '**', '*.java'), recursive=True))
for path in java_files:
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    for kind, name in R_REF.findall(src):
        if kind in ('drawable', 'layout', 'raw', 'font', 'mipmap', 'xml', 'anim'):
            pool = declared[FAMILY[kind]]
        elif kind == 'color':
            # a colour may be declared in values/colors.xml or as a res/color selector file
            pool = declared['color'] | declared['color_files']
        else:
            pool = declared[kind]
        if name not in pool:
            err('R.%s.%s referenced in %s is not declared' % (kind, name, rel))
    # 7. brace balance over the code only. Regexes cannot do this: a URL inside a string
    #    ("https://...") looks like a line comment, and an apostrophe inside a comment looks
    #    like a char literal, so the source is walked once with a small scanner instead.
    opens = closes = 0
    i, n = 0, len(src)
    state = None  # None | 'line' | 'block' | 'str' | 'chr'
    while i < n:
        c = src[i]
        two = src[i:i + 2]
        if state is None:
            if two == '//':
                state, i = 'line', i + 2
                continue
            if two == '/*':
                state, i = 'block', i + 2
                continue
            if c == '"':
                state, i = 'str', i + 1
                continue
            if c == "'":
                state, i = 'chr', i + 1
                continue
            if c == '{':
                opens += 1
            elif c == '}':
                closes += 1
        elif state == 'line':
            if c == '\n':
                state = None
        elif state == 'block':
            if two == '*/':
                state, i = None, i + 2
                continue
        elif state in ('str', 'chr'):
            quote = '"' if state == 'str' else "'"
            if c == '\\':
                i += 2
                continue
            if c == quote or c == '\n':
                state = None
        i += 1
    if opens != closes:
        err('unbalanced braces in %s (%d open / %d close)' % (rel, opens, closes))
print('java files checked: %d' % len(java_files))


# ══════════════════════════ 5. @type/name references from XML ══════════════════════════
XML_REF = re.compile(r'"@(?:android:)?(\+?)(string|color|dimen|style|integer|bool|attr|array'
                     r'|id|drawable|layout|raw|font|mipmap|xml|anim)/([A-Za-z0-9_.]+)"')
for path, root in parsed.items():
    rel = os.path.relpath(path, ROOT)
    for node in root.iter():
        for value in node.attrib.values():
            if not isinstance(value, str) or '@' not in value:
                continue
            for plus, kind, name in XML_REF.findall(value):
                if kind in ('drawable', 'layout', 'raw', 'font', 'mipmap', 'xml', 'anim'):
                    pool = declared[FAMILY[kind]] if kind in FAMILY else declared[kind + '_files']
                elif kind == 'color':
                    pool = declared['color'] | declared['color_files']
                else:
                    pool = declared[kind]
                if plus or kind == 'id':
                    continue  # @+id/... declares rather than references
                if name not in pool:
                    err('@%s/%s in %s does not resolve' % (kind, name, rel))
print('xml references checked in %d files' % len(parsed))


# ══════════════════════════ 6. findViewById targets exist ══════════════════════════
FIND = re.compile(r'findViewById\(\s*R\.id\.([A-Za-z_][A-Za-z0-9_]*)\s*\)')
for path in java_files:
    src = open(path, encoding='utf-8').read()
    rel = os.path.relpath(path, ROOT)
    for name in FIND.findall(src):
        if name not in declared['id']:
            err('findViewById(R.id.%s) in %s has no @+id/%s in any layout' % (name, rel, name))

# ══════════════════════════ 8. Quran reader asset integrity ══════════════════════════
# The reader displays the official Uthmanic text (Hafs reading) of the fawazahmed0/quran-api
# repository verbatim, together with that repository's companion metadata (surah names, the
# 604 Madani pages, the 30 juzs). The git blob SHAs below pin the two files to the exact
# upstream blobs, so an accidental edit to the sacred text is caught the way the old the upstream Quran source
# checksums did; an intentional upstream update must refresh these SHAs and
# docs/QURAN_TEXT_ATTRIBUTION.md together after independent review.
#
# Upstream: https://github.com/fawazahmed0/quran-api (branch 1)
#   editions/ara-quranuthmanihaf.json  ->  app/src/main/assets/quran.json
#   info.json                          ->  app/src/main/assets/quran_info.json
QURAN_TEXT = os.path.join(MAIN, 'assets', 'quran.json')
QURAN_INFO = os.path.join(MAIN, 'assets', 'quran_info.json')
QURAN_TEXT_BLOB = '8c4aadbff424a69370db89d74267147a6dcd2717'
QURAN_INFO_BLOB = '93b2aa5b00fb4f3337a219340e699ec77efd20fa'
SURAH_AYAH_COUNTS = [
    7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
    123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
    112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
    34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
    54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
    60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
    14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
    28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
    29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
    15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
    11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
    5, 4, 5, 6,
]


def git_blob_sha(path):
    """The SHA-1 git stores for a file: sha1('blob <len>\\0<content>')."""
    data = open(path, 'rb').read()
    digest = hashlib.sha1()
    digest.update(('blob %d\x00' % len(data)).encode('ascii'))
    digest.update(data)
    return digest.hexdigest()


try:
    if git_blob_sha(QURAN_TEXT) != QURAN_TEXT_BLOB:
        err('quran.json no longer matches the reviewed upstream edition '
            '(editions/ara-quranuthmanihaf.json) byte for byte')
    if git_blob_sha(QURAN_INFO) != QURAN_INFO_BLOB:
        err('quran_info.json no longer matches the reviewed upstream metadata (info.json) '
            'byte for byte')

    text = json.load(open(QURAN_TEXT, encoding='utf-8'))
    rows = text.get('quran') if isinstance(text, dict) else None
    if not isinstance(rows, list):
        err('quran.json has no "quran" array')
        rows = []
    if len(rows) != 6236:
        err('Quran text must contain 6236 ayahs, found %d' % len(rows))
    seen_counts = {}
    for index, row in enumerate(rows):
        surah, ayah = row.get('chapter'), row.get('verse')
        verse = row.get('text')
        if not isinstance(surah, int) or not isinstance(ayah, int) or not isinstance(verse, str) \
                or not verse:
            err('invalid Quran row at index %d' % index)
            continue
        expected = seen_counts.get(surah, 0) + 1
        if ayah != expected:
            err('Quran ayah sequence breaks at %d:%d' % (surah, ayah))
            break
        seen_counts[surah] = expected
        if '\u06DD' in verse:
            err('Quran text at %d:%d already carries an end-of-ayah glyph; the app '
                'is the only place one may be added' % (surah, ayah))
    if set(seen_counts) != set(range(1, 115)):
        err('Quran text must cover surahs 1 through 114')
    for number, expected in enumerate(SURAH_AYAH_COUNTS, 1):
        if seen_counts.get(number) != expected:
            err('Quran surah %d expected %d ayahs, found %d'
                % (number, expected, seen_counts.get(number, 0)))

    info = json.load(open(QURAN_INFO, encoding='utf-8'))
    chapters = info.get('chapters') if isinstance(info, dict) else None
    if not isinstance(chapters, list) or len(chapters) != 114:
        err('Quran metadata must contain 114 surahs, found %s'
            % (len(chapters) if isinstance(chapters, list) else 'none'))
        chapters = []
    verse_meta = {}
    for chapter in chapters:
        number = chapter.get('chapter')
        verses = chapter.get('verses')
        if number is None or not isinstance(verses, list) \
                or len(verses) != SURAH_AYAH_COUNTS[number - 1]:
            err('Quran metadata surah %s has the wrong ayah count' % number)
            continue
        for verse in verses:
            page = verse.get('page')
            juz = verse.get('juz')
            line = verse.get('line')
            if not (isinstance(page, int) and 1 <= page <= 604) \
                    or not (isinstance(juz, int) and 1 <= juz <= 30) \
                    or not (isinstance(line, int) and line >= 1):
                err('Quran navigation is out of range at %d:%s' % (number, verse.get('verse')))
            verse_meta[(number, verse.get('verse'))] = (page, juz, line)
    if len(verse_meta) != 6236:
        err('Quran metadata must describe 6236 ayahs, found %d' % len(verse_meta))

    pages = (info.get('pages') or {}).get('references') if isinstance(info, dict) else None
    if not isinstance(pages, list) or len(pages) != 604:
        err('Quran page metadata must contain 604 Madani pages, found %s'
            % (len(pages) if isinstance(pages, list) else 'none'))
        pages = []
    for page_number, reference in enumerate(pages, 1):
        if reference.get('page') != page_number:
            err('Quran page numbering breaks at page %d' % page_number)
            break
        start = (reference['start']['chapter'], reference['start']['verse'])
        end = (reference['end']['chapter'], reference['end']['verse'])
        if start not in verse_meta or end not in verse_meta:
            err('Quran page %d references a missing ayah' % page_number)
            continue
        if verse_meta[start][0] != page_number or verse_meta[end][0] != page_number:
            err('Quran page %d disagrees with the per-ayah page values' % page_number)
    if pages and pages[0]['start'] != {'chapter': 1, 'verse': 1}:
        err('Quran page 1 must start at ayah 1:1')
    if pages and len(verse_meta) == 6236:
        last_reference = None
        for reference in pages:
            start = (reference['start']['chapter'], reference['start']['verse'])
            if last_reference is not None and start <= last_reference:
                err('Quran page references are not in strictly increasing ayah order')
                break
            last_reference = start

    juzs = (info.get('juzs') or {}).get('references') if isinstance(info, dict) else None
    if not isinstance(juzs, list) or len(juzs) != 30:
        err('Quran juz metadata must contain the 30 juzs, found %s'
            % (len(juzs) if isinstance(juzs, list) else 'none'))
        juzs = []
    for juz_number, reference in enumerate(juzs, 1):
        if reference.get('juz') != juz_number:
            err('Quran juz numbering breaks at juz %d' % juz_number)
            break
        start = (reference['start']['chapter'], reference['start']['verse'])
        if start not in verse_meta:
            err('Quran juz %d starts at a missing ayah' % juz_number)
            continue
        if verse_meta[start][1] != juz_number:
            err('Quran juz %d disagrees with the per-ayah juz values' % juz_number)
    if juzs and juzs[0]['start'] != {'chapter': 1, 'verse': 1}:
        err('Quran juz 1 must start at ayah 1:1')
    if juzs and len(verse_meta) == 6236:
        last_reference = None
        for reference in juzs:
            start = (reference['start']['chapter'], reference['start']['verse'])
            if last_reference is not None and start <= last_reference:
                err('Quran juz references are not in strictly increasing ayah order')
                break
            last_reference = start

    print('quran data: %d surahs, %d ayahs, %d Madani pages, %d juzs, upstream blobs %s… / %s…'
          % (len(chapters), len(rows), len(pages), len(juzs), QURAN_TEXT_BLOB[:12],
             QURAN_INFO_BLOB[:12]))
except (OSError, UnicodeDecodeError, json.JSONDecodeError, KeyError, TypeError, ValueError) as exc:
    err('could not verify Quran reader data: %s' % exc)

# ══════════════════════════ report ══════════════════════════
for note in notes:
    if note:
        print('note:', note)
if errors:
    print('\n%d ERROR(S):' % len(errors))
    for e in errors:
        print(' -', e)
    sys.exit(1)
print('\nNO ERRORS')
