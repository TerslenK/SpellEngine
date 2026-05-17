package io.github.terslenk.spellengine.gui

import io.github.terslenk.spellengine.item.SpellKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.AnvilWindow

object AnvilInputManager {
    fun prompt(player: Player, slot: EquipmentSlot) {
        var currentText = ""
        val confirmItem = Item.builder()
            .setItemProvider(ItemBuilder(Material.LIME_DYE).setName("<green><bold>Confirm Number").addLoreLines("<gray>Click to save custom number."))
            .addClickHandler { click ->
                val num = currentText.toDoubleOrNull()
                if (num != null) {
                    val item = if (slot == EquipmentSlot.HAND) player.inventory.itemInMainHand else player.inventory.itemInOffHand
                    val meta = item.itemMeta
                    if (meta != null) {
                        val pdc = meta.persistentDataContainer
                        val currentIds = pdc.get(SpellKeys.SPELL_IDS, PersistentDataType.STRING) ?: ""
                        val newId = "stack_push_number_$num"
                        val newIds = if (currentIds.isBlank()) newId else "$currentIds,$newId"
                        pdc.set(SpellKeys.SPELL_IDS, PersistentDataType.STRING, newIds)
                        item.setItemMeta(meta)
                    }
                    GrimoireGui(player, item, slot).open()
                } else {
                    player.sendMessage(Component.text("'$currentText' is not a valid number.", NamedTextColor.RED))
                }
            }
            .build()
            
        val upperGui = Gui.builder()
            .setStructure("x . c")
            .addIngredient('x', Item.simple(ItemBuilder(Material.PAPER).setName("0")))
            .addIngredient('c', confirmItem)
            .build()

        AnvilWindow.builder()
            .setViewer(player)
            .setTitle("Enter Number")
            .addRenameHandler { currentText = it }
            .setUpperGui(upperGui)
            .open(player)
    }
}
