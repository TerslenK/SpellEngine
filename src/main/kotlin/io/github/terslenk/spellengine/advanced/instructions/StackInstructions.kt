package io.github.terslenk.spellengine.advanced.instructions

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.SpellStack
import io.github.terslenk.spellengine.advanced.toMishapOrNull
import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult

// â”€â”€â”€ Push Literal Number â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Pushes a hard-coded number onto the stack.
 * Each item in the GUI with this instruction carries its own [value].
 */
data class PushNumberInstruction(val value: Double) : AdvancedInstruction {
    override val id = "stack_push_number_${value.toInt()}"
    override val displayName = "Push Number ($value)"
    override val description = "Pushes the number $value onto the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.push(Iota.NumberIota(value))
        return SpellResult.Success
    }
}

// â”€â”€â”€ Duplicate top â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

object DupInstruction : AdvancedInstruction {
    override val id = "stack_dup"
    override val displayName = "Duplicate"
    override val description = "Duplicates the top value on the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val top = stack.peek()
            ?: return SpellResult.Mishap("Cannot duplicate â€” the stack is empty.")
        stack.push(top)
        return SpellResult.Success
    }
}

// â”€â”€â”€ Drop top â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

object DropInstruction : AdvancedInstruction {
    override val id = "stack_drop"
    override val displayName = "Drop"
    override val description = "Discards the top value on the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.pop().toMishapOrNull()?.let { return it }
        return SpellResult.Success
    }
}

// â”€â”€â”€ Swap top two â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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


// ¦¦¦ Filter Items ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

object FilterItemsInstruction : AdvancedInstruction {
    override val id = "stack_filter_items"
    override val displayName = "Filter Items"
    override val description = "Pops a List. Pushes a new List containing only the item entities."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val list = stack.popList().getOrElse { return SpellResult.Mishap(it.message!!) }
        val filtered = list.items.filter { it is Iota.EntityIota && it.entity is org.bukkit.entity.Item }
        stack.push(Iota.ListIota(filtered))
        return SpellResult.Success
    }
}

// ¦¦¦ Filter Mobs ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

object FilterMobsInstruction : AdvancedInstruction {
    override val id = "stack_filter_mobs"
    override val displayName = "Filter Mobs"
    override val description = "Pops a List. Pushes a new List containing only the living entities."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val list = stack.popList().getOrElse { return SpellResult.Mishap(it.message!!) }
        val filtered = list.items.filter { it is Iota.EntityIota && it.entity is org.bukkit.entity.LivingEntity }
        stack.push(Iota.ListIota(filtered))
        return SpellResult.Success
    }
}

// ¦¦¦ Print Stack ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

object PrintStackInstruction : AdvancedInstruction {
    override val id = "stack_print"
    override val displayName = "Print Stack"
    override val description = "Prints the current state of the stack to your chat. Useful for debugging!"

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        ctx.caster.sendMessage(net.kyori.adventure.text.Component.text("Stack: $stack", net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE))
        return SpellResult.Success
    }
}
