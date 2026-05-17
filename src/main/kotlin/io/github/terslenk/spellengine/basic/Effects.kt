package io.github.terslenk.spellengine.basic

import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

// ─── Damage ───────────────────────────────────────────────────────────────────

object DamageEffect : Effect {
    override val id = "effect_damage"
    override val displayName = "Damage"

    override fun apply(ctx: SpellContext, target: Entity, params: BasicSpellParams): SpellResult {
        val living = target as? LivingEntity
            ?: return SpellResult.Mishap("Damage can only be applied to living entities.")

        if (living == ctx.caster) {
            return SpellResult.Mishap("You cannot damage yourself with this spell.")
        }

        living.damage(6.0 * params.power, ctx.caster)
        return SpellResult.Success
    }
}

// ─── Heal ─────────────────────────────────────────────────────────────────────

object HealEffect : Effect {
    override val id = "effect_heal"
    override val displayName = "Heal"

    override fun apply(ctx: SpellContext, target: Entity, params: BasicSpellParams): SpellResult {
        val living = target as? LivingEntity
            ?: return SpellResult.Mishap("Heal can only be applied to living entities.")

        living.health = (living.health + 6.0 * params.power).coerceAtMost(living.maxHealth)
        return SpellResult.Success
    }
}

// ─── Ignite ───────────────────────────────────────────────────────────────────

object IgniteEffect : Effect {
    override val id = "effect_ignite"
    override val displayName = "Ignite"

    override fun apply(ctx: SpellContext, target: Entity, params: BasicSpellParams): SpellResult {
        if (target !is LivingEntity) {
            return SpellResult.Mishap("Ignite can only be applied to living entities.")
        }

        target.fireTicks = params.duration
        return SpellResult.Success
    }
}

// ─── Teleport ─────────────────────────────────────────────────────────────────

/**
 * Teleports the target to the caster's location (or vice-versa when targeting self).
 * When used with [SelfShape] it acts as a blink toward the caster's look direction.
 */
object TeleportEffect : Effect {
    override val id = "effect_teleport"
    override val displayName = "Teleport"

    private const val BLINK_DISTANCE = 8.0

    override fun apply(ctx: SpellContext, target: Entity, params: BasicSpellParams): SpellResult {
        if (target == ctx.caster) {
            // Blink forward
            val dest = ctx.caster.rayTraceBlocks(BLINK_DISTANCE * params.power)
                ?.hitBlock
                ?.location
                ?.add(0.0, 1.0, 0.0)
                ?: ctx.caster.location.add(
                    ctx.caster.location.direction.multiply(BLINK_DISTANCE * params.power)
                )
            ctx.caster.teleport(dest)
        } else {
            // Pull the target to the caster
            target.teleport(ctx.caster.location)
        }
        return SpellResult.Success
    }
}

// ─── Push ─────────────────────────────────────────────────────────────────────

object PushEffect : Effect {
    override val id = "effect_push"
    override val displayName = "Push"

    override fun apply(ctx: SpellContext, target: Entity, params: BasicSpellParams): SpellResult {
        if (target == ctx.caster) {
            return SpellResult.Mishap("You cannot push yourself.")
        }

        val direction = target.location.toVector()
            .subtract(ctx.caster.location.toVector())
            .normalize()
            .multiply(1.5 * params.power)
            .setY(0.4 * params.power)

        target.velocity = direction
        return SpellResult.Success
    }
}
