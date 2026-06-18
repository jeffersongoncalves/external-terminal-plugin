package com.jeffersongoncalves.externalterminal.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.XCollection
import com.jeffersongoncalves.externalterminal.terminal.TerminalRegistry

/** A user-defined executable override for one terminal id. */
class ExecutableOverride {
    var terminalId: String = ""
    var path: String = ""
}

/** Application-level, persisted settings for the External Terminal Launcher plugin. */
@State(
    name = "ExternalTerminalSettings",
    storages = [Storage("external-terminal.xml")],
)
class ExternalTerminalSettings : PersistentStateComponent<ExternalTerminalSettings.State> {

    class State {
        var selectedTerminalId: String = TerminalRegistry.defaultProviderId
        var reuseTab: Boolean = true

        @XCollection(style = XCollection.Style.v2)
        var overrides: MutableList<ExecutableOverride> = mutableListOf()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var selectedTerminalId: String
        get() = state.selectedTerminalId
        set(value) {
            state.selectedTerminalId = value
        }

    var reuseTab: Boolean
        get() = state.reuseTab
        set(value) {
            state.reuseTab = value
        }

    /** Custom executable path for [terminalId], or null/blank when using the provider default. */
    fun executablePath(terminalId: String): String? =
        state.overrides.firstOrNull { it.terminalId == terminalId }?.path?.takeIf { it.isNotBlank() }

    fun setExecutablePath(terminalId: String, path: String?) {
        state.overrides.removeAll { it.terminalId == terminalId }
        if (!path.isNullOrBlank()) {
            state.overrides.add(ExecutableOverride().apply {
                this.terminalId = terminalId
                this.path = path.trim()
            })
        }
    }

    companion object {
        fun getInstance(): ExternalTerminalSettings =
            ApplicationManager.getApplication().getService(ExternalTerminalSettings::class.java)
    }
}
