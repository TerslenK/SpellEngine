package io.github.terslenk.spellengine.advanced.instructions

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.SpellStack
import io.github.terslenk.spellengine.advanced.toMishapOrNull
import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult

// ─── Push Literal Number ──────────────────────────────────────────────────────

/**
 * Pushes a hard-coded number onto the stack.
 * Each item in the GUI with this instruction carries its own [value].
 */
data class PushNumberInstruction(val value: Double) : AdvancedInstruction {
    override val id = "push_number"
    override val displayName = "Push Number ($value)"
    override val description = "Pushes the number $value onto the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.push(Iota.NumberIota(value))
        return SpellResult.Success
    }
}

// ─── Duplicate top ────────────────────────────────────────────────────────────

object DupInstruction : AdvancedInstruction {
    override val id = "stack_dup"
    override val displayName = "Duplicate"
    override val description = "Duplicates the top value on the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val top = stack.peek()
            ?: return SpellResult.Mishap("Cannot duplicate — the stack is empty.")
        stack.push(top)
        return SpellResult.Success
    }
}

// ─── Drop top ─────────────────────────────────────────────────────────────────

object DropInstruction : AdvancedInstruction {
    override val id = "stack_drop"
    override val displayName = "Drop"
    override val description = "Discards the top value on the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.pop().toMishapOrNull()?.let { return it }
        return SpellResult.Success
    }
}

// ─── Swap top two ─────────────────────────────────────────────────────────────

object SwapInstruction : AdvancedInstruction {
    override val id = "stack_swap"
    override val displayName = "Swap"
    override val description = "Swaps the top two values on the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val a = stack.pop().getOrElse { return SpellResult.Mishap(it.message!!) }
        val b = stack.pop().getOrElse { return SpellResult.Mishap(it.message!!) }
        stack.push(a)
        stack.push(b)
        return SpellResult.Success
    }
}
