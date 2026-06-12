# TODO — next steps for MC Clothing

Roughly ordered by what unblocks visible progress first. Items marked
**[blocker]** must be addressed for the mod to look/feel right; **[polish]**
items are quality-of-life or aesthetic improvements that can wait.

## P0 — make it look like a mod

- **[blocker] Draw all textures.** The mod compiles and runs but renders
  missing-texture purple until the PNGs listed in `README.md` are drawn.
  Priorities: inventory icons (so the creative tab is usable), then entity
  textures (so the player actually shows clothing), then pattern overlays
  (so the loom does something visible), then slot icons.
- **[blocker] Generate the Gradle wrapper.** `gradle wrapper` once, then
  commit `gradle/wrapper/gradle-wrapper.jar`, `.properties`, `gradlew`,
  and `gradlew.bat` so contributors don't need a system Gradle.
- **[blocker] Add a `LICENSE` file** or remove the `from('LICENSE')` line
  in `build.gradle`. Currently the jar task will fail.
- **[polish] Draw a custom Tailor's Loom GUI background** at
  `assets/mcclothing/textures/gui/container/tailors_loom.png` and swap
  the `BG` constant in `TailorsLoomScreen`. Vanilla loom reuse works
  but reads as "loom".

## P1 — gameplay holes

- **Inventory drop on death.** Trinkets respects vanilla keep-inventory,
  but a config to keep clothing on death (separate from armor) is a
  common ask for "cosmetic" clothing mods.
- **Equip/unequip sounds.** Trinkets supports `Trinket.canUnequip` and
  per-item sound predicates — wire cloth rustle sounds for SFX polish.
- **Wash-out interaction.** Vanilla leather armor can be washed clean in
  a cauldron. Add the same handler for clothing so the dye reset path
  matches player intuition. (Vanilla cauldron `CauldronBehavior` map +
  `LEATHER_ARMOR_BEHAVIOR` is the reference.)
- **Pattern removal at the loom.** Currently each loom click *appends*
  a pattern. Add a way to clear the top entry (e.g., shears in the dye
  slot pops the last pattern).
- **Banner-pattern items?** Vanilla banner patterns ship as standalone
  items (Globe Banner Pattern, etc.). Decide if clothing patterns should
  be gated behind item unlocks the same way, or freely available like
  the current scaffold.

## P2 — model & rendering quality

- **Per-shape bespoke geometry.** Today all shapes share one
  `ClothingModel` and just toggle parts. For better silhouette:
  - Overalls: add a small chest-bib panel between the straps; widen the
    leg cuffs slightly so they read as denim, not skin.
  - Apron: the apron strap (neck loop) currently doesn't exist — add it
    as a thin part anchored to the body, not the head.
  - Vest: open V-neck at the front (subtract a notch from the body
    cuboid via a second smaller cuboid).
  - Pants vs shorts: shorts use a shorter leg cuboid (height 6 instead
    of 12).
- **Slim-arm support.** `PlayerEntityModel` has a thinArms flag for
  Alex-model skins; the clothing model currently uses fixed 4-wide
  sleeves. Detect `PlayerEntityRenderer` arm width and render a
  3-wide variant.
- **Cape/elytra slot conflict.** Verify that overalls + an elytra
  render together cleanly (z-fighting check, animation conflicts).
- **First-person sleeve render.** When the player has a t-shirt or vest
  equipped, their first-person hand should show the sleeve. Vanilla
  does this for armor via the `HumanoidArmor` first-person hook —
  Trinkets has `getRenderedArm`/`HandRenderHelper` for this.
- **Item model 3D variant.** Items currently render flat (2D) in-hand.
  Add `display.thirdperson_righthand` transforms so a held t-shirt
  rotates sensibly when carried.

## P3 — patterns

- **Pattern previews in the loom GUI.** Replace the letter-glyph buttons
  in `TailorsLoomScreen` with mini renders of the pattern atop the
  current clothing's base color (sample a small offscreen framebuffer or
  use sprite atlases keyed by `pattern.assetId()`).
- **More built-in patterns.** Stars, lace trim, work-shirt rivets,
  flannel, gingham, polka dots, racing stripes, pinstripes. Cheap to add
  — one PNG + one registry key + one lang entry per pattern.
- **Datapack-driven patterns.** Convert `ClothingPatterns` to a JSON
  registry loaded from `data/<namespace>/clothing_pattern/*.json` so
  pack authors can add patterns without code.
- **Pattern unlocks.** Loot-table integration: gate certain patterns
  behind exploration (find an "Embroidery Pattern" item in a village).
- **Higher pattern limit option.** Currently capped at 8 layers in
  `ClothingPatternsComponent.MAX_PATTERNS` to bound renderer cost. Make
  this configurable.

## P4 — warmth + cross-mod compat

- **Temperature mod integration sample.** Ship a small reference impl
  for Overly Realistic in its own optional subproject — guarded by
  `FabricLoader.getInstance().isModLoaded("overlyrealistic")`. Easier to
  copy than to read tags + figure out numbers.
- **Per-slot warmth resolution.** A player wearing overalls + vest +
  pants today sums all three warmth tiers — confirm with the OR team
  whether that's desired (real overalls are warmer *because* they
  include the bottom layer, but the player is also wearing pants).
  Consider exposing a "covers chest/legs" flag separately from warmth.
- **Wetness handling.** Clothing should arguably get heavier / colder
  when the player is in rain or water. Hookable via Trinket tick.

## P5 — infrastructure

- **Datagen.** Convert hand-written recipe/tag/lang JSON to Fabric
  datagen so we get free in-IDE validation and the JSON stays in sync
  with the registry.
- **Tests.** Fabric supports gametests — add a smoke test that crafts
  each clothing piece, dyes it, applies a pattern at the loom, equips
  it, and confirms `ClothingPatternsComponent` reflects the chain.
- **Localization.** Pull translations for at least DE / FR / ES / JA /
  ZH-CN — clothing terms are common.
- **CI.** GitHub Actions: `./gradlew build` on push, attach jar
  artifact on tag.
- **Mixin housekeeping.** `mcclothing.mixins.json` currently lists zero
  mixins. We may need one if we want to intercept `ScreenHandlerType`
  registration timing, or if a Loom-screen rework needs vanilla loom
  modifications. Delete the mixin config if it stays unused.

## Known smaller issues to fix

- `ModItems.register()` adds the Tailor's Loom block to
  `ItemGroups.TOOLS`, which is fine but a bit arbitrary — consider a
  dedicated `mcclothing` creative tab to gather all items + the loom
  cleanly.
- `ClothingItem.inventoryTick` is an explicit no-op — remove the
  override; default behavior is identical.
- `ClothingTrinketRenderer` uses `EnumMap<ClothingShape, ClothingModel>`
  which is fine, but the lazy init runs on the render thread. If the
  `EntityModelLoader` isn't ready on first tick (rare), we'd NPE.
  Pre-warm models on world join.
