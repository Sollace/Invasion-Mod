package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class EntityAIWatchTarget extends Goal {
    private final MobEntity theEntity;

    public EntityAIWatchTarget(MobEntity entity) {
        this.theEntity = entity;
        setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return theEntity.getTarget() != null;
    }

    @Override
    public void tick() {
        theEntity.getLookControl().lookAt(theEntity.getTarget(), 2, 2);
    }
}