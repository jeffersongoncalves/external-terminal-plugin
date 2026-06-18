package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem

/** Central catalogue of supported terminals. Order here is the order shown in settings. */
object TerminalRegistry {
    val providers: List<TerminalProvider> = listOf(
        WarpProvider(),
        WindowsTerminalProvider(),
        WezTermProvider(),
        AlacrittyProvider(),
        KittyProvider(),
    )

    val defaultProviderId: String = WarpProvider().id

    fun byId(id: String?): TerminalProvider? = providers.firstOrNull { it.id == id }

    fun byIdOrDefault(id: String?): TerminalProvider =
        byId(id) ?: byId(defaultProviderId) ?: providers.first()

    /**
     * Providers whose binary is detected on [os] via [ExecutableResolver]. Registry order is
     * preserved. Terminals unsupported on the OS report no detection candidates, so they are
     * naturally excluded.
     */
    fun installedProviders(os: OperatingSystem = OperatingSystem.current()): List<TerminalProvider> =
        providers.filter { ExecutableResolver.isInstalled(it.detectionCandidates(os), os) }
}
