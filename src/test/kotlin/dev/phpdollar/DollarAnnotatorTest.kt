package dev.phpdollar

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DollarAnnotatorTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            DollarSettings.getInstance().enabled = true
            DollarSettings.getInstance().customColor = null
        } finally {
            super.tearDown()
        }
    }

    /** Offsets of every `$` highlighted by the plugin; asserts each range is exactly one `$`. */
    private fun colored(code: String): List<Int> {
        myFixture.configureByText("a.php", code)
        val infos = myFixture.doHighlighting().filter { it.forcedTextAttributesKey == DollarColors.DOLLAR_SIGN }
        for (i in infos) {
            assertEquals("range must be exactly one char", 1, i.endOffset - i.startOffset)
            assertEquals('$', code[i.startOffset])
        }
        return infos.map { it.startOffset }.sorted()
    }

    private fun dollars(code: String, vararg needle: String): List<Int> =
        needle.flatMap { n -> Regex(Regex.escape(n)).findAll(code).map { it.range.first }.toList() }.sorted()

    fun testLocalVariables() {
        val code = "<?php\n\$user = \$repository->find(\$id);\n\$total = \$price * \$quantity;\n"
        assertEquals(Regex("\\$").findAll(code).map { it.range.first }.toList(), colored(code))
    }

    fun testFunctionParameters() {
        val code = "<?php\nfunction f(int \$a, string \$b = 'x') { return \$a . \$b; }\n"
        assertEquals(Regex("\\$").findAll(code).map { it.range.first }.toList(), colored(code))
    }

    fun testPropertiesAccess() {
        val code = "<?php\nclass A { public \$p; function m() { return \$this->p . \$other->q; } }\n"
        // `$p` declaration, `$this`, `$other`; `->p` has no $
        assertEquals(Regex("\\$").findAll(code).map { it.range.first }.toList(), colored(code))
    }

    fun testSuperglobalsAndThis() {
        val code = "<?php\n\$a = \$_POST['x'] . \$_GET['y'] . \$_SERVER['z'] . \$this;\n"
        assertEquals(Regex("\\$").findAll(code).map { it.range.first }.toList(), colored(code))
    }

    fun testDynamicVariables() {
        val code = "<?php\n\$variable = 'a'; \$\$variable = 1; echo \${\$variable};\n"
        val result = colored(code)
        // Every `$` here is real variable syntax (including both `$` of `$$variable` and the one of `${`).
        assertEquals(Regex("\\$").findAll(code).map { it.range.first }.toList(), result)
    }

    fun testStringsAndRegexNotColored() {
        val code = "<?php\n\$text = 'Le prix est de \$100';\n\$pattern = '/\\\$[0-9]+/';\n\$d = \"cost \\\$5 or 5\$\";\n"
        val result = colored(code)
        // Only the three variable declarations on column 0
        val expected = Regex("(?m)^\\$").findAll(code).map { it.range.first }.toList()
        assertEquals(expected, result)
    }

    fun testCommentsNotColored() {
        val code = "<?php\n// \$commented\n/* \$block */\n/** @param \$x doc */\n# \$hash\n\$real = 1;\n"
        assertEquals(listOf(code.indexOf("\$real")), colored(code))
    }

    fun testInterpolatedStrings() {
        val code = "<?php\n\$a = \"hello \$name and {\$obj->p} and \${other}\";\n"
        val result = colored(code)
        assertTrue(result.contains(code.indexOf("\$a")))
        assertTrue(result.contains(code.indexOf("\$name")))
        assertTrue(result.contains(code.indexOf("\$obj")))
    }

    fun testDisabled() {
        DollarSettings.getInstance().enabled = false
        assertEquals(emptyList<Int>(), colored("<?php\n\$a = \$b;\n"))
    }

    fun testReEnabled() {
        DollarSettings.getInstance().enabled = false
        colored("<?php\n\$a = 1;\n")
        DollarSettings.getInstance().enabled = true
        assertEquals(1, colored("<?php\n\$a = 1;\n").size)
    }

    fun testColorIsForegroundOnlyAndCustomizable() {
        val scheme = com.intellij.openapi.editor.colors.EditorColorsManager.getInstance().globalScheme
        val default = scheme.getAttributes(DollarColors.DOLLAR_SIGN)
        assertEquals(DollarColors.DEFAULT_COLOR, default.foregroundColor)
        assertNull(default.backgroundColor)
        assertEquals(0, default.fontType)
        val custom = default.clone().apply { foregroundColor = java.awt.Color.RED }
        scheme.setAttributes(DollarColors.DOLLAR_SIGN, custom)
        try {
            assertEquals(java.awt.Color.RED, scheme.getAttributes(DollarColors.DOLLAR_SIGN).foregroundColor)
            assertEquals(1, colored("<?php\n\$a = 1;\n").size)
        } finally {
            scheme.setAttributes(DollarColors.DOLLAR_SIGN, default)
        }
    }

    fun testVariableNameUntouched() {
        val code = "<?php\n\$user = 1;\n"
        val at = colored(code).single()
        // The name part [at+1, end of "user") must not carry the plugin key.
        val nameInfos = myFixture.doHighlighting().filter {
            it.forcedTextAttributesKey == DollarColors.DOLLAR_SIGN && it.endOffset > at + 1
        }
        assertTrue(nameInfos.isEmpty())
        assertEquals(code, myFixture.editor.document.text)
    }

    fun testCustomColorOverridesScheme() {
        DollarSettings.getInstance().customColor = java.awt.Color(0x11, 0x22, 0x33)
        val code = "<?php\n\$a = \$b;\n"
        myFixture.configureByText("a.php", code)
        val infos = myFixture.doHighlighting().filter { it.forcedTextAttributes?.foregroundColor == java.awt.Color(0x11, 0x22, 0x33) }
        assertEquals(2, infos.size)
        infos.forEach { assertEquals(1, it.endOffset - it.startOffset) }
        // Foreground only: nothing else is overridden.
        infos.forEach { assertNull(it.forcedTextAttributes.backgroundColor); assertEquals(0, it.forcedTextAttributes.fontType) }
    }

    fun testResetCustomColor() {
        DollarSettings.getInstance().customColor = java.awt.Color.RED
        DollarSettings.getInstance().customColor = null
        assertEquals(1, colored("<?php\n\$a = 1;\n").size)
    }

    fun testIntentionAvailableOnlyOnDollar() {
        myFixture.configureByText("a.php", "<?php\n<caret>\$user = 1;\n")
        assertTrue(myFixture.filterAvailableIntentions("Change the color of the \$ sign…").isNotEmpty())
        myFixture.configureByText("b.php", "<?php\n\$u<caret>ser = 1;\n")
        assertTrue(myFixture.filterAvailableIntentions("Change the color of the \$ sign…").isEmpty())
        myFixture.configureByText("c.php", "<?php\n\$a = '<caret>\$100';\n")
        assertTrue(myFixture.filterAvailableIntentions("Change the color of the \$ sign…").isEmpty())
    }

    /** Third-party / custom schemes don't define our key: the key's own default must still give the color. */
    fun testDefaultColorWorksWithSchemeThatLacksTheKey() {
        assertEquals(DollarColors.DEFAULT_COLOR, DollarColors.DOLLAR_SIGN.defaultAttributes.foregroundColor)
        val scheme = com.intellij.openapi.editor.colors.EditorColorsManager.getInstance().globalScheme.clone()
            as com.intellij.openapi.editor.colors.EditorColorsScheme
        // Empty attributes in the scheme = what a scheme without the key yields once resolved.
        assertEquals(DollarColors.DEFAULT_COLOR, DollarColors.DOLLAR_SIGN.defaultAttributes.foregroundColor)
        assertNotNull(scheme)
    }
}
