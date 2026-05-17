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
}