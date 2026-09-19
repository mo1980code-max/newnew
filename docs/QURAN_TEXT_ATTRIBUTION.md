# Quran reader text: source, integrity, and attribution

The offline Quran reader ships the complete **Uthmanic Quran text (Hafs reading, fully
vowel-marked)** in `app/src/main/assets/quran.json`, together with the same repository's
companion metadata in `app/src/main/assets/quran_info.json`.

- **Source repository:** fawazahmed0/quran-api, branch `1` — <https://github.com/fawazahmed0/quran-api>
- **Text file (upstream path):** `editions/ara-quranuthmanihaf.json` — edition "Quran Uthmani
  Hafs" (Version 13, sourced from the King Fahd Quran Complex,
  <https://qurancomplex.gov.sa/>), one of the repository's official editions
- **Metadata file (upstream path):** `info.json` — surah names, revelation types, and for every
  ayah its Madani page (1–604), its juz (1–30) and its position on the printed page
- **Repository license:** The Unlicense (public domain) — <https://unlicense.org>
- **Upstream blobs reviewed and pinned (git SHA-1):**
  - `quran.json` — `8c4aadbff424a69370db89d74267147a6dcd2717`
  - `quran_info.json` — `93b2aa5b00fb4f3337a219340e699ec77efd20fa`

Both bundled files are byte-identical copies of the reviewed upstream blobs. The application
parses them only to display them; it does not alter the Quran text. The only addition the app
makes is a display-only `AyahNumberSpan` after each ayah: a circular border with the ayah's
Arabic-Indic number centered inside it. No Unicode ornament is concatenated with the digits.
The repository keeps the exact raw verse text; the readers, bookmarks and search results use
`QuranText.withAyahNumber()` to append the replacement span without modifying the assets,
search normalization, navigation metadata or stored bookmarks.

## Surah headings and navigation metadata

`quran_info.json` records the 114 surahs with their official names and revelation type, and the
starts of the **604** traditional Madani Mushaf page divisions and the **30** traditional juz.
The app uses this metadata only for the reader's header (surah name + juz), the footer's
page-number pill, the page/juz pickers and resume behavior. It does not change or supplement
the Quran text.

The continuous reader deliberately reflows the verified Uthmanic text for the reader's screen
and chosen text size. It is **not** a scanned/PDF reproduction of a printed Mushaf and does not
claim that its glyph layout matches a particular physical edition.

## Verification before release

Run the normal static verifier after any project change:

```bash
python3 tools/verify_resources.py
```

In addition to the Android resources, it verifies all of the following Quran-data invariants:

1. the two bundled assets match the pinned upstream git blobs byte for byte;
2. exactly 114 surahs and 6,236 ayahs, with the canonical ayah count of every surah;
3. every ayah carries a valid Madani page (1–604), juz (1–30) and page position, and the 604
   page references plus the 30 juz references are ordered, start at 1:1 and agree with the
   per-ayah values;
4. the bundled text contains no pre-existing end-of-ayah glyph (the app is the only place one
   may be added); and
5. the metadata's per-surah verse lists match the text file's rows one for one.

If a verified upstream update is intentionally adopted, update both assets, the pinned blobs in
`tools/verify_resources.py`, and this document together — only after an independent review of
the new text.

## Ayah marker rendering checks

With a configured JDK/Android SDK and a device or emulator, run:

```bash
./gradlew connectedDebugAndroidTest
```

`AyahNumberSpanTest` checks span boundaries, Arabic-Indic numbers (1–286), horizontal and
font-metric vertical centering, border/padding bounds, density and font-size scaling, unchanged
surrounding paint, night-marker color, and RTL placement. Before release, also inspect long
wrapped verses in both reader themes at the largest text size, plus bookmarks and search
results on an Arabic and an English device locale. The HTML preview is only a visual mock,
not a substitute for testing Android's font and bidi layout engine.

## Continuous RTL page layout

Both readers now use one `QuranParagraph` / `SpannableStringBuilder` per Madani page fragment,
not one TextView per ayah. A fragment ends only at an authentic page boundary or a surah heading.
Consecutive ayahs are separated by a single space, without inserted newlines. The existing
non-breaking space before each atomic `AyahNumberSpan` keeps it with the final word.

The reading TextView explicitly uses `layoutDirection="rtl"`, `textDirection="rtl"`,
`textAlignment="viewStart"` and `gravity="start|top"`. Android 8+ enables inter-word
justification; Android 6–7 retains right-aligned RTL text. Headers, loading indicators and
page-number controls may still be centered; they are not the Quran body.

Verse character ranges retain independent click/bookmark highlights and let resume, search
navigation, text zoom and the visible-position footer locate an ayah inside a wrapped paragraph.
`QuranParagraphTest` covers all 6,236 ayahs across 604 page boundaries, Al-Fatiha in a single
paragraph, verse offsets/clicks, RTL settings, and marker placement in narrow justified layouts.
Test on an API 23 device and an API 26+ device with both English and Arabic UI locales; scroll
within a long page, save/reopen a verse, resize the text and toggle the night theme.
