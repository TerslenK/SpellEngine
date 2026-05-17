package io.github.terslenk.spellengine.advanced.instructions

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.SpellStack
import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import org.bukkit.entity.LivingEntity

// ─── Get Caster ───────────────────────────────────────────────────────────────

/**
 * Pushes the caster (Player) as an EntityIota onto the stack.
 */
object GetCasterInstruction : AdvancedInstruction {
    override val id = "entity_get_caster"
    override val displayName = "Get Caster"
    override val description = "Pushes YOU (the caster) onto the stack as an Entity."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.push(Iota.EntityIota(ctx.caster))
        return SpellResult.Success
    }
}

// ─── Get Entity Position ──────────────────────────────────────────────────────

/**
 * Pops an Entity, pushes its location as a VectorIota.
 */
object GetEntityPosInstruction : AdvancedInstruction {
    override val id = "entity_get_pos"
    override val displayName = "Get Entity Position"
    override val description = "Pops an Entity, pushes its location as a Vector."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }
        stack.push(Iota.VectorIota(entity.entity.location.toVector()))
        return SpellResult.Success
    }
}

// ─── Get Ray-traced Entity ────────────────────────────────────────────────────

/**
 * Pushes the first living entity in the caster's line of sight (up to 20 blocks).
 */
object GetLookedAtEntityInstruction : AdvancedInstruction {
    override val id = "entity_get_looked_at"
    override val displayName = "Get Looked-At Entity"
    override val description = "Pushes the entity you're looking at onto the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val ray = ctx.caster.rayTraceEntities(20)
            ?: return SpellResult.Mishap("No entity in your line of sight within 20 blocks.")

        val hit = ray.hitEntity as? LivingEntity
            ?: return SpellResult.Mishap("The looked-at object isn't a living entity.")

        stack.push(Iota.EntityIota(hit))
        return SpellResult.Success
    }
}

// ─── Damage Entity ────────────────────────────────────────────────────────────

/**
 * Pops: Entity, Number (damage)
 * Deals [damage] to the entity.
 */
object DamageEntityInstruction : AdvancedInstruction {
    override val id = "entity_damage"
    override val displayName = "Damage Entity"
    override val description = "Pops an Entity and a Number, deals that much damage."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val damage = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }

        val living = entity.entity as? LivingEntity
            ?: return SpellResult.Mishap("Cannot damage a non-living entity.")

        if (damage.value <= 0) {
            return SpellResult.Mishap("Damage amount must be greater than zero (got ${damage.value}).")
        }

        living.damage(damage.value, ctx.caster)
        return SpellResult.Success
    }
}

// ─── Heal Entity ──────────────────────────────────────────────────────────────

/**
 * Pops: Entity, Number (heal amount)
 */
object HealEntityInstruction : AdvancedInstruction {
    override val id = "entity_heal"
    override val displayName = "Heal Entity"
    override val description = "Pops an Entity and a Number, heals that much health."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val amount = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }

        val living = entity.entity as? LivingEntity
            ?: return SpellResult.Mishap("Cannot heal a non-living entity.")

        if (amount.value <= 0) {
            return SpellResult.Mishap("Heal amount must be greater than zero (got ${amount.value}).")
        }

        living.health = (living.health + amount.value).coerceAtMost(living.maxHealth)
        return SpellResult.Success
    }
}

// ─── Teleport Entity ──────────────────────────────────────────────────────────

/**
 * Pops: Entity, Vector (destination)
 * Teleports the entity to that position.
 */
object TeleportEntityInstruction : AdvancedInstruction {
    override val id = "entity_teleport"
    override val displayName = "Teleport Entity"
    override val description = "Pops an Entity and a Vector, teleports the entity there."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val dest = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }

        val location = dest.value.toLocation(ctx.world)
        location.yaw = entity.entity.location.yaw
        location.pitch = entity.entity.location.pitch

        entity.entity.teleport(location)
        return SpellResult.Success
    }
}
