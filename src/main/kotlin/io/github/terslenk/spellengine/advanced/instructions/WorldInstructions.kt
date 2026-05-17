package io.github.terslenk.spellengine.advanced.instructions

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.SpellStack
import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import org.bukkit.util.Vector

// ─── Get Caster Look Direction ────────────────────────────────────────────────

/**
 * Pushes the caster's look direction as a normalised VectorIota.
 */
object GetLookDirectionInstruction : AdvancedInstruction {
    override val id = "world_get_look_dir"
    override val displayName = "Get Look Direction"
    override val description = "Pushes the direction you're looking as a Vector."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.push(Iota.VectorIota(ctx.caster.eyeLocation.direction.clone().normalize()))
        return SpellResult.Success
    }
}

// ─── Offset Vector ────────────────────────────────────────────────────────────

/**
 * Pops: Vector (base), Vector (offset) → pushes base + offset.
 */
object OffsetVectorInstruction : AdvancedInstruction {
    override val id = "world_offset_vector"
    override val displayName = "Offset Vector"
    override val description = "Pops two Vectors (base, offset), pushes their sum."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val offset = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }
        val base   = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }
        stack.push(Iota.VectorIota(base.value.clone().add(offset.value)))
        return SpellResult.Success
    }
}

// ─── Scale Vector ─────────────────────────────────────────────────────────────

/**
 * Pops: Vector, Number (scale) → pushes Vector * scale.
 */
object ScaleVectorInstruction : AdvancedInstruction {
    override val id = "world_scale_vector"
    override val displayName = "Scale Vector"
    override val description = "Pops a Vector and a Number, pushes the Vector multiplied by the Number."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val scale  = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val vector = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }
        stack.push(Iota.VectorIota(vector.value.clone().multiply(scale.value)))
        return SpellResult.Success
    }
}

// ─── Push Constant Vector ─────────────────────────────────────────────────────

/**
 * Pushes a hard-coded directional vector (e.g. UP, NORTH).
 */
enum class CardinalDirection(val vector: Vector) {
    UP(Vector(0, 1, 0)),
    DOWN(Vector(0, -1, 0)),
    NORTH(Vector(0, 0, -1)),
    SOUTH(Vector(0, 0, 1)),
    EAST(Vector(1, 0, 0)),
    WEST(Vector(-1, 0, 0))
}

data class PushVectorInstruction(val direction: CardinalDirection) : AdvancedInstruction {
    override val id = "world_push_vector_${direction.name.lowercase()}"
    override val displayName = "Push ${direction.name.lowercase().replaceFirstChar { it.uppercase() }}"
    override val description = "Pushes the ${direction.name} direction vector onto the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.push(Iota.VectorIota(direction.vector.clone()))
        return SpellResult.Success
    }
}

// ─── Explode ──────────────────────────────────────────────────────────────────

/**
 * Pops: Vector (position), Number (power, clamped 0–4)
 * Creates an explosion at the given position.
 */
object ExplodeInstruction : AdvancedInstruction {
    override val id = "world_explode"
    override val displayName = "Explode"
    override val description = "Pops a Vector and a Number (power 0–4), creates an explosion."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val power    = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val position = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }

        val clampedPower = power.value.coerceIn(0.0, 4.0).toFloat()
        val location = position.value.toLocation(ctx.world)

        ctx.world.createExplosion(location, clampedPower, false, false, ctx.caster)
        return SpellResult.Success
    }
}
