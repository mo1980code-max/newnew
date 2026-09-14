#!/usr/bin/env python3
"""Build the English Learn to Pray with Layan video.

The script intentionally uses only Python's standard library plus an ffmpeg
binary. Set FFMPEG_BIN when ffmpeg is not on PATH.
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "assets"
AUDIO = ROOT / "audio"
BUILD = ROOT / ".build"
OUTPUT = ROOT / "english_prayer_with_layan.mp4"
POSTER = ROOT / "preview.png"

FFMPEG = os.environ.get("FFMPEG_BIN", shutil.which("ffmpeg") or "ffmpeg")
FONT_BOLD = str((ROOT.parent / "app/src/main/assets/montserrat_semi_bold.ttf").resolve())
FONT_REGULAR = str((ROOT.parent / "app/src/main/assets/open_sans_regular.ttf").resolve())

# Scenes 7–15 use the detailed continuation frames generated from the latest
# brief; each title card and narrated chapter follows that updated sequence.
SCENES = [
    ("01_intro.png", "LEARN TO PRAY WITH LAYAN", "A gentle step by step guide"),
    ("02_qiblah.png", "FACE THE QIBLAH", "Make your intention in your heart"),
    ("03_takbeer.png", "TAKBEER", "Raise your hands and say Allahu Akbar"),
    ("04_hands_dua.png", "HANDS AND OPENING DUA", "Right hand over left, then opening dua"),
    ("05_recitation.png", "RECITATION", "Al Fatihah, then a short surah"),
    ("06_ruku.png", "RUKU", "Straight back and hands on your knees"),
    ("07_rising_v2.png", "RISE FROM RUKU", "Stand tall and praise Allah"),
    ("08_first_sujud_v2.png", "FIRST SUJOOD", "Seven points touch the mat"),
    ("09_between_sujud_v2.png", "SIT BETWEEN PROSTRATIONS", "Rest calmly and ask Allah to forgive you"),
    ("10_second_sujud_v2.png", "SECOND SUJOOD", "Repeat the humble prostration, then stand"),
    ("11_second_rakah_v2.png", "SECOND RAKAH", "Repeat the first rakah movements"),
    ("12_middle_tashahhud_v2.png", "MIDDLE TASHAHHUD", "Raise your right index finger gently"),
    ("13_third_fourth_v2.png", "THIRD AND FOURTH RAKAH", "Recite Al Fatihah quietly"),
    ("14_final_tashahhud_v2.png", "FINAL TASHAHHUD AND DUA", "Complete the Tashahhud and Ibrahimic prayer"),
    ("15_salam_wave_v2.png", "TASLEEM AND ENDING", "Turn right, then left, then wave"),
]

# One voice clip covers each chapter of three scenes. The weights keep the
# image changes near the corresponding narration instead of splitting every
# chapter into equal thirds.
CHAPTERS = [
    ("01_intro_to_takbeer.mp3", (0.35, 0.38, 0.27)),
    ("02_opening_to_ruku.mp3", (0.34, 0.28, 0.38)),
    ("03_rising_to_first_sujud_v2.mp3", (0.27, 0.43, 0.30)),
    ("04_second_sujud_to_tashahhud_v2.mp3", (0.28, 0.40, 0.32)),
    ("05_final_tashahhud_to_salam_v2.mp3", (0.30, 0.43, 0.27)),
]


def run(args: list[str], *, capture: bool = False) -> subprocess.CompletedProcess[str]:
    command = [FFMPEG, *args]
    print("+", " ".join(command))
    return subprocess.run(
        command,
        check=True,
        text=True,
        stdout=subprocess.PIPE if capture else None,
        stderr=subprocess.PIPE if capture else None,
    )


def audio_duration(path: Path) -> float:
    result = run(["-hide_banner", "-i", str(path), "-f", "null", "-"], capture=True)
    text = (result.stdout or "") + (result.stderr or "")
    match = re.search(r"Duration:\s+(\d+):(\d+):(\d+(?:\.\d+)?)", text)
    if not match:
        raise RuntimeError(f"Could not read duration from {path}")
    hours, minutes, seconds = match.groups()
    return int(hours) * 3600 + int(minutes) * 60 + float(seconds)


def escape_drawtext(text: str) -> str:
    # drawtext uses ':' and ',' as option separators even inside text='...'.
    return (
        text.replace("\\", r"\\")
        .replace(":", r"\:")
        .replace(",", r"\,")
        .replace("'", r"\'")
    )


def make_filter(scene_no: int, title: str, subtitle: str) -> str:
    title_size = 40 if scene_no == 1 else 31
    title_y = 38 if scene_no == 1 else 42
    subtitle_y = 103 if scene_no == 1 else 96
    title = escape_drawtext(title)
    subtitle = escape_drawtext(subtitle)
    number = escape_drawtext(f"SCENE {scene_no:02d} OF 15")
    return ",".join(
        [
            "scale=1440:810:force_original_aspect_ratio=increase",
            "crop=1440:810",
            "zoompan=z='min(zoom+0.0007,1.055)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=1:s=1280x720:fps=25",
            "setsar=1",
            f"drawtext=fontfile={FONT_BOLD}:text='{title}':fontcolor=white:fontsize={title_size}:x=48:y={title_y}:box=1:boxcolor=0x243b53@0.88:boxborderw=16",
            f"drawtext=fontfile={FONT_REGULAR}:text='{subtitle}':fontcolor=white@0.97:fontsize=23:x=52:y={subtitle_y}:box=1:boxcolor=0x243b53@0.66:boxborderw=10",
            "drawbox=x=48:y=678:w=1184:h=4:color=white@0.34:t=fill",
            f"drawbox=x=48:y=678:w={max(8, round(1184 * scene_no / 15))}:h=4:color=0xf4c6a8@0.98:t=fill",
            f"drawtext=fontfile={FONT_REGULAR}:text='{number}':fontcolor=white@0.96:fontsize=18:x=48:y=693:box=1:boxcolor=0x243b53@0.78:boxborderw=8",
        ]
    )


def make_scene_segment(index: int, image_name: str, title: str, subtitle: str, duration: float) -> Path:
    out = BUILD / f"scene_{index:02d}.mp4"
    # A small pad avoids cutting the final phoneme at a chapter boundary. The
    # final mux is still limited by the narration with -shortest.
    duration += 0.08
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-loop",
            "1",
            "-i",
            str(ASSETS / image_name),
            "-t",
            f"{duration:.3f}",
            "-vf",
            make_filter(index, title, subtitle),
            "-an",
            "-c:v",
            "libx264",
            "-preset",
            "veryfast",
            "-crf",
            "22",
            "-pix_fmt",
            "yuv420p",
            "-r",
            "25",
            str(out),
        ]
    )
    return out


def main() -> int:
    if not Path(FFMPEG).exists() and shutil.which(FFMPEG) is None:
        raise SystemExit(
            "ffmpeg was not found. Install ffmpeg or run with FFMPEG_BIN=/path/to/ffmpeg."
        )
    for image_name, _, _ in SCENES:
        if not (ASSETS / image_name).exists():
            raise FileNotFoundError(ASSETS / image_name)
    for audio_name, _ in CHAPTERS:
        if not (AUDIO / audio_name).exists():
            raise FileNotFoundError(AUDIO / audio_name)

    if BUILD.exists():
        shutil.rmtree(BUILD)
    BUILD.mkdir(parents=True)

    durations = [audio_duration(AUDIO / name) for name, _ in CHAPTERS]
    scene_durations: list[float] = []
    for duration, (_, weights) in zip(durations, CHAPTERS):
        scene_durations.extend(duration * weight for weight in weights)

    segments = [
        make_scene_segment(i, image, title, subtitle, scene_durations[i - 1])
        for i, (image, title, subtitle) in enumerate(SCENES, start=1)
    ]

    video_list = BUILD / "video_concat.txt"
    video_list.write_text(
        "".join(f"file '{segment.as_posix()}'\n" for segment in segments),
        encoding="utf-8",
    )
    silent_video = BUILD / "silent_video.mp4"
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-f",
            "concat",
            "-safe",
            "0",
            "-i",
            str(video_list),
            "-c",
            "copy",
            "-an",
            str(silent_video),
        ]
    )

    audio_list = BUILD / "audio_concat.txt"
    audio_list.write_text(
        "".join(f"file '{(AUDIO / name).as_posix()}'\n" for name, _ in CHAPTERS),
        encoding="utf-8",
    )
    narration = BUILD / "narration.m4a"
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-f",
            "concat",
            "-safe",
            "0",
            "-i",
            str(audio_list),
            "-ar",
            "44100",
            "-ac",
            "2",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            str(narration),
        ]
    )

    if OUTPUT.exists():
        OUTPUT.unlink()
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(silent_video),
            "-i",
            str(narration),
            "-map",
            "0:v:0",
            "-map",
            "1:a:0",
            "-c:v",
            "copy",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            "-shortest",
            "-movflags",
            "+faststart",
            str(OUTPUT),
        ]
    )

    if POSTER.exists():
        POSTER.unlink()
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(OUTPUT),
            "-frames:v",
            "1",
            str(POSTER),
        ]
    )
    shutil.rmtree(BUILD)
    print(f"Built {OUTPUT} ({OUTPUT.stat().st_size / 1024 / 1024:.1f} MB)")
    print(f"Poster: {POSTER}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except subprocess.CalledProcessError as exc:
        print(f"ffmpeg failed with exit code {exc.returncode}", file=sys.stderr)
        raise
