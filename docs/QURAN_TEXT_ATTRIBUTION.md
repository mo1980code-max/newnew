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
3. the reviewed SHA-256 checksum; and
4. the mandatory Tanzil attribution and license asset.

If a verified upstream Tanzil update is intentionally adopted, update the text asset, checksum
in `tools/verify_resources.py`, and this document together only after an independent review.
