#!/usr/bin/env python3
"""
Generate placeholder textures for MC Clothing.

Writes:
  assets/mcclothing/textures/item/{id}.png         (16x16, greyscale base)
  assets/mcclothing/textures/item/{id}_overlay.png (16x16, colored overlay tinted at runtime)
  assets/mcclothing/textures/entity/clothing/{id}.png  (64x64, player-UV-mapped solid fill)
  assets/mcclothing/textures/item/empty_{slot}_slot.png (16x16, empty trinket slot icon)

The entity textures paint solid colors only at the UV regions that ClothingModel
actually samples, so DyedColorComponent tint visibly applies. Other regions stay
transparent.

Run from project root:
  python3 scripts/gen_textures.py
"""
from PIL import Image, ImageDraw
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/mcclothing"
ITEM_DIR = ASSETS / "textures/item"
ENTITY_DIR = ASSETS / "textures/entity/clothing"
PATTERN_DIR = ENTITY_DIR / "pattern"

ITEM_DIR.mkdir(parents=True, exist_ok=True)
ENTITY_DIR.mkdir(parents=True, exist_ok=True)
PATTERN_DIR.mkdir(parents=True, exist_ok=True)

# UV regions from ClothingModel.createBaseModelData (32-wide model boxed to 64x64 atlas).
# Each region is the unwrapped rectangle of the cuboid as Minecraft lays them out.
# For an 8x12x4 body cuboid at uv(16,16), the unwrap is 24x20 covering top/bottom/sides/front/back.
UV_REGIONS = {
    "body":       (16, 16, 16 + 24, 16 + 20),   # 24x20
    "strap_l":    ( 0, 48,  0 +  4, 48 + 14),   # narrow band
    "strap_r":    ( 8, 48,  8 +  4, 48 + 14),
    "sleeve_l":   (40, 32, 40 + 16, 32 + 14),
    "sleeve_r":   (40, 16, 40 + 16, 16 + 14),
    "front":      ( 0, 32,  0 + 16, 32 + 16),   # apron front panel (8 wide cuboid -> 16 in unwrap)
    "leg_l":      ( 0, 16,  0 + 16, 16 + 16),
    "leg_r":      (16, 48, 16 + 16, 48 + 16),
}

# Defaults match ClothingItem default colors. Used only for the inventory icon overlay;
# the entity texture is painted neutral and tinted by DyedColorComponent at runtime.
ITEM_COLORS = {
    "overalls": (0x3B, 0x5F, 0x8A),  # denim
    "apron":    (0xCB, 0xB4, 0x8A),  # canvas
    "t_shirt":  (0xE8, 0xDE, 0xC4),  # linen
    "vest":     (0x8B, 0x5A, 0x2B),  # leather
    "pants":    (0x3B, 0x5F, 0x8A),  # denim
    "shorts":   (0xCB, 0xB4, 0x8A),  # canvas
}

# Which UV regions each clothing shape fills.
SHAPE_REGIONS = {
    "overalls": ["body", "strap_l", "strap_r", "leg_l", "leg_r"],
    "apron":    ["front"],
    "t_shirt":  ["body", "sleeve_l", "sleeve_r"],
    "vest":     ["body"],
    "pants":    ["leg_l", "leg_r"],
    "shorts":   ["leg_l", "leg_r"],
}

# Entity texture fill color (neutral grey so dye tint reads cleanly).
# Vanilla leather armor uses ~0xC4C4C4 for the dyeable layer.
ENTITY_FILL = (0xC4, 0xC4, 0xC4, 0xFF)
ENTITY_STITCH = (0x80, 0x80, 0x80, 0xFF)  # stitching/seam highlight

