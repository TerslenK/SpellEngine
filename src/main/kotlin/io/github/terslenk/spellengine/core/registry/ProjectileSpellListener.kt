package io.github.terslenk.spellengine.core.registry

import io.github.terslenk.spellengine.basic.BasicSpell
import io.github.terslenk.spellengine.basic.BasicSpellParams
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import io.github.terslenk.spellengine.item.SpellKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.nova.initialize.Init
import xyz.xenondevs.nova.initialize.InitFun
import xyz.xenondevs.nova.initialize.InitStage
import xyz.xenondevs.nova.util.registerEvents
import java.util.UUID

/**
 * Holds spells that are waiting for a projectile to land.
 * Register a pending spell before launching; the listener resolves it on hit.
 */
object PendingSpellManager {

    data class PendingSpell(
        val spell: BasicSpell,
        val ctx: SpellContext,
        val params: BasicSpellParams,
        val pierce: Boolean
    )

    private val pending = mutableMapOf<UUID, PendingSpell>()

    fun register(casterUUID: UUID, entry: PendingSpell) {
        pending[casterUUID] = entry
    }

    fun consume(casterUUID: UUID): PendingSpell? = pending.remove(casterUUID)
}

// --- Listener ----------------------------------------------------------------

@Init(stage = InitStage.POST_WORLD)
object ProjectileSpellListener : Listener {

    @InitFun
    private fun init() {
        registerEvents()
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectile = event.entity
        val pdc = projectile.persistentDataContainer

        val casterUUIDStr = pdc.get(SpellKeys.MAGIC_PROJECTILE, PersistentDataType.STRING)
            ?: return

        val casterUUID = runCatching { UUID.fromString(casterUUIDStr) }.getOrNull() ?: return

        val pending = PendingSpellManager.consume(casterUUID) ?: return
        val ctx = pending.ctx

        // Resolve target
        val hitEntity = event.hitEntity as? LivingEntity
        if (hitEntity == null) {
            ctx.caster.sendMessage(
                Component.text("[Spell] ").color(NamedTextColor.GOLD)
                    .append(Component.text("Your projectile hit nothing.").color(NamedTextColor.RED))
            )
            if (!pending.pierce) event.isCancelled = false
            return
        }

        ctx.targets.add(hitEntity)

        // Apply the effect now that we have a target
        val result = pending.spell.effect.apply(ctx, hitEntity, pending.params)

        when (result) {
            is SpellResult.Success -> { /* all good */ }
            is SpellResult.Mishap  -> ctx.caster.sendMishap(result.reason)
        }

        // Pierce: don't cancel the event so the projectile keeps going
        if (!pending.pierce) event.isCancelled = true
    }
}

// --- Extension ---------------------------------------------------------------

fun Player.sendMishap(reason: String) {
    sendMessage(
        Component.text("[Mishap] ").color(NamedTextColor.DARK_RED)
            .append(Component.text(reason).color(NamedTextColor.RED))
    )
}

fun Player.sendSpellSuccess() {
    sendMessage(
        Component.text("[Spell] ").color(NamedTextColor.GOLD)
            .append(Component.text("Cast successfully.").color(NamedTextColor.GREEN))
    )
}
