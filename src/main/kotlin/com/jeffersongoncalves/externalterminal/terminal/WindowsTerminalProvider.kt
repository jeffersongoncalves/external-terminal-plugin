package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem

/**
 * Windows Terminal (`wt.exe`). Windows-only.
 *
 * - reuseTab = true  -> `wt -w 0 nt -d <dir>` : new tab in the most-recent window (id 0).
 * - reuseTab = false -> `wt -d <dir>`         : brand-new window.
 */
class WindowsTerminalProvider : TerminalProvider {
    override val id = "windows-terminal"
    override val displayName = "Windows Terminal"

    override fun launchSpec(
        os: OperatingSystem,
        executablePath: String?,
        workingDir: String,
        reuseTab: Boolean,
    ): LaunchSpec? {
        if (os != OperatingSystem.WINDOWS) return null
        val args = if (reuseTab) {
            listOf("-w", "0", "nt", "-d", workingDir)
        } else {
            listOf("-d", workingDir)
        }
        return LaunchSpec(executable = executablePath ?: "wt.exe", args = args)
    }

    override fun detectionCandidates(os: OperatingSystem): List<String> =
        if (os == OperatingSystem.WINDOWS) listOf("wt.exe", "wt") else emptyList()
}
