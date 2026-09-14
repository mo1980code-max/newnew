#!/usr/bin/env python3
"""Rebuild the English prayer tutorial with one consistent Layan character.

The existing first six scenes are reused with two visual corrections: scene 4
shows the right hand over the left for the opening dua, and scene 15 uses two
seated frames so Tasleem visibly turns right and then left. Scenes 7–14 use
single-character, anatomy-checked frames and narration-derived timings.
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

# Durations from the base render's first six scenes. The first-six audio is
# extracted from the base MP4 so the corrected visuals keep the same narration.
PREFIX_DURATIONS = [11.539, 12.521, 8.920, 12.980, 10.703, 14.497]
PREFIX_TOTAL = sum(PREFIX_DURATIONS)

FFMPEG = os.environ.get("FFMPEG_BIN", shutil.which("ffmpeg") or "ffmpeg")
FONT_BOLD = str((ROOT.parent / "app/src/main/assets/montserrat_semi_bold.ttf").resolve())
FONT_REGULAR = str((ROOT.parent / "app/src/main/assets/open_sans_regular.ttf").resolve())

TAIL_SCENES = [
    ("07_rising_single_v4.png", "RISING FROM RUKU", "Stand straight and praise Allah"),
    ("08_first_sujud_single_v4.png", "FIRST SUJOOD", "Forehead and nose touch the mat"),
    ("09_between_sujud_single_v4.png", "SITTING BETWEEN SUJOODS", "Straight back, hands on the thighs"),
    ("10_second_sujud_single_v4.png", "SECOND SUJOOD AND STANDING", "Prostrate, then stand for rakah two"),
    ("11_second_rakah_single_v4.png", "THE SECOND RAKAH", "Repeat the prayer movements calmly"),
    ("12_middle_tashahhud_single_v4.png", "MIDDLE TASHAHHUD", "Raise the right index finger gently"),
    ("13_third_fourth_single_v4.png", "THIRD AND FOURTH RAKAH", "Recite Al Fatihah quietly"),
    ("14_final_tashahhud_single_v4.png", "FINAL TASHAHHUD AND DUA", "Complete the prayer with calm focus"),
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


def encode_still(index: int, image_name: str, title: str, subtitle: str, duration: float) -> Path:
    out = BUILD / f"scene_{index:02d}_{len(list(BUILD.glob('scene_*.mp4'))):02d}.mp4"
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


def extract_video(base: Path, start: float, duration: float, name: str) -> Path:
    out = BUILD / name
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(base),
            "-ss",
            f"{start:.3f}",
            "-t",
            f"{duration:.3f}",
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


def concat_video(files: list[Path], name: str) -> Path:
    list_file = BUILD / f"{name}.txt"
    list_file.write_text("".join(f"file '{item.as_posix()}'\n" for item in files), encoding="utf-8")
    out = BUILD / f"{name}.mp4"
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
            str(list_file),
            "-c",
            "copy",
            "-an",
            str(out),
        ]
    )
    return out


def mux(video: Path, audio: Path, name: str) -> Path:
    out = BUILD / name
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(video),
            "-i",
            str(audio),
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
            str(out),
        ]
    )
    return out


def main() -> int:
    if not Path(FFMPEG).exists() and shutil.which(FFMPEG) is None:
        raise SystemExit("ffmpeg was not found; set FFMPEG_BIN to its executable path")
    if not OUTPUT.exists():
        raise FileNotFoundError(f"The base video is missing: {OUTPUT}")

    required_images = ["04_hands_dua_corrected_v5.png", "15_tasleem_sitting_single_v5.png", "15_tasleem_sitting_left_v5.png"]
    required_images += [item[0] for item in TAIL_SCENES]
    required_audio = [item[0] for item in CHAPTERS]
    for image_name in required_images:
        if not (ASSETS / image_name).exists():
            raise FileNotFoundError(ASSETS / image_name)
    for audio_name in required_audio:
        if not (AUDIO / audio_name).exists():
            raise FileNotFoundError(AUDIO / audio_name)

    if BUILD.exists():
        shutil.rmtree(BUILD)
    BUILD.mkdir(parents=True)
    base = BUILD / "base_before_corrections.mp4"
    shutil.copyfile(OUTPUT, base)

    # Preserve the first three base scenes, replace scene 4 with corrected hands,
    # then preserve scenes 5 and 6.
    start = 0.0
    first_three = extract_video(base, start, sum(PREFIX_DURATIONS[:3]), "scenes_01_03.mp4")
    start += sum(PREFIX_DURATIONS[:3])
    corrected_scene4 = encode_still(
        4,
        "04_hands_dua_corrected_v5.png",
        "HANDS AND OPENING DUA",
        "Right hand over left, then opening dua",
        PREFIX_DURATIONS[3],
    )
    start += PREFIX_DURATIONS[3]
    scenes_05_06 = extract_video(base, start, sum(PREFIX_DURATIONS[4:6]), "scenes_05_06.mp4")
    prefix_silent = concat_video([first_three, corrected_scene4, scenes_05_06], "prefix_silent")

    prefix_audio = BUILD / "prefix_audio.m4a"
    run(
        [
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(base),
            "-t",
            f"{PREFIX_TOTAL:.3f}",
            "-vn",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            "-ar",
            "44100",
            "-ac",
            "2",
            str(prefix_audio),
        ]
    )
    prefix = mux(prefix_silent, prefix_audio, "prefix.mp4")

    durations = [audio_duration(AUDIO / name) for name, _ in CHAPTERS]
    tail_durations: list[float] = []
    for chapter_duration, (_, weights) in zip(durations, CHAPTERS):
        tail_durations.extend(chapter_duration * weight for weight in weights)

    tail_files: list[Path] = []
    for scene_no, ((image, title, subtitle), duration) in enumerate(zip(TAIL_SCENES, tail_durations[:8]), start=7):
        tail_files.append(encode_still(scene_no, image, title, subtitle, duration))

    # Scene 15 explicitly shows seated Tasleem turning right and then left.
    final_duration = tail_durations[8]
    tail_files.append(
        encode_still(15, "15_tasleem_sitting_single_v5.png", "TASLEEM AND ENDING", "Turn your head right", final_duration / 2)
    )
    tail_files.append(
        encode_still(15, "15_tasleem_sitting_left_v5.png", "TASLEEM AND ENDING", "Then turn your head left", final_duration / 2)
    )
    tail_silent = concat_video(tail_files, "tail_silent")

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
    tail = mux(tail_silent, tail_audio, "tail.mp4")

    final_list = BUILD / "full_video_concat.txt"
    final_list.write_text(f"file '{prefix.as_posix()}'\nfile '{tail.as_posix()}'\n", encoding="utf-8")
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
            str(final_list),
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
    run(["-y", "-hide_banner", "-loglevel", "error", "-i", str(OUTPUT), "-frames:v", "1", str(POSTER)])
    shutil.rmtree(BUILD)
    print(f"Built {OUTPUT} ({OUTPUT.stat().st_size / 1024 / 1024:.1f} MB)")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except subprocess.CalledProcessError as exc:
        print(f"ffmpeg failed with exit code {exc.returncode}", file=sys.stderr)
        raise
