package test

import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.gui.Gui

fun testAnvil(player: Player, gui: Gui) {
    AnvilWindow.builder()
        .setViewer(player)
        .addRenameHandler { println(it) }
        .setUpperGui(gui)
}
