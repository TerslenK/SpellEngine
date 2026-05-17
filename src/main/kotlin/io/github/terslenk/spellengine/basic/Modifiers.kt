package io.github.terslenk.spellengine.basic

// ¦¦¦ Amplify ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

/**
 * Multiplies the spell's power (damage / heal amount).
 * Stack multiple to keep increasing power.
 */
object AmplifyModifier : Modifier {
    override val id = "mod_amplify"
    override val displayName = "Amplify"

    override fun modify(params: BasicSpellParams) =
        params.copy(power = params.power * 1.5)
}

// ¦¦¦ AoE ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

/**
 * Enables area-of-effect splash around each resolved target.
 * The [BasicSpellExecutor] reads [BasicSpellParams.aoe] after modifiers run.
 */
object AoeModifier : Modifier {
    override val id = "mod_aoe"
    override val displayName = "Area of Effect"

    override fun modify(params: BasicSpellParams) =
        params.copy(aoe = true)
}

// ¦¦¦ Extend ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

/**
 * Doubles the duration of time-based effects (e.g. Ignite).
 */
object ExtendModifier : Modifier {
    override val id = "mod_extend"
    override val displayName = "Extend"

    override fun modify(params: BasicSpellParams) =
        params.copy(duration = params.duration * 2)
}

// ¦¦¦ Pierce ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

/**
 * Marks a projectile-shape spell to pass through entities.
 * Handled in [io.github.terslenk.spellengine.core.registry.ProjectileSpellListener].
 */
object PierceModifier : Modifier {
    override val id = "mod_pierce"
    override val displayName = "Pierce"

    override fun modify(params: BasicSpellParams) =
        params.copy(pierce = true)
}

// ¦¦¦ Filter Items ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

object ItemFilterModifier : Modifier {
    override val id = "mod_filter_items"
    override val displayName = "Target Items"

    override fun modify(params: BasicSpellParams): BasicSpellParams {
        return params.copy(targetType = "ITEM")
    }
}

// ¦¦¦ Filter Mobs ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦

object MobFilterModifier : Modifier {
    override val id = "mod_filter_mobs"
    override val displayName = "Target Mobs"

    override fun modify(params: BasicSpellParams): BasicSpellParams {
        return params.copy(targetType = "LIVING")
    }
}
