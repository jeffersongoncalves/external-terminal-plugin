package com.jeffersongoncalves.externalterminal.actions

import com.jeffersongoncalves.externalterminal.TerminalLauncher
import com.jeffersongoncalves.externalterminal.icons.ExternalTerminalIcons
import com.jeffersongoncalves.externalterminal.settings.ExternalTerminalSettings
import com.jeffersongoncalves.externalterminal.terminal.TerminalRegistry
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

/** Top-toolbar / Tools-menu action that opens the configured external terminal at the project root. */
class OpenExternalTerminalAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null
        e.presentation.isVisible = true

        val provider = TerminalRegistry.byIdOrDefault(ExternalTerminalSettings.getInstance().selectedTerminalId)
        e.presentation.icon = ExternalTerminalIcons.Terminal
        e.presentation.text = "Open ${provider.displayName}"
        e.presentation.description = "Open ${provider.displayName} at the project root"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        TerminalLauncher.launch(project)
    }
}
