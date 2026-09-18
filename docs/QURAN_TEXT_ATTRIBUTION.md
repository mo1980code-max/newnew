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
makes is closing each ayah with its end-of-ayah glyph: the ARABIC END OF AYAH character
(U+06DD) followed by the ayah's number in Arabic-Indic digits, exactly the shape the edition's
rendered text uses — the ornament is a character of the text, so no position is ever computed
or painted by hand.

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
