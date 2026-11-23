package dev.phpdollar

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter
import com.jetbrains.php.lang.PhpFileType
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import javax.swing.Icon

/** Settings | Editor | Color Scheme | PHP Dollar Highlight: color picker + native "Restore defaults". */
class DollarColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName() = "PHP Dollar Highlight"
    override fun getIcon(): Icon? = null
    override fun getAttributeDescriptors() = arrayOf(AttributesDescriptor("Dollar sign (\$)", DollarColors.DOLLAR_SIGN))
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter =
        SyntaxHighlighterFactory.getSyntaxHighlighter(PhpFileType.INSTANCE, null, null) ?: PlainSyntaxHighlighter()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> =
        mapOf("d" to DollarColors.DOLLAR_SIGN)

    override fun getDemoText(): String {
        val d = "<d>$</d>"
        return """<?php
${d}user = ${d}repository->find(${d}id);
${d}total = ${d}price * ${d}quantity;
${d}this->service->execute(${d}_POST);
${d}${d}dynamic = '${'$'}100 is not a variable';
"""
    }
}
