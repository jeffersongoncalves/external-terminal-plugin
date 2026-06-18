package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem

/**
 * Alacritty (https://alacritty.org). Cross-platform.
 *
 * Alacritty has no tabs, so [reuseTab] is ignored — every launch is a new window
 * opened at the working directory via `alacritty --working-directory <dir>`.
 */
class AlacrittyProvider : TerminalProvider {
    override val id = "alacritty"
    override val displayName = "Alacritty"

    override fun launchSpec(
        os: OperatingSystem,
        executablePath: String?,
        workingDir: String,
        reuseTab: Boolean,
    ): LaunchSpec? {
        if (os == OperatingSystem.UNKNOWN) return null
        return LaunchSpec(
            executable = executablePath ?: if (os == OperatingSystem.WINDOWS) "alacritty.exe" else "alacritty",
            args = listOf("--working-directory", workingDir),
        )
    }

    override fun detectionCandidates(os: OperatingSystem): List<String> = when (os) {
        OperatingSystem.WINDOWS -> listOf("alacritty.exe", "alacritty")
        OperatingSystem.MAC -> listOf("/Applications/Alacritty.app", "alacritty")
        OperatingSystem.LINUX -> listOf("alacritty")
        OperatingSystem.UNKNOWN -> emptyList()
    }
}
