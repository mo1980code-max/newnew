# -*- coding: utf-8 -*-
"""Regenerates app/src/main/res/raw/athkar.json from the islambook.com transcription.

    python3 tools/athkar/build_athkar.py

Morning: 31 athkar (ids 1..31). Evening: 30 athkar (ids 101..130).
Content lives in athkar_shared.py (the blocks both lists use) and athkar_lists.py (the order
of each list plus the morning-only / evening-only athkar). Runs a hard validation pass first -
a malformed or truncated dhikr must never ship, so the build fails loudly instead.
"""
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from athkar_lists import MORNING, EVENING  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
OUT = os.path.join(ROOT, 'app', 'src', 'main', 'res', 'raw', 'athkar.json')
FIELDS = ('repeat', 'ar', 'en', 'tr', 'ref_ar', 'ref_en', 'virtue_ar', 'virtue_en')


def validate(name, items):
    errors = []
    if not items:
        errors.append('%s: empty list' % name)
    for i, it in enumerate(items, 1):
        for f in FIELDS:
            if f not in it:
                errors.append('%s #%d: missing field %s' % (name, i, f))
        if not isinstance(it.get('repeat'), int) or it['repeat'] < 1:
            errors.append('%s #%d: bad repeat %r' % (name, i, it.get('repeat')))
        for f in ('ar', 'en', 'tr'):
            v = it.get(f) or ''
            if len(v.strip()) < 8:
                errors.append('%s #%d: %s too short' % (name, i, f))
        # Arabic text must actually contain Arabic letters; the others must not.
        if not any('\u0600' <= c <= '\u06ff' for c in it.get('ar', '')):
            errors.append('%s #%d: arabic_text has no Arabic letters' % (name, i))
        for f in ('en', 'tr'):
            if any('\u0600' <= c <= '\u06ff' for c in it.get(f, '')):
                errors.append('%s #%d: %s contains Arabic letters' % (name, i, f))
        # A virtue note (when present) must exist in both languages.
        if bool(it.get('virtue_ar')) != bool(it.get('virtue_en')):
            errors.append('%s #%d: virtue present in one language only' % (name, i))
        if bool(it.get('ref_ar')) != bool(it.get('ref_en')):
            errors.append('%s #%d: reference present in one language only' % (name, i))
        # The HTML mock builds its cards with innerHTML, so a stray < or & would corrupt a card.
        for f in FIELDS:
            if f == 'repeat':
                continue
            for c in '<>&"':
                if c in (it.get(f) or ''):
                    errors.append('%s #%d: %s contains %r (breaks the HTML preview)'
                                  % (name, i, f, c))
    return errors


def build(name, items, first_id):
    out = []
    for i, it in enumerate(items):
        out.append({
            'id': first_id + i,
            'type': name,
            'repeat': it['repeat'],
            'arabic_text': it['ar'],
            'english_text': it['en'],
            'transliteration': it['tr'],
            'reference_ar': it['ref_ar'],
            'reference_en': it['ref_en'],
            'virtue_ar': it['virtue_ar'],
            'virtue_en': it['virtue_en'],
        })
    return out


errors = validate('morning', MORNING) + validate('evening', EVENING)
if len(MORNING) != 31:
    errors.append('morning has %d items, expected 31' % len(MORNING))
if len(EVENING) != 30:
    errors.append('evening has %d items, expected 30' % len(EVENING))
if errors:
    print('VALIDATION FAILED:')
    for e in errors:
        print('  -', e)
    sys.exit(1)

athkar = build('morning', MORNING, 1) + build('evening', EVENING, 101)
ids = [a['id'] for a in athkar]
assert len(set(ids)) == len(ids), 'duplicate ids'

with open(OUT, 'w', encoding='utf-8') as fh:
    json.dump({'athkar': athkar}, fh, ensure_ascii=False, indent=1)
    fh.write('\n')

size = os.path.getsize(OUT)
print('OK: %d athkar (morning %d + evening %d), %d KB -> %s'
      % (len(athkar), len(MORNING), len(EVENING), size // 1024, OUT))
print('repeat totals: morning %d taps, evening %d taps'
      % (sum(i['repeat'] for i in MORNING), sum(i['repeat'] for i in EVENING)))
print('with virtue note: morning %d, evening %d'
      % (sum(1 for i in MORNING if i['virtue_ar']), sum(1 for i in EVENING if i['virtue_ar'])))
