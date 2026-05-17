package io.github.terslenk.spellengine.basic

import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import org.bukkit.entity.Entity

// ─── Params ──────────────────────────────────────────────────────────────────

/**
 * Mutable parameters that modifiers can tweak before an effect runs.
 */
data class BasicSpellParams(
    val power: Double = 1.0,
    val duration: Int = 100, // ticks
    val aoe: Boolean = false,
    val radius: Double = 3.0,
    val pierce: Boolean = false,
    val targetType: String = "ALL" // "ALL", "LIVING", "ITEM"
)

// ─── Shape ───────────────────────────────────────────────────────────────────

/**
 * A Shape resolves HOW a spell is delivered.
 * It fills [SpellContext.targets] with the relevant entities.
 */
interface Shape {
    val id: String
    val displayName: String

    /**
     * Populate [ctx.targets].
     * Return [SpellResult.Mishap] if no valid targets exist.
     */
    fun resolveTargets(ctx: SpellContext): SpellResult
}

// ─── Effect ──────────────────────────────────────────────────────────────────

/**
 * An Effect defines WHAT the spell does to each target.
 */
interface Effect {
    val id: String
    val displayName: String

    /**
     * Apply the effect to a single [target].
     * [params] carries power / duration after modifiers are applied.
     */
    fun apply(ctx: SpellContext, target: Entity, params: BasicSpellParams): SpellResult
}

// ─── Modifier ────────────────────────────────────────────────────────────────

/**
 * A Modifier adjusts [BasicSpellParams] before the effect runs.
 * Modifiers are applied in the order they appear in the spell.
 */
interface Modifier {
    val id: String
    val displayName: String

    fun modify(params: BasicSpellParams): BasicSpellParams
}

