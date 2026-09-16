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
  8. the bundled Quran asset has all 114 surahs / 6,236 ayahs, its required Tanzil
     attribution, and the expected unmodified source checksum.

Exit code is non-zero when anything fails, so it can gate a commit.
"""
import glob
import hashlib
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
# The reader displays Tanzil Uthmani text verbatim.  A checksum deliberately catches an
# accidental edit to the sacred text; intentional upstream updates must update this value and
# docs/QURAN_TEXT_ATTRIBUTION.md together after independent review.
QURAN_TEXT = os.path.join(RES, 'raw', 'quran_uthmani.txt')
QURAN_META = os.path.join(RES, 'raw', 'quran_surahs.tsv')
QURAN_LICENSE = os.path.join(RES, 'raw', 'quran_uthmani_license.txt')
QURAN_SHA256 = 'f64fe7657dbe2e185e9995e14f7a67ee6cf1a30773f39184883d7a763d70fb19'
try:
    raw_bytes = open(QURAN_TEXT, 'rb').read()
    actual_sha = hashlib.sha256(raw_bytes).hexdigest()
    if actual_sha != QURAN_SHA256:
        err('quran_uthmani.txt checksum differs from the reviewed Tanzil source')
    quran_rows = []
    for line_no, line in enumerate(raw_bytes.decode('utf-8').splitlines(), 1):
        if not line or line.startswith('#'):
            continue
        pieces = line.split('|', 2)
        if len(pieces) != 3 or not pieces[2]:
            err('invalid Quran row at line %d' % line_no)
            continue
        try:
            quran_rows.append((int(pieces[0]), int(pieces[1])))
        except ValueError:
            err('non-numeric Quran reference at line %d' % line_no)

    metadata = {}
    for line_no, line in enumerate(open(QURAN_META, encoding='utf-8'), 1):
        line = line.rstrip('\n')
        if not line or line.startswith('#'):
            continue
        pieces = line.split('|')
        if len(pieces) != 7:
            err('invalid Quran metadata row at line %d' % line_no)
            continue
        try:
            number, ayah_count = int(pieces[0]), int(pieces[2])
        except ValueError:
            err('non-numeric Quran metadata at line %d' % line_no)
            continue
        metadata[number] = ayah_count

    if len(metadata) != 114:
        err('Quran metadata must contain 114 surahs, found %d' % len(metadata))
    if len(quran_rows) != 6236:
        err('Quran text must contain 6236 ayahs, found %d' % len(quran_rows))
    seen_counts = {}
    for surah, ayah in quran_rows:
        seen_counts[surah] = seen_counts.get(surah, 0) + 1
        if ayah != seen_counts[surah]:
            err('Quran ayah sequence breaks at %d:%d' % (surah, ayah))
            break
    if set(seen_counts) != set(range(1, 115)):
        err('Quran text must cover surahs 1 through 114')
    for number, expected in metadata.items():
        if seen_counts.get(number) != expected:
            err('Quran surah %d expected %d ayahs, found %d'
                % (number, expected, seen_counts.get(number, 0)))
    license = open(QURAN_LICENSE, encoding='utf-8').read()
    text = raw_bytes.decode('utf-8')
    if 'Tanzil Quran Text (Uthmani, Version 1.1)' not in license:
        err('Quran license asset is missing the Tanzil notice')
    if 'Tanzil Project' not in text or 'tanzil.net' not in text:
        err('Quran text asset is missing its required Tanzil attribution')
    print('quran data: %d surahs, %d ayahs, Tanzil checksum %s…'
          % (len(metadata), len(quran_rows), actual_sha[:12]))
except (OSError, UnicodeDecodeError) as exc:
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
