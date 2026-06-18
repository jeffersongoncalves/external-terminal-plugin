package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem
import java.io.File

/**
 * Resolves whether a terminal's detection candidate exists on the host.
 *
 * A candidate resolves when it is either an existing absolute path (file or directory —
 * macOS `.app` bundles are directories) or a bare command found on `PATH`. The PATH lookup
 * and filesystem access are injected so the logic can be unit-tested deterministically.
 */
object ExecutableResolver {

    fun isInstalled(candidates: List<String>, os: OperatingSystem = OperatingSystem.current()): Boolean =
        candidates.any { resolve(it, os) != null }

    /** Returns the resolved absolute path for [candidate], or null if it cannot be found. */
    fun resolve(
        candidate: String,
        os: OperatingSystem = OperatingSystem.current(),
        pathDirs: List<String> = systemPathDirs(),
        exists: (String) -> Boolean = { File(it).exists() },
    ): String? {
        if (candidate.isBlank()) return null

        // Absolute / explicit path — accept as-is when it exists.
        if (looksLikePath(candidate)) {
            return if (exists(candidate)) candidate else null
        }

        // Bare command — probe each PATH dir, trying Windows executable extensions.
        val sep = if (os == OperatingSystem.WINDOWS) "\\" else "/"
        val names = if (os == OperatingSystem.WINDOWS) windowsNames(candidate) else listOf(candidate)
        for (dir in pathDirs) {
            for (name in names) {
                val full = dir.trimEnd('/', '\\') + sep + name
                if (exists(full)) return full
            }
        }
        return null
    }

    private fun looksLikePath(candidate: String): Boolean =
        candidate.contains('/') || candidate.contains('\\') || (candidate.length > 1 && candidate[1] == ':')

    private fun windowsNames(command: String): List<String> {
        val hasExt = command.substringAfterLast('.', "").isNotEmpty() && command.contains('.')
        if (hasExt) return listOf(command)
        return listOf("$command.exe", "$command.cmd", "$command.bat", command)
    }

    private fun systemPathDirs(): List<String> {
        val path = System.getenv("PATH") ?: System.getenv("Path") ?: return emptyList()
        return path.split(File.pathSeparatorChar).filter { it.isNotBlank() }
    }
}
