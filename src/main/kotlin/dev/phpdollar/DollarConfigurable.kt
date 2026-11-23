package dev.phpdollar

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

/**
 * Settings | Tools | PHP Dollar Highlight: the enable switch. The color is edited in the native
 * Color Scheme page (PHP can't be extended with extra attributes via public API, so it is a sibling page).
 */
class DollarConfigurable : BoundSearchableConfigurable("PHP Dollar Highlight", "dev.phpdollar.settings") {
    override fun createPanel(): com.intellij.openapi.ui.DialogPanel {
        val settings = DollarSettings.getInstance()
        return panel {
            row {
                checkBox("Highlight the \$ sign of PHP variables").bindSelected(settings::enabled)
            }
            row {
                button("Reset custom color") {
                    settings.customColor = null
                    DollarSettings.refreshEditors()
                }
                comment("Removes the color picked with Alt+Enter and goes back to the color scheme.")
            }
            row {
                comment("Color: Settings | Editor | Color Scheme | PHP Dollar Highlight")
            }
        }
    }

    override fun apply() {
        super.apply()
        DollarSettings.refreshEditors()
    }
}
