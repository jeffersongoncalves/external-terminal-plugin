package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem

/**
 * kitty (https://sw.kovidgoyal.net/kitty). macOS and Linux only (no native Windows build).
 *
 * - reuseTab = true  -> `kitty @ launch --type=tab --cwd <dir>` : new tab via remote
 *   control (requires `allow_remote_control` enabled in kitty.conf).
 * - reuseTab = false -> `kitty --directory <dir>`               : new OS window.
 */
class KittyProvider : TerminalProvider {
    override val id = "kitty"
    override val displayName = "kitty"

    override fun launchSpec(
        os: OperatingSystem,
        executablePath: String?,
        workingDir: String,
        reuseTab: Boolean,
    ): LaunchSpec? {
        if (os != OperatingSystem.MAC && os != OperatingSystem.LINUX) return null
        val exe = executablePath ?: "kitty"
        val args = if (reuseTab) {
            listOf("@", "launch", "--type=tab", "--cwd", workingDir)
        } else {
            listOf("--directory", workingDir)
        }
        return LaunchSpec(executable = exe, args = args)
    }

    override fun detectionCandidates(os: OperatingSystem): List<String> = when (os) {
        OperatingSystem.MAC -> listOf("/Applications/kitty.app", "kitty")
        OperatingSystem.LINUX -> listOf("kitty")
        else -> emptyList()
    }
}
