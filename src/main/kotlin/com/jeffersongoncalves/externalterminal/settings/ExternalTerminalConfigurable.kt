package com.jeffersongoncalves.externalterminal.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.jeffersongoncalves.externalterminal.OperatingSystem
import com.jeffersongoncalves.externalterminal.terminal.TerminalProvider
import com.jeffersongoncalves.externalterminal.terminal.TerminalRegistry
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.ListCellRenderer

/** Settings → Tools → External Terminal. Lets the user pick the terminal, tab behaviour
 *  and a per-terminal executable path override. */
class ExternalTerminalConfigurable : Configurable {

    private val settings = ExternalTerminalSettings.getInstance()

    private lateinit var terminalCombo: ComboBox<TerminalProvider>
    private lateinit var reuseTabCheck: JBCheckBox
    private lateinit var pathField: TextFieldWithBrowseButton

    /** Working copy of per-terminal path overrides, edited live and flushed on apply(). */
    private val workingPaths = mutableMapOf<String, String>()
    private var shownTerminalId: String = settings.selectedTerminalId

    override fun getDisplayName(): String = "External Terminal"

    override fun createComponent(): JComponent {
        TerminalRegistry.providers.forEach { provider ->
            workingPaths[provider.id] = settings.executablePath(provider.id).orEmpty()
        }

        terminalCombo = ComboBox(DefaultComboBoxModel(shownProviders().toTypedArray())).apply {
            renderer = ListCellRenderer { list, value, index, selected, focused ->
                javax.swing.DefaultListCellRenderer().getListCellRendererComponent(
                    list, value?.displayName ?: "", index, selected, focused,
                )
            }
            selectedItem = TerminalRegistry.byIdOrDefault(settings.selectedTerminalId)
            addActionListener {
                val newId = (selectedItem as? TerminalProvider)?.id ?: return@addActionListener
                // Persist what is currently typed before switching, then load the new terminal's path.
                workingPaths[shownTerminalId] = pathField.text.trim()
                shownTerminalId = newId
                pathField.text = workingPaths[newId].orEmpty()
            }
        }

        reuseTabCheck = JBCheckBox("Reuse existing window as a new tab when possible", settings.reuseTab)

        pathField = TextFieldWithBrowseButton().apply {
            text = workingPaths[shownTerminalId].orEmpty()
            val descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                .withTitle("Select Terminal Executable")
                .withDescription("Leave empty to use the default for the selected terminal")
            addBrowseFolderListener(null, descriptor)
        }

        return panel {
            row("Terminal:") {
                cell(terminalCombo).align(AlignX.LEFT)
            }
            row {
                comment("Only terminals detected on this system are listed.")
            }
            row {
                cell(reuseTabCheck)
            }
            row("Executable path:") {
                cell(pathField).align(AlignX.FILL)
            }
            row {
                comment("Optional. Empty = use the bundled default for the selected terminal.")
            }
        }
    }

    override fun isModified(): Boolean {
        flushShownPath()
        if ((terminalCombo.selectedItem as? TerminalProvider)?.id != settings.selectedTerminalId) return true
        if (reuseTabCheck.isSelected != settings.reuseTab) return true
        return TerminalRegistry.providers.any { p ->
            workingPaths[p.id].orEmpty() != settings.executablePath(p.id).orEmpty()
        }
    }

    override fun apply() {
        flushShownPath()
        settings.selectedTerminalId =
            (terminalCombo.selectedItem as? TerminalProvider)?.id ?: TerminalRegistry.defaultProviderId
        settings.reuseTab = reuseTabCheck.isSelected
        TerminalRegistry.providers.forEach { p ->
            settings.setExecutablePath(p.id, workingPaths[p.id].orEmpty())
        }
    }

    override fun reset() {
        terminalCombo.selectedItem = TerminalRegistry.byIdOrDefault(settings.selectedTerminalId)
        reuseTabCheck.isSelected = settings.reuseTab
        TerminalRegistry.providers.forEach { p ->
            workingPaths[p.id] = settings.executablePath(p.id).orEmpty()
        }
        shownTerminalId = settings.selectedTerminalId
        pathField.text = workingPaths[shownTerminalId].orEmpty()
    }

    private fun flushShownPath() {
        if (::pathField.isInitialized) {
            workingPaths[shownTerminalId] = pathField.text.trim()
        }
    }

    /**
     * Only terminals detected on this OS are offered, plus the currently-selected one and any
     * with a saved custom path (so an existing choice is never silently dropped). Falls back to
     * the full catalogue when nothing is detected, so the user can still point at a custom path.
     */
    private fun shownProviders(): List<TerminalProvider> {
        val os = OperatingSystem.current()
        return TerminalRegistry.providers.filter { p ->
            p.id == settings.selectedTerminalId ||
                settings.executablePath(p.id) != null ||
                TerminalRegistry.installedProviders(os).any { it.id == p.id }
        }.ifEmpty { TerminalRegistry.providers }
    }
}
