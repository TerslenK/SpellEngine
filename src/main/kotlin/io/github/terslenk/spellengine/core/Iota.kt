package io.github.terslenk.spellengine.core

import org.bukkit.entity.Entity
import org.bukkit.util.Vector

/**
 * Iota — the type system for values on the spell stack.
 * Used by the Advanced Grimoire's stack-based engine.
 */
sealed class Iota {

    data class NumberIota(val value: Double) : Iota()
    data class VectorIota(val value: Vector) : Iota()
    data class EntityIota(val entity: Entity) : Iota()
    data class BoolIota(val value: Boolean) : Iota()
    data class ListIota(val items: List<Iota>) : Iota()
    data object NullIota : Iota()

    fun typeName(): String = when (this) {
        is NumberIota -> "Number"
        is VectorIota -> "Vector"
        is EntityIota -> "Entity"
        is BoolIota   -> "Boolean"
        is ListIota   -> "List"
        is NullIota   -> "Null"
    }

    fun asString(): String = when (this) {
        is NumberIota -> if (value % 1.0 == 0.0) value.toLong().toString() else String.format(java.util.Locale.US, "%.2f", value)
        is VectorIota -> "(${String.format(java.util.Locale.US, "%.2f", value.x)}, ${String.format(java.util.Locale.US, "%.2f", value.y)}, ${String.format(java.util.Locale.US, "%.2f", value.z)})"
        is EntityIota -> entity.name
        is BoolIota   -> value.toString()
        is ListIota   -> items.joinToString(prefix = "[", postfix = "]") { it.asString() }
        is NullIota   -> "Null"
    }
}