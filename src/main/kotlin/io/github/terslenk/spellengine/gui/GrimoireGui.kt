package io.github.terslenk.spellengine.gui

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.core.registry.SpellRegistry
import io.github.terslenk.spellengine.item.SpellKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window

class GrimoireGui(private val player: Player, private val grimoireItem: ItemStack, private val slot: EquipmentSlot) {

    private val currentInstructions = mutableListOf<AdvancedInstruction>()
    private var sequencePage = 0
    private var instructionPage = 0

    private lateinit var instructionItems: Array<Item>
    private lateinit var instPrevItem: Item
    private lateinit var instNextItem: Item

    private fun notifyInstructions() {
        if (::instructionItems.isInitialized) instructionItems.forEach { it.notifyWindows() }
        if (::instPrevItem.isInitialized) instPrevItem.notifyWindows()
        if (::instNextItem.isInitialized) instNextItem.notifyWindows()
    }

    private val simulatedStacks = mutableListOf<String>()

    private fun computeSimulation() {
        simulatedStacks.clear()
        val stack = mutableListOf<String>()
        for (inst in currentInstructions) {
            when (inst.id) {
                "entity_get_caster", "entity_get_looked_at" -> stack.add("Entity")
                "world_get_looked_at_block" -> stack.add("Vector")
                "world_construct_vector" -> { stack.removeLastOrNull(); stack.removeLastOrNull(); stack.removeLastOrNull(); stack.add("Vector") }
                "entity_get_in_radius" -> { stack.removeLastOrNull(); stack.removeLastOrNull(); stack.add("List") }
                "entity_get_pos", "entity_get_eye_pos" -> { stack.removeLastOrNull(); stack.add("Vector") }
                "world_get_look_dir" -> { stack.removeLastOrNull(); stack.add("Vector") }
                "math_add", "math_sub", "math_mul", "math_div" -> { stack.removeLastOrNull(); stack.removeLastOrNull(); stack.add("Number") }
                "math_abs" -> { stack.removeLastOrNull(); stack.add("Number") }
                "world_offset_vector" -> { stack.removeLastOrNull(); stack.removeLastOrNull(); stack.add("Vector") }
                "world_scale_vector" -> { stack.removeLastOrNull(); stack.removeLastOrNull(); stack.add("Vector") }
                "world_push_vector_up", "world_push_vector_down", "world_push_vector_north", "world_push_vector_south", "world_push_vector_east", "world_push_vector_west" -> stack.add("Vector")
                "world_raycast_block" -> { stack.removeLastOrNull(); stack.removeLastOrNull(); stack.removeLastOrNull(); stack.add("Vector") }
                "world_explode" -> { stack.removeLastOrNull(); stack.removeLastOrNull() }
                "world_break_block" -> { stack.removeLastOrNull() }
                "entity_damage", "entity_heal", "entity_teleport", "entity_ignite" -> { stack.removeLastOrNull(); stack.removeLastOrNull() }
                "stack_drop" -> { stack.removeLastOrNull() }
                "stack_dup" -> { val v = stack.lastOrNull() ?: "Any"; stack.add(v) }
                "stack_swap" -> { 
                    val a = stack.removeLastOrNull() ?: "Any"
                    val b = stack.removeLastOrNull() ?: "Any"
                    stack.add(a)
                    stack.add(b)
                }
                "stack_filter_items", "stack_filter_mobs" -> { stack.removeLastOrNull(); stack.add("List") }
                "stack_print" -> { /* does nothing to stack */ }
                else -> { if (inst.id.startsWith("stack_push_number")) stack.add("Number") }
            }
            simulatedStacks.add(stack.joinToString(", ", prefix = "[", postfix = "]"))
        }
    }

    init {
        val pdc = grimoireItem.itemMeta?.persistentDataContainer
        if (pdc != null) {
            val raw = pdc.get(SpellKeys.SPELL_IDS, PersistentDataType.STRING)
            if (!raw.isNullOrBlank()) {
                val ids = raw.split(",")
                currentInstructions.addAll(SpellRegistry.deserializeAdvanced(ids).instructions)
            }
        }
        computeSimulation()
    }

    private lateinit var seqPrevItem: Item
    private lateinit var seqNextItem: Item

