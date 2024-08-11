package invmod.common.entity.ai;

import org.jetbrains.annotations.Nullable;

//NOOB HAUS: Done

import invmod.common.entity.EntityIMCreeper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityAICreeperIMSwell extends Goal {
    private final EntityIMCreeper theEntity;

    @Nullable
    private LivingEntity targetEntity;

    public EntityAICreeperIMSwell(EntityIMCreeper creeper) {
        theEntity = creeper;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = theEntity.getTarget();
        return theEntity.getCreeperState() > 0 || (target != null
                    && theEntity.squaredDistanceTo(target) < 9
                    && (target instanceof PlayerEntity || target instanceof WolfEntity));
    }

    @Override
    public void start() {
        theEntity.getNavigatorNew().clearPath();
        targetEntity = theEntity.getTarget();
    }

    @Override
    public void stop() {
        targetEntity = null;
    }

    @Override
    public void tick() {
        theEntity.setCreeperState(targetEntity != null
            && theEntity.squaredDistanceTo(targetEntity) <= 49
            && theEntity.getVisibilityCache().canSee(targetEntity) ? 1 : -1);
    }
}