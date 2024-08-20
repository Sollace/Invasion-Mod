package com.invasion.entity.ai.goal;

import com.invasion.entity.EntityIMLiving;
import com.invasion.entity.Leader;

import net.minecraft.entity.LivingEntity;

public class RallyBehindLeaderGoal<T extends LivingEntity> extends FollowEntityGoal<T> {
    private static final float DEFAULT_FOLLOW_DISTANCE = 5;

    private int rallyCooldown;

    public RallyBehindLeaderGoal(EntityIMLiving entity, Class<T> leader) {
        this(entity, leader, DEFAULT_FOLLOW_DISTANCE);
    }

    public RallyBehindLeaderGoal(EntityIMLiving entity, Class<T> leader, float followDistance) {
        super(entity, leader, followDistance);
    }

    @Override
    public boolean canStart() {
        if (rallyCooldown > 0) {
            rallyCooldown--;
        }
        return rallyCooldown <= 0 && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return rallyCooldown <= 0 && super.shouldContinue();
    }

    @Override
    public void tick() {
        super.tick();
        if (rallyCooldown > 0) {
            rallyCooldown--;
        }
        if (rallyCooldown <= 0 && getTarget() instanceof Leader leader && leader.isMartyr()) {
            rallyCooldown = 30;
        }
    }
}