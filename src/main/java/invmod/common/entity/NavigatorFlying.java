package invmod.common.entity;


import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class NavigatorFlying extends NavigatorIM implements INavigationFlying {
	private static final int VISION_RESOLUTION_H = 30;
	private static final int VISION_RESOLUTION_V = 20;
	private static final float FOV_H = 300;
	private static final float FOV_V = 220;
	private final EntityIMFlying theEntity;
	private INavigationFlying.MoveType moveType = INavigationFlying.MoveType.MIXED;
	private boolean wantsToBeFlying;
	private float targetYaw;
	private float targetPitch;
	private float targetSpeed;
	private float visionDistance = 14;
	private int visionUpdateRate = 3;
	private int timeSinceVision = 3;
	private float[][] retina = new float[VISION_RESOLUTION_H][VISION_RESOLUTION_V];
	private float[][] headingAppeal = new float[28][18];
	private Vec3d intermediateTarget = Vec3d.ZERO;
	private Vec3d finalTarget;
	private boolean isCircling;
	private float circlingHeight;
	private float circlingRadius;
	private float pitchBias;
	private float pitchBiasAmount;
	private int timeLookingForEntity;
	private boolean precisionTarget;
	private float closestDistToTarget;
	private int timeSinceGotCloser;

	public NavigatorFlying(EntityIMFlying entityFlying, IPathSource pathSource) {
		super(entityFlying, pathSource);
		theEntity = entityFlying;
		targetYaw = entityFlying.getYaw();
		targetSpeed = entityFlying.getMaxPoweredFlightSpeed();
	}

	@Override
    public void setMovementType(INavigationFlying.MoveType moveType) {
		this.moveType = moveType;
	}

	@Override
    public void enableDirectTarget(boolean enabled) {
		precisionTarget = enabled;
	}

	@Override
    public void setLandingPath() {
		clearPath();
		setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
		setWantsToBeFlying(false);
	}

	@Override
    public void setCirclingPath(Vec3d pos, float preferredHeight, float preferredRadius) {
		clearPath();
		this.finalTarget = pos;
		this.circlingHeight = preferredHeight;
		this.circlingRadius = preferredRadius;
		this.isCircling = true;
	}

	@Override
    public float getDistanceToCirclingRadius() {
	    if (finalTarget == null) {
	        return Float.MAX_VALUE;
	    }
		return (float)theEntity.getPos().distanceTo(finalTarget) - circlingRadius;
	}

	@Override
    public void setFlySpeed(float speed) {
		targetSpeed = speed;
	}

	@Override
    public void setPitchBias(float pitch, float biasAmount) {
		pitchBias = pitch;
		pitchBiasAmount = biasAmount;
	}

	@Override
    protected void updateAutoPathToEntity() {
		double dist = theEntity.distanceTo(pathEndEntity);
		if (dist < closestDistToTarget - 1) {
			closestDistToTarget = ((float) dist);
			timeSinceGotCloser = 0;
		} else {
			timeSinceGotCloser++;
		}

		boolean pathUpdate = false;
		boolean needsPathfinder = false;
		if (path != null) {
			double dSq = MathHelper.square(dist);
			if ((moveType == INavigationFlying.MoveType.PREFER_FLYING || (moveType == INavigationFlying.MoveType.MIXED && dSq > 100)) && theEntity.canSee(pathEndEntity)) {
				this.timeLookingForEntity = 0;
				pathUpdate = true;
			} else {
				double d1 = Math.sqrt(pathEndEntity.squaredDistanceTo(pathEndEntityLastPos));
				double d2 = Math.sqrt(theEntity.squaredDistanceTo(pathEndEntityLastPos));
				if (d1 / d2 > 0.1D) {
					pathUpdate = true;
				}
			}

		} else if (moveType == INavigationFlying.MoveType.PREFER_WALKING || timeSinceGotCloser > 160 || timeLookingForEntity > 600) {
			pathUpdate = true;
			needsPathfinder = true;
			timeSinceGotCloser = 0;
			timeLookingForEntity = 500;
		} else if (moveType == INavigationFlying.MoveType.MIXED) {
			double dSq = theEntity.squaredDistanceTo(pathEndEntity.getPos());
			if (dSq < 100) {
				pathUpdate = true;
			}

		}

		if (pathUpdate) {
			if (moveType == INavigationFlying.MoveType.PREFER_FLYING) {
				if (needsPathfinder) {
					theEntity.setPathfindFlying(true);
					path = createPath(theEntity, pathEndEntity, 0);
					if (path != null) {
						setWantsToBeFlying(true);
						setPath(path, moveSpeed);
					}

				} else {
					setWantsToBeFlying(true);
					resetStatus();
				}
			} else if (moveType == INavigationFlying.MoveType.MIXED) {
				theEntity.setPathfindFlying(false);
				Path path = createPath(theEntity, pathEndEntity, 0);
				if ((path != null) && (path.getCurrentPathLength() < dist * 1.8D)) {
					setWantsToBeFlying(false);
					setPath(path, this.moveSpeed);
				} else if (needsPathfinder) {
					theEntity.setPathfindFlying(true);
					path = createPath(theEntity, pathEndEntity, 0);
					setWantsToBeFlying(true);
					if (path != null) {
						setPath(path, moveSpeed);
					} else {
						resetStatus();
					}
				} else {
					setWantsToBeFlying(true);
					resetStatus();
				}
			} else {
				setWantsToBeFlying(false);
				theEntity.setPathfindFlying(false);
				Path path = createPath(theEntity, pathEndEntity, 0);
				if (path != null) {
					setPath(path, moveSpeed);
				}
			}
			pathEndEntityLastPos = pathEndEntity.getPos();
		}
	}

	@Override
    public void autoPathToEntity(Entity target) {
		super.autoPathToEntity(target);
		this.isCircling = false;
	}

	@Override
    public boolean tryMoveToEntity(Entity targetEntity, float targetRadius, float speed) {
		if (this.moveType != INavigationFlying.MoveType.PREFER_WALKING) {
			clearPath();
			this.pathEndEntity = targetEntity;
			this.finalTarget = pathEndEntity.getPos();
			this.isCircling = false;
			return true;
		}

		this.theEntity.setPathfindFlying(false);
		return super.tryMoveToEntity(targetEntity, targetRadius, speed);
	}

	@Override
    public boolean tryMoveToXYZ(Vec3d pos, float targetRadius, float speed) {
		if (this.moveType != INavigationFlying.MoveType.PREFER_WALKING) {
			clearPath();
			this.finalTarget = pos;
			this.isCircling = false;
			return true;
		}

		this.theEntity.setPathfindFlying(false);
		return super.tryMoveToXYZ(pos, targetRadius, speed);
	}

	@Override
    public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed) {
		Vec3d target = findValidPointNear(x, z, min, max, verticalRange);
		return target != null && tryMoveToXYZ(target, 0, speed);
	}

	@Override
    public void clearPath() {
		super.clearPath();
		this.pathEndEntity = null;
		this.isCircling = false;
	}

	@Override
    public boolean isCircling() {
		return this.isCircling;
	}

	@Override
    public String getStatus() {
		if (!noPath()) {
			return super.getStatus();
		}
		String s = "";
		if (isAutoPathingToEntity()) {
			s = s + "Auto:";
		}

		s = s + "Flyer:";
		if (this.isCircling) {
			s = s + "Circling:";
		} else if (this.wantsToBeFlying) {
			if (this.theEntity.getFlyState() == FlyState.TAKEOFF)
				s = s + "TakeOff:";
			else {
				s = s + "Flying:";
			}

		} else if ((this.theEntity.getFlyState() == FlyState.LANDING) || (this.theEntity.getFlyState() == FlyState.TOUCHDOWN))
			s = s + "Landing:";
		else {
			s = s + "Ground";
		}
		return s;
	}

	@Override
    protected void pathFollow() {
		Vec3d vec3d = getEntityPosition();
		int maxNextLeg = this.path.getCurrentPathLength();

		float fa = MathHelper.square(this.theEntity.getWidth() * 0.5F);
		for (int j = this.path.getCurrentPathIndex(); j < maxNextLeg; j++) {
			if (vec3d.squaredDistanceTo(this.path.getPositionAtIndex(this.theEntity, j)) < fa)
				this.path.setCurrentPathIndex(j + 1);
		}
	}

	@Override
    protected void noPathFollow() {
		if ((this.theEntity.getMoveState() != MoveState.FLYING) && (this.theEntity.getAIGoal() == Goal.CHILL)) {
			setWantsToBeFlying(false);
			return;
		}

		if (this.moveType == INavigationFlying.MoveType.PREFER_FLYING)
			setWantsToBeFlying(true);
		else if (this.moveType == INavigationFlying.MoveType.PREFER_WALKING) {
			setWantsToBeFlying(false);
		}
		if (++this.timeSinceVision >= this.visionUpdateRate) {
			this.timeSinceVision = 0;
			if ((!this.precisionTarget) || (this.pathEndEntity == null))
				updateHeading();
			else {
				updateHeadingDirectTarget(this.pathEndEntity);
			}
			this.intermediateTarget = convertToVector(this.targetYaw, this.targetPitch, this.targetSpeed);
		}
		this.theEntity.getMoveHelper().moveTo(this.intermediateTarget.x, this.intermediateTarget.y, this.intermediateTarget.z, this.targetSpeed);
	}

	protected Vec3d convertToVector(float yaw, float pitch, float idealSpeed) {
		int time = this.visionUpdateRate + 20;
		double x = this.theEntity.getX() + -Math.sin(yaw * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		double y = this.theEntity.getY() + Math.sin(pitch * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		double z = this.theEntity.getZ() + Math.cos(yaw * MathHelper.RADIANS_PER_DEGREE) * idealSpeed * time;
		return new Vec3d(x, y, z);
	}

	protected void updateHeading() {
		float pixelDegreeH = 10;
		float pixelDegreeV = 11;
		for (int i = 0; i < VISION_RESOLUTION_H; i++) {
			double nextAngleH = i * pixelDegreeH + 0.5D * pixelDegreeH - 150 + this.theEntity.getYaw();
			for (int j = 0; j < 20; j++) {
				double nextAngleV = j * pixelDegreeV + 0.5D * pixelDegreeV - 110;
				double y = this.theEntity.getY() + Math.sin(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * this.visionDistance;
				double distanceXZ = Math.cos(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * this.visionDistance;
				double x = this.theEntity.getX() + -Math.sin(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
				double z = this.theEntity.getZ() + Math.cos(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
				Vec3d target = new Vec3d(x, y, z);
				Vec3d origin = this.theEntity.getPos().add(0, 1, 0);

				BlockHitResult object = this.theEntity.getWorld().raycast(new RaycastContext(origin, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, theEntity));
				if ((object != null) && object.getType() == Type.BLOCK) {
					this.retina[i][j] = (float)object.getPos().distanceTo(theEntity.getPos());
				} else {
					this.retina[i][j] = (this.visionDistance + 1);
				}
			}

		}

		for (int i = 1; i < 29; i++) {
			for (int j = 1; j < 19; j++) {
				float appeal = this.retina[i][j];
				appeal += this.retina[(i - 1)][(j - 1)];
				appeal += this.retina[(i - 1)][j];
				appeal += this.retina[(i - 1)][(j + 1)];
				appeal += this.retina[i][(j - 1)];
				appeal += this.retina[i][(j + 1)];
				appeal += this.retina[(i + 1)][(j - 1)];
				appeal += this.retina[(i + 1)][j];
				appeal += this.retina[(i + 1)][(j + 1)];
				appeal /= 9;
				this.headingAppeal[(i - 1)][(j - 1)] = appeal;
			}

		}

		if (this.isCircling) {
			double dX = this.finalTarget.x - this.theEntity.getX();
			double dY = this.finalTarget.y - this.theEntity.getY();
			double dZ = this.finalTarget.z - this.theEntity.getZ();
			double dXZ = Math.sqrt(dX * dX + dZ * dZ);

			if ((dXZ > 0) && (dXZ > this.circlingRadius * 0.6D)) {
				double intersectRadius = Math.abs((this.circlingRadius - dXZ) * 2) + 8;
				if (intersectRadius > this.circlingRadius * 1.8D) {
					intersectRadius = dXZ + 5;
				}

				float preferredYaw1 = (float) (Math.acos((dXZ * dXZ - this.circlingRadius * this.circlingRadius + intersectRadius * intersectRadius) / (2 * dXZ) / intersectRadius) * MathHelper.DEGREES_PER_RADIAN);
				float preferredYaw2 = -preferredYaw1;

				double dYaw = Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90;
				preferredYaw1 = (float) (preferredYaw1 + dYaw);
				preferredYaw2 = (float) (preferredYaw2 + dYaw);

				float preferredPitch = (float) (Math.atan((dY + this.circlingHeight) / intersectRadius) * MathHelper.DEGREES_PER_RADIAN);

				float yawBias = (float) (1.5D * Math.abs(dXZ - this.circlingRadius) / this.circlingRadius);
				float pitchBias = (float) (1.9D * Math.abs((dY + this.circlingHeight) / this.circlingHeight));

				doHeadingBiasPass(this.headingAppeal, preferredYaw1, preferredYaw2, preferredPitch, yawBias, pitchBias);
			} else {
				float yawToTarget = (float) (Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90);
				yawToTarget += 180;
				float preferredPitch = (float) (Math.atan((dY + this.circlingHeight) / Math.abs(this.circlingRadius - dXZ)) * MathHelper.DEGREES_PER_RADIAN);
				float yawBias = (float) (0.5D * Math.abs(dXZ - this.circlingRadius) / this.circlingRadius);
				float pitchBias = (float) (0.9D * Math.abs((dY + this.circlingHeight) / this.circlingHeight));
				doHeadingBiasPass(this.headingAppeal, yawToTarget, yawToTarget, preferredPitch, yawBias, pitchBias);
			}
		} else if (this.pathEndEntity != null) {
			double dX = this.pathEndEntity.getX() - this.theEntity.getX();
			double dY = this.pathEndEntity.getY() - this.theEntity.getY();
			double dZ = this.pathEndEntity.getZ() - this.theEntity.getZ();
			double dXZ = Math.sqrt(dX * dX + dZ * dZ);
			float yawToTarget = (float) (Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90);
			float pitchToTarget = (float) (Math.atan(dY / dXZ) * MathHelper.DEGREES_PER_RADIAN);
			doHeadingBiasPass(this.headingAppeal, yawToTarget, yawToTarget, pitchToTarget, 20.6F, 20.6F);
		}

		if (this.pathEndEntity == null) {
			float dOldYaw = MathHelper.subtractAngles(this.targetYaw, this.theEntity.getYaw());
			float dOldPitch = this.targetPitch;
			float approxLastTargetX = dOldYaw / pixelDegreeH + 14;
			float approxLastTargetY = dOldPitch / pixelDegreeV + 9;
			if (approxLastTargetX > 28)
				approxLastTargetX = 28;
			else if (approxLastTargetX < 0) {
				approxLastTargetX = 0;
			}
			if (approxLastTargetY > 18)
				approxLastTargetY = 18;
			else if (approxLastTargetY < 0) {
				approxLastTargetY = 0;
			}
			float statusQuoBias = 0.4F;
			float falloffDist = 30;
			for (int i = 0; i < 28; i++) {
				float dXSq = (approxLastTargetX - i) * (approxLastTargetX - i);
				for (int j = 0; j < 18; j++) {
					float dY = approxLastTargetY - j;
					int tmp1306_1304 = j;
					float[] tmp1306_1303 = this.headingAppeal[i];
					tmp1306_1303[tmp1306_1304] = ((float) (tmp1306_1303[tmp1306_1304] * (1 + statusQuoBias - statusQuoBias * Math.sqrt(dXSq + dY * dY) / falloffDist)));
				}
			}
		}

		if (this.pitchBias != 0) {
			doHeadingBiasPass(this.headingAppeal, 0, 0, this.pitchBias, 0, this.pitchBiasAmount);
		}

		if (!this.wantsToBeFlying) {
		    FloatFloatPair landingInfo = appraiseLanding();
			if (landingInfo.secondFloat() < 4) {
				if (landingInfo.firstFloat() >= 0.9F)
					doHeadingBiasPass(this.headingAppeal, 0, 0, -45, 0, 3.5F);
				else if (landingInfo.firstFloat() >= 0.65F) {
					doHeadingBiasPass(this.headingAppeal, 0, 0, -15, 0, 0.4F);
				}

			} else if (landingInfo.firstFloat() >= 0.52F) {
				doHeadingBiasPass(this.headingAppeal, 0, 0, -15, 0, 0.8F);
			}

		}

		IntIntPair bestPixel = chooseCoordinate();
		this.targetYaw = (this.theEntity.getYaw() - 150 + (bestPixel.firstInt() + 1) * pixelDegreeH + 0.5F * pixelDegreeH);
		this.targetPitch = (-110 + (bestPixel.secondInt() + 1) * pixelDegreeV + 0.5F * pixelDegreeV);
	}

	protected void updateHeadingDirectTarget(Entity target) {
		double dX = target.getX() - this.theEntity.getX();
		double dY = target.getY() - this.theEntity.getY();
		double dZ = target.getZ() - this.theEntity.getZ();
		double dXZ = Math.sqrt(dX * dX + dZ * dZ);
		this.targetYaw = ((float) (Math.atan2(dZ, dX) * MathHelper.DEGREES_PER_RADIAN - 90));
		this.targetPitch = ((float) (Math.atan(dY / dXZ) * MathHelper.DEGREES_PER_RADIAN));
	}

	protected IntIntPair chooseCoordinate() {
		int bestPixelX = 0;
		int bestPixelY = 0;
		for (int i = 0; i < 28; i++) {
			for (int j = 0; j < 18; j++) {
				if (this.headingAppeal[bestPixelX][bestPixelY] < this.headingAppeal[i][j]) {
					bestPixelX = i;
					bestPixelY = j;
				}
			}
		}
		return IntIntPair.of(bestPixelX, bestPixelY);
	}

	protected void setTarget(double x, double y, double z) {
		intermediateTarget = new Vec3d(x, y, z);
	}

	protected Vec3d getTarget() {
		return intermediateTarget;
	}

	protected void doHeadingBiasPass(float[][] array, float preferredYaw1, float preferredYaw2, float preferredPitch, float yawBias, float pitchBias) {
		float pixelDegreeH = 10;
		float pixelDegreeV = 11;
		for (int i = 0; i < array.length; i++) {
			double nextAngleH = (i + 1) * pixelDegreeH + 0.5D * pixelDegreeH - 150 + this.theEntity.getYaw();
			double dYaw1 = MathHelper.wrapDegrees(preferredYaw1 - nextAngleH);
			double dYaw2 = MathHelper.wrapDegrees(preferredYaw2 - nextAngleH);
			double yawBiasAmount = 1 + Math.min(Math.abs(dYaw1), Math.abs(dYaw2)) * yawBias / 180;
			for (int j = 0; j < array[0].length; j++) {
				double nextAngleV = (j + 1) * pixelDegreeV + 0.5D * pixelDegreeV - 110;
				double pitchBiasAmount = 1 + Math.abs(MathHelper.wrapDegrees(preferredPitch - nextAngleV)) * pitchBias / 180;
				int tmp162_160 = j;
				float[] tmp162_159 = array[i];
				tmp162_159[tmp162_160] = ((float) (tmp162_159[tmp162_160] / (yawBiasAmount * pitchBiasAmount)));
			}
		}
	}

	private void setWantsToBeFlying(boolean flag) {
		this.wantsToBeFlying = flag;
		this.theEntity.getMoveHelper().setWantsToBeFlying(flag);
	}

	private FloatFloatPair appraiseLanding() {
		float safety = 0;
		float distance = 0;
		int landingResolution = 3;
		double nextAngleH = this.theEntity.getYaw();
		for (int i = 0; i < landingResolution; i++) {
			double nextAngleV = -90 + i * VISION_RESOLUTION_H / landingResolution;
			double y = this.theEntity.getY() + Math.sin(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * 64;
			double distanceXZ = Math.cos(nextAngleV * MathHelper.RADIANS_PER_DEGREE) * 64;
			double x = this.theEntity.getX() + -Math.sin(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
			double z = this.theEntity.getZ() + Math.cos(nextAngleH * MathHelper.RADIANS_PER_DEGREE) * distanceXZ;
			Vec3d target = new Vec3d(x, y, z);
			Vec3d origin = this.theEntity.getPos();
			BlockHitResult hit = this.theEntity.getWorld().raycast(new RaycastContext(origin, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, theEntity));
			if (hit != null && hit.getType() == Type.BLOCK) {
				BlockState Block = this.theEntity.getWorld().getBlockState(hit.getBlockPos());
				if (!this.theEntity.avoidsBlock(Block)) {
					safety += 0.7F;
				}
				if (hit.getSide() == Direction.UP) {
					safety += 0.3F;
				}
				distance = (float)hit.getPos().distanceTo(theEntity.getPos());
			} else {
				distance += 64;
			}
		}
		distance /= landingResolution;
		safety /= landingResolution;
		return FloatFloatPair.of(Float.valueOf(safety), Float.valueOf(distance));
	}
}
