# MC Clothing

A Fabric mod that adds dyeable, pattern-customizable clothing layers to
Minecraft: **overalls, aprons, t-shirts, vests, pants, shorts**. Clothing
equips into dedicated Trinkets slots (so it doesn't fight vanilla armor),
drapes over the player model, and exposes warmth tags that any temperature
mod (e.g. Overly Realistic) can consume.

- **Minecraft:** 1.21.1
- **Loader:** Fabric (loader 0.16.5, Fabric API 0.103.0+1.21.1)
- **Required mods:** Fabric API, [Trinkets](https://modrinth.com/mod/trinkets) 3.10.0+
- **Java:** 21

## Feature summary

| Feature | Status |
| --- | --- |
| Six clothing items registered | Done |
| Dyeable in vanilla crafting grid (leather-armor style) | Done |
| Trinkets slot wiring (`shirt`, `apron`, `pants`) | Done |
| 3D model drapes over biped, syncs to animation | Done |
| Tailor's Loom block + screen for pattern application | Done |
| Pattern stack persists on item via data component | Done |
| Built-in pattern catalog (8 patterns) | Done |
| Warmth tags exposed for temperature mods | Done |
| Per-item bespoke 3D geometry | Shared model with shape toggles |
| Pattern previews in loom GUI | Letter glyphs only (placeholder) |
| Textures (item, entity, patterns, slot icons) | Not drawn yet — placeholders show as missing-texture purple |
| Custom Tailor's Loom GUI background | Reuses vanilla loom texture |

## Building

The Gradle wrapper jar is not checked in. Generate it once with a system
Gradle install, then use `./gradlew` from there on:

```sh
gradle wrapper            # one-time, requires gradle 8+ on PATH
./gradlew build           # produces build/libs/mc-clothing-<version>.jar
./gradlew runClient       # launches a dev client with the mod loaded
./gradlew runServer       # dev server
```

`build.gradle` references a `LICENSE` file in the jar `from(...)` block —
either drop a `LICENSE` next to `build.gradle` or remove that line.

## Project layout

```
src/main/java/com/mcclothing/
  MCClothing.java                  common entrypoint
  MCClothingClient.java            client entrypoint
  item/
    ClothingItem.java              base item, dyeable, holds pattern component
    ClothingShape.java             which body parts a shape uses (body/straps/sleeves/...)
    ClothingSlot.java              which trinket subslot the item targets
    ModItems.java                  registers the 6 clothing items + creative-tab hookup
  block/
    ModBlocks.java
    TailorsLoomBlock.java          horizontal-facing block, opens loom GUI
  screen/
    ModScreenHandlers.java
    TailorsLoomScreenHandler.java  clothing + dye -> patterned output
  client/
    ModEntityModelLayers.java      registers the clothing model layer
    ClothingTrinketRenderer.java   one renderer for all clothing types
    model/ClothingModel.java       biped model with toggleable parts
    screen/TailorsLoomScreen.java
  pattern/
    ClothingPattern.java           record: assetId + translation key
    ClothingPatterns.java          registry + 8 built-in pattern keys
    ClothingPatternEntry.java      one stack layer (pattern id + dye color)
    ClothingPatternsComponent.java data component value (list of entries, capped at 8)
  component/
    ModDataComponents.java         registers the CLOTHING_PATTERNS component
  trinket/
    ClothingTrinket.java           Trinket impl, slot validation
  compat/
    WarmthTags.java                public TagKey<Item> constants for temperature mods

src/main/resources/
  fabric.mod.json
  mcclothing.mixins.json
  assets/mcclothing/
    lang/en_us.json
    models/{item,block}/...
    blockstates/tailors_loom.json
    textures/...                   YOU NEED TO DRAW THESE
  data/mcclothing/
    recipe/...
    tags/item/warmth/{light,medium,heavy}.json
  data/minecraft/tags/item/dyeable.json
  data/trinkets/slots/{chest,legs}/...
```

## Architecture notes

### Dyeing

Each `ClothingItem` is constructed with `DyedColorComponent` set to a
sensible default (denim, canvas, linen, leather). The item ids are listed
in `data/minecraft/tags/item/dyeable.json`, which is what vanilla's
crafting-table dye recipe checks. Combining a clothing item with any dye
in the 2x2 grid produces the dyed result, mirroring leather armor exactly.

The inventory icon uses a two-layer item model (`layer0` greyscale +
`layer1` overlay tinted from the component) via
`ColorProviderRegistry.ITEM.register(...)` in `MCClothingClient`.

### Pattern customization

Patterns live in a custom registry (`mcclothing:clothing_pattern`) built
on top of `FabricRegistryBuilder.createSimple`. The Tailor's Loom takes
a clothing piece + a dye, lets the player pick a pattern, and stores the
result on the output stack via the `mcclothing:clothing_patterns` data
component as an ordered list of `(patternId, dyeColor)` entries. The
renderer iterates this list and draws each pattern as a translucent
overlay, tinted by its dye color.

Stack limit is **8 patterns**. Pattern ids are stored as raw
`Identifier`s rather than `RegistryEntry<ClothingPattern>` — unknown ids
just render as absent rather than nuking the whole component.

### Rendering

`ClothingModel` extends `BipedEntityModel` and exposes named parts
(`bodyOverlay`, `straps`, `leftSleeve`, `rightSleeve`, `frontPanel`,
`leftLeg`, `rightLeg`). Each `ClothingShape` toggles a subset of those
parts visible — overalls use `body + straps + legs` (no sleeves), t-shirts
use `body + sleeves`, etc.

Per frame, `copyTransforms(host)` copies the player biped's pivot &
rotation onto the clothing parts, so the cloth animates with the player.
A single instance per `ClothingShape` is cached in
`ClothingTrinketRenderer.models`.

### Warmth integration

Three item tags expose warmth tiers. Any mod can read them with the
constants in `com.mcclothing.compat.WarmthTags`:

```java
if (stack.isIn(WarmthTags.LIGHT))  warmth += 0.10 * leatherBase;
if (stack.isIn(WarmthTags.MEDIUM)) warmth += 0.25 * leatherBase;
if (stack.isIn(WarmthTags.HEAVY))  warmth += 0.45 * leatherBase;
```

Defaults:

| Tag | Items |
| --- | --- |
| `mcclothing:warmth/light`  | t-shirt, shorts, apron |
| `mcclothing:warmth/medium` | pants, vest |
| `mcclothing:warmth/heavy`  | overalls |

Players or pack authors can override by adding items to the tags via a
datapack.

## Extending the mod

### Adding a pattern (in-code)

```java
public static final RegistryKey<ClothingPattern> MY_PATTERN = key("my_pattern");
```

…in `ClothingPatterns.java` next to the existing keys, then drop a
`textures/entity/clothing/pattern/my_pattern.png` (64x64, white pixels
on transparent) and a lang entry per dye color.

### Adding a new clothing item

1. Add a new `ClothingShape` enum entry with the right body-part toggles.
2. Register the item in `ModItems` with `register("my_item", s -> new ClothingItem(...))`.
3. Add it to a slot in `ClothingTrinket.matches(...)` and `ClothingTrinket.register(...)`.
4. Register a renderer mapping in `ClothingTrinketRenderer.register()`.
5. Add lang, item model, recipe, dyeable tag entry, and a warmth tag entry.
6. Draw the entity texture at `textures/entity/clothing/<name>.png` (64x64, player UV layout).

## Required assets to draw

| Path | Size | Notes |
| --- | --- | --- |
| `assets/mcclothing/textures/item/<id>.png`           | 16x16 | inventory icon base (greyscale) |
| `assets/mcclothing/textures/item/<id>_overlay.png`   | 16x16 | overlay tinted from `DyedColorComponent` |
| `assets/mcclothing/textures/entity/clothing/<id>.png`| 64x64 | matches the player UV layout |
| `assets/mcclothing/textures/entity/clothing/pattern/<id>.png` | 64x64 | white pixels = pattern, transparent elsewhere |
| `assets/mcclothing/textures/item/empty_{shirt,apron,pants}_slot.png` | 16x16 | empty trinket slot icon |

## License

TBD. Source provided as-is for the user to build on.
