# Localisation

All player-visible text lives in message catalogues under `data/strings/exiledSector/`:

- `en.json` holds the English source for everything the Java code shows.
- `zh_CN.json` holds the Simplified Chinese translation.
- Skill type names and descriptions, NPC layout names, and our hull mods' names and descriptions keep their English source in `data/skilltrees/skill_types.json`, `data/config/exiledSector/npc_layouts.json` and `data/hullmods/hull_mods.csv`. Translations reference them by id: `skillType.<id>.name`, `skillType.<id>.description`, `npcLayout.<id>.name`, `hullmod.<id>.name` and `hullmod.<id>.description`.

## Choosing the language

- **LunaLib setting:** the Exiled Sector tab has a **Language** option: Auto, English or Simplified Chinese. It takes effect after a restart.
- **Auto:** follows Starsector's `localeOverride` from `settings.json`.
- **Fallback:** each key falls back to its English text when a translation is missing.

## Where text is drawn

- **Our own UI (the skill tree screen):** this is drawn by the mod with LazyLib, using Orbitron, or the font a language names with `meta.font`. It can always show the chosen language.
- **Game-drawn text (LunaLib settings, dialogs, the codex, combat floaters and hull mod text):** this uses the game's fonts. It switches to Chinese only when the game can display Chinese characters, meaning its locale is Chinese or `cjkMode` is on. Otherwise it stays in English.

## Writing strings

- **Placeholders:** `{name}` placeholders are filled in by the code. Values are inserted as-is.
- **Highlights:** colour comes from tags around words.
  - `<good>…</good>` and `<bad>…</bad>` mark improvements and drawbacks. They swap automatically for stats where lower is better.
  - `<hl>…</hl>` is a neutral highlight.
  - `<hullmod>…</hullmod>` and `<node>…</node>` use the hull mod and node colours.
  - Tags can't be nested.
- **Plurals:** a key can have `.one` and `.other` forms; the code picks one from the count. Languages without plurals, such as Chinese, only need `.other`.
- **Lists:** the separator is `format.list.sep`.
- **Sentence joining:** appended sentences are joined with `format.sentences`.
- **LunaLib text:** `settings.*` strings must not contain `%`. LunaLib passes them through `String.format`.
- **Reserved character:** never use `#`. Starsector's JSON loader treats it as a comment.

`CatalogueLintTest` checks every catalogue:
- placeholders and tags must match English;
- tags must be balanced;
- every key must match an English key or a data id, except `meta.*` settings, which have no English counterpart.

It also writes `target/i18n/missing-<locale>.txt`, which lists untranslated strings; `release.ps1` warns when that file isn't empty.

## Adding a language from another mod

A translation mod can ship `data/strings/exiledSector/<locale>.json`, for example `ru.json` or `ja.json`. Starsector merges it into our catalogue. It can also override individual keys of an existing language.

- **Fonts:** if the language needs characters Orbitron lacks, set `meta.font` to a single-page BMFont atlas and ship it too. LazyLib can't read multi-page fonts. Set `meta.fakeBold` to `false` if the double-drawn bold smears the glyphs.
- **Generating an atlas:** use the test-scope `BitmapFontGenerator`:

```
java -cp target/test-classes;target/classes exiledsector.i18n.BitmapFontGenerator <font.ttf|.otf> graphics/fonts/exiledSector/<name>.fnt 17 20 2048 1024 data/strings/exiledSector/en.json data/strings/exiledSector/<locale>.json
```

Arguments:
- the source font;
- the output `.fnt` path;
- the glyph size in pixels;
- the line height LazyLib scales against: keep it at 20, the body text size, so body text draws at the atlas's own size instead of being scaled down;
- the atlas size: use the smallest power-of-two page the glyphs fit, since every pixel costs 4 bytes of video memory;
- the text files whose characters must be included.

On top of those, the generator always includes ASCII, Latin-1, common punctuation, CJK symbols, full-width forms and the 3,755 common GB2312 hanzi. It fails instead of dropping glyphs that don't fit.

## Testing for missed strings

Set `localeOverride` to `en_XA` to switch on the pseudo-locale. Every catalogue string then shows as `[text~]`, so any hard-coded English stands out. `PseudoLocaleLeakTest` and `ExiledSectorSettingsTest` run the same check automatically over every description and setting.
