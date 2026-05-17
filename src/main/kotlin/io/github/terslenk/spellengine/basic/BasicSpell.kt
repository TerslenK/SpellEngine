package io.github.terslenk.spellengine.basic

/**
 * Represents a fully composed Basic (Ars-style) spell.
 *
 * Structure: one [Shape] -> one [Effect] -> zero or more [Modifier]s
 *
 * Serialised to PDC by Nova as a list of component IDs.
 * Deserialised via [io.github.terslenk.spellengine.core.registry.SpellRegistry].
 */
data class BasicSpell(
    val shape: Shape,
    val effect: Effect,
    val modifiers: List<Modifier> = emptyList()
)
