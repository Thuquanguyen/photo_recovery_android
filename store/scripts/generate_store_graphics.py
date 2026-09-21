"""Generate Play Store graphics at exact Console sizes.

Outputs (RGB PNG, no alpha except the 512 icon which is flattened RGB):
  store/graphics/icon-512.png
  store/graphics/feature-graphic-1024x500.png
  store/graphics/phone/01-home.png ... 06-vault.png
  store/graphics/phone-vi/01-home.png ... 06-vault.png
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "store" / "graphics"
ICON_SRC = Path(
    r"C:\Users\kekho\.cursor\projects\e-WorkSpace-photo-recovery-android\assets\play-icon-512.png"
)
FONTS = Path(r"C:\Windows\Fonts")

PRIMARY = (107, 56, 212)
PRIMARY_2 = (132, 85, 239)
CYAN = (103, 232, 249)
PINK = (236, 72, 153)
AMBER = (251, 191, 36)
EMERALD = (52, 211, 153)
WHITE = (255, 255, 255)
NAVY = (13, 26, 56)
SURFACE = (250, 248, 255)
MUTED = (73, 68, 84)

DARK_STOPS = [
    (0.00, (18, 8, 33)),
    (0.25, (29, 10, 55)),
    (0.55, (43, 13, 75)),
    (0.80, (60, 14, 88)),
    (1.00, (86, 17, 98)),
]


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONTS / name), size)


def F_REG(size: int) -> ImageFont.FreeTypeFont:
    return font("segoeui.ttf", size)


def F_SB(size: int) -> ImageFont.FreeTypeFont:
    return font("seguisb.ttf", size)


def F_BD(size: int) -> ImageFont.FreeTypeFont:
    return font("segoeuib.ttf", size)


def lerp(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))  # type: ignore[return-value]


def vertical_gradient(size: tuple[int, int], stops: list[tuple[float, tuple[int, int, int]]]) -> Image.Image:
    w, h = size
    line = Image.new("RGB", (1, h))
    px = line.load()
    for y in range(h):
        t = y / max(h - 1, 1)
        color = stops[-1][1]
        for i in range(len(stops) - 1):
            t0, c0 = stops[i]
            t1, c1 = stops[i + 1]
            if t0 <= t <= t1:
                local = 0 if t1 == t0 else (t - t0) / (t1 - t0)
                color = lerp(c0, c1, local)
                break
        px[0, y] = color
    return line.resize((w, h), Image.Resampling.BILINEAR)


def radial_glow(img: Image.Image, center: tuple[int, int], radius: int, color: tuple[int, int, int], alpha: int) -> None:
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    layer = Image.new("L", (radius * 2, radius * 2), 0)
    d = ImageDraw.Draw(layer)
    d.ellipse((0, 0, radius * 2 - 1, radius * 2 - 1), fill=alpha)
    layer = layer.filter(ImageFilter.GaussianBlur(radius * 0.45))
    glow = Image.new("RGBA", (radius * 2, radius * 2), color + (0,))
    glow.putalpha(layer)
    overlay.alpha_composite(glow, (center[0] - radius, center[1] - radius))
    blended = Image.alpha_composite(img.convert("RGBA"), overlay)
    img.paste(blended.convert("RGB"))


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=radius, fill=255)
    return m


def paste_round(base: Image.Image, overlay: Image.Image, xy: tuple[int, int], radius: int) -> None:
    mask = rounded_mask(overlay.size, radius)
    base.paste(overlay, xy, mask)


def text_size(draw: ImageDraw.ImageDraw, text: str, fnt: ImageFont.FreeTypeFont) -> tuple[int, int]:
    box = draw.textbbox((0, 0), text, font=fnt)
    return box[2] - box[0], box[3] - box[1]


def center_text(draw: ImageDraw.ImageDraw, text: str, cy: int, fnt: ImageFont.FreeTypeFont, fill, x0: int, x1: int) -> None:
    w, h = text_size(draw, text, fnt)
    draw.text(((x0 + x1 - w) // 2, cy - h // 2), text, font=fnt, fill=fill)


def make_play_icon() -> None:
    """Full-bleed 512 square: flood-fill only the white corners, keep the glyph."""
    from collections import deque

    src = Image.open(ICON_SRC).convert("RGB")
    src = src.resize((512, 512), Image.Resampling.LANCZOS)
    px = src.load()
    w, h = src.size

    def is_white(x: int, y: int) -> bool:
        r, g, b = px[x, y]
        return r > 240 and g > 240 and b > 240

    seen = bytearray(w * h)
    q: deque[tuple[int, int]] = deque()
    for seed in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)):
        if is_white(*seed):
            q.append(seed)
            seen[seed[1] * w + seed[0]] = 1
    corners: list[tuple[int, int]] = []
    while q:
        x, y = q.popleft()
        corners.append((x, y))
        for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if 0 <= nx < w and 0 <= ny < h and not seen[ny * w + nx] and is_white(nx, ny):
                seen[ny * w + nx] = 1
                q.append((nx, ny))

    row_color: list[tuple[int, int, int]] = []
    for y in range(h):
        colors = [
            px[x, y]
            for x in range(w)
            if not seen[y * w + x] and (px[x, y][0] < 240 or px[x, y][1] < 240 or px[x, y][2] < 240)
        ]
        if colors:
            row_color.append(colors[len(colors) // 2])
        else:
            row_color.append(row_color[-1] if row_color else (110, 30, 210))
    for x, y in corners:
        px[x, y] = row_color[y]
    OUT.mkdir(parents=True, exist_ok=True)
    src.save(OUT / "icon-512.png", "PNG")


def photo_tile(size: tuple[int, int], hue: str) -> Image.Image:
    palettes = {
        "sunset": [(255, 183, 77), (240, 98, 146), (123, 31, 162)],
        "ocean": [(14, 165, 233), (59, 130, 246), (30, 64, 175)],
        "forest": [(52, 211, 153), (16, 185, 129), (6, 95, 70)],
        "sand": [(253, 224, 171), (251, 191, 36), (180, 83, 9)],
        "lilac": [(196, 181, 253), (167, 139, 250), (91, 33, 182)],
        "rose": [(251, 113, 133), (244, 63, 94), (136, 19, 55)],
        "mint": [(167, 243, 208), (45, 212, 191), (15, 118, 110)],
        "night": [(76, 29, 149), (49, 46, 129), (15, 23, 42)],
    }
    colors = palettes[hue]
    img = vertical_gradient(size, [(0.0, colors[0]), (0.55, colors[1]), (1.0, colors[2])])
    d = ImageDraw.Draw(img)
    w, h = size
    # Simple landscape silhouette so tiles read as photos, not flat color.
    d.ellipse((int(w * 0.62), int(h * 0.12), int(w * 0.86), int(h * 0.36)), fill=(255, 255, 255, ))
    d.polygon(
        [(0, h), (int(w * 0.18), int(h * 0.55)), (int(w * 0.42), int(h * 0.72)), (int(w * 0.7), int(h * 0.48)), (w, int(h * 0.62)), (w, h)],
        fill=lerp(colors[-1], (20, 10, 30), 0.45),
    )
    return img


def phone_chrome(inner: Image.Image) -> Image.Image:
    """Wrap UI in a dark bezel so screenshots look like a device, not a raw crop."""
    pad = 18
    bezel = 10
    w, h = inner.size
    frame_w, frame_h = w + (pad + bezel) * 2, h + (pad + bezel) * 2
    frame = Image.new("RGB", (frame_w, frame_h), (12, 8, 22))
    d = ImageDraw.Draw(frame)
    d.rounded_rectangle((0, 0, frame_w - 1, frame_h - 1), radius=72, fill=(18, 14, 32), outline=(80, 70, 110), width=3)
    d.rounded_rectangle(
        (bezel, bezel, frame_w - 1 - bezel, frame_h - 1 - bezel),
        radius=62,
        fill=(6, 4, 14),
    )
    paste_round(frame, inner, (pad + bezel, pad + bezel), 52)
    # Dynamic island
    island_w, island_h = 160, 28
    ix = (frame_w - island_w) // 2
    d.rounded_rectangle((ix, 22, ix + island_w, 22 + island_h), radius=14, fill=(8, 6, 16))
    return frame


def draw_status(d: ImageDraw.ImageDraw, w: int, dark: bool = True) -> None:
    fill = (255, 255, 255, 220) if dark else NAVY
    d.text((28, 14), "9:41", font=F_SB(22), fill=fill)
    d.text((w - 118, 14), "5G  100%", font=F_SB(20), fill=fill)


# ---------------------------------------------------------------------------
# In-phone UI mockups (simplified, on-brand, no fake ratings)
# ---------------------------------------------------------------------------

def ui_home() -> Image.Image:
    img = vertical_gradient((900, 1680), DARK_STOPS)
    radial_glow(img, (450, 80), 280, (168, 85, 247), 70)
    radial_glow(img, (80, 720), 260, (79, 70, 229), 50)
    d = ImageDraw.Draw(img)
    draw_status(d, 900, True)
    d.text((28, 64), "Photo Recovery", font=F_BD(34), fill=WHITE)
    d.text((28, 108), "Deep Storage Engine", font=F_REG(20), fill=(196, 181, 253))
    card = Image.new("RGBA", img.size, (0, 0, 0, 0))
    cd = ImageDraw.Draw(card)
    cd.rounded_rectangle((28, 160, 872, 720), radius=36, fill=(255, 255, 255, 28), outline=(255, 255, 255, 50), width=2)
    img = Image.alpha_composite(img.convert("RGBA"), card).convert("RGB")
    d = ImageDraw.Draw(img)
    center_text(d, "100% Secure  ·  On-Device", 200, F_SB(20), CYAN, 28, 872)
    # Radar
    cx, cy, r = 450, 400, 118
    for rad, col in ((176, (167, 139, 250)), (144, (244, 114, 182)), (112, (132, 85, 239))):
        d.ellipse((cx - rad, cy - rad, cx + rad, cy + rad), outline=col, width=2)
    d.ellipse((cx - 78, cy - 78, cx + 78, cy + 78), fill=PRIMARY_2)
    d.ellipse((cx - 62, cy - 62, cx + 62, cy + 62), fill=(58, 12, 163))
    center_text(d, "SCAN", cy - 6, F_BD(22), WHITE, 0, 900)
    center_text(d, "Ready to Deep Scan", 560, F_BD(30), WHITE, 28, 872)
    center_text(d, "Find photos and videos on this device", 600, F_REG(20), (226, 232, 240), 28, 872)
    d.rounded_rectangle((90, 640, 810, 696), radius=18, fill=PINK)
    center_text(d, "Start Deep Scan   ·   42 GB Ready", 668, F_SB(22), WHITE, 90, 810)

    d.text((32, 752), "SMART CLEAN & RECOVERY", font=F_BD(18), fill=(226, 232, 240))
    tiles = [
        ("Duplicate Cleaner", "Identical photos", (196, 181, 253)),
        ("Screenshots", "Remove clutter", (249, 168, 212)),
        ("Recently Deleted", "Restore copies", CYAN),
        ("Secure Vault", "Private folder", EMERALD),
    ]
    positions = [(28, 790), (464, 790), (28, 1040), (464, 1040)]
    for (title, sub, tint), (x, y) in zip(tiles, positions):
        overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
        od = ImageDraw.Draw(overlay)
        od.rounded_rectangle((x, y, x + 408, y + 230), radius=24, fill=(255, 255, 255, 24), outline=(255, 255, 255, 40), width=2)
        img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
        d = ImageDraw.Draw(img)
        d.rounded_rectangle((x + 22, y + 22, x + 70, y + 70), radius=14, fill=tint)
        d.text((x + 22, y + 96), title, font=F_BD(24), fill=WHITE)
        d.text((x + 22, y + 136), sub, font=F_REG(18), fill=(203, 213, 225))

    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.rounded_rectangle((28, 1290, 872, 1410), radius=22, fill=(255, 255, 255, 24), outline=(255, 255, 255, 40), width=2)
    img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((50, 1314, 114, 1378), radius=16, fill=EMERALD)
    d.text((136, 1320), "Quick Swipe Clean", font=F_BD(24), fill=WHITE)
    d.text((136, 1356), "Swipe to keep, protect or clean up", font=F_REG(18), fill=(203, 213, 225))
    return img


def ui_restore() -> Image.Image:
    img = Image.new("RGB", (900, 1680), SURFACE)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 900, 128), fill=PRIMARY)
    draw_status(d, 900, True)
    d.text((28, 64), "Photo Recovery", font=F_BD(30), fill=WHITE)
    d.text((28, 150), "Deep Scan Complete", font=F_SB(20), fill=PRIMARY)
    d.text((28, 184), "1,842 Photos Found", font=F_BD(36), fill=NAVY)
    d.text((28, 232), "Select items to export into Restored.", font=F_REG(22), fill=MUTED)
    stats = [("Photos", "1,624"), ("Videos", "218"), ("Restored", "36")]
    for i, (label, value) in enumerate(stats):
        x = 28 + i * 288
        d.rounded_rectangle((x, 280, x + 268, 380), radius=20, fill=WHITE, outline=(203, 195, 215), width=1)
        d.text((x + 20, 296), label, font=F_SB(18), fill=MUTED)
        d.text((x + 20, 324), value, font=F_BD(30), fill=NAVY)
    hues = ["sunset", "ocean", "forest", "sand", "lilac", "rose", "mint", "night", "sunset"]
    gap, tile = 12, 276
    y0 = 410
    for i, hue in enumerate(hues):
        col, row = i % 3, i // 3
        x = 28 + col * (tile + gap)
        y = y0 + row * (tile + gap)
        tile_img = photo_tile((tile, tile), hue)
        paste_round(img, tile_img, (x, y), 18)
        if i in (0, 2, 4, 5):
            d = ImageDraw.Draw(img)
            d.ellipse((x + tile - 46, y + 12, x + tile - 12, y + 46), fill=PRIMARY)
            d.ellipse((x + tile - 40, y + 18, x + tile - 18, y + 40), outline=WHITE, width=3)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((28, 1520, 872, 1610), radius=28, fill=PRIMARY)
    center_text(d, "Recover Selected (4)", 1565, F_BD(24), WHITE, 28, 872)
    return img


def ui_swipe() -> Image.Image:
    img = Image.new("RGB", (900, 1680), SURFACE)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 900, 128), fill=PRIMARY)
    draw_status(d, 900, True)
    d.text((28, 64), "Quick Swipe Clean", font=F_BD(30), fill=WHITE)
    d.text((28, 156), "Delete protection on", font=F_SB(20), fill=PRIMARY)
    d.text((28, 190), "Left = Trash   ·   Right = Vault", font=F_REG(22), fill=MUTED)
    hero = photo_tile((844, 980), "ocean")
    paste_round(img, hero, (28, 250), 32)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((48, 270, 260, 322), radius=16, fill=(15, 23, 42))
    d.text((68, 282), "Photo  12 / 86", font=F_SB(18), fill=WHITE)
    # Gesture chips
    chips = [(28, 1280, (186, 26, 26), "Bin"), (244, 1280, MUTED, "Prev"), (460, 1280, MUTED, "Next"), (676, 1280, (0, 110, 28), "Vault")]
    for x, y, color, label in chips:
        d.rounded_rectangle((x, y, x + 196, y + 88), radius=22, fill=color)
        center_text(d, label, y + 44, F_BD(22), WHITE, x, x + 196)
    d.rounded_rectangle((28, 1390, 872, 1490), radius=22, fill=WHITE, outline=(203, 195, 215), width=1)
    d.text((52, 1412), "1.2 GB freed", font=F_BD(24), fill=NAVY)
    d.text((52, 1448), "24 protected in Vault", font=F_REG(20), fill=MUTED)
    return img


def ui_duplicates() -> Image.Image:
    img = Image.new("RGB", (900, 1680), SURFACE)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 900, 128), fill=PRIMARY)
    draw_status(d, 900, True)
    d.text((28, 64), "Clean Storage", font=F_BD(30), fill=WHITE)
    d.text((28, 156), "CHUNK MD5 HASH VERIFIED", font=F_SB(16), fill=PRIMARY)
    d.text((28, 188), "12 Groups Found", font=F_BD(36), fill=NAVY)
    d.text((28, 236), "Scanned 2,410 photos  ·  1.8 GB can be freed", font=F_REG(20), fill=MUTED)
    y = 290
    groups = [("sunset", "ocean", "Group of 3  ·  24.6 MB"), ("forest", "mint", "Group of 2  ·  8.1 MB"), ("rose", "lilac", "Group of 4  ·  41.0 MB")]
    for a, b, caption in groups:
        d.rounded_rectangle((28, y, 872, y + 330), radius=24, fill=WHITE, outline=(203, 195, 215), width=1)
        d.text((48, y + 18), caption, font=F_SB(20), fill=NAVY)
        d.text((330, y + 18), "Original", font=F_SB(16), fill=(0, 110, 28))
        left = photo_tile((390, 240), a)
        right = photo_tile((390, 240), b)
        paste_round(img, left, (48, y + 60), 16)
        paste_round(img, right, (462, y + 60), 16)
        d = ImageDraw.Draw(img)
        d.ellipse((48 + 390 - 50, y + 72, 48 + 390 - 16, y + 106), fill=(0, 110, 28))
        d.ellipse((462 + 390 - 50, y + 72, 462 + 390 - 16, y + 106), fill=PRIMARY)
        y += 354
    d.rounded_rectangle((28, 1520, 872, 1610), radius=28, fill=(186, 26, 26))
    center_text(d, "Delete Selected (8 items  ·  1.8 GB)", 1565, F_BD(22), WHITE, 28, 872)
    return img


def ui_screenshots() -> Image.Image:
    img = Image.new("RGB", (900, 1680), SURFACE)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 900, 128), fill=PRIMARY)
    draw_status(d, 900, True)
    d.text((28, 64), "Screenshot Cleaner", font=F_BD(30), fill=WHITE)
    d.text((28, 156), "SMART STORAGE AUDIT", font=F_SB(16), fill=PRIMARY)
    d.text((28, 188), "146 screenshots detected", font=F_BD(32), fill=NAVY)
    d.text((28, 236), "Today  ·  Yesterday  ·  Older", font=F_REG(20), fill=MUTED)
    hues = ["lilac", "sand", "night", "ocean", "rose", "mint", "sunset", "forest", "lilac"]
    gap, tile = 12, 276
    y0 = 290
    for i, hue in enumerate(hues):
        col, row = i % 3, i // 3
        x = 28 + col * (tile + gap)
        y = y0 + row * (tile + 72)
        # Fake screenshot chrome
        shot = Image.new("RGB", (tile, tile), (241, 245, 249))
        sd = ImageDraw.Draw(shot)
        sd.rectangle((0, 0, tile, 36), fill=(15, 23, 42))
        sd.text((12, 8), "Chrome", font=F_SB(16), fill=WHITE)
        inner = photo_tile((tile - 24, tile - 60), hue)
        shot.paste(inner, (12, 48))
        paste_round(img, shot, (x, y), 16)
        d = ImageDraw.Draw(img)
        if i % 2 == 0:
            d.ellipse((x + tile - 46, y + 12, x + tile - 12, y + 46), fill=PRIMARY)
        d.text((x, y + tile + 8), "Screenshot", font=F_REG(16), fill=MUTED)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((28, 1520, 872, 1610), radius=28, fill=(186, 26, 26))
    center_text(d, "Delete Selected (5)", 1565, F_BD(24), WHITE, 28, 872)
    return img


def ui_vault() -> Image.Image:
    img = Image.new("RGB", (900, 1680), SURFACE)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 900, 128), fill=PRIMARY)
    draw_status(d, 900, True)
    d.text((28, 64), "Secure Vault", font=F_BD(30), fill=WHITE)
    d.text((28, 156), "Locked private media folder", font=F_SB(20), fill=PRIMARY)
    d.text((28, 190), "18 items  ·  stays on this device", font=F_BD(28), fill=NAVY)
    hues = ["night", "rose", "forest", "sand"]
    gap, tile = 20, 400
    y0 = 280
    for i, hue in enumerate(hues):
        col, row = i % 2, i // 2
        x = 28 + col * (tile + gap)
        y = y0 + row * (tile + gap)
        tile_img = photo_tile((tile, tile), hue)
        paste_round(img, tile_img, (x, y), 22)
        d = ImageDraw.Draw(img)
        d.rounded_rectangle((x + 16, y + 16, x + 70, y + 52), radius=12, fill=(15, 23, 42))
        d.text((x + 28, y + 22), "LOCK", font=F_BD(14), fill=EMERALD)
    d.rounded_rectangle((28, 1520, 440, 1610), radius=28, fill=PRIMARY)
    d.rounded_rectangle((460, 1520, 872, 1610), radius=28, fill=(186, 26, 26))
    center_text(d, "Restore", 1565, F_BD(24), WHITE, 28, 440)
    center_text(d, "Delete", 1565, F_BD(24), WHITE, 460, 872)
    return img


CAPTIONS = {
    "en": [
        ("Find photos on your device", "Scan the gallery and save copies"),
        ("Save copies to Restored", "Export selected photos into an album"),
        ("Swipe to clean or protect", "Trash with system confirm, or Vault"),
        ("Remove identical photos", "Keep one original, reclaim storage"),
        ("Clear screenshot clutter", "Find captures and delete what you skip"),
        ("Private folder on this phone", "Vault stays on-device — no cloud upload"),
    ],
    "vi": [
        ("Tìm ảnh đang có trên máy", "Quét thư viện và lưu bản sao"),
        ("Lưu vào album Restored", "Xuất ảnh đã chọn ra thư viện"),
        ("Vuốt để xóa hoặc bảo vệ", "Xóa có xác nhận hệ thống, hoặc Vault"),
        ("Xóa ảnh trùng lặp", "Giữ một bản gốc, lấy lại dung lượng"),
        ("Dọn ảnh chụp màn hình", "Tìm screenshot và xóa mục không cần"),
        ("Thư mục riêng trên máy", "Vault lưu trên thiết bị, không tải lên mây"),
    ],
}


def compose_store_shot(ui: Image.Image, title: str, subtitle: str) -> Image.Image:
    canvas = vertical_gradient((1080, 1920), DARK_STOPS)
    radial_glow(canvas, (200, 120), 320, (168, 85, 247), 80)
    radial_glow(canvas, (900, 1700), 360, (236, 72, 153), 55)
    d = ImageDraw.Draw(canvas)
    overlay = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.rounded_rectangle((70, 56, 330, 108), radius=24, fill=(255, 255, 255, 28), outline=(103, 232, 249, 160), width=2)
    canvas = Image.alpha_composite(canvas.convert("RGBA"), overlay).convert("RGB")
    d = ImageDraw.Draw(canvas)
    d.text((88, 68), "ON-DEVICE", font=F_BD(20), fill=CYAN)
    # Wrap title if needed
    title_font = F_BD(52)
    tw, _ = text_size(d, title, title_font)
    if tw > 940:
        title_font = F_BD(44)
    d.text((70, 130), title, font=title_font, fill=WHITE)
    d.text((70, 210), subtitle, font=F_REG(28), fill=(196, 181, 253))
    phone = phone_chrome(ui.resize((820, 1530), Image.Resampling.LANCZOS))
    px = (1080 - phone.size[0]) // 2
    canvas.paste(phone, (px, 280))
    return canvas.convert("RGB")


def make_feature_graphic() -> None:
    canvas = vertical_gradient((1024, 500), DARK_STOPS)
    radial_glow(canvas, (180, 40), 220, (168, 85, 247), 90)
    radial_glow(canvas, (860, 420), 240, (236, 72, 153), 70)
    d = ImageDraw.Draw(canvas)
    overlay = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.rounded_rectangle((56, 58, 250, 104), radius=20, fill=(255, 255, 255, 30), outline=(103, 232, 249, 160), width=2)
    canvas = Image.alpha_composite(canvas.convert("RGBA"), overlay).convert("RGB")
    d = ImageDraw.Draw(canvas)
    d.text((72, 68), "ON-DEVICE", font=F_BD(18), fill=CYAN)
    d.text((56, 130), "Photo Recovery", font=F_BD(54), fill=WHITE)
    d.text((56, 210), "Restore  ·  Clean  ·  Protect", font=F_SB(28), fill=(196, 181, 253))
    d.text((56, 270), "Save copies, remove duplicates", font=F_REG(22), fill=(226, 232, 240))
    d.text((56, 304), "and keep media private in Vault.", font=F_REG(22), fill=(226, 232, 240))

    # Mini phone previews on the right
    home = ui_home().resize((280, 520), Image.Resampling.LANCZOS)
    rest = ui_restore().resize((280, 520), Image.Resampling.LANCZOS)
    paste_round(canvas, rest, (700, 40), 28)
    paste_round(canvas, home, (560, 90), 28)
    # Crop anything that spilled — paste_round already clipped.
    canvas = canvas.crop((0, 0, 1024, 500)).convert("RGB")
    canvas.save(OUT / "feature-graphic-1024x500.png", "PNG")


def make_screenshots() -> None:
    uis = [ui_home(), ui_restore(), ui_swipe(), ui_duplicates(), ui_screenshots(), ui_vault()]
    names = ["01-home", "02-restore", "03-swipe", "04-duplicates", "05-screenshots", "06-vault"]
    for locale, folder in (("en", OUT / "phone"), ("vi", OUT / "phone-vi")):
        folder.mkdir(parents=True, exist_ok=True)
        for ui, name, (title, sub) in zip(uis, names, CAPTIONS[locale]):
            shot = compose_store_shot(ui, title, sub)
            shot.save(folder / f"{name}.png", "PNG")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    print("Icon…")
    make_play_icon()
    print("Feature graphic…")
    make_feature_graphic()
    print("Screenshots…")
    make_screenshots()
    print("Done ->", OUT)


if __name__ == "__main__":
    main()