def make_entity_texture(shape_name: str, regions: list[str]) -> Image.Image:
    """64x64 transparent canvas, fill the named UV regions."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    pix = img.load()
    for region in regions:
        x0, y0, x1, y1 = UV_REGIONS[region]
        for x in range(x0, x1):
            for y in range(y0, y1):
                pix[x, y] = ENTITY_FILL
        # Add a faint seam border so it doesn't look like a flat blob.
        for x in range(x0, x1):
            pix[x, y0] = ENTITY_STITCH
            pix[x, y1 - 1] = ENTITY_STITCH
        for y in range(y0, y1):
            pix[x0, y] = ENTITY_STITCH
            pix[x1 - 1, y] = ENTITY_STITCH
    return img


def shade(rgb: tuple[int, int, int], factor: float) -> tuple[int, int, int]:
    return tuple(max(0, min(255, int(c * factor))) for c in rgb)


def make_item_icon(shape_name: str) -> tuple[Image.Image, Image.Image]:
    """
    Two-layer item icon (vanilla leather-armor pattern):
      base (layer0) — greyscale silhouette
      overlay (layer1) — full color shape, ItemColorProvider tints from DyedColorComponent
    """
    base = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    overlay = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    bd = ImageDraw.Draw(base)
    od = ImageDraw.Draw(overlay)
    fill_b = (160, 160, 160, 255)
    edge_b = (100, 100, 100, 255)
    fill_o = (*ITEM_COLORS[shape_name], 255)
    edge_o = (*shade(ITEM_COLORS[shape_name], 0.6), 255)

    if shape_name == "overalls":
        # straps
        for d in (bd, od):
            d.rectangle((4, 1, 5, 4), fill=(fill_b if d is bd else fill_o))
            d.rectangle((10, 1, 11, 4), fill=(fill_b if d is bd else fill_o))
        # body+legs
        bd.rectangle((3, 4, 12, 14), fill=fill_b, outline=edge_b)
        od.rectangle((3, 4, 12, 14), fill=fill_o, outline=edge_o)
        # crotch split
        bd.rectangle((7, 9, 8, 14), fill=edge_b)
        od.rectangle((7, 9, 8, 14), fill=edge_o)
    elif shape_name == "apron":
        # neck loop
        bd.rectangle((6, 1, 9, 3), outline=edge_b)
        od.rectangle((6, 1, 9, 3), outline=edge_o)
        # body
        bd.rectangle((3, 3, 12, 14), fill=fill_b, outline=edge_b)
        od.rectangle((3, 3, 12, 14), fill=fill_o, outline=edge_o)
        # waist tie
        bd.rectangle((1, 8, 14, 9), fill=edge_b)
        od.rectangle((1, 8, 14, 9), fill=edge_o)
    elif shape_name == "t_shirt":
        bd.rectangle((4, 3, 11, 12), fill=fill_b, outline=edge_b)
        od.rectangle((4, 3, 11, 12), fill=fill_o, outline=edge_o)
        # sleeves
        bd.rectangle((1, 4, 3, 7), fill=fill_b, outline=edge_b)
        bd.rectangle((12, 4, 14, 7), fill=fill_b, outline=edge_b)
        od.rectangle((1, 4, 3, 7), fill=fill_o, outline=edge_o)
        od.rectangle((12, 4, 14, 7), fill=fill_o, outline=edge_o)
        # collar
        bd.rectangle((6, 3, 9, 4), fill=edge_b)
        od.rectangle((6, 3, 9, 4), fill=edge_o)
    elif shape_name == "vest":
        bd.rectangle((4, 3, 11, 13), fill=fill_b, outline=edge_b)
        od.rectangle((4, 3, 11, 13), fill=fill_o, outline=edge_o)
        # open V neck
        bd.rectangle((7, 3, 8, 7), fill=(0, 0, 0, 0))
        od.rectangle((7, 3, 8, 7), fill=(0, 0, 0, 0))
    elif shape_name == "pants":
        bd.rectangle((4, 3, 11, 14), fill=fill_b, outline=edge_b)
        od.rectangle((4, 3, 11, 14), fill=fill_o, outline=edge_o)
        bd.rectangle((7, 6, 8, 14), fill=edge_b)
        od.rectangle((7, 6, 8, 14), fill=edge_o)
    elif shape_name == "shorts":
        bd.rectangle((4, 5, 11, 10), fill=fill_b, outline=edge_b)
        od.rectangle((4, 5, 11, 10), fill=fill_o, outline=edge_o)
        bd.rectangle((7, 7, 8, 10), fill=edge_b)
        od.rectangle((7, 7, 8, 10), fill=edge_o)
    else:
        raise ValueError(shape_name)

    return base, overlay


def make_empty_slot(letter: str) -> Image.Image:
    """16x16 faint slot icon."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle((1, 1, 14, 14), outline=(96, 96, 96, 200))
    # Tiny letter glyph (drawn as pixels — default font is too big for 16x16)
    glyphs = {
        "S": [(5,4),(6,4),(7,4),(8,4),(9,4),(5,5),(5,6),(5,7),(6,7),(7,7),(8,7),(9,7),(9,8),(9,9),(9,10),(5,10),(6,10),(7,10),(8,10),(9,10)],
        "A": [(7,4),(6,5),(8,5),(5,6),(9,6),(5,7),(9,7),(5,8),(6,8),(7,8),(8,8),(9,8),(5,9),(9,9),(5,10),(9,10)],
        "P": [(5,4),(6,4),(7,4),(8,4),(5,5),(9,5),(5,6),(9,6),(5,7),(6,7),(7,7),(8,7),(5,8),(5,9),(5,10)],
    }
    for (x, y) in glyphs.get(letter.upper(), []):
        img.putpixel((x, y), (180, 180, 180, 220))
    return img


def main():
    for shape in ITEM_COLORS:
        base, overlay = make_item_icon(shape)
        base.save(ITEM_DIR / f"{shape}.png")
        overlay.save(ITEM_DIR / f"{shape}_overlay.png")
        ent = make_entity_texture(shape, SHAPE_REGIONS[shape])
        ent.save(ENTITY_DIR / f"{shape}.png")
        print(f"wrote {shape}")

    for slot, letter in [("shirt", "S"), ("apron", "A"), ("pants", "P")]:
        make_empty_slot(letter).save(ITEM_DIR / f"empty_{slot}_slot.png")
        print(f"wrote empty_{slot}_slot")

    # Stub pattern textures — single-pixel white so the renderer doesn't crash on
    # missing textures while patterns are still being designed.
    stub = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for x in range(64):
        for y in range(64):
            if (x + y) % 8 == 0:
                stub.putpixel((x, y), (255, 255, 255, 180))
    for pat in ["stripe", "checkered", "plaid", "pocket", "hem_trim", "collar", "embroidery", "patch"]:
        stub.save(PATTERN_DIR / f"{pat}.png")
    print(f"wrote 8 pattern stubs")


if __name__ == "__main__":
    main()
