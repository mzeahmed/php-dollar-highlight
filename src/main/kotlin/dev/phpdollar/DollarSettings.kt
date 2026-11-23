package dev.phpdollar

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.ProjectManager
import java.awt.Color

/**
 * Persistent settings: on/off switch and an optional color override.
 * Without override, the color comes from the color scheme (see [DollarColors]).
 */
@Service(Service.Level.APP)
@State(name = "PhpDollarHighlightSettings", storages = [Storage("phpDollarHighlight.xml")])
class DollarSettings : PersistentStateComponent<DollarSettings.State> {
    class State {
        var enabled: Boolean = true
        /** RGB of the override, or -1 to use the color scheme. */
        var customRgb: Int = -1
    }

    private var state = State()
    private var cached: TextAttributes? = null

    var enabled: Boolean
        get() = state.enabled
        set(value) { state.enabled = value }

    var customColor: Color?
        get() = if (state.customRgb < 0) null else Color(state.customRgb)
        set(value) {
            state.customRgb = value?.rgb?.and(0xFFFFFF) ?: -1
            cached = null
        }

    /** Foreground-only attributes for the override (cached: no allocation per annotation), or null. */
    fun customAttributes(): TextAttributes? {
        cached?.let { return it }
        val c = customColor ?: return null
        return TextAttributes(c, null, null, null, 0).also { cached = it }
    }

    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state; cached = null }

    companion object {
        fun getInstance(): DollarSettings = service()

        /** Re-run highlighting in every open project: no restart needed. */
        fun refreshEditors() {
            for (project in ProjectManager.getInstance().openProjects) {
                DaemonCodeAnalyzer.getInstance(project).restart(this)
            }
        }
    }
}
