package io.github.terslenk.spellengine.core.registry

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.AdvancedSpell
import io.github.terslenk.spellengine.advanced.instructions.*
import io.github.terslenk.spellengine.basic.*
import xyz.xenondevs.nova.initialize.Init
import xyz.xenondevs.nova.initialize.InitFun
import xyz.xenondevs.nova.initialize.InitStage

/**
 * Central registry for all spell components.
 *
 * Initialised automatically by Nova during [InitStage.PRE_PACK] via [@Init][Init].
 * Use [SpellRegistry.deserializeBasic] and [SpellRegistry.deserializeAdvanced]
 * to rebuild spells from the ID lists stored in Nova NBT / PDC.
 */
@Init(stage = InitStage.PRE_PACK)
object SpellRegistry {

    // -- Basic components -----------------------------------------------------

    private val shapes     = mutableMapOf<String, Shape>()
    private val effects    = mutableMapOf<String, Effect>()
    private val modifiers  = mutableMapOf<String, Modifier>()

    // -- Advanced instructions ------------------------------------------------

    private val instructions = mutableMapOf<String, AdvancedInstruction>()

    // -- Registration ---------------------------------------------------------

    fun registerShape(shape: Shape)             { shapes[shape.id] = shape }
    fun registerEffect(effect: Effect)           { effects[effect.id] = effect }
    fun registerModifier(modifier: Modifier)     { modifiers[modifier.id] = modifier }
    fun registerInstruction(i: AdvancedInstruction) { instructions[i.id] = i }

    // -- Lookups --------------------------------------------------------------

    fun getShape(id: String)       = shapes[id]
    fun getEffect(id: String)      = effects[id]
    fun getModifier(id: String)    = modifiers[id]
    fun getInstruction(id: String) = instructions[id]

    fun allShapes()       = shapes.values.toList()
    fun allEffects()      = effects.values.toList()
    fun allModifiers()    = modifiers.values.toList()
    fun allInstructions() = instructions.values.toList()

    // -- Deserialization ------------------------------------------------------

    /**
     * Rebuild a [BasicSpell] from a list of IDs stored in NBT / PDC.
     * Format: [shapeId, effectId, modifierId, modifierId, ...]
     *
     * Returns null if the shape or effect ID is missing/unregistered.
     */
    fun deserializeBasic(ids: List<String>): BasicSpell? {
        if (ids.size < 2) return null

        val shape  = shapes[ids[0]]  ?: return null
        val effect = effects[ids[1]] ?: return null
        val mods   = ids.drop(2).mapNotNull { modifiers[it] }

        return BasicSpell(shape, effect, mods)
    }

    /**
     * Rebuild an [AdvancedSpell] from a list of instruction IDs stored in NBT / PDC.
     * Unknown IDs are silently skipped (so old spells don't crash on reload).
     */
    fun deserializeAdvanced(ids: List<String>): AdvancedSpell {
        return AdvancedSpell(ids.mapNotNull { instructions[it] })
    }

    /**
     * Serialize a [BasicSpell] back to a list of IDs for NBT / PDC storage.
     */
    fun serializeBasic(spell: BasicSpell): List<String> =
        buildList {
            add(spell.shape.id)
            add(spell.effect.id)
            addAll(spell.modifiers.map { it.id })
        }

    /**
     * Serialize an [AdvancedSpell] back to a list of IDs for NBT / PDC storage.
     */
    fun serializeAdvanced(spell: AdvancedSpell): List<String> =
        spell.instructions.map { it.id }

    // -- Init -----------------------------------------------------------------

    /**
     * Register all built-in components. Called automatically by Nova.
     */
    @InitFun
    private fun init() {
        // Basic -- Shapes
        registerShape(SelfShape)
        registerShape(RayShape)
        registerShape(AreaShape)
        registerShape(ProjectileShape)

        // Basic -- Effects
        registerEffect(DamageEffect)
        registerEffect(HealEffect)
        registerEffect(IgniteEffect)
        registerEffect(TeleportEffect)
        registerEffect(PushEffect)

        // Basic -- Modifiers
        registerModifier(AmplifyModifier)
        registerModifier(AoeModifier)
                registerModifier(ExtendModifier)
        registerModifier(PierceModifier)
        registerModifier(ItemFilterModifier)
        registerModifier(MobFilterModifier)

                // Advanced -- Stack
        registerInstruction(DropInstruction)
        registerInstruction(DupInstruction)
        registerInstruction(SwapInstruction)
        registerInstruction(FilterItemsInstruction)
        registerInstruction(FilterMobsInstruction)
        registerInstruction(PrintStackInstruction)

        // Advanced -- Math
        registerInstruction(AddInstruction)
        registerInstruction(SubtractInstruction)
        registerInstruction(MultiplyInstruction)
        registerInstruction(DivideInstruction)
                registerInstruction(AbsInstruction)

        listOf(1.0, 2.0, 3.0, 4.0, 5.0, 10.0).forEach { registerInstruction(PushNumberInstruction(it)) }

        // Advanced -- Entity
        registerInstruction(GetCasterInstruction)
        registerInstruction(GetEntityPosInstruction)
        registerInstruction(GetEntityEyePosInstruction)
        registerInstruction(GetLookedAtEntityInstruction)
                registerInstruction(DamageEntityInstruction)
        registerInstruction(HealEntityInstruction)
        registerInstruction(TeleportEntityInstruction)
        registerInstruction(GetEntitiesInRadiusInstruction)
        registerInstruction(IgniteEntityInstruction)
                
        // Advanced -- World
        registerInstruction(GetLookDirectionInstruction)
        registerInstruction(OffsetVectorInstruction)
        registerInstruction(ScaleVectorInstruction)
        registerInstruction(ConstructVectorInstruction)
        registerInstruction(GetLookedAtBlockInstruction)
        registerInstruction(ExplodeInstruction)
        registerInstruction(RaycastBlockInstruction)
        registerInstruction(BreakBlockInstruction)
            }
}











