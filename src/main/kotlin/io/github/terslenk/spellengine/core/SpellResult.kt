package io.github.terslenk.spellengine.core

/**
 * Result of executing a spell or instruction.
 *
 * Success  — spell worked fine.
 * Mishap   — something went wrong; [reason] is shown to the caster.
 */
sealed class SpellResult {
    data object Success : SpellResult()
    data class Mishap(val reason: String) : SpellResult()
}