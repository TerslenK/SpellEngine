package io.github.terslenk.spellengine.item

import io.github.terslenk.spellengine.advanced.AdvancedSpellExecutor
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import io.github.terslenk.spellengine.core.registry.SpellRegistry
import io.github.terslenk.spellengine.core.registry.sendMishap
import io.github.terslenk.spellengine.core.registry.sendSpellSuccess
import io.github.terslenk.spellengine.gui.GrimoireGui
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.nova.context.Context
import xyz.xenondevs.nova.context.intention.ItemUse
import xyz.xenondevs.nova.world.InteractionResult
import xyz.xenondevs.nova.world.item.behavior.ItemBehavior

/**
 * ItemBehavior for the Advanced Grimoire.
 *
 * Shift-Right-click to open the InvUI grimoire editor.
 * Right-click to execute the stack-based spell program stored on the item.
 */
object GrimoireBehavior : ItemBehavior {

    override fun use(itemStack: ItemStack, ctx: Context<ItemUse>): InteractionResult {
        val player = ctx[ItemUse.SOURCE_PLAYER] ?: return InteractionResult.Pass

        if (player.isSneaking) {
            GrimoireGui(player, itemStack).open()
            return InteractionResult.Success(true)
        }

        val pdc = itemStack.itemMeta?.persistentDataContainer ?: run {
            player.sendMishap("This grimoire is blank. Sneak-Right-Click to edit.")
            return InteractionResult.Success(true)
        }

        val raw = pdc.get(SpellKeys.SPELL_IDS, PersistentDataType.STRING)
        if (raw.isNullOrBlank()) {
            player.sendMishap("This grimoire is blank. Sneak-Right-Click to edit.")
            return InteractionResult.Success(true)
        }

        val ids = raw.split(",")
        val spell = SpellRegistry.deserializeAdvanced(ids)

        val spellCtx = SpellContext(caster = player)
        val result = AdvancedSpellExecutor.execute(spell, spellCtx)

        when (result) {
            is SpellResult.Success -> player.sendSpellSuccess()
            is SpellResult.Mishap  -> player.sendMishap(result.reason)
        }

        return InteractionResult.Success(true)
    }
}
