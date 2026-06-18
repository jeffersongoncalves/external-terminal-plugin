package com.jeffersongoncalves.externalterminal.terminal

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
}
