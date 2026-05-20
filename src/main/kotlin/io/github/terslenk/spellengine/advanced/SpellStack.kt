package io.github.terslenk.spellengine.advanced

import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellResult

/**
 * The stack used by the Advanced Grimoire's execution engine.
 *
 * All pop operations return a [SpellResult.Mishap] on failure instead of
 * throwing exceptions, so errors surface cleanly to the player.
 */
class SpellStack {

    private val items = ArrayDeque<Iota>()

    val size get() = items.size
    val isEmpty get() = items.isEmpty()

    fun push(iota: Iota) = items.addLast(iota)

    fun peek(): Iota? = items.lastOrNull()

    /** Pop any Iota — fails only if the stack is empty. */
    fun pop(): Result<Iota> {
        if (items.isEmpty()) return Result.failure(StackUnderflowException("Stack is empty — nothing to pop."))
        return Result.success(items.removeLast())
    }

    /** Pop and assert a specific type, with a clear mishap message on mismatch. */
    inline fun <reified T : Iota> popTyped(expectedName: String): Result<T> {
        val result = pop().getOrElse { return Result.failure(it) }
        if (result !is T) {
            return Result.failure(
                TypeMismatchException(
                    "Expected $expectedName on the stack, but found ${result.typeName()}."
                )
            )
        }
        return Result.success(result)
    }

    fun popNumber()  = popTyped<Iota.NumberIota>("Number")
    fun popVector()  = popTyped<Iota.VectorIota>("Vector")
    fun popEntity()  = popTyped<Iota.EntityIota>("Entity")
    fun popBool()    = popTyped<Iota.BoolIota>("Boolean")
    fun popList()    = popTyped<Iota.ListIota>("List")

    override fun toString() = items.joinToString(prefix = "[", postfix = "]") { it.asString() }
}

// ─── Exception types (converted to SpellResult.Mishap by the executor) ────────

class StackUnderflowException(message: String) : Exception(message)
class TypeMismatchException(message: String) : Exception(message)

// ─── Extension to convert a Result<T> failure into SpellResult.Mishap ─────────

fun <T> Result<T>.toMishapOrNull(): SpellResult.Mishap? {
    return exceptionOrNull()?.let { SpellResult.Mishap(it.message ?: "Unknown stack error.") }
}
