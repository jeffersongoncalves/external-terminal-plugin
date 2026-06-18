package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem

/** A resolved command ready to be spawned: an executable plus its argument list. */
data class LaunchSpec(val executable: String, val args: List<String>)

/**
 * A supported external terminal emulator.
 *
 * Implementations are pure: given an OS, an optional user-configured executable path,
 * a working directory and the reuse-tab preference, they return a [LaunchSpec] — or
 * `null` when the terminal does not run on that OS. This keeps command construction
 * fully unit-testable without spawning real processes.
 */
interface TerminalProvider {
    /** Stable id persisted in settings, e.g. "warp", "windows-terminal". */
    val id: String

    /** Human-readable name shown in the settings dropdown. */
    val displayName: String

    /**
     * Build the launch command, or return `null` if this terminal is not supported on [os].
     *
     * @param executablePath user override from settings; when null the provider uses its default.
     * @param workingDir absolute path the terminal should open in.
     * @param reuseTab when true, prefer opening a tab in an existing window over a new instance
     *                 (best-effort — not every terminal can do it).
     */
    fun launchSpec(
        os: OperatingSystem,
        executablePath: String?,
        workingDir: String,
        reuseTab: Boolean,
    ): LaunchSpec?

    /**
     * Candidate absolute paths and/or bare command names that, if any resolves to an
     * existing file or a PATH entry, mean this terminal is installed on [os].
     */
    fun detectionCandidates(os: OperatingSystem): List<String>
}
