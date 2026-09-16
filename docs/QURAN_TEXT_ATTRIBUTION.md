# Quran reader text: source, integrity, and attribution

The offline Quran reader ships the complete **Uthmani Quran text** in
`app/src/main/res/raw/quran_uthmani.txt`.

- **Source:** Tanzil Project — <https://tanzil.net>
- **Text version:** Uthmani, Version 1.1
- **Copyright:** Copyright (C) 2007–2024 Tanzil Project
- **License:** Creative Commons Attribution 3.0
- **Reviewed source copy:** `acfatah/tanzil` commit
  [`052b515f3a24dfacbe4cafc3b89f0681a447f462`](https://github.com/acfatah/tanzil/tree/052b515f3a24dfacbe4cafc3b89f0681a447f462)
- **SHA-256:** `f64fe7657dbe2e185e9995e14f7a67ee6cf1a30773f39184883d7a763d70fb19`

The text asset is a byte-identical copy of the reviewed source. The application parses the
`surah|ayah|text` rows only to display them; it does not alter Quran text. The original Tanzil
notice is retained at the end of the text asset and copied in full to
`app/src/main/res/raw/quran_uthmani_license.txt`.

## Madani page and juz navigation metadata

`app/src/main/res/raw/quran_pages.tsv` records the first `surah|ayah` reference for each of the
**604** traditional Madani Mushaf page divisions. `app/src/main/res/raw/quran_juz.tsv` records the
starts of the **30** traditional juz (parts). Both are derived from `QuranData.Page` and
`QuranData.Juz` in the same reviewed Tanzil source copy above. Their SHA-256 values are,
respectively:

```text
946e458e8866da0621c172579e825c352d86b892f530b282ff2636276d37088a
9c9b80824ddc8bfa16da5bbb59433441f233611843f0ee4d1c44fc05b3794f18
```

Each upstream JavaScript array uses an empty zero index and ends with `[115, 1]`, a
one-past-the-end sentinel. Neither is a navigation start; the bundled TSVs contain precisely
pages 1 through 604 and juzs 1 through 30. This metadata does not change or supplement the Quran
text. The app uses it only to group the verified Uthmani rows for navigation and resume behavior.

The page-reading UI deliberately reflows those text rows for the reader's screen and chosen text
size. It is **not** a scanned/PDF reproduction of a printed Mushaf or a claim that its glyph layout
matches a particular physical edition. That distinction remains important until the user's actual
Mushaf file is supplied and independently reviewed for rights and integration.

## Tanzil notice

> Tanzil Quran Text (Uthmani, Version 1.1
> Copyright (C) 2007-2024 Tanzil Project
> License: Creative Commons Attribution 3.0
>
> This copy of the Quran text is carefully produced, highly verified and continuously monitored
> by a group of specialists at Tanzil Project.
>
> Permission is granted to copy and distribute verbatim copies of this text, but changing it is
> not allowed. This Quran text can be used in any website or application, provided that its
> source (Tanzil Project) is clearly indicated, and a link is made to tanzil.net to enable users
> to keep track of changes. This copyright notice shall be included in all verbatim copies of the
> text, and shall be reproduced appropriately in all works derived from or containing substantial
> portions of this text.

See the exact notice in `app/src/main/res/raw/quran_uthmani_license.txt` and check
<http://tanzil.net/updates/> before intentionally changing the bundled text.

## Verification before release

Run the normal static verifier after any project change:

```bash
python3 tools/verify_resources.py
```

In addition to Android resources, it verifies all of the following Quran-data invariants:

1. exactly 114 surahs and 6,236 ayahs;
2. sequential ayah numbering and metadata counts for every surah;
3. exactly 604 ordered page starts and 30 ordered juz starts, each covering every ayah exactly
   once;
4. the reviewed SHA-256 checksums for the text plus both navigation metadata assets; and
5. the mandatory Tanzil attribution and license asset.

If a verified upstream Tanzil update is intentionally adopted, update the text asset, navigation
metadata, checksums in `tools/verify_resources.py`, and this document together only after an
independent review.
