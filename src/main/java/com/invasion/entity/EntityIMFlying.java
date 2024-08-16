package com.invasion.entity;

import org.joml.Vector3f;

import com.invasion.entity.ai.FlyState;
import com.invasion.entity.ai.IMLookHelper;
import com.invasion.entity.ai.IMMoveHelperFlying;
import com.invasion.entity.pathfinding.INavigation;
import com.invasion.entity.pathfinding.INavigationFlying;
import com.invasion.entity.pathfinding.IPathSource;
import com.invasion.entity.pathfinding.NavigatorFlying;
import com.invasion.entity.pathfinding.PathCreator;
import com.invasion.nexus.INexusAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityIMFlying extends EntityIMLiving {
    private static final TrackedData<Vector3f> TARGET_POS = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.VECTOR3F);
    private static final TrackedData<Boolean> THRUSTING = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> THRUST_EFFORT = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> FLY_STATE = DataTracker.registerData(EntityIMFlying.class, TrackedDataHandlerRegistry.INTEGER);

	private float liftFactor = 0.4F;
	private float maxPoweredFlightSpeed = 0.28F;
	private float thrust = 0.08F;
	private float thrustComponentRatioMin = 0;
	private float thrustComponentRatioMax = 0.1F;
	private float maxTurnForce = (float)(getGravity() * 3);

    private float rotationRoll;
    private float prevRotationRoll;

	private float optimalPitch = 52;
	private float maxRunSpeed = 0.45F;

	private Vec3d accelleration = Vec3d.ZERO;

	private boolean flyPathfind = true;
	private boolean debugFlying = true;

	public EntityIMFlying(EntityType<? extends EntityIMFlying> type, World world) {
		this(type, world, null);
	}

	public EntityIMFlying(EntityType<? extends EntityIMFlying> type, World world, INexusAccess nexus) {
		super(type, world, nexus);
		moveControl = new IMMoveHelperFlying(this);
		lookControl = new IMLookHelper(this);
	}

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TARGET_POS, new Vector3f());
        builder.add(THRUSTING, false);
        builder.add(THRUST_EFFORT, 1F);
        builder.add(FLY_STATE, FlyState.GROUNDED.ordinal());
    }

    public Vector3f getTargetPos() {
        return dataTracker.get(TARGET_POS);
    }

    public void setTargetPos(Vector3f target) {
        dataTracker.set(TARGET_POS, target);
    }

	@Override
    protected INavigation createIMNavigation(IPathSource pathSource) {
	    return new NavigatorFlying(this, pathSource);
	}

    @Override
    protected IPathSource createPathSource() {
        return new PathCreator(800, 200);
    }

    @Override
    protected BodyControl createBodyControl() {
        return new BodyControl(this) {
            @Override
            public void tick() {
            }
        };
    }

	@Override
	public void baseTick() {
		super.baseTick();
		/*if (!this.getWorld().isClient) {
			byte thrustData = this.dataWatcher.getWatchableObjectByte(META_THRUST_DATA);
			int oldThrustOn = thrustData & 0x1;
			int oldThrustEffortEncoded = thrustData >> 1 & 0xF;
			int thrustEffortEncoded = (int) (this.thrustEffort * 15.0F);
			if (this.thrustOn == oldThrustOn > 0) {
				if (thrustEffortEncoded == oldThrustEffortEncoded)
					;
		}*/
	}

	public FlyState getFlyState() {
		return FlyState.of(dataTracker.get(FLY_STATE));
	}

    public float getRoll(float tickDelta) {
        return MathHelper.lerpAngleDegrees(tickDelta, prevRotationRoll, rotationRoll);
    }

    public void setRoll(float roll) {
        rotationRoll = roll;
    }

	public boolean isThrustOn() {
		return dataTracker.get(THRUSTING);
	}

	public float getThrustEffort() {
		return dataTracker.get(THRUST_EFFORT);
	}

    public void setThrustEffort(float effortFactor) {
        dataTracker.set(THRUST_EFFORT, effortFactor);
    }

	@Deprecated
	public Vector3f getFlyTarget() {
		return getTargetPos();
	}

    @Override
    public boolean isClimbing() {
        return false;
    }

    public boolean hasFlyingDebug() {
        return this.debugFlying;
    }

    public void setPathfindFlying(boolean flag) {
        this.flyPathfind = flag;
    }

    public boolean getPathFindFlying() {
        return flyPathfind;
    }

    public void setFlyState(FlyState flyState) {
        dataTracker.set(FLY_STATE, flyState.ordinal());
    }

    public float getMaxPoweredFlightSpeed() {
        return this.maxPoweredFlightSpeed;
    }

    public float getLiftFactor() {
        return this.liftFactor;
    }

    protected void setLiftFactor(float liftFactor) {
        this.liftFactor = liftFactor;
    }

    public float getThrust() {
        return this.thrust;
    }

    protected void setThrust(float thrust) {
        this.thrust = thrust;
    }

    public float getThrustComponentRatioMin() {
        return this.thrustComponentRatioMin;
    }

    public float getThrustComponentRatioMax() {
        return this.thrustComponentRatioMax;
    }

    public float getMaxTurnForce() {
        return this.maxTurnForce;
    }

    public float getMaxPitch() {
        return this.optimalPitch;
    }

    public float getLandingSpeedThreshold() {
        return getMovementSpeed() * 1.2F;
    }

    protected float getMaxRunSpeed() {
        return this.maxRunSpeed;
    }

    public void setAcceleration(Vec3d accelleration) {
       this.accelleration = accelleration;
    }

    public void setThrustOn(boolean flag) {
        dataTracker.set(THRUSTING, flag);
    }

    protected void setMaxPoweredFlightSpeed(float speed) {
        this.maxPoweredFlightSpeed = speed;
        ((INavigationFlying)getNavigatorNew()).setFlySpeed(speed);
    }

    protected void setThrustComponentRatioMin(float ratio) {
        thrustComponentRatioMin = ratio;
    }

    protected void setThrustComponentRatioMax(float ratio) {
        thrustComponentRatioMax = ratio;
    }

    protected void setMaxTurnForce(float maxTurnForce) {
        this.maxTurnForce = maxTurnForce;
    }

    protected void setOptimalPitch(float pitch) {
        optimalPitch = pitch;
    }

    protected void setMaxRunSpeed(float speed) {
        maxRunSpeed = speed;
    }

	@Override
	public IMMoveHelperFlying getMoveControl() {
		return (IMMoveHelperFlying)super.getMoveControl();
	}

	@Override
	public IMLookHelper getLookControl() {
		return (IMLookHelper)super.getLookControl();
	}

    // based on FlyingEntity (originals in comments)
	@Override
	public void travel(Vec3d movementInput) {
	    if (isLogicalSideForUpdatingMovement()) {
    		if (isTouchingWater()) {
    			updateVelocity(isAiDisabled() ? 0.02F : 0.04F /*0.02F*/, movementInput);
    			move(MovementType.SELF, getVelocity());
    			setVelocity(getVelocity().multiply(0.8F));
    		} else if (isInLava()) {
                updateVelocity(isAiDisabled() ? 0.02F : 0.04F /*0.02F*/, movementInput);
                move(MovementType.SELF, getVelocity());
                setVelocity(getVelocity().multiply(0.5));
    		} else {
    		    float groundFriction = 0.9995F;
                if (isOnGround()) {
                    groundFriction = getWorld().getBlockState(getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91F;
                }

                float landMoveSpeed = 0.16277137F / (groundFriction * groundFriction * groundFriction);
                groundFriction = getGroundFriction();
                if (isOnGround()) {
                    groundFriction = getWorld().getBlockState(getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91F;
                }

                /** IM additions **/
                // added air resistance
                groundFriction *= airResistance;

                // added accelleration
                addVelocity(accelleration);
                /** end IM additions **/

    			updateVelocity(isOnGround() ? 0.1F * landMoveSpeed : 0.02F, movementInput);
    			move(MovementType.SELF, getVelocity());
    			//setVelocity(getVelocity().multiply(groundFriction));
    			setVelocity(getVelocity().multiply(groundFriction, airResistance, airResistance).add(-getGravity(), 0, 0));
    		}
	    }

	    updateLimbs(false);
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
	}

}