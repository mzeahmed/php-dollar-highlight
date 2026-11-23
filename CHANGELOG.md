# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-11-23

### Added
- GitHub Actions workflow publishing a GitHub Release with the installable ZIP when a `v*` tag is pushed.
- Colors only the `$` sign of PHP variables (default `#C792EA`), leaving the variable name and the rest of the theme untouched.
- Support for `$name`, `$this`, superglobals, function parameters, properties, dynamic variables (`$$x`, `${$x}`) and
  variables inside interpolated strings. `$` in comments and string literals is never colored.
- Color customization in `Settings → Editor → Color Scheme → PHP Dollar Highlight`, with a built-in preview.
- `Alt+Enter` action on a `$` ("Change the color of the $ sign…") to pick the color directly from the editor.
- `Settings → Tools → PHP Dollar Highlight` page to enable/disable the highlighting and reset the custom color.
- Default color that works with any color scheme, including third-party themes.
- Automated tests, Makefile and build setup targeting PhpStorm 2026.2 (build 262) and later.

[Unreleased]: https://github.com/mzeahmed/php-dollar-highlight/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/mzeahmed/php-dollar-highlight/releases/tag/v0.1.0
