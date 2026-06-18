package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Warp terminal (https://www.warp.dev).
 *
 * Tab-vs-instance note: on macOS/Linux Warp's `warp://` URI scheme cannot carry a working
 * directory, so to honour [workingDir] we invoke the binary with the directory as a
 * positional argument. On Windows the binary instead parses its first argument as a URI, so
 * a bare path like `C:\foo` is rejected ("unexpected scheme: c"); there we pass Warp's
 * documented `warp://action/new_tab?path=` deep link. When Warp is already running it reuses
 * the existing window as a new tab on its own — there is no explicit flag for it — so
 * [reuseTab] does not change the command.
 */
class WarpProvider : TerminalProvider {
    override val id = "warp"
    override val displayName = "Warp"

    override fun launchSpec(
        os: OperatingSystem,
        executablePath: String?,
        workingDir: String,
        reuseTab: Boolean,
    ): LaunchSpec? = when (os) {
        OperatingSystem.WINDOWS -> LaunchSpec(
            executable = executablePath ?: WINDOWS_DEFAULT,
            args = listOf(windowsLaunchUri(workingDir)),
        )

        OperatingSystem.MAC -> LaunchSpec(
            // `open -a Warp <dir>` activates Warp and opens the folder.
            executable = executablePath ?: "open",
            args = listOf("-a", "Warp", workingDir),
        )

        OperatingSystem.LINUX -> LaunchSpec(
            executable = executablePath ?: "warp-terminal",
            args = listOf("--working-directory", workingDir),
        )

        OperatingSystem.UNKNOWN -> null
    }

    override fun detectionCandidates(os: OperatingSystem): List<String> = when (os) {
        OperatingSystem.WINDOWS -> listOf(WINDOWS_DEFAULT, "warp.exe", "warp")
        OperatingSystem.MAC -> listOf("/Applications/Warp.app")
        OperatingSystem.LINUX -> listOf("warp-terminal")
        OperatingSystem.UNKNOWN -> emptyList()
    }

    /**
     * Warp on Windows parses its first argument as a URI. Use the documented deep link and
     * URL-encode the path so backslashes and spaces survive the round trip.
     */
    private fun windowsLaunchUri(workingDir: String): String {
        val encoded = URLEncoder.encode(workingDir, StandardCharsets.UTF_8.name())
        return "warp://action/new_tab?path=$encoded"
    }

    companion object {
        const val WINDOWS_DEFAULT = "C:\\Program Files\\Warp\\warp.exe"
    }
}
