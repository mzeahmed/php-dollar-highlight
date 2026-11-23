package dev.phpdollar

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color

object DollarColors {
    val DEFAULT_COLOR = Color(0xC7, 0x92, 0xEA)

    /**
     * Foreground-only default and no fallback key: the annotation layer only overrides the foreground, so the
     * variable's own style (bold, italic, background...) is kept.
     *
     * The default attributes are required: color schemes that don't define this key (any third-party theme or
     * custom scheme) fall back to them. The constructor is flagged deprecated by the platform, but it is the only
     * way to have a default that works with every scheme; the scheme XML files only add per-theme tuning.
     */
    @Suppress("DEPRECATION")
    @JvmField
    val DOLLAR_SIGN: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
        "PHP_DOLLAR_HIGHLIGHT_SIGN",
        TextAttributes(DEFAULT_COLOR, null, null, null, 0),
    )
}
