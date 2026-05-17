package io.github.terslenk.spellengine.basic

import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

object BasicSpellExecutor {

    fun execute(spell: BasicSpell, ctx: SpellContext): SpellResult {

        // 1. Shape resolves targets into ctx.targets
        val shapeResult = spell.shape.resolveTargets(ctx)
        if (shapeResult is SpellResult.Mishap) return shapeResult

        if (ctx.targets.isEmpty()) {
            return SpellResult.Mishap("Your spell found no valid targets.")
        }

        // 2. Build params then run each modifier in order
        var params = BasicSpellParams()
        for (modifier in spell.modifiers) {
            params = modifier.modify(params)
        }

        // 3. Expand to AoE targets if the modifier is active
                val rawTargets: List<Entity> = if (params.aoe) {
            ctx.targets.flatMap { origin ->
                origin.world
                    .getNearbyEntities(origin.location, params.radius, params.radius, params.radius)
                    .filter { it != ctx.caster }
            }.distinct()
        } else {
            ctx.targets.toList()
        }

        val targets = rawTargets.filter { target ->
            when (params.targetType) {
                "LIVING" -> target is LivingEntity
                "ITEM" -> target is org.bukkit.entity.Item
                else -> true
            }
        }

        if (targets.isEmpty()) {
            return SpellResult.Mishap("No entities found within the spell's area.")
        }

        // 4. Apply effect to every target; stop on first mishap
        for (target in targets) {
            val result = spell.effect.apply(ctx, target, params)
            if (result is SpellResult.Mishap) return result
        }

        return SpellResult.Success
    }
}
