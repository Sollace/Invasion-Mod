package com.invasion.entity;

import com.invasion.InvasionMod;
import com.invasion.entity.ai.IMMoveHelperSpider;
import com.invasion.entity.ai.goal.EntityAIAttackNexus;
import com.invasion.entity.ai.goal.EntityAIGoToNexus;
import com.invasion.entity.ai.goal.EntityAIKillEntity;
import com.invasion.entity.ai.goal.EntityAILayEgg;
import com.invasion.entity.ai.goal.EntityAIPounce;
import com.invasion.entity.ai.goal.EntityAIRallyBehindEntity;
import com.invasion.entity.ai.goal.EntityAISimpleTarget;
import com.invasion.entity.ai.goal.EntityAITargetOnNoNexusPath;
import com.invasion.entity.ai.goal.EntityAITargetRetaliate;
import com.invasion.entity.ai.goal.EntityAIWaitForEngy;
import com.invasion.entity.ai.goal.EntityAIWanderIM;
import com.invasion.nexus.EntityConstruct;
import com.invasion.nexus.INexusAccess;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIMSpider extends TieredIMMobEntity implements ISpawnsOffspring {
	private int airborneTime;

	public EntityIMSpider(EntityType<EntityIMSpider> type, World world) {
		this(type, world, null);
		// TODO: Add this entity to EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS
	}

	public EntityIMSpider(EntityType<EntityIMSpider> type, World world, INexusAccess nexus) {
		super(type, world, nexus);
		moveControl = new IMMoveHelperSpider(this);
		getNavigatorNew().getActor().setCanClimb(true);
	}

	@Override
    protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EntityAIKillEntity<>(this, PlayerEntity.class, 40));
		goalSelector.add(1, new EntityAIRallyBehindEntity<>(this, EntityIMCreeper.class, 4));
		goalSelector.add(2, new EntityAIAttackNexus(this));
		goalSelector.add(3, new EntityAIWaitForEngy(this, 5, false));
		goalSelector.add(4, new EntityAIKillEntity<>(this, MobEntity.class, 40));
		goalSelector.add(5, new EntityAIGoToNexus(this));
		goalSelector.add(7, new EntityAIWanderIM(this));
		goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		goalSelector.add(9, new LookAroundGoal(this));
        goalSelector.add(10, new LookAtEntityGoal(this, EntityIMCreeper.class, 12));

        if (getTier() == 2) {
            if (getFlavour() == 0) {
                goalSelector.add(3, new EntityAIPounce(this, 0.2F, 1.55F, 18));
            } else if (getFlavour() == 1) {
                goalSelector.add(1, new EntityAILayEgg(this, 1));
            }
        }else if (getFlavour() == 1) {
            goalSelector.add(3, new EntityAIPounce(this, 0.2F, 1.55F, 18));
        }
		targetSelector.add(0, new EntityAITargetRetaliate<>(this, MobEntity.class, 12));
		targetSelector.add(1, new EntityAISimpleTarget<>(this, PlayerEntity.class, this.getSenseRange(), false));
		targetSelector.add(2, new EntityAISimpleTarget<>(this, PlayerEntity.class, this.getAggroRange(), true));
		targetSelector.add(3, new EntityAITargetOnNoNexusPath<>(this, EntityIMPigEngy.class, 3.5F));
		targetSelector.add(4, new RevengeGoal(this));
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
        if (!getWorld().isClient) {
            setClimbing(horizontalCollision);
        }
    }

	@Override
	protected float getJumpVelocity(float strength) {
	    return super.getJumpVelocity(strength + 0.41F);
	}

	@Override
	protected void initTieredAttributes() {
	    setGravity(0.08F);
        //setSize(1.4F, 0.9F);
        if (getTier() == 1) {
            if (getFlavour() == 0) {
                setName("Spider");
                setMovementSpeed(0.29F);
                setAttackStrength(3);
                setCanDestroyBlocks(false);
                setAggroRange(10);
                setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
            } else if (getFlavour() == 1) {
                setName("Baby-Spider");
                //setSize(0.42F, 0.3F);
                setMovementSpeed(0.34F);
                setAttackStrength(1);
                setCanDestroyBlocks(false);
                setAggroRange(10);
                setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
            }
        } else if (getTier() == 2) {
            if (getFlavour() == 0) {
                setName("Jumping-Spider");
                setMovementSpeed(0.3F);
                setAttackStrength(5);
                setCanDestroyBlocks(false);
                setAggroRange(18);
                setGravity(0.043F);

                setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
            } else if (getFlavour() == 1) {
                setName("Mother-Spider");
                //setSize(2.8F, 1.8F);
                setMovementSpeed(0.22F);
                setAttackStrength(4);
                setCanDestroyBlocks(false);
                setAggroRange(18);
                setMaxHealthAndHealth(InvasionMod.getConfig().getHealth(this));
            }
        }

		if (getTier() == 1) {
			setTexture(0);
		} else if (getTier() == 2) {
			if (getFlavour() == 0) {
				setTexture(1);
			} else {
				setTexture(2);
			}
		}
	}

	@Deprecated
	@Override
    public String getLegacyName() {
	    return String.format("%s-T%d-%s", getClass().getName().replace("Entity", ""), getTier(), getDisplayName());
	}

	@Override
	public Vec3d getVehicleAttachmentPos(Entity vehicle) {
		return super.getVehicleAttachmentPos(vehicle).multiply(1, 0.75D, 1).subtract(0, 0.5D, 0);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound compound) {
	    compound.putInt("tier", getTier());
	    compound.putInt("flavour", getFlavour());
	    compound.putInt("textureId", getTextureId());
		super.writeCustomDataToNbt(compound);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound compound) {
		super.readCustomDataFromNbt(compound);
		setFlavour(compound.getInt("flavour"));
		setTier(compound.getInt("tier"));
		setTexture(compound.getInt("textureId"));
	}

	@Override
    public float getScaleFactor() {
		if (getTier() == 1 && getFlavour() == 1) {
			return 0.35F;
		}
		if (getTier() == 2 && getFlavour() == 1) {
			return 1.3F;
		}
		return 1;
	}

	@Override
	public Entity[] getOffspring(Entity partner) {
		if (getTier() == 2 && getFlavour() == 1) {
			EntityConstruct template = new EntityConstruct(InvEntities.SPIDER, 1, 0, 1, 1.0F, 0, 0);
			Entity[] offSpring = new Entity[6];
			for (int i = 0; i < offSpring.length; i++) {
				offSpring[i] = template.createMob(getWorld(), getNexus());
			}
			return offSpring;
		}

		return null;
	}

	public int getAirborneTime() {
		return this.airborneTime;
	}

	@Override
	public boolean isPushable() {
		return !isClimbing();
	}

	@Override
	public boolean isClimbing() {
		return horizontalCollision;
	}

	public void setAirborneTime(int time) {
		this.airborneTime = time;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SPIDER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_SPIDER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SPIDER_DEATH;
	}

	@Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
	    // TODO: Add this to EntityTypeTags.FALL_DAMAGE_IMMUNE
	    return false;
	}
}