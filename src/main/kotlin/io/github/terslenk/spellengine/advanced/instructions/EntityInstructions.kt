package io.github.terslenk.spellengine.advanced.instructions

import io.github.terslenk.spellengine.advanced.AdvancedInstruction
import io.github.terslenk.spellengine.advanced.SpellStack
import io.github.terslenk.spellengine.core.Iota
import io.github.terslenk.spellengine.core.SpellContext
import io.github.terslenk.spellengine.core.SpellResult
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

// ─── Get Caster ───────────────────────────────────────────────────────────────

/**
 * Pushes the caster (Player) as an EntityIota onto the stack.
 */
object GetCasterInstruction : AdvancedInstruction {
    override val id = "entity_get_caster"
    override val displayName = "Get Caster"
    override val description = "Pushes YOU (the caster) onto the stack as an Entity."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        stack.push(Iota.EntityIota(ctx.caster))
        return SpellResult.Success
    }
}

// ─── Get Entity Position ──────────────────────────────────────────────────────

/**
 * Pops an Entity, pushes its location as a VectorIota.
 */
object GetEntityPosInstruction : AdvancedInstruction {
    override val id = "entity_get_pos"
    override val displayName = "Get Entity Position"
    override val description = "Pops an Entity, pushes its location as a Vector."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }
        stack.push(Iota.VectorIota(entity.entity.location.toVector()))
        return SpellResult.Success
    }
}

// ─── Get Ray-traced Entity ────────────────────────────────────────────────────

/**
 * Pushes the first living entity in the caster's line of sight (up to 20 blocks).
 */
object GetLookedAtEntityInstruction : AdvancedInstruction {
    override val id = "entity_get_looked_at"
    override val displayName = "Get Looked-At Entity"
    override val description = "Pushes the entity you're looking at onto the stack."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val target = ctx.caster.getTargetEntity(100, false)
            ?: return SpellResult.Mishap("No entity in your line of sight within 100 blocks.")

        val hit = target

        stack.push(Iota.EntityIota(hit))
        return SpellResult.Success
    }
}

// ─── Damage Entity ────────────────────────────────────────────────────────────

/**
 * Pops: Entity, Number (damage)
 * Deals [damage] to the entity.
 */
object DamageEntityInstruction : AdvancedInstruction {
    override val id = "entity_damage"
    override val displayName = "Damage Entity"
    override val description = "Pops an Entity and a Number, deals that much damage."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val damage = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }

        val living = entity.entity as? LivingEntity ?: return SpellResult.Success

        if (damage.value <= 0) {
            return SpellResult.Mishap("Damage amount must be greater than zero (got ${damage.value}).")
        }

        living.damage(damage.value, ctx.caster)
        return SpellResult.Success
    }
}

// ─── Heal Entity ──────────────────────────────────────────────────────────────

/**
 * Pops: Entity, Number (heal amount)
 */
object HealEntityInstruction : AdvancedInstruction {
    override val id = "entity_heal"
    override val displayName = "Heal Entity"
    override val description = "Pops an Entity and a Number, heals that much health."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val amount = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val entity = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }

        val living = entity.entity as? LivingEntity ?: return SpellResult.Success

        if (amount.value <= 0) {
            return SpellResult.Mishap("Heal amount must be greater than zero (got ${amount.value}).")
        }

        living.health = (living.health + amount.value).coerceAtMost(living.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0)
        return SpellResult.Success
    }
}

// ─── Teleport Entity ──────────────────────────────────────────────────────────

/**
 * Pops: Entity, Vector (destination)
 * Teleports the entity to that position.
 */
object TeleportEntityInstruction : AdvancedInstruction {
    override val id = "entity_teleport"
    override val displayName = "Teleport Entity"
    override val description = "Pops an Entity and a Vector, teleports the entity there."

