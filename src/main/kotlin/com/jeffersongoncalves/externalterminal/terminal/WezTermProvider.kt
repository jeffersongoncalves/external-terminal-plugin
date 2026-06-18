package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem

/**
 * WezTerm (https://wezterm.org). Cross-platform.
 *
 * - reuseTab = true  -> `wezterm cli spawn --cwd <dir>` : new tab in the running GUI
 *   (requires an existing WezTerm window / mux server).
 * - reuseTab = false -> `wezterm start --cwd <dir>`     : new window.
 */
class WezTermProvider : TerminalProvider {
    override val id = "wezterm"
    override val displayName = "WezTerm"

    override fun launchSpec(
        os: OperatingSystem,
        executablePath: String?,
        workingDir: String,
        reuseTab: Boolean,
    ): LaunchSpec? {
        if (os == OperatingSystem.UNKNOWN) return null
        val args = if (reuseTab) {
            listOf("cli", "spawn", "--cwd", workingDir)
        } else {
            listOf("start", "--cwd", workingDir)
        }
        return LaunchSpec(executable = executablePath ?: defaultExecutable(os), args = args)
    }

    private fun defaultExecutable(os: OperatingSystem): String =
        if (os == OperatingSystem.WINDOWS) "wezterm.exe" else "wezterm"

    override fun detectionCandidates(os: OperatingSystem): List<String> = when (os) {
        OperatingSystem.WINDOWS -> listOf("wezterm.exe", "wezterm")
        OperatingSystem.MAC -> listOf("/Applications/WezTerm.app", "wezterm")
        OperatingSystem.LINUX -> listOf("wezterm")
        OperatingSystem.UNKNOWN -> emptyList()
    }
}
