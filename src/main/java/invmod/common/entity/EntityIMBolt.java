package invmod.common.entity;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIMBolt extends Entity
{
  private int ticksToRender;
  private long timeCreated = System.currentTimeMillis();
  private double[][] vertices = new double[3][0];
  private long lastVertexUpdate = timeCreated;
  private float yaw;
  private float pitch;
  private double distance;
  private float widthVariance = 6;
  private float vecX;
  private float vecY;
  private float vecZ;
  private int soundMade;

  public EntityIMBolt(EntityType<EntityIMBolt> type, World world)
  {
    super(type, world);
    ignoreCameraFrustum = true;
  }

  public EntityIMBolt(World world, double x, double y, double z)
  {
    this(InvEntities.BOLT, world);
    setPosition(x, y, z);
  }

  public EntityIMBolt(World world, Vec3d pos, Vec3d targetPos, int ticksToRender, int soundMade)
  {
    this(InvEntities.BOLT, world, x, y, z);
    this.vecX = ((float)(x2 - x));
    this.vecY = ((float)(y2 - y));
    this.vecZ = ((float)(z2 - z));
    this.ticksToRender = ticksToRender;
    this.soundMade = soundMade;
    setHeading(this.vecX, this.vecY, this.vecZ);
    doVertexUpdate();
  }

  public void writeSpawnData(ByteArrayDataOutput data)
  {
    data.writeShort((short)this.ticksToRender);
    data.writeFloat((float)this.posX);
    data.writeFloat((float)this.posY);
    data.writeFloat((float)this.posZ);
    data.writeFloat(this.vecX);
    data.writeFloat(this.vecY);
    data.writeFloat(this.vecZ);
    data.writeByte((byte)this.soundMade);
  }

  public void readSpawnData(ByteArrayDataInput data)
  {
    this.ticksToRender = data.readShort();
    setPosition(data.readFloat(), data.readFloat(), data.readFloat());
    setHeading(data.readFloat(), data.readFloat(), data.readFloat());
    this.soundMade = data.readByte();
    doVertexUpdate();
  }

  @Override
  public void onUpdate()
  {
    super.onUpdate();
    this.age += 1;
    if ((this.age == 1) && (this.soundMade == 1)) {
      this.worldObj.playSoundAtEntity(this, "invmod:zap"+(rand.nextInt(3)+1), 1.0F, 1.0F);
    }
    if (this.age > this.ticksToRender)
      setDead();
  }

  public double[][] getVertices()
  {
    long time = System.currentTimeMillis();
    if (time - this.timeCreated > this.ticksToRender * 50) {
      return null;
    }
    if (time - this.lastVertexUpdate >= 75L)
    {
      doVertexUpdate();
      while (this.lastVertexUpdate + 50L <= time) {
        this.lastVertexUpdate += 50L;
      }
    }
    return this.vertices;
  }

  @Override
public float getYaw()
  {
    return this.yaw;
  }

  @Override
public float getPitch()
  {
    return this.pitch;
  }

  @Override
  public void handleHealthUpdate(byte byte0)
  {
    if (byte0 == 0)
    {
      this.worldObj.playSoundAtEntity(this, "invmod:zap"+(rand.nextInt(3)+1), 1.0F, 1.0F);
    }
    else if (byte0 != 1)
    {
      if (byte0 != 2);
    }
  }


  @Override
  public void entityInit()
  {
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
  {
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
  {
  }

  private void setHeading(float x, float y, float z)
  {
    float xzSq = x * x + z * z;
    this.yaw = ((float)(Math.atan2(x, z) * 180.0D / 3.141592653589793D) + 90.0F);
    this.pitch = ((float)(Math.atan2(MathHelper.sqrt_double(xzSq), y) * 180.0D / 3.141592653589793D));
    this.distance = Math.sqrt(xzSq + y * y);
  }

  private void doVertexUpdate()
  {
    this.worldObj.theProfiler.startSection("IMBolt");
    this.widthVariance = (10.0F / (float)Math.log10(this.distance + 1.0D));
    int numberOfVertexes = 60;
    if (numberOfVertexes != this.vertices[0].length)
    {
      this.vertices[0] = new double[numberOfVertexes];
      this.vertices[1] = new double[numberOfVertexes];
      this.vertices[2] = new double[numberOfVertexes];
    }

    for (int vertex = 0; vertex < numberOfVertexes; vertex++)
    {
      this.vertices[1][vertex] = (vertex * this.distance / (numberOfVertexes - 1));
    }

    createSegment(0, numberOfVertexes - 1);
    this.worldObj.theProfiler.endSection();
  }

  private void createSegment(int begin, int end)
  {
    int points = end + 1 - begin;
    if (points <= 4)
    {
      if (points == 3)
      {
        createVertex(begin, begin + 1, end);
      }
      else
      {
        createVertex(begin, begin + 1, end);
        createVertex(begin, begin + 2, end);
      }
      return;
    }
    int midPoint = begin + points / 2;
    createVertex(begin, midPoint, end);
    createSegment(begin, midPoint);
    createSegment(midPoint, end);
  }

  private void createVertex(int begin, int mid, int end)
  {
    double difference = this.vertices[0][end] - this.vertices[0][begin];
    double yDiffToMid = this.vertices[1][mid] - this.vertices[1][begin];
    double yRatio = yDiffToMid / (this.vertices[1][end] - this.vertices[1][begin]);
    this.vertices[0][mid] = (this.vertices[0][begin] + difference * yRatio + (this.worldObj.rand.nextFloat() - 0.5D) * yDiffToMid * this.widthVariance);
    difference = this.vertices[2][end] - this.vertices[2][begin];
    this.vertices[2][mid] = (this.vertices[2][begin] + difference * yRatio + (this.worldObj.rand.nextFloat() - 0.5D) * yDiffToMid * this.widthVariance);
  }
}