                override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val dest = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }
        val targetIota = stack.pop().getOrElse { return SpellResult.Mishap(it.message!!) }

        val targets = mutableListOf<org.bukkit.entity.Entity>()
        if (targetIota is Iota.EntityIota) {
            targets.add(targetIota.entity)
        } else if (targetIota is Iota.ListIota) {
            targetIota.items.forEach { if (it is Iota.EntityIota) targets.add(it.entity) }
        } else {
            return SpellResult.Mishap("Expected Entity or List of Entities, got ${targetIota.typeName()}")
        }

        for (target in targets) {
            val startLoc = target.location.clone().add(0.0, 0.2, 0.0)
            val endVec = dest.value.clone().add(org.bukkit.util.Vector(0.0, 0.2, 0.0))
            val direction = endVec.clone().subtract(startLoc.toVector())
            val distance = direction.length()
            
            var targetVec = dest.value.clone()
            if (distance > 0.05) {
                val rayTrace = ctx.world.rayTraceBlocks(
                    startLoc,
                    direction.normalize(),
                    distance,
                    org.bukkit.FluidCollisionMode.NEVER,
                    true
                )
                if (rayTrace != null) {
                    targetVec = rayTrace.hitPosition.subtract(org.bukkit.util.Vector(0.0, 0.2, 0.0))
                    if (rayTrace.hitBlockFace != null) {
                        targetVec.add(rayTrace.hitBlockFace!!.direction.multiply(0.1))
                    }
                }
            }

            val location = targetVec.toLocation(ctx.world)
            location.yaw = target.location.yaw
            location.pitch = target.location.pitch

            target.teleport(location)
        }
        return SpellResult.Success
    }
}

// ��� Get Entities In Radius ���������������������������������������������������

object GetEntitiesInRadiusInstruction : AdvancedInstruction {
    override val id = "entity_get_in_radius"
    override val displayName = "Get Nearby Entities"
    override val description = "Pops Vector(center), Number(radius). Pushes a List of all nearby Entities."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val radius = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val center = stack.popVector().getOrElse { return SpellResult.Mishap(it.message!!) }

        val entities = ctx.world.getNearbyEntities(center.value.toLocation(ctx.world), radius.value, radius.value, radius.value)
            .filter { it != ctx.caster }
            .map { Iota.EntityIota(it) }

        stack.push(Iota.ListIota(entities))
        return SpellResult.Success
    }
}

// ��� Ignite Entity ������������������������������������������������������������

object IgniteEntityInstruction : AdvancedInstruction {
    override val id = "entity_ignite"
    override val displayName = "Ignite Entity"
    override val description = "Pops an Entity (or List) and a Number (ticks). Ignites or smelts them."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val duration = stack.popNumber().getOrElse { return SpellResult.Mishap(it.message!!) }
        val targetIota = stack.pop().getOrElse { return SpellResult.Mishap(it.message!!) }

        val targets = mutableListOf<org.bukkit.entity.Entity>()
        if (targetIota is Iota.EntityIota) {
            targets.add(targetIota.entity)
        } else if (targetIota is Iota.ListIota) {
            targetIota.items.forEach { if (it is Iota.EntityIota) targets.add(it.entity) }
        } else {
            return SpellResult.Mishap("Expected Entity or List of Entities, got ${targetIota.typeName()}")
        }

        for (target in targets) {
            if (target is LivingEntity) {
                target.fireTicks = duration.value.toInt()
            } else if (target is org.bukkit.entity.Item) {
                val iter = org.bukkit.Bukkit.recipeIterator()
                var smelted = false
                while (iter.hasNext()) {
                    val recipe = iter.next()
                    if (recipe is org.bukkit.inventory.FurnaceRecipe) {
                        if (recipe.inputChoice.test(target.itemStack)) {
                            val result = recipe.result.clone()
                            result.amount = target.itemStack.amount
                            target.itemStack = result
                            smelted = true
                            break
                        }
                    }
                }
                if (smelted) {
                    target.world.spawnParticle(org.bukkit.Particle.FLAME, target.location, 10, 0.2, 0.2, 0.2, 0.05)
                } else {
                    target.world.spawnParticle(org.bukkit.Particle.LAVA, target.location, 10, 0.2, 0.2, 0.2, 0.05)
                    target.remove()
                }
            }
        }
        return SpellResult.Success
    }
}




// ��� Get Entity Eye Pos �������������������������������������������������������

object GetEntityEyePosInstruction : AdvancedInstruction {
    override val id = "entity_get_eye_pos"
    override val displayName = "Get Entity Eye Pos"
    override val description = "Pops an Entity. Pushes a Vector representing its eye location (or center if not living)."

    override fun execute(ctx: SpellContext, stack: SpellStack): SpellResult {
        val target = stack.popEntity().getOrElse { return SpellResult.Mishap(it.message!!) }
        
        val vec = if (target.entity is LivingEntity) {
            target.entity.eyeLocation.toVector()
        } else {
            target.entity.location.toVector().add(org.bukkit.util.Vector(0.0, target.entity.height / 2.0, 0.0))
        }
        
        stack.push(Iota.VectorIota(vec))
        return SpellResult.Success
    }
}

