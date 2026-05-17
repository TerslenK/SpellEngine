package io.github.terslenk.spellengine.advanced

import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult

/**
 * A single instruction in an Advanced (Hex-style) spell.
 *
 * Each instruction reads from and writes to the [SpellStack].
 * Instructions are responsible for popping their own arguments and
 * pushing their own results — and for returning a clear [SpellResult.Mishap]
 * when the stack doesn't have what they need.
 */
interface AdvancedInstruction {
    val id: String
    val displayName: String

    /** Human-readable description shown in the grimoire GUI. */
    val description: String

    fun execute(ctx: SpellContext, stack: SpellStack): SpellResult
}

// ─── Spell ────────────────────────────────────────────────────────────────────

/**
 * An ordered sequence of [AdvancedInstruction]s that forms a complete spell.
 */
data class AdvancedSpell(
    val instructions: List<AdvancedInstruction>
)
