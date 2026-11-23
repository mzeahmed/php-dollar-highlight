# PHP Dollar Highlight

A PhpStorm plugin that colors **only the `$` sign** of PHP variables, independently from the variable name.
Files are never modified: only their display changes, and the rest of your theme's highlighting is kept.

```php
$user = $repository->find($id);     // only the "$" signs are colored (default #C792EA)
$this->service->execute($_POST);
$$dynamic = ${$other};              // both "$" of $$x and the "$" of ${ are colored
$text = 'The price is $100';        // string literal: not colored
// $comment                         // comment: not colored
```

## How it works

In PhpStorm's PHP lexer, `$name` is **a single token**, `PhpTokenTypes.VARIABLE`; `$$x` and `${` produce
`DOLLAR` / `DOLLAR_LBRACE` tokens under a `Variable`. The plugin therefore uses a silent `Annotator`
(`newSilentAnnotation(INFORMATION)`) that applies a `TextAttributesKey` to a **one-character `TextRange`**.

- Works per token (PSI leaf): O(1) per element, never walks the document, incremental by nature.
- The attribute is foreground-only with no fallback key: the theme's bold/italic/background are left intact.
- `$` inside comments and string literals are never `VARIABLE` tokens, so they are never colored.
- Only public IntelliJ Platform APIs are used. The single exception is the `TextAttributesKey` constructor with
  default attributes, which the platform flags as deprecated but which is the only way to get a default color that
  works with every color scheme (third-party themes don't define this plugin's key).

Why not a `SyntaxHighlighter`: PHP's one is provided by PhpStorm and cannot be extended to split a token.

## Supported versions

PhpStorm **2026.2 (build 262)** and later (`sinceBuild = 262`, no `untilBuild`).
Verified with build **PS-262.12573.41** only. Earlier versions are not supported (the platform requires JBR 25).

## Requirements

- JDK 25 (PhpStorm 2026.2 ships with JBR 25).
- Gradle 9+ (the `./gradlew` wrapper provides Gradle 9.9.2).

## Build

```bash
# With an installed PhpStorm (no SDK download):
./gradlew buildPlugin -PlocalIde=/path/to/PhpStorm
# Or let Gradle download PhpStorm:
./gradlew buildPlugin

./gradlew test             # automated tests
./gradlew verifyPlugin     # JetBrains Plugin Verifier
```

A `Makefile` wraps these commands (`make help`).

The ZIP is written to `build/distributions/php-dollar-highlight-<version>.zip`.
If your `~/.gradle/gradle.properties` forces another JDK, add `-Dorg.gradle.java.home=/usr/lib/jvm/java-25-openjdk-amd64`.

## Release (GitHub Actions)

Pushing a `vX.Y.Z` tag triggers `.github/workflows/release.yml`: tests, build, then a GitHub Release is created with
the installable ZIP (`php-dollar-highlight-X.Y.Z.zip`) attached. The plugin version is taken from the tag.

```bash
git tag v0.1.0 && git push origin v0.1.0
```

## Installation

Download the ZIP (from a GitHub Release or from your own build), then in PhpStorm:
`Settings → Plugins → ⚙ → Install Plugin from Disk…` → select the ZIP → restart if the IDE asks
(the Plugin Verifier reports that the plugin can be loaded without a restart).

## Configuration

- **Color from the editor**: put the caret right before a `$`, press `Alt+Enter` →
  *Change the color of the $ sign…* → pick a color. It applies immediately and is saved. This color **takes
  precedence** over the color scheme; `Settings → Tools → PHP Dollar Highlight → Reset custom color` goes back to the
  scheme. (Built-in schemes such as Darcula are read-only and the public API doesn't allow editing them cleanly, hence
  this plugin-level setting.)
- **Color from the color scheme**: `Settings → Editor → Color Scheme → PHP Dollar Highlight` (with a built-in preview).
  The scheme's "Restore defaults" brings back `#C792EA`. Defaults work with any scheme and apply without a restart.
- **Enable/disable**: `Settings → Tools → PHP Dollar Highlight` (persistent, applied without a restart).

Why two pages: the `PHP` page of the Color Scheme settings belongs to PhpStorm and cannot be extended through the
public API, so a sibling page is used for the color and a Tools page for the on/off switch (a `ColorSettingsPage`
has no checkbox).

## Uninstallation

`Settings → Plugins → Installed → PHP Dollar Highlight → Uninstall`.

## Tests

`DollarAnnotatorTest` (16 tests, run against the real PHP highlighting engine): local variables, parameters,
properties, superglobals, `$this`, dynamic variables, strings, regexes, comments, interpolated strings,
enable/disable, custom color, variable name left untouched, intention availability. Each test checks that the
highlighted `TextRange` is exactly one `$` character.

## Known limitations

- In interpolated strings, `"$name"`, `"{$obj->p}"` and `"${name}"` are colored (they are real variables);
  an escaped `\$` is not.
- `$` in PHPDoc comments is not colored (tested). Heredoc/nowdoc behavior is not covered by the tests.
- The rendering was checked manually in PhpStorm 2026.2 only; other IDEs and versions are untested.
