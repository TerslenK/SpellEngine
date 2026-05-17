package io.github.terslenk.spellengine.basic

import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import io.github.terslenk.spellengine.item.SpellKeys
import org.bukkit.entity.Snowball
import org.bukkit.persistence.PersistentDataType

// ─── Self ─────────────────────────────────────────────────────────────────────

/**
 * Targets only the caster.
 */
object SelfShape : Shape {
    override val id = "shape_self"
    override val displayName = "Self"

    override fun resolveTargets(ctx: SpellContext): SpellResult {
        ctx.targets.add(ctx.caster)
        return SpellResult.Success
    }
}

// ─── Ray ──────────────────────────────────────────────────────────────────────

/**
 * Targets the first living entity in the caster's line of sight.
 */
object RayShape : Shape {
    override val id = "shape_ray"
    override val displayName = "Ray"

    private const val MAX_DISTANCE = 20

    override fun resolveTargets(ctx: SpellContext): SpellResult {
        val ray = ctx.caster.rayTraceEntities(MAX_DISTANCE)
            ?: return SpellResult.Mishap("No entity is in your line of sight within $MAX_DISTANCE blocks.")

        val hit = ray.hitEntity
            ?: return SpellResult.Mishap("The ray hit something, but it wasn't a living entity.")

        ctx.targets.add(hit)
        return SpellResult.Success
    }
}

// ─── Area ─────────────────────────────────────────────────────────────────────

/**
 * Targets all nearby living entities (excluding the caster).
 */
object AreaShape : Shape {
    override val id = "shape_area"
    override val displayName = "Area"

    private const val RADIUS = 6.0

    override fun resolveTargets(ctx: SpellContext): SpellResult {
        val nearby = ctx.caster
            .getNearbyEntities(RADIUS, RADIUS, RADIUS)
            .filter { it != ctx.caster }

        if (nearby.isEmpty()) {
            return SpellResult.Mishap("No entities are within ${RADIUS.toInt()} blocks of you.")
        }

        ctx.targets.addAll(nearby)
        return SpellResult.Success
    }
}

// ─── Projectile ───────────────────────────────────────────────────────────────

/**
 * Launches a magic projectile.  Target resolution is DEFERRED — the
 * [io.github.terslenk.spellengine.core.registry.ProjectileSpellListener] handles the hit event,
 * re-runs the effect once the projectile lands.
 *
 * Because resolution is async this shape always returns [SpellResult.Success]
 * immediately; the projectile carries the caster's UUID in its PDC.
 */
object ProjectileShape : Shape {
    override val id = "shape_projectile"
    override val displayName = "Projectile"

    override fun resolveTargets(ctx: SpellContext): SpellResult {
        // Projectile is launched; actual target set in ProjectileSpellListener.
        // We tag the projectile with the caster's UUID via PDC so the listener
        // can look up the pending spell from PendingSpellManager.
        val projectile = ctx.caster.launchProjectile(Snowball::class.java)
        projectile.shooter = ctx.caster
        projectile.persistentDataContainer.set(
            SpellKeys.MAGIC_PROJECTILE,
            PersistentDataType.STRING,
            ctx.caster.uniqueId.toString()
        )
        return SpellResult.Success // listener will finish the job
    }
}


