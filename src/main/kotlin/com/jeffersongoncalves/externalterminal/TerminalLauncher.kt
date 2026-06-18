package com.jeffersongoncalves.externalterminal

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.jeffersongoncalves.externalterminal.settings.ExternalTerminalSettings
import com.jeffersongoncalves.externalterminal.terminal.ExecutableResolver
import com.jeffersongoncalves.externalterminal.terminal.TerminalProvider
import com.jeffersongoncalves.externalterminal.terminal.TerminalRegistry

/** Resolves the configured terminal and spawns it at the project's root directory. */
object TerminalLauncher {

    private const val NOTIFICATION_GROUP = "ExternalTerminalLauncher"
    private val LOG = logger<TerminalLauncher>()

    fun launch(project: Project) {
        val workingDir = project.basePath
        if (workingDir.isNullOrBlank()) {
            notify(project, "Sem diretório de projeto para abrir o terminal.", NotificationType.WARNING)
            return
        }

        val settings = ExternalTerminalSettings.getInstance()
        val provider = TerminalRegistry.byIdOrDefault(settings.selectedTerminalId)
        val os = OperatingSystem.current()

        val spec = provider.launchSpec(os, settings.executablePath(provider.id), workingDir, settings.reuseTab)
        if (spec == null) {
            notify(
                project,
                "${provider.displayName} não é suportado em ${os.name.lowercase()}.",
                NotificationType.WARNING,
            )
            return
        }

        if (!isAvailable(provider, settings, os)) {
            notify(
                project,
                "${provider.displayName} não foi encontrado. Defina o caminho do executável em " +
                    "Settings → Tools → External Terminal.",
                NotificationType.WARNING,
            )
            return
        }

        try {
            GeneralCommandLine(spec.executable)
                .withParameters(spec.args)
                .withWorkDirectory(workingDir)
                .createProcess()
        } catch (e: ExecutionException) {
            LOG.warn("Failed to launch ${provider.displayName}", e)
            notify(
                project,
                "Falha ao abrir ${provider.displayName}: ${e.message}",
                NotificationType.ERROR,
            )
        }
    }

    /** A user-set executable path is trusted; otherwise probe the provider's detection candidates. */
    private fun isAvailable(
        provider: TerminalProvider,
        settings: ExternalTerminalSettings,
        os: OperatingSystem,
    ): Boolean {
        settings.executablePath(provider.id)?.let { return true }
        return ExecutableResolver.isInstalled(provider.detectionCandidates(os), os)
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification("External Terminal", message, type)
            .notify(project)
    }
}
