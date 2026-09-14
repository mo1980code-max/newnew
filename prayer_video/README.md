# English Prayer Tutorial Video

**Deliverable:** `english_prayer_with_layan.mp4`

A 16:9 English narrated tutorial with one consistent Layan character: a cheerful 7-year-old Arab girl in a pastel-pink prayer dress and white hijab. It includes all 15 scenes in order, English title cards, gentle Ken Burns motion, and a warm feminine voiceover. Scenes 7–15 follow the detailed continuation supplied in the latest brief with corrected single-character prayer poses. The first and second Sujood frames keep the forehead and nose down on the mat.

- Resolution: 1280 × 720
- Frame rate: 25 fps
- Duration: about 2 minutes 48 seconds
- Audio: English AAC narration, stereo
- Poster: `preview.png`

`ENGLISH_STORYBOARD.md` contains the complete English narration and scene plan. `make_video.py` documents the assembly pipeline used to create the MP4. The generated image and audio sources are kept locally under `assets/` and `audio/` and are ignored by Git because the finished MP4 is the requested deliverable.

Scene 4 now visibly places the right hand over the left during the opening dua. Scene 15 keeps Layan seated and shows the Tasleem turning to the right and then to the left. The current render uses a separate English narration track over the educational frames; it does not claim phoneme-level lip-sync. True mouth-to-audio sync requires a dedicated lip-sync pass such as Hedra, D-ID, LivePortrait, or a comparable video model using the Layan reference and the supplied narration.

Prayer practice can differ by school or community. Children should use this as a gentle visual guide and learn the details with a trusted parent, teacher, or imam.