    private val sequenceItems = Array(9) { i ->
        Item.builder()
            .setItemProvider {
                val idx = sequencePage * 9 + i
                if (idx < currentInstructions.size) {
                    val inst = currentInstructions[idx]
                    ItemBuilder(Material.ENCHANTED_BOOK)
                        .setName("<aqua><bold>${idx + 1}. ${inst.displayName}")
                        .addLoreLines("<red>Left-Click to remove", "<yellow>Right-Click to shift left", "", "<gray>Stack state after execution:", "<light_purple>${simulatedStacks.getOrNull(idx) ?: "[]"}")
                } else {
                    ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName("<gray>-")
                }
            }
            .addClickHandler { click ->
                val idx = sequencePage * 9 + i
                if (idx < currentInstructions.size) {
                    if (click.clickType.isRightClick && idx > 0) {
                        val temp = currentInstructions[idx]
                        currentInstructions[idx] = currentInstructions[idx - 1]
                        currentInstructions[idx - 1] = temp
                    } else if (click.clickType.isLeftClick) {
                        currentInstructions.removeAt(idx)
                    }
                    notifySequence()
                }
            }
            .build()
    }

    private fun notifySequence() {
        computeSimulation()
        sequenceItems.forEach { it.notifyWindows() }
        if (::seqPrevItem.isInitialized) seqPrevItem.notifyWindows()
        if (::seqNextItem.isInitialized) seqNextItem.notifyWindows()
    }

    private fun saveToItem() {
        val meta = grimoireItem.itemMeta
        if (meta != null) {
            val ids = currentInstructions.joinToString(",") { it.id }
            meta.persistentDataContainer.set(SpellKeys.SPELL_IDS, PersistentDataType.STRING, ids)
            
            val lore = mutableListOf<Component>()
            lore.add(Component.text("Instructions:", NamedTextColor.GRAY))
            currentInstructions.forEach {
                lore.add(Component.text("- ${it.displayName}", NamedTextColor.AQUA))
            }
            meta.lore(lore)
            grimoireItem.setItemMeta(meta)

            if (slot == EquipmentSlot.OFF_HAND) player.inventory.setItemInOffHand(grimoireItem) else player.inventory.setItemInMainHand(grimoireItem)
            player.updateInventory()
        }
    }

