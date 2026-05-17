package io.github.terslenk.spellengine.advanced

import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult

object AdvancedSpellExecutor {

    fun execute(spell: AdvancedSpell, ctx: SpellContext): SpellResult {
        if (spell.instructions.isEmpty()) {
            return SpellResult.Mishap("Your grimoire is empty — there are no instructions to execute.")
        }

        val stack = SpellStack()

        for ((index, instruction) in spell.instructions.withIndex()) {
            val result = runCatching { instruction.execute(ctx, stack) }
                .getOrElse { ex ->
                    SpellResult.Mishap(
                        "Instruction #${index + 1} (${instruction.displayName}) crashed: ${ex.message}"
                    )
                }

            if (result is SpellResult.Mishap) {
                return SpellResult.Mishap(
                    "[${instruction.displayName}] ${result.reason}"
                )
            }
        }

        return SpellResult.Success
    }
}