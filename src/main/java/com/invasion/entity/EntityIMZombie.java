package com.invasion.entity;

import java.util.List;

import com.invasion.InvSounds;
import com.invasion.InvasionMod;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIKillEntity;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAISprint;
import com.invasion.entity.ai.goal.EntityAIStoop;
import com.invasion.entity.ai.goal.EntityAITargetOnNoNexusPath;
import com.invasion.entity.ai.goal.EntityAITargetRetaliate;
import com.invasion.entity.ai.goal.EntityAIWaitForEngy;
import com.invasion.entity.ai.goal.EntityAIWanderIM;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.INexusAccess;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityIMZombie extends AbstractIMZombieEntity {

    public EntityIMZombie(EntityType<EntityIMZombie> type, World world) {
        this(type, world, null);

    }

    public EntityIMZombie(EntityType<EntityIMZombie> type, World world, INexusAccess nexus) {
        super(type, world, nexus, 2F);
    }

    @Override
    protected void initGoals() {
        // added EntityAISwimming and increased all other tasks order numbers with 1
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
        goalSelector.add(1, new EntityAIKillEntity<>(this, ServerPlayerEntity.class, 40));
        goalSelector.add(1, new EntityAIKillEntity<>(this, IronGolemEntity.class, 30));
        goalSelector.add(2, new EntityAIAttackNexus(this));
        goalSelector.add(3, new EntityAIWaitForEngy(this, 4.0F, true));
        goalSelector.add(4, new EntityAIKillEntity<>(this, AnimalEntity.class, 40));
        goalSelector.add(5, new EntityAIGoToNexus(this));
        goalSelector.add(6, new EntityAIWanderIM(this));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(8, new LookAtEntityGoal(this, EntityIMCreeper.class, 12.0F));
        goalSelector.add(8, new LookAroundGoal(this));

        targetSelector.add(0, new EntityAITargetRetaliate<>(this, LivingEntity.class, getAggroRange()));
        targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, getAggroRange(), true));
        targetSelector.add(5, new RevengeGoal(this));

        if (getTier() == 3) {
            goalSelector.add(4, new EntityAIStoop(this));
            goalSelector.add(3, new EntityAISprint(this));
        } else {
            // track players from sensing them
            targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, getSenseRange(), false));
            targetSelector.add(3, new EntityAITargetOnNoNexusPath<>(this, EntityIMPigEngy.class, 3.5F));
        }
    }

    @Override
    public void onSpawned(INexusAccess nexus, EntityConstruct spawnConditions) {
        super.onSpawned(nexus, spawnConditions);
        setTexture(spawnConditions.texture());
        setFlavour(spawnConditions.flavour());
        setTier(spawnConditions.tier());
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && flammability >= 20 && isOnFire()) {
            doFireball();
        }
    }

    @Override
    protected void initTieredAttributes() {
        setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
        if (getTier() == 1) {
            if (getFlavour() == 0) {
                setName("Zombie");
                setMovementSpeed(0.19F);
                setAttackStrength(4);
                selfDamage = 3;
                maxSelfDamage = 6;
                flammability = 3;
                setCanDestroyBlocks(true);
            } else if (getFlavour() == 1) {
                setName("Zombie");
                setMovementSpeed(0.19F);
                setAttackStrength(6);
                selfDamage = 3;
                maxSelfDamage = 6;
                flammability = 3;
                setStackInHand(Hand.MAIN_HAND, Items.WOODEN_SWORD.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.2F);
                setCanDestroyBlocks(false);
            }
        } else if (getTier() == 2) {
            if (getFlavour() == 0) {
                setName("Zombie");
                setMovementSpeed(0.19F);
                setAttackStrength(7);
                selfDamage = 4;
                maxSelfDamage = 12;
                flammability = 4;
                equipStack(EquipmentSlot.CHEST, Items.IRON_CHESTPLATE.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.CHEST, 0.25F);
                setCanDestroyBlocks(true);
            } else if (getFlavour() == 1) {
                setName("Zombie");
                setMovementSpeed(0.19F);
                setAttackStrength(10);
                selfDamage = 3;
                maxSelfDamage = 9;
                setStackInHand(Hand.MAIN_HAND, Items.IRON_SWORD.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.25F);
                setCanDestroyBlocks(false);
            } else if (getFlavour() == 2) {
                setName("Tar Zombie");
                setMovementSpeed(0.19F);
                setAttackStrength(5);
                selfDamage = 3;
                maxSelfDamage = 9;
                flammability = 30;
                floatsInWater = false;
                setCanDestroyBlocks(true);
            } else if (getFlavour() == 3) {
                setName("Zombie Pigman");
                setMovementSpeed(0.25F);
                setAttackStrength(8);
                setFireImmune(true);
                setStackInHand(Hand.MAIN_HAND, Items.GOLDEN_SWORD.getDefaultStack());
                setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.2F);
                setCanDestroyBlocks(true);
            }
        } else if (getTier() == 3) {
            if (getFlavour() == 0) {
                setName("Zombie Brute");
                setMovementSpeed(0.17F);
                setAttackStrength(18);
                selfDamage = 4;
                maxSelfDamage = 20;
                flammability = 4;
                equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
                setCanDestroyBlocks(true);
            }
        }

        if (getTier() == 1) {
            int r = random.nextInt(2);
            if (r == 0)
                setTexture(0);
            else if (r == 1)
                setTexture(1);
        } else if (getTier() == 2) {
            if (getFlavour() == 2) {
                setTexture(5);
            } else if (getFlavour() == 3) {
                setTexture(3);
            } else {
                int r = random.nextInt(2);
                if (r == 0)
                    setTexture(2);
                else if (r == 1)
                    setTexture(4);
            }
        } else if (getTier() == 3) {
            setTexture(6);
        }
    }

    @Override
    public boolean isBigRenderTempHack() {
        return getFlavour() == 3;
    }

    @Override
    protected void sunlightDamageTick() {
        if (getTier() == 2 && getFlavour() == 2) {
            damage(getDamageSources().generic(), 3);
        } else {
            super.sunlightDamageTick();
        }
    }

    @Override
    public void updateAnimation(boolean override) {
        if (!getWorld().isClient && (terrainModifier.isBusy() || override)) {
            setSwinging(true);
        }
        int swingSpeed = getSwingSpeed();
        if (isSwinging()) {
            if (++swingTimer >= swingSpeed) {
                swingTimer = 0;
                setSwinging(false);
            }
        } else {
            swingTimer = 0;
        }
        handSwingProgress = (float) swingTimer / (float) swingSpeed;
    }

    @Override
    protected void updateSound() {
        if (terrainModifier.isBusy()) {
            super.updateSound();
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        if (super.getTier() == 3) {
            return getRandom().nextInt(3) == 0 ? InvSounds.ENTITY_BIG_ZOMBIE_AMBIENT : null;
        }

        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    private void doFireball() {
        for (BlockPos pos : BlockPos.iterateOutwards(getBlockPos(), 2, 2, 2)) {
            if (getWorld().isAir(pos) && getWorld().getBlockState(pos.down()).isBurnable()) {
                getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        List<Entity> entities = getWorld().getOtherEntities(this, getBoundingBox().expand(1.5, 1.5, 1.5));
        for (int el = entities.size() - 1; el >= 0; el--) {
            entities.get(el).setFireTicks(8);
        }
        damage(getDamageSources().explosion(this, this), 500);
    }
}