    fun open() {
        seqPrevItem = Item.builder()
            .setItemProvider {
                if (sequencePage > 0) ItemBuilder(Material.SPECTRAL_ARROW).setName("<yellow>Previous Sequence Page")
                else ItemBuilder(Material.AIR)
            }
            .addClickHandler { _ ->
                if (sequencePage > 0) {
                    sequencePage--
                    notifySequence()
                }
            }.build()

        seqNextItem = Item.builder()
            .setItemProvider {
                if ((sequencePage + 1) * 9 < currentInstructions.size) ItemBuilder(Material.SPECTRAL_ARROW).setName("<yellow>Next Sequence Page")
                else ItemBuilder(Material.AIR)
            }
            .addClickHandler { _ ->
                if ((sequencePage + 1) * 9 < currentInstructions.size) {
                    sequencePage++
                    notifySequence()
                }
            }.build()

        val saveItem = Item.builder()
            .setItemProvider(ItemBuilder(Material.LIME_DYE).setName("<green><bold>Save & Close"))
            .addClickHandler { _ ->
                saveToItem()
                player.closeInventory()
            }
            .build()

        val clearItem = Item.builder()
            .setItemProvider(ItemBuilder(Material.BARRIER).setName("<red><bold>Clear Sequence"))
            .addClickHandler { click ->
                currentInstructions.clear()
                sequencePage = 0
                notifySequence()
            }
            .build()

        instPrevItem = Item.builder()
            .setItemProvider {
                if (instructionPage > 0) ItemBuilder(Material.ARROW).setName("<yellow>Previous Page (Instructions)")
                else ItemBuilder(Material.AIR)
            }
            .addClickHandler { _ ->
                if (instructionPage > 0) {
                    instructionPage--
                    notifyInstructions()
                }
            }
            .build()

        instNextItem = Item.builder()
            .setItemProvider {
                // Not sure if there are more pages without the total size, so we'll check it in a sec
                ItemBuilder(Material.ARROW).setName("<yellow>Next Page (Instructions)")
            }
            .addClickHandler { _ ->
                // Handled below
            }
            .build()

        val groups = SpellRegistry.allInstructions()
            .sortedBy { it.id }
            .groupBy { it.id.substringBefore("_") }

        val allInstructions = mutableListOf<Pair<xyz.xenondevs.invui.item.ItemProvider, AdvancedInstruction?>>()
        for ((category, insts) in groups) {
            val catColor = when (category) {
                "math" -> "<green>"
                "stack" -> "<gold>"
                "entity" -> "<light_purple>"
                "world" -> "<aqua>"
                else -> "<white>"
            }
            val catMat = when (category) {
                "math" -> Material.COMPARATOR
                "stack" -> Material.HOPPER
                "entity" -> Material.ZOMBIE_HEAD
                "world" -> Material.GRASS_BLOCK
                else -> Material.PAPER
            }
            val catName = category.replaceFirstChar { it.uppercase() }

            insts.forEach { inst ->
                val provider = ItemBuilder(catMat)
                    .setName("$catColor<bold>${inst.displayName}")
                    .addLoreLines(
                        "<dark_gray>Category: $catName",
                        "",
                        "<gray>${inst.description}",
                        "",
                        "<yellow>Click to add to sequence"
                    )
                allInstructions.add(Pair(provider, inst))
            }
            
            // Pad the rest of the row with panes so the next category starts on a new line
            val remainder = allInstructions.size % 27
            if (remainder > 0) {
                for (i in 0 until (27 - remainder)) {
                    val pad = ItemBuilder(Material.AIR)
                    allInstructions.add(Pair(pad, null))
                }
            }
        }

        // Redefine next item now that we know total size
        instNextItem = Item.builder()
            .setItemProvider {
                if ((instructionPage + 1) * 27 < allInstructions.size) ItemBuilder(Material.ARROW).setName("<yellow>Next Page (Instructions)")
                else ItemBuilder(Material.AIR)
            }
            .addClickHandler { _ ->
                if ((instructionPage + 1) * 27 < allInstructions.size) {
                    instructionPage++
                    notifyInstructions()
                }
            }
            .build()

        instructionItems = Array(27) { idx ->
            Item.builder()
                .setItemProvider {
                    val pageIdx = instructionPage * 27 + idx
                    if (pageIdx < allInstructions.size) {
                        allInstructions[pageIdx].first
                    } else {
                        ItemBuilder(Material.AIR)
                    }
                }
                .addClickHandler { _ ->
                    val pageIdx = instructionPage * 27 + idx
                    if (pageIdx < allInstructions.size) {
                        val inst = allInstructions[pageIdx].second
                        if (inst != null) {
                            currentInstructions.add(inst)
                            if (currentInstructions.size > (sequencePage + 1) * 9) {
                                sequencePage = (currentInstructions.size - 1) / 9
                            }
                            notifySequence()
                        }
                    }
                }
                .build()
        }

        val customNumberItem = Item.builder()
            .setItemProvider(ItemBuilder(Material.WRITABLE_BOOK).setName("<gold><bold>Custom Number").addLoreLines("<gray>Click to type a custom number", "<gray>in an anvil to push onto the stack."))
            .addClickHandler { _ ->
                saveToItem()
                AnvilInputManager.prompt(player, slot)
            }
            .build()

        val gui = xyz.xenondevs.invui.gui.Gui.empty(9, 6)

        sequenceItems.forEachIndexed { i, item -> gui.setItem(i, item) }
        gui.setItem(9, seqPrevItem)
        gui.setItem(10, customNumberItem)
        gui.setItem(11, clearItem)
        gui.setItem(13, saveItem)
        gui.setItem(17, seqNextItem)
        gui.setItem(48, instPrevItem)
        gui.setItem(50, instNextItem)
        
        for (i in 0 until 27) {
            gui.setItem(18 + i, instructionItems[i])
        }
        
        val pad = Item.simple(ItemBuilder(Material.AIR))
        for (i in 0 until 54) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, pad)
            }
        }

        Window.builder()
            .setTitle("<dark_purple><bold>Edit Grimoire")
            .setUpperGui(gui)
            .open(player)
    }
}











