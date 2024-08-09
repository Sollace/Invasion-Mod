package invmod.common.entity;

import invmod.client.render.AnimationRegistry;
import invmod.client.render.animation.AnimationAction;
import invmod.client.render.animation.AnimationState;
import invmod.common.mod_Invasion;
import invmod.common.nexus.INexusAccess;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EntityIMBird extends EntityIMFlying
{
  private static final int META_ANIMATION_FLAGS = 26;
  private final WingController wingController = AnimationRegistry.instance().getAnimation("wing_flap_2_piece").createState(this, AnimationAction.WINGTUCK, WingController::new);
  private final LegController legController = AnimationRegistry.instance().getAnimation("bird_run").createState(this, AnimationAction.STAND, LegController::new);
  private final MouthController beakController = AnimationRegistry.instance().getAnimation("bird_beak").createState(this, AnimationAction.MOUTH_CLOSE, MouthController::new);
  private int animationFlags;
  private float carriedEntityYawOffset;
  private int tier;

  public EntityIMBird(World world)
  {
    this(world, null);
  }

  public EntityIMBird(World world, INexusAccess nexus)
  {
	  super(world, nexus);
	    setName("Bird");
	    setGender(2);
	    setBaseMoveSpeedStat(1.0F);
	    setAttackStrength(1);
	    this.animationFlags = 0;
	    this.carriedEntityYawOffset = 0.0F;
	    setGravity(0.025F);
	    setThrust(0.1F);
	    setMaxPoweredFlightSpeed(0.5F);
	    setLiftFactor(0.35F);
	    setThrustComponentRatioMin(0.0F);
	    setThrustComponentRatioMax(0.5F);
	    setMaxTurnForce(getGravity() * 8.0F);
	    setMoveState(MoveState.STANDING);
	    setFlyState(FlyState.GROUNDED);
	    this.tier=1;

	    this.dataWatcher.addObject(26, Integer.valueOf(0));
  }

  public void doScreech()
  {
  }

  public void doMeleeSound()
  {
  }

  protected void doHurtSound()
  {
  }

  protected void doDeathSound()
  {
  }

  public AnimationState<?> getWingAnimationState()
  {
    return wingController.getState();
  }

  public float getLegSweepProgress()
  {
    return 1.0F;
  }

  public AnimationState<?> getLegAnimationState()
  {
    return legController.getState();
  }

  public AnimationState<?> getBeakAnimationState()
  {
    return beakController.getState();
  }

  @Override
  public void onUpdate()
  {
    super.onUpdate();
    if (this.worldObj.isRemote)
    {
      updateFlapAnimation();
      updateLegAnimation();
      updateBeakAnimation();
      this.animationFlags = this.dataWatcher.getWatchableObjectInt(26);
    }
    else
    {
      this.dataWatcher.updateObject(26, Integer.valueOf(this.animationFlags));
    }
  }

  @Override
public String getSpecies()
  {
    return "Bird";
  }

  public boolean getClawsForward()
  {
    return (this.animationFlags & 0x1) > 0;
  }

  public boolean isAttackingWithWings()
  {
    return (this.animationFlags & 0x2) > 0;
  }

  public boolean isBeakOpen()
  {
    return (this.animationFlags & 0x4) > 0;
  }

  public float getCarriedEntityYawOffset()
  {
    return this.carriedEntityYawOffset;
  }

  @Override
  public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
  {
    if (isEntityInvulnerable())
    {
      return false;
    }
    if (this.worldObj.isRemote)
    {
      return false;
    }

    this.entityAge = 0;

    if (getHealth() <= 0.0F)
    {
      return false;
    }
    if ((par1DamageSource.isFireDamage()) && (isPotionActive(Potion.fireResistance)))
    {
      return false;
    }


//    if (((par1DamageSource == DamageSource.anvil) || (par1DamageSource == DamageSource.fallingBlock)) && (getCurrentItemOrArmor(4) != null))
//    {
//      //getCurrentItemOrArmor(4).damageItem((int)(par2 * 4.0F + this.rand.nextFloat() * par2 * 2.0F), this);
//      par2 *= 0.75F;
//    }

		this.limbSwingAmount = 1.5F;
    boolean flag = true;

    if (this.hurtResistantTime > this.maxHurtResistantTime / 2.0F)
    {
      if (par2 <= this.lastDamage)
      {
        return false;
      }

      damageEntity(par1DamageSource, par2 - this.lastDamage);
      this.lastDamage = par2;
      flag = false;
    }
    else
    {
      this.lastDamage = par2;
      this.prevHealth = getHealth();
      this.hurtResistantTime = this.maxHurtResistantTime;
      damageEntity(par1DamageSource, par2);
      this.hurtTime = (this.maxHurtTime = 10);
    }

    this.attackedAtYaw = 0.0F;
    Entity entity = par1DamageSource.getEntity();

    if (entity != null)
    {
      if ((entity instanceof EntityLivingBase))
      {
        setRevengeTarget((EntityLivingBase)entity);
      }

      if ((entity instanceof EntityPlayer))
      {
        this.recentlyHit = 100;
        this.attackingPlayer = ((EntityPlayer)entity);
      }
      else if ((entity instanceof EntityWolf))
      {
        EntityWolf entitywolf = (EntityWolf)entity;

        if (entitywolf.isTamed())
        {
          this.recentlyHit = 100;
          this.attackingPlayer = null;
        }
      }
    }

    if (flag)
    {
      this.worldObj.setEntityState(this, (byte)2);

      if (par1DamageSource != DamageSource.drown)
      {
        setBeenAttacked();
      }

      if (entity != null)
      {
        double d0 = entity.posX - this.posX;
        double d1 = entity.posZ - this.posZ;

        for (d1 = entity.posZ - this.posZ; d0 * d0 + d1 * d1 < 0.0001D; d1 = (Math.random() - Math.random()) * 0.01D)
        {
          d0 = (Math.random() - Math.random()) * 0.01D;
        }

        this.attackedAtYaw = ((float)(Math.atan2(d1, d0) * 180.0D / 3.141592653589793D) - this.rotationYaw);
        knockBack(entity, par2, d0, d1);
      }
      else
      {
        this.attackedAtYaw = ((int)(Math.random() * 2.0D) * 180);
      }
    }

    if (getHealth() <= 0.0F)
    {
      if (flag)
      {
        doDeathSound();
      }

      onDeath(par1DamageSource);
    }
    else if (flag)
    {
      doHurtSound();
    }

    return true;
  }

  protected void setBeakState(int timeOpen)
  {
    this.beakController.setMouthState(timeOpen);
  }

  protected void onPickedUpEntity(Entity entity)
  {
    this.carriedEntityYawOffset = (entity.rotationYaw - entity.rotationYaw);
  }

  public void setClawsForward(boolean flag)
  {
    if ((flag ? 1 : 0) != (this.animationFlags & 0x1))
      this.animationFlags ^= 1;
  }

  public void setAttackingWithWings(boolean flag)
  {
    if ((flag ? 1 : 0) != (this.animationFlags & 0x2))
      this.animationFlags ^= 2;
  }

  protected void setBeakOpen(boolean flag)
  {
    if ((flag ? 1 : 0) != (this.animationFlags & 0x4))
      this.animationFlags ^= 4;
  }

  @Override
  protected void updateAITick()
  {
  }

  protected void updateFlapAnimation()
  {
    this.wingController.update();
  }

  protected void updateLegAnimation()
  {
    this.legController.update();
  }

  protected void updateBeakAnimation()
  {
    this.beakController.update();
  }

  @Override
  public int getTier()
  {
	  return this.tier;
  }
}