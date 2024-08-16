package com.invasion.entity.ai.goal;

import com.invasion.entity.AbstractIMZombieEntity;

public class EntityAISprinpigman extends EntityAISprint {
    public EntityAISprinpigman(AbstractIMZombieEntity entity) {
        super(entity);
    }

    @Override
    protected void startSprint() {
        ((AbstractIMZombieEntity) theEntity).updateAnimation(true);
        super.startSprint();
    }

    @Override
    protected void endSprint() {
        ((AbstractIMZombieEntity) theEntity).updateAnimation(true);
        super.endSprint();
    }
}