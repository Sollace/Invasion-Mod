package invmod.common.entity.ai;

import java.util.EnumSet;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.Goal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class EntityAIMeleeAttack<T extends LivingEntity> extends net.minecraft.entity.ai.goal.Goal {
	protected final EntityIMLiving mob;
	private final Class<? extends T> targetClass;
	private float attackRange = 0.6F;
	private int attackDelay;
	private int nextAttack;

	public EntityAIMeleeAttack(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay) {
		this.mob = entity;
		this.targetClass = targetClass;
		this.attackDelay = attackDelay;
		setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
    public boolean canStart() {
		LivingEntity target = mob.getTarget();
		return target != null && target.isAlive() && mob.getAIGoal() == Goal.MELEE_TARGET
		        && mob.squaredDistanceTo(target) < (attackRange + mob.getWidth() + target.getWidth()) * 4
		        && target.getClass().isAssignableFrom(targetClass);
	}

	@Override
    public void tick() {
		LivingEntity target = mob.getTarget();
		if (canAttackEntity(target)) {
			attackEntity(target);
		}
		setAttackTime(getAttackTime() - 1);
	}

	public Class<? extends T> getTargetClass() {
		return this.targetClass;
	}

	protected void attackEntity(LivingEntity target) {
		mob.tryAttack(target);
		setAttackTime(getAttackDelay());
	}

	protected boolean canAttackEntity(LivingEntity target) {
		return getAttackTime() <= 0 && mob.squaredDistanceTo(target.getPos()) < MathHelper.square(mob.getWidth() + attackRange);
	}

	protected int getAttackTime() {
		return nextAttack;
	}

	protected void setAttackTime(int time) {
		nextAttack = time;
	}

	protected int getAttackDelay() {
		return attackDelay;
	}

	protected void setAttackDelay(int time) {
		attackDelay = time;
	}
}