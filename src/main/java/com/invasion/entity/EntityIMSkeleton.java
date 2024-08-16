package com.invasion.entity;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAIWanderIM;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class EntityIMSkeleton extends EntityIMMob implements RangedAttackMob {
    public EntityIMSkeleton(EntityType<EntityIMSkeleton> type, World world) {
        super(type, world, null);
    }

    public static DefaultAttributeContainer.Builder createIMSkeletonAttributes() {
        return SkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.21);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new BowAttackGoal<>(this, 65D, 20, 16F));
        // goalSelector.add(1, new EntityAIRallyBehindEntity(this, EntityIMCreeper.class, 4.0F));
        goalSelector.add(3, new EntityAIAttackNexus(this));
        goalSelector.add(4, new EntityAIGoToNexus(this));
        goalSelector.add(5, new EntityAIWanderIM(this));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(6, new LookAroundGoal(this));
        goalSelector.add(6, new LookAtEntityGoal(this, EntityIMCreeper.class, 12));

        targetSelector.add(0, new EntityAISimpleTarget<>(this, PlayerEntity.class, getSenseRange(), false));
        targetSelector.add(1, new RevengeGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SKELETON_DEATH;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack bow = getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        ItemStack arrow = getProjectileType(bow);
        PersistentProjectileEntity projectile = createArrowProjectile(arrow, pullProgress, bow);
        double dX = target.getX() - getX();
        double dY = target.getBodyY(0.3333333333333333) - projectile.getY();
        double dZ = target.getZ() - getZ();
        double horLength = Math.sqrt(dX * dX + dZ * dZ);
        projectile.setVelocity(dX, dY + horLength * 0.2F, dZ, 1.6F, 14 - getWorld().getDifficulty().getId() * 4);
        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1, 1 / (getRandom().nextFloat() * 0.4F + 0.8F));
        getWorld().spawnEntity(projectile);
    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier, shotFrom);
    }
}