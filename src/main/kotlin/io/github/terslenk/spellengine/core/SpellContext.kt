package io.github.terslenk.spellengine.core

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Shared context passed to every spell instruction during execution.
 *
 * [caster]       — the player who cast the spell.
 * [castLocation] — where the spell originated (caster's eye by default).
 * [targets]      — resolved by the Shape; populated before effects run (Basic only).
 */
data class SpellContext(
    val caster: Player,
    val castLocation: Location = caster.eyeLocation,
    val targets: MutableList<Entity> = mutableListOf()
) {
    val world get() = caster.world
}