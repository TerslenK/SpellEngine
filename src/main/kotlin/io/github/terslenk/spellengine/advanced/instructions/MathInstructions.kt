package io.github.terslenk.spellengine.advanced.instructions

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.SpellStack
import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import kotlin.math.abs

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun binaryNumberOp(
    name: String,
    stack: SpellStack,
    op: (Double, Double) -> Double
): SpellResult {
    val b = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
    val a = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
    stack.push(Iota.NumberIota(op(a.value, b.value)))
    return SpellResult.Success
}

// ─── Instructions ─────────────────────────────────────────────────────────────

object AddInstruction : AdvancedInstruction {
    override val id = "math_add"
    override val displayName = "Add"
    override val description = "Pops two Numbers, pushes their sum."

    override fun execute(ctx: SpellContext, stack: SpellStack) =
        binaryNumberOp("Add", stack) { a, b -> a + b }
}

object SubtractInstruction : AdvancedInstruction {
    override val id = "math_sub"
    override val displayName = "Subtract"
    override val description = "Pops two Numbers (a, b), pushes a - b."

    override fun execute(ctx: SpellContext, stack: SpellStack) =
        binaryNumberOp("Subtract", stack) { a, b -> a - b }
}

object MultiplyInstruction : AdvancedInstruction {
    override val id = "math_mul"
    override val displayName = "Multiply"
    override val description = "Pops two Numbers, pushes their product."

    override fun execute(ctx: SpellContext, stack: SpellStack) =
        binaryNumberOp("Multiply", stack) { a, b -> a * b }
}

object DivideInstruction : AdvancedInstruction {
    override val id = "math_div"
    override val displayName = "Divide"
    override val description = "Pops two Numbers (a, b), pushes a / b. Mishap on divide-by-zero."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val b = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val a = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        if (b.value == 0.0) return SpellResult.Mishap("Cannot divide by zero.")
        stack.push(Iota.NumberIota(a.value / b.value))
        return SpellResult.Success
    }
}

object AbsInstruction : AdvancedInstruction {
    override val id = "math_abs"
    override val displayName = "Absolute Value"
    override val description = "Pops a Number, pushes its absolute value."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val n = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        stack.push(Iota.NumberIota(abs(n.value)))
        return SpellResult.Success
    }
}
