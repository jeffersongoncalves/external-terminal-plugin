package com.jeffersongoncalves.externalterminal.terminal

import com.jeffersongoncalves.externalterminal.OperatingSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalProviderTest {

    private val dir = "/home/user/project"

    @Test
    fun `warp on windows passes dir as encoded deep link`() {
        val winDir = "C:\\Users\\me\\my project"
        val spec = WarpProvider().launchSpec(OperatingSystem.WINDOWS, null, winDir, reuseTab = true)!!
        assertEquals(WarpProvider.WINDOWS_DEFAULT, spec.executable)
        assertEquals(
            listOf("warp://action/new_tab?path=C%3A%5CUsers%5Cme%5Cmy+project"),
            spec.args,
        )
    }

    @Test
    fun `warp windows detection includes per-user LOCALAPPDATA install`() {
        val candidates = WarpProvider().detectionCandidates(OperatingSystem.WINDOWS)
        assertTrue(candidates.contains(WarpProvider.WINDOWS_DEFAULT))
        val localAppData = System.getenv("LOCALAPPDATA")
        if (!localAppData.isNullOrBlank()) {
            assertTrue(candidates.any { it.endsWith("\\Programs\\Warp\\warp.exe") })
        }
    }

    @Test
    fun `warp honours custom executable path`() {
        val custom = "D:\\tools\\warp.exe"
        val spec = WarpProvider().launchSpec(OperatingSystem.WINDOWS, custom, dir, reuseTab = false)!!
        assertEquals(custom, spec.executable)
    }

    @Test
    fun `warp on mac uses open -a`() {
        val spec = WarpProvider().launchSpec(OperatingSystem.MAC, null, dir, reuseTab = true)!!
        assertEquals("open", spec.executable)
        assertEquals(listOf("-a", "Warp", dir), spec.args)
    }

    @Test
    fun `warp on linux uses working-directory flag`() {
        val spec = WarpProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = true)!!
        assertEquals("warp-terminal", spec.executable)
        assertEquals(listOf("--working-directory", dir), spec.args)
    }

    @Test
    fun `windows terminal reuses window 0 when reuseTab`() {
        val spec = WindowsTerminalProvider().launchSpec(OperatingSystem.WINDOWS, null, dir, reuseTab = true)!!
        assertEquals("wt.exe", spec.executable)
        assertEquals(listOf("-w", "0", "nt", "-d", dir), spec.args)
    }

    @Test
    fun `windows terminal opens new window without reuseTab`() {
        val spec = WindowsTerminalProvider().launchSpec(OperatingSystem.WINDOWS, null, dir, reuseTab = false)!!
        assertEquals(listOf("-d", dir), spec.args)
    }

    @Test
    fun `windows terminal unsupported off windows`() {
        assertNull(WindowsTerminalProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = true))
        assertNull(WindowsTerminalProvider().launchSpec(OperatingSystem.MAC, null, dir, reuseTab = true))
    }

    @Test
    fun `wezterm spawns tab when reuseTab else starts window`() {
        val tab = WezTermProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = true)!!
        assertEquals(listOf("cli", "spawn", "--cwd", dir), tab.args)
        val win = WezTermProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = false)!!
        assertEquals(listOf("start", "--cwd", dir), win.args)
    }

    @Test
    fun `wezterm uses exe suffix on windows`() {
        val spec = WezTermProvider().launchSpec(OperatingSystem.WINDOWS, null, dir, reuseTab = false)!!
        assertEquals("wezterm.exe", spec.executable)
    }

    @Test
    fun `alacritty ignores reuseTab and always passes working-directory`() {
        val a = AlacrittyProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = true)!!
        val b = AlacrittyProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = false)!!
        assertEquals(listOf("--working-directory", dir), a.args)
        assertEquals(a.args, b.args)
    }

    @Test
    fun `kitty only supported on mac and linux`() {
        assertNull(KittyProvider().launchSpec(OperatingSystem.WINDOWS, null, dir, reuseTab = true))
        val tab = KittyProvider().launchSpec(OperatingSystem.MAC, null, dir, reuseTab = true)!!
        assertEquals(listOf("@", "launch", "--type=tab", "--cwd", dir), tab.args)
        val win = KittyProvider().launchSpec(OperatingSystem.LINUX, null, dir, reuseTab = false)!!
        assertEquals(listOf("--directory", dir), win.args)
    }

    @Test
    fun `registry default is warp and lookup falls back`() {
        assertEquals("warp", TerminalRegistry.defaultProviderId)
        assertEquals("warp", TerminalRegistry.byIdOrDefault("does-not-exist").id)
        assertEquals("wezterm", TerminalRegistry.byIdOrDefault("wezterm").id)
    }

    @Test
    fun `installed providers is a subset preserving registry order`() {
        val installed = TerminalRegistry.installedProviders(OperatingSystem.LINUX)
        assertTrue(TerminalRegistry.providers.containsAll(installed))
        val order = TerminalRegistry.providers.filter { it in installed }
        assertEquals(order, installed)
    }
}
