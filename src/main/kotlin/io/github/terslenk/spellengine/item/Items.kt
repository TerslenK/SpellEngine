package io.github.terslenk.spellengine.item

import io.github.terslenk.spellengine.SpellEngine
import xyz.xenondevs.nova.initialize.Init
import xyz.xenondevs.nova.initialize.InitStage

/**
 * Registers all custom items provided by SpellEngine.
 *
 * Nova loads this object during [InitStage.PRE_PACK],
 * causing each field to be initialised and the items to be registered.
 */
@Init(stage = InitStage.PRE_PACK)
object Items {

    val BASIC_WAND = SpellEngine.registerItem("basic_wand", WandBehavior)

    val ADVANCED_GRIMOIRE = SpellEngine.registerItem("advanced_grimoire", GrimoireBehavior)
}
