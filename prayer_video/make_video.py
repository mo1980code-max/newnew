#!/usr/bin/env python3
"""Rebuild the English prayer tutorial with one consistent Layan character.

The first six scenes are preserved from the already-rendered base video. The
continuation scenes use one Layan per frame with corrected prayer anatomy and
scene durations derived from the narration clips.
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
PREFIX_DURATION = 70.68  # scenes 1–6 in the base render

FFMPEG = os.environ.get("FFMPEG_BIN", shutil.which("ffmpeg") or "ffmpeg")
FONT_BOLD = str((ROOT.parent / "app/src/main/assets/montserrat_semi_bold.ttf").resolve())
FONT_REGULAR = str((ROOT.parent / "app/src/main/assets/open_sans_regular.ttf").resolve())

CONTINUATION_SCENES = [
    ("07_rising_single_v4.png", "RISING FROM RUKU", "Stand straight and praise Allah"),
    ("08_first_sujud_single_v4.png", "FIRST SUJOOD", "Forehead and nose touch the mat"),
    ("09_between_sujud_single_v4.png", "SITTING BETWEEN SUJOODS", "Straight back, hands on the thighs"),
    ("10_second_sujud_single_v4.png", "SECOND SUJOOD AND STANDING", "Prostrate, then stand for rakah two"),
    ("11_second_rakah_single_v4.png", "THE SECOND RAKAH", "Repeat the prayer movements calmly"),
    ("12_middle_tashahhud_single_v4.png", "MIDDLE TASHAHHUD", "Raise the right index finger gently"),
    ("13_third_fourth_single_v4.png", "THIRD AND FOURTH RAKAH", "Recite Al Fatihah quietly"),
    ("14_final_tashahhud_single_v4.png", "FINAL TASHAHHUD AND DUA", "Complete the prayer with calm focus"),
    ("15_salam_single_v4.png", "TASLEEM AND ENDING", "Turn right, left, then wave"),
]

CHAPTERS = [
    ("03_rising_to_first_sujud_v3.mp3", (0.26, 0.47, 0.27)),
    ("04_second_sujud_to_tashahhud_v3.mp3", (0.27, 0.30, 0.43)),
    ("05_final_tashahhud_to_salam_v3.mp3", (0.31, 0.37, 0.32)),
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
    return (
        text.replace("\\", r"\\")
        .replace(":", r"\:")
        .replace(",", r"\,")
        .replace("'", r"\'")
    )


def make_filter(scene_no: int, title: str, subtitle: str) -> str:
    title = escape_drawtext(title)
    subtitle = escape_drawtext(subtitle)
    number = escape_drawtext(f"SCENE {scene_no:02d} OF 15")
    return ",".join(
        [
            "scale=1440:810:force_original_aspect_ratio=increase",
            "crop=1440:810",
            "zoompan=z='min(zoom+0.0008,1.06)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=1:s=1280x720:fps=25",
            "setsar=1",
            f"drawtext=fontfile={FONT_BOLD}:text='{title}':fontcolor=white:fontsize=30:x=48:y=42:box=1:boxcolor=0x243b53@0.88:boxborderw=16",
            f"drawtext=fontfile={FONT_REGULAR}:text='{subtitle}':fontcolor=white@0.97:fontsize=22:x=52:y=96:box=1:boxcolor=0x243b53@0.66:boxborderw=10",
            "drawbox=x=48:y=678:w=1184:h=4:color=white@0.34:t=fill",
            f"drawbox=x=48:y=678:w={max(8, round(1184 * scene_no / 15))}:h=4:color=0xf4c6a8@0.98:t=fill",
            f"drawtext=fontfile={FONT_REGULAR}:text='{number}':fontcolor=white@0.96:fontsize=18:x=48:y=693:box=1:boxcolor=0x243b53@0.78:boxborderw=8",
        ]
    )


def make_segment(index: int, image_name: str, title: str, subtitle: str, duration: float) -> Path:
    out = BUILD / f"tail_scene_{index:02d}.mp4"
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
            f"{duration + 0.15:.3f}",
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
        raise SystemExit("ffmpeg was not found; set FFMPEG_BIN to its executable path")
    if not OUTPUT.exists():
        raise FileNotFoundError(f"The base video is missing: {OUTPUT}")
    for image_name, _, _ in CONTINUATION_SCENES:
        if not (ASSETS / image_name).exists():
            raise FileNotFoundError(ASSETS / image_name)
    for audio_name, _ in CHAPTERS:
        if not (AUDIO / audio_name).exists():
            raise FileNotFoundError(AUDIO / audio_name)

    if BUILD.exists():
        shutil.rmtree(BUILD)
    BUILD.mkdir(parents=True)

    # Render the existing first-six-scenes portion before replacing OUTPUT.
    prefix = BUILD / "prefix_scenes_01_06.mp4"
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(OUTPUT),
            "-t",
            f"{PREFIX_DURATION:.3f}",
            "-map",
            "0:v:0",
            "-map",
            "0:a:0",
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
            "-c:a",
            "aac",
            "-ar",
            "44100",
            "-ac",
            "2",
            "-b:a",
            "128k",
            "-avoid_negative_ts",
            "make_zero",
            str(prefix),
        ]
    )

    durations = [audio_duration(AUDIO / name) for name, _ in CHAPTERS]
    weights = [weight for _, weight in CHAPTERS]
    scene_durations: list[float] = []
    for duration, chapter_weights in zip(durations, weights):
        scene_durations.extend(duration * weight for weight in chapter_weights)

    segments = [
        make_segment(i, image, title, subtitle, scene_durations[i - 7])
        for i, (image, title, subtitle) in enumerate(CONTINUATION_SCENES, start=7)
    ]
    tail_video_list = BUILD / "tail_video_concat.txt"
    tail_video_list.write_text(
        "".join(f"file '{segment.as_posix()}'\n" for segment in segments),
        encoding="utf-8",
    )
    tail_silent = BUILD / "tail_silent.mp4"
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
            str(tail_video_list),
            "-c",
            "copy",
            "-an",
            str(tail_silent),
        ]
    )

    tail_audio_list = BUILD / "tail_audio_concat.txt"
    tail_audio_list.write_text(
        "".join(f"file '{(AUDIO / name).as_posix()}'\n" for name, _ in CHAPTERS),
        encoding="utf-8",
    )
    tail_audio = BUILD / "tail_audio.m4a"
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
            str(tail_audio_list),
            "-ar",
            "44100",
            "-ac",
            "2",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            str(tail_audio),
        ]
    )
    tail = BUILD / "tail_scenes_07_15.mp4"
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(tail_silent),
            "-i",
            str(tail_audio),
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
            str(tail),
        ]
    )

    concat_list = BUILD / "full_video_concat.txt"
    concat_list.write_text(
        f"file '{prefix.as_posix()}'\nfile '{tail.as_posix()}'\n",
        encoding="utf-8",
    )
    combined = BUILD / "combined.mp4"
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
            str(concat_list),
            "-c",
            "copy",
            "-movflags",
            "+faststart",
            str(combined),
        ]
    )
    shutil.copyfile(combined, OUTPUT)
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
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except subprocess.CalledProcessError as exc:
        print(f"ffmpeg failed with exit code {exc.returncode}", file=sys.stderr)
        raise
