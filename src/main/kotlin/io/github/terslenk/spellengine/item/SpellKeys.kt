package io.github.terslenk.spellengine.item

import org.bukkit.NamespacedKey

/**
 * PDC keys used by SpellEngine items.
 */
object SpellKeys {

    /**
     * Stores the comma-separated list of spell component / instruction IDs on an item.
     */
    @JvmField
    val SPELL_IDS = NamespacedKey("spellengine", "spell_ids")

    /**
     * Tag placed on magic projectiles so the listener can identify them.
     * Value is the caster's UUID string.
     */
    @JvmField
    val MAGIC_PROJECTILE = NamespacedKey("spellengine", "magic_projectile")
}
