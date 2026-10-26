#!/usr/bin/env python3
"""Render the TV launcher banner (connector shape + app name) as a PNG.

Usage: python3 scripts/make-banner.py ["Label text"]
Needs Pillow and the Open Sans font (Fedora: open-sans-fonts).
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

TEXT = sys.argv[1] if len(sys.argv) > 1 else "Input Selector"
FONT = "/usr/share/fonts/open-sans/OpenSans-Bold.ttf"
OUT = Path(__file__).resolve().parent.parent / "app/src/main/res/drawable-xhdpi/banner.png"

S = 2  # xhdpi: 320x180dp banner -> 640x360px
W, H = 320 * S, 180 * S
BLUE, WHITE = "#1A73E8", "#FFFFFF"

img = Image.new("RGB", (W, H), BLUE)
draw = ImageDraw.Draw(img)


def connector(cx, top, width, height, color):
    """HDMI-style trapezoid: straight top half, tapered bottom."""
    half, bevel = width / 2, width / 8
    straight = height * 2 / 3
    draw.polygon([
        (cx - half, top), (cx + half, top),
        (cx + half, top + straight), (cx + half - bevel, top + height),
        (cx - half + bevel, top + height), (cx - half, top + straight),
    ], fill=color)


connector(W / 2, 28 * S, 96 * S, 48 * S, WHITE)
connector(W / 2, 37 * S, 72 * S, 30 * S, BLUE)

font = ImageFont.truetype(FONT, 30 * S)
draw.text((W / 2, 128 * S), TEXT, font=font, fill=WHITE, anchor="mm")

OUT.parent.mkdir(parents=True, exist_ok=True)
img.save(OUT, optimize=True)
print(f"wrote {OUT}")
