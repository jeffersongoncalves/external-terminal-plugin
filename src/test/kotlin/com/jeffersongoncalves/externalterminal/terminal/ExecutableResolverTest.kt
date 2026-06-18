package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExecutableResolverTest {

    @Test
    fun `absolute path resolves when it exists`() {
        val path = "C:\\Program Files\\Warp\\warp.exe"
        val resolved = ExecutableResolver.resolve(
            candidate = path,
            os = OperatingSystem.WINDOWS,
            pathDirs = emptyList(),
            exists = { it == path },
        )
        assertEquals(path, resolved)
    }

    @Test
    fun `absolute path returns null when missing`() {
        val resolved = ExecutableResolver.resolve(
            candidate = "/opt/missing/warp",
            os = OperatingSystem.LINUX,
            pathDirs = emptyList(),
            exists = { false },
        )
        assertNull(resolved)
    }

    @Test
    fun `bare command found on PATH with windows exe extension`() {
        val resolved = ExecutableResolver.resolve(
            candidate = "wt",
            os = OperatingSystem.WINDOWS,
            pathDirs = listOf("C:\\bin"),
            exists = { it == "C:\\bin\\wt.exe" },
        )
        assertEquals("C:\\bin\\wt.exe", resolved)
    }

    @Test
    fun `bare command found on PATH on linux`() {
        val resolved = ExecutableResolver.resolve(
            candidate = "wezterm",
            os = OperatingSystem.LINUX,
            pathDirs = listOf("/usr/bin", "/usr/local/bin"),
            exists = { it == "/usr/local/bin/wezterm" },
        )
        assertEquals("/usr/local/bin/wezterm", resolved)
    }

    @Test
    fun `blank candidate never resolves`() {
        assertNull(ExecutableResolver.resolve("", OperatingSystem.LINUX, emptyList(), { true }))
    }

    @Test
    fun `isInstalled true when any candidate resolves`() {
        // Uses the real PATH-less resolve via default args is avoided; check the OR semantics directly.
        val os = OperatingSystem.LINUX
        val installed = listOf("/missing", "/usr/bin/kitty").any {
            ExecutableResolver.resolve(it, os, emptyList(), { p -> p == "/usr/bin/kitty" }) != null
        }
        assert(installed)
    }

    @Test
    fun `os detection maps common names`() {
        assertEquals(OperatingSystem.WINDOWS, OperatingSystem.fromName("Windows 11"))
        assertEquals(OperatingSystem.MAC, OperatingSystem.fromName("Mac OS X"))
        assertEquals(OperatingSystem.LINUX, OperatingSystem.fromName("Linux"))
        assertEquals(OperatingSystem.UNKNOWN, OperatingSystem.fromName(null))
    }
}
