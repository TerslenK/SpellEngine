package io.github.terslenk.spellengine.item

import io.github.terslenk.spellengine.basic.BasicSpell
import io.github.terslenk.spellengine.basic.BasicSpellExecutor
import io.github.terslenk.spellengine.basic.BasicSpellParams
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import io.github.terslenk.spellengine.core.registry.PendingSpellManager
import io.github.terslenk.spellengine.core.registry.SpellRegistry
import io.github.terslenk.spellengine.core.registry.sendMishap
import io.github.terslenk.spellengine.core.registry.sendSpellSuccess
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.nova.context.Context
import xyz.xenondevs.nova.context.intention.ItemUse
import xyz.xenondevs.nova.world.InteractionResult
import xyz.xenondevs.nova.world.item.behavior.ItemBehavior

/**
 * ItemBehavior for the Basic Wand.
 *
 * Right-click to cast the spell stored on the item.
 * The spell is stored in the item's PDC as a comma-separated list of component IDs
 * (e.g. "shape_ray,effect_damage,mod_amplify").
 */
object WandBehavior : ItemBehavior {

    /**
     * Casts the basic spell stored on the wand when the player right-clicks.
     */
    override fun use(itemStack: ItemStack, ctx: Context<ItemUse>): InteractionResult {
        val player = ctx[ItemUse.SOURCE_PLAYER] ?: return InteractionResult.Pass

        // Read spell IDs from PDC
        val pdc = itemStack.itemMeta?.persistentDataContainer ?: run {
            player.sendMishap("This wand has no spell inscribed.")
            return InteractionResult.Success(true)
        }

        val raw = pdc.get(SpellKeys.SPELL_IDS, PersistentDataType.STRING)
        if (raw.isNullOrBlank()) {
            player.sendMishap("This wand has no spell inscribed.")
            return InteractionResult.Success(true)
        }

        val ids = raw.split(",")
        val spell = SpellRegistry.deserializeBasic(ids)
        if (spell == null) {
            player.sendMishap("The spell on this wand is malformed or uses unknown components.")
            return InteractionResult.Success(true)
        }

        val spellCtx = SpellContext(caster = player)

        // For projectile shapes we need to register a pending spell first
        if (spell.shape.id == "shape_projectile") {
            var params = BasicSpellParams()
            for (modifier in spell.modifiers) {
                params = modifier.modify(params)
            }
            PendingSpellManager.register(
                player.uniqueId,
                PendingSpellManager.PendingSpell(spell, spellCtx, params, params.pierce)
            )
        }

        val result = BasicSpellExecutor.execute(spell, spellCtx)

        when (result) {
            is SpellResult.Success -> player.sendSpellSuccess()
            is SpellResult.Mishap  -> player.sendMishap(result.reason)
        }

        return InteractionResult.Success(true)
    }
}
