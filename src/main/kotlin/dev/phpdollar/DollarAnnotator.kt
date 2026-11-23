package dev.phpdollar

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.Variable

/**
 * Colors the `$` of PHP variables only.
 *
 * Works on leaf tokens, so it is O(1) per element and never walks the document:
 *  - `VARIABLE` ("$name", also inside interpolated strings): first char.
 *  - `DOLLAR` / `DOLLAR_LBRACE` ("$" of `$$x`, "${" of `${$x}`): first char, only if the parent is a [Variable].
 * `$` inside plain strings and comments never reaches these token types.
 */
class DollarAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LeafPsiElement) return
        val type = element.node.elementType
        val isDollar = when (type) {
            PhpTokenTypes.VARIABLE -> true
            PhpTokenTypes.DOLLAR, PhpTokenTypes.DOLLAR_LBRACE -> element.parent is Variable
            else -> false
        }
        if (!isDollar || !DollarSettings.getInstance().enabled) return
        if (element.textLength == 0) return
        val start = element.textRange.startOffset
        if (element.text[0] != '$') return
        val builder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(start, start + 1))
        val custom = DollarSettings.getInstance().customAttributes()
        if (custom != null) builder.enforcedTextAttributes(custom) else builder.textAttributes(DollarColors.DOLLAR_SIGN)
        builder.create()
    }
}
