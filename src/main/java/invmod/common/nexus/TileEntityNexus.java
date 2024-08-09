package invmod.common.nexus;

import invmod.common.ConfigInvasion;
import invmod.common.InvasionMod;
import invmod.common.BountyHunter;
import invmod.common.mod_Invasion;
import invmod.common.block.InvBlockEntities;
import invmod.common.block.InvBlocks;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.ai.AttackerAI;
import invmod.common.item.InvItems;
import invmod.common.util.ComparatorDistanceFrom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TileEntityNexus extends BlockEntity implements INexusAccess, SidedInventory {
    private static final int[] SLOTS = {0, 1};
    public static final long BIND_EXPIRE_TIME = 300000L;

    private int activationTimer;
    private int currentWave;
    private int spawnRadius = 52;
    private int nexusLevel = 1;
    private int nexusKills;
    private int generation;
    private int cookTime;
    private int maxHp = 100;
    private int hp = 100;
    private int lastHp = 100;
    private int mode;
    private int powerLevel;
    private int lastPowerLevel;
    private int powerLevelTimer;
    private int mobsLeftInWave;
    private int lastMobsLeftInWave;
    private int mobsToKillInWave;
    private int nextAttackTime;
    private int daysToAttack;
    private long lastWorldTime;
    private int zapTimer;
    private int tickCount;
    private int cleanupTimer;
    private long spawnerElapsedRestore;
    private long waveDelayTimer;
    private long waveDelay;
    private boolean continuousAttack;
    private boolean mobsSorted;
    private boolean resumedFromNBT;
    private boolean activated;

    private final IMWaveSpawner waveSpawner = new IMWaveSpawner(this, this.spawnRadius);
    private final IMWaveBuilder waveBuilder = new IMWaveBuilder();
    private final SimpleInventory nexusItemStacks = new SimpleInventory(2);

    private final Map<UUID, Long> boundPlayers = new HashMap<>();
    private final List<EntityIMLiving> mobList = new ArrayList<>();
    private final AttackerAI attackerAI = new AttackerAI(this);

    private final ConfigInvasion config = InvasionMod.getConfig();

    private Box boundingBoxToRadius;

    public TileEntityNexus(BlockPos pos, BlockState state) {
        super(InvBlockEntities.NEXUS, pos, state);
        boundingBoxToRadius = computeSpawnArea();
        nexusItemStacks.addListener(l -> markDirty());
    }

    private Box computeSpawnArea() {
        return new Box(pos).expand(spawnRadius + 10, spawnRadius + 40, spawnRadius + 10);
    }

    private Box getChunkBox(World world) {
        return new Box(pos).expand(spawnRadius + 10, spawnRadius + 40, spawnRadius + 10).withMinY(world.getBottomY()).withMaxY(world.getTopY());
    }

    @Override
    public Map<UUID, Long> getBoundPlayers() {
        return boundPlayers;
    }

    public boolean isActive() {
        return this.activated;
    }

    @Override
    public boolean isActivating() {
        return (activationTimer > 0) && (activationTimer < 400);
    }

    public int getHp() {
        return hp;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public int getActivationTimer() {
        return activationTimer;
    }

    @Override
    public int getSpawnRadius() {
        return spawnRadius;
    }

    @Override
    public int getNexusKills() {
        return nexusKills;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public int getNexusLevel() {
        return nexusLevel;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getCookTime() {
        return cookTime;
    }

    @Override
    public int getXCoord() {
        return getPos().getX();
    }

    @Override
    public int getYCoord() {
        return getPos().getY();
    }

    @Override
    public int getZCoord() {
        return getPos().getZ();
    }

    @Override
    public List<EntityIMLiving> getMobList() {
        return mobList;
    }

    public int getActivationProgressScaled(int i) {
        return activationTimer * i / 400;
    }

    public int getGenerationProgressScaled(int i) {
        return generation * i / 3000;
    }

    public int getCookProgressScaled(int i) {
        return cookTime * i / 1200;
    }

    public int getNexusPowerLevel() {
        return powerLevel;
    }

    @Override
    public int getCurrentWave() {
        return currentWave;
    }

    @Override
    public int size() {
        return nexusItemStacks.size();
    }

    @Override
    public void setStack(int i, ItemStack stack) {
        this.nexusItemStacks.setStack(i, stack);
    }

    @Override
    public ItemStack getStack(int i) {
        return nexusItemStacks.getStack(i);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return nexusItemStacks.removeStack(slot, amount);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity entityplayer) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return nexusItemStacks.isEmpty();
    }

    @Override
    public ItemStack removeStack(int slot) {
        return nexusItemStacks.removeStack(slot);
    }

    @Override
    public void clear() {
        nexusItemStacks.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public void tick(ServerWorld world) {
        updateStatus();
        this.attackerAI.update();

        if (mode == 1 || mode == 2 || mode == 3) {
            if (this.resumedFromNBT) {
                boundingBoxToRadius = getChunkBox(world);
                if (mode == 2 && continuousAttack) {
                    if (resumeSpawnerContinuous()) {
                        mobsLeftInWave = (lastMobsLeftInWave += acquireEntities());
                        InvasionMod.log("mobsLeftInWave: " + mobsLeftInWave);
                        InvasionMod.log("mobsToKillInWave: " + mobsToKillInWave);
                    }
                } else {
                    resumeSpawnerInvasion();
                    acquireEntities();
                }
                attackerAI.onResume();
                resumedFromNBT = false;
            }
            try {
                tickCount = (tickCount + 1) % 60;
                if (tickCount == 0) {
                    bindPlayers();
                    updateMobList();
                }

                if (mode == 1 || mode == 3) {
                    doInvasion(50);
                } else if (this.mode == 2) {
                    doContinuous(50);
                }
            } catch (WaveSpawnerException e) {
                InvasionMod.LOGGER.error("Exception occured whilst updating invasion", e);
                stop();
            }
        }

        cleanupTimer = (cleanupTimer + 1) % 40;
        if (cleanupTimer == 0) {
            if (!world.getBlockState(getPos()).isOf(InvBlocks.NEXUS_CORE)) {
                mod_Invasion.setInvasionEnded(this);
                stop();
                markDirty();
                markRemoved();
                InvasionMod.LOGGER.warn("Stranded Nexus entity trying to delete itself...");
            }
        }
    }

    public void emergencyStop() {
        InvasionMod.LOGGER.info("Nexus manually stopped by command");
        stop();
        killAllMobs();
    }

    public void debugStatus() {
        for (Map.Entry<UUID, Long> entry : this.getBoundPlayers().entrySet()) {
            PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
            if (player != null) {
                player.sendMessage(Text.literal("Current Time: " + getWorld().getTime()));
                player.sendMessage(Text.literal("Time to next: " + nextAttackTime));
                player.sendMessage(Text.literal("Days to attack: " + daysToAttack));
                player.sendMessage(Text.literal("Mobs left: " + mobsLeftInWave));
                player.sendMessage(Text.literal("Mode: " + mode));
            }
        }
    }

    public void debugStartInvaion(int startWave) {
        mod_Invasion.tryGetInvasionPermission(this);
        startInvasion(startWave);
        this.activated = true;
    }

    public void createBolt(BlockPos target, int t) {
        getWorld().spawnEntity(new EntityIMBolt(getWorld(), getPos().toCenterPos(), target.toCenterPos(), t, 1));
    }

    public boolean setSpawnRadius(int radius) {
        if ((!waveSpawner.isActive()) && (radius > 8)) {
            spawnRadius = radius;
            waveSpawner.setRadius(radius);
            boundingBoxToRadius = getChunkBox(getWorld());
            return true;
        }

        return false;
    }

    @Override
    public void attackNexus(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            if (this.mode == 1) {
                theEnd();
            }
        }
        while (this.hp + 5 <= this.lastHp) {
            sendMessage(Formatting.DARK_RED, "invmod.message.nexus.hpat", (this.lastHp - 5));
            this.lastHp -= 5;
            playSoundForBoundPlayers(SoundEvents.ENTITY_BLAZE_HURT);
        }
    }

    @Override
    public void registerMobDied() {
        nexusKills++;
        mobsLeftInWave--;
        if (mobsLeftInWave <= 0) {
            if (lastMobsLeftInWave > 0) {
                sendMessage(Formatting.GREEN, "invmod.message.nexus.stableagain");
                sendMessage(Formatting.GREEN, "invmod.message.nexus.unleashingenergy");
                lastMobsLeftInWave = mobsLeftInWave;
            }
            return;
        }
        while (mobsLeftInWave + mobsToKillInWave * 0.1F <= lastMobsLeftInWave) {
            sendMessage(Formatting.GREEN, "invmod.message.nexus.stabilizedto", "" + Formatting.DARK_GREEN + (100 - 100 * mobsLeftInWave / mobsToKillInWave) + "%");
            this.lastMobsLeftInWave = ((int) (lastMobsLeftInWave - mobsToKillInWave * 0.1F));
        }
    }

    public void registerMobClose() {
    }

    @Override
    public void askForRespawn(EntityIMLiving entity) {
        InvasionMod.LOGGER.warn("Stuck entity asking for respawn: " + entity);
        this.waveSpawner.askForRespawn(entity);
    }

    @Override
    public AttackerAI getAttackerAI() {
        return this.attackerAI;
    }

    protected void setActivationTimer(int i) {
        this.activationTimer = i;
    }

    protected void setNexusLevel(int i) {
        this.nexusLevel = i;
    }

    protected void setNexusKills(int i) {
        this.nexusKills = i;
    }

    protected void setGeneration(int i) {
        this.generation = i;
    }

    protected void setNexusPowerLevel(int i) {
        this.powerLevel = i;
    }

    protected void setCookTime(int i) {
        this.cookTime = i;
    }

    protected void setWave(int wave) {
        this.currentWave = wave;
    }

    private void startInvasion(int startWave) {
        this.boundingBoxToRadius = computeSpawnArea();
        if (mode == 2 && continuousAttack) {
            sendWarning("invmod.message.nexus.alreadyactivated");
            return;
        }

        if ((this.mode == 0) || (this.mode == 2)) {
            if (this.waveSpawner.isReady()) {
                try {
                    this.currentWave = startWave;
                    this.waveSpawner.beginNextWave(this.currentWave);
                    if (this.mode == 0)
                        setMode(1);
                    else {
                        setMode(3);
                    }
                    bindPlayers();
                    this.hp = this.maxHp;
                    this.lastHp = this.maxHp;
                    this.waveDelayTimer = -1L;
                    String boundPlayers = Formatting.AQUA + "";
                    for (UUID playerId : getBoundPlayers().keySet()) {
                        PlayerEntity player = getWorld().getPlayerByUuid(playerId);
                        if (player != null) {
                            boundPlayers += player.getGameProfile().getName() + Formatting.DARK_AQUA + ", " + Formatting.AQUA;
                        }
                    }
                    boundPlayers = boundPlayers.substring(0, boundPlayers.length() - 4);
                    sendMessage(Formatting.DARK_AQUA, "invmod.message.nexus.listboundplayers", boundPlayers);
                    sendWarning("invmod.message.nexus.firstwavesoon");
                    playSoundForBoundPlayers("invmod:rumble1");
                } catch (WaveSpawnerException e) {
                    stop();
                    InvasionMod.log(e.getMessage());
                    sendNotice(e.getMessage());
                }
            } else {
                InvasionMod.log("Wave spawner is not in ready state");
            }
        } else {
            InvasionMod.log("Tried to activate Nexus while already active");
        }
    }

    private void startContinuousPlay() {
        this.boundingBoxToRadius = getChunkBox(getWorld());
        if (mode == 4 && waveSpawner.isReady() && mod_Invasion.tryGetInvasionPermission(this)) {
            setMode(2);
            hp = maxHp;
            lastHp = maxHp;
            lastPowerLevel = powerLevel;
            lastWorldTime = getWorld().getTime();
            nextAttackTime = ((int) (lastWorldTime / 24000L * 24000L) + 14000);
            if ((this.lastWorldTime % 24000L > 12000L) && (lastWorldTime % 24000L < 16000L)) {
                sendWarning("invmod.message.nexus.nightlooming");
            } else {
                sendWarning("invmod.message.nexus.activatedandstable");
            }
        } else {
            sendWarning("invmod.message.nexus.couldnotactivate");
        }
    }

    private void doInvasion(int elapsed) throws WaveSpawnerException {
        if (waveSpawner.isActive()) {
            if (hp <= 0) {
                theEnd();
            } else {
                generateFlux(1);
                if (waveSpawner.isWaveComplete()) {
                    if (waveDelayTimer == -1L) {
                        sendMessage(Formatting.GREEN, "invmod.message.wave.complete", "" + Formatting.DARK_GREEN + this.currentWave);
                        playSoundForBoundPlayers("invmod:chime1");
                        waveDelayTimer = 0L;
                        waveDelay = waveSpawner.getWaveRestTime();
                    } else {
                        waveDelayTimer += elapsed;
                        if (waveDelayTimer > waveDelay) {
                            currentWave += 1;
                            sendWarning("invmod.message.wave.begin", "" + Formatting.DARK_RED + currentWave);
                            waveSpawner.beginNextWave(currentWave);
                            waveDelayTimer = -1L;
                            playSoundForBoundPlayers("invmod:rumble1");
                            if (currentWave > nexusLevel) {
                                nexusLevel = currentWave;
                            }
                        }
                    }
                } else {
                    waveSpawner.spawn(elapsed);
                }
            }
        }
    }

    @Deprecated
    private void playSoundForBoundPlayers(String sound) {
        playSoundForBoundPlayers(Registries.SOUND_EVENT.get(Identifier.of(sound)));
    }

    private void playSoundForBoundPlayers(SoundEvent sound) {
        for (Map.Entry<UUID, Long> entry : getBoundPlayers().entrySet()) {
            try {
                PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
                if (player != null) {
                    player.getWorld().playSound(null, player.getBlockPos(), sound, SoundCategory.AMBIENT, 1F, 1F);
                }
            } catch (Exception e) {
                InvasionMod.LOGGER.error("Problem while trying to play sound " + sound + " at player " + entry.getKey(), e);
            }
        }
    }

    private void doContinuous(int elapsed) {
        powerLevelTimer += elapsed;
        if (powerLevelTimer > 2200) {
            powerLevelTimer -= 2200;
            generateFlux(5 + (int) (5 * powerLevel / 1550F));
            if (!getStack(0).isOf(InvItems.DAMPING_AGENT)) {
                powerLevel++;
            }
        }

        if (getStack(0).isOf(InvItems.STRONG_DAMPING_AGENT) && powerLevel >= 0 && !continuousAttack && --powerLevel < 0) {
            stop();
        }

        if (!this.continuousAttack) {
            long currentTime = getWorld().getTime();
            int timeOfDay = (int) (this.lastWorldTime % 24000L);
            if ((timeOfDay < 12000) && (currentTime % 24000L >= 12000L) && (currentTime + 12000L > this.nextAttackTime)) {
                sendWarning("invmod.message.nexus.nightlooming");
            }
            if (this.lastWorldTime > currentTime) {
                this.nextAttackTime = ((int) (this.nextAttackTime - (this.lastWorldTime - currentTime)));
            }
            this.lastWorldTime = currentTime;

            if (this.lastWorldTime >= this.nextAttackTime) {
                float difficulty = 1.0F + this.powerLevel / 4500;
                float tierLevel = 1.0F + this.powerLevel / 4500;
                int timeSeconds = 240;
                try {
                    Wave wave = this.waveBuilder.generateWave(difficulty, tierLevel, timeSeconds);
                    this.mobsLeftInWave = (this.lastMobsLeftInWave = this.mobsToKillInWave = (int) (wave
                            .getTotalMobAmount() * 0.8F));
                    this.waveSpawner.beginNextWave(wave);
                    this.continuousAttack = true;
                    int days = getWorld().getRandom().nextBetween(config.minContinuousModeDays, config.maxContinuousModeDays);
                    this.nextAttackTime = ((int) (currentTime / 24000L * 24000L) + 14000 + days * 24000);
                    this.hp = (this.lastHp = 100);
                    this.zapTimer = 0;
                    this.waveDelayTimer = -1L;
                    sendWarning("invmod.message.nexus.destabilizing");
                    playSoundForBoundPlayers("invmod:rumble1");
                } catch (WaveSpawnerException e) {
                    InvasionMod.log(e.getMessage());
                    e.printStackTrace();
                    stop();
                }

            }

        } else if (this.hp <= 0) {
            this.continuousAttack = false;
            continuousNexusHurt();
        } else if (this.waveSpawner.isWaveComplete()) {

            if (this.waveDelayTimer == -1L) {
                this.waveDelayTimer = 0L;
                this.waveDelay = this.waveSpawner.getWaveRestTime();
            } else {

                this.waveDelayTimer += elapsed;
                if ((this.waveDelayTimer > this.waveDelay) && (this.zapTimer < -200)) {
                    this.waveDelayTimer = -1L;
                    this.continuousAttack = false;
                    this.waveSpawner.stop();
                    this.hp = 100;
                    this.lastHp = 100;
                    this.lastPowerLevel = this.powerLevel;
                }
            }

            this.zapTimer -= 1;
            if (this.mobsLeftInWave <= 0) {
                if ((this.zapTimer <= 0) && (zapEnemy(1))) {
                    zapEnemy(0);
                    this.zapTimer = 23;
                }
            }
        } else {
            try {
                this.waveSpawner.spawn(elapsed);
            } catch (WaveSpawnerException e) {
                InvasionMod.LOGGER.error("Exception occured whilst spawning wave", e);
                stop();
            }
        }
    }

    @Override
    public void sendMessage(Formatting color, String translationKey, Object...params) {
        for (Map.Entry<UUID, Long> entry : getBoundPlayers().entrySet()) {
            PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
            if (player != null) {
                player.sendMessage(Text.translatable(translationKey, params).formatted(color));
            }
        }
    }

    private void updateStatus() {
        ItemStack firstStack = getStack(0);
        ItemStack secondStack = getStack(1);

        if (!firstStack.isEmpty()) {
            if (firstStack.isOf(InvItems.EMPTY_TRAP)) {
                if (cookTime < 1200) {
                    cookTime += mode == 0 ? 1 : 9;
                } else {
                    if (secondStack.isEmpty()) {
                        setStack(1, secondStack = InvItems.FLAME_TRAP.getDefaultStack());
                        firstStack.decrement(1);
                        cookTime = 0;
                    } else if (secondStack.isOf(InvItems.FLAME_TRAP) && secondStack.getCount() < secondStack.getMaxCount()) {
                        secondStack.increment(1);
                        firstStack.decrement(1);
                        cookTime = 0;
                    }
                }
            } else if (firstStack.isOf(InvItems.RIFT_FLUX)) {
                if (cookTime < 1200 && nexusLevel >= 10) {
                    cookTime += 5;
                }

                if (cookTime >= 1200) {
                    if (secondStack.isEmpty()) {
                        setStack(1, secondStack = InvItems.STRONG_NEXUS_CATALYST.getDefaultStack());
                        firstStack.decrement(1);
                        cookTime = 0;
                    }
                }
            }
        } else {
            cookTime = 0;
        }

        if (activationTimer >= 400) {
            activationTimer = 0;
            if ((mod_Invasion.tryGetInvasionPermission(this)) && !firstStack.isEmpty()) {
                if (firstStack.isOf(InvItems.NEXUS_CATALYST)) {
                    firstStack.decrement(1);
                    activated = true;
                    startInvasion(1);
                } else if (firstStack.isOf(InvItems.STRONG_NEXUS_CATALYST)) {
                    firstStack.decrement(1);
                    activated = true;
                    startInvasion(10);
                } else if (firstStack.isOf(InvItems.STABLE_NEXUS_CATALYST)) {
                    firstStack.decrement(1);
                    activated = true;
                    startContinuousPlay();
                }
            }

        } else if (mode == 0 || mode == 4) {
            if (!firstStack.isEmpty()) {
                if (firstStack.isOf(InvItems.NEXUS_CATALYST) || firstStack.isOf(InvItems.STRONG_NEXUS_CATALYST)) {
                    activationTimer++;
                    mode = 0;
                } else if (firstStack.isOf(InvItems.STABLE_NEXUS_CATALYST)) {
                    activationTimer++;
                    mode = 4;
                }
            } else {
                activationTimer = 0;
            }
        } else if (mode == 2) {
            if (!firstStack.isEmpty()) {
                if (firstStack.isOf(InvItems.NEXUS_CATALYST) || firstStack.isOf(InvItems.STRONG_NEXUS_CATALYST)) {
                    activationTimer++;
                }
            } else {
                activationTimer = 0;
            }
        }
    }

    private void generateFlux(int increment) {
        generation += increment;
        if (generation >= 3000) {
            ItemStack currentGeneratedItem = getStack(1);
            if (currentGeneratedItem.isEmpty()) {
                setStack(1, InvItems.RIFT_FLUX.getDefaultStack());
                generation -= 3000;
            } else if (currentGeneratedItem.isOf(InvItems.RIFT_FLUX)) {
                currentGeneratedItem.increment(1);
                generation -= 3000;
            }

        }
    }

    private void stop() {
        if (mode == 3) {
            setMode(2);
            int days = getWorld().getRandom().nextBetween(config.minContinuousModeDays, config.maxContinuousModeDays);
            this.nextAttackTime = ((int) (getWorld().getTime() / 24000L * 24000L) + 14000 + days * 24000);
        } else {
            setMode(0);
        }

        waveSpawner.stop();
        mod_Invasion.setInvasionEnded(this);
        activationTimer = 0;
        currentWave = 0;
        activated = false;
    }

    private void bindPlayers() {
        for (PlayerEntity entityPlayer : getWorld().getEntitiesByClass(PlayerEntity.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            long time = System.currentTimeMillis();
            if (!boundPlayers.containsKey(entityPlayer.getUuid())) {
                boundPlayers.put(entityPlayer.getUuid(), time);
                sendNotice("invmod.message.nexus.lifenowbound", Formatting.GREEN + entityPlayer.getDisplayName().getString() + (entityPlayer.getDisplayName().getString().toLowerCase().endsWith("s") ? "'" : "'s"));
            } else if (time - boundPlayers.get(entityPlayer.getUuid()) > BIND_EXPIRE_TIME) {
                boundPlayers.put(entityPlayer.getUuid(), time);
                sendNotice("invmod.message.nexus.lifenowbound", Formatting.GREEN + entityPlayer.getDisplayName().getString() + (entityPlayer.getDisplayName().getString().toLowerCase().endsWith("s") ? "'" : "'s"));
            }
        }
    }

    private void updateMobList() {
        mobList.clear();
        this.mobList.addAll(getWorld().getEntitiesByClass(EntityIMLiving.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
        this.mobsSorted = false;
    }

    protected void setMode(int i) {
        this.mode = i;
        setActive(mode != 0);
    }

    private void setActive(boolean flag) {
        if (getWorld() instanceof ServerWorld sw) {
            sw.setBlockState(getPos(), getCachedState().with(BlockNexus.LIT, flag));
        }
    }

    private int acquireEntities() {
        List<EntityIMLiving> entities = getWorld().getEntitiesByClass(EntityIMLiving.class, boundingBoxToRadius.expand(10.0D, 128.0D, 10.0D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        for (EntityIMLiving entity : entities) {
            entity.acquiredByNexus(this);
        }
        InvasionMod.log("Acquired " + entities.size() + " entities after state restore");
        return entities.size();
    }

    private void theEnd() {
        if (!getWorld().isClient) {
            sendWarning("invmod.message.nexus.destroyed");
            stop();
            long time = System.currentTimeMillis();
            for (Map.Entry<UUID, Long> entry : getBoundPlayers().entrySet()) {
                if (time - entry.getValue() < BIND_EXPIRE_TIME) {
                    PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
                    if (player != null) {
                        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.AMBIENT, 4, 1);
                        player.damage(player.getWorld().getDamageSources().magic(), 500.0F);
                    } else if (getWorld() instanceof ServerWorld sw) {
                        BountyHunter.of(sw).add(entry.getKey());
                    }
                }
            }

            boundPlayers.clear();
            killAllMobs();
        }
    }

    private void continuousNexusHurt() {
        sendWarning("invmod.message.nexus.severelydamaged");
        for (Map.Entry<UUID, Long> entry : boundPlayers.entrySet()) {
            PlayerEntity player = getWorld().getPlayerByUuid(entry.getKey());
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.AMBIENT, 4, 1);
        }
        killAllMobs();
        this.waveSpawner.stop();
        this.powerLevel = ((int) ((this.powerLevel - (this.powerLevel - this.lastPowerLevel)) * 0.7F));
        this.lastPowerLevel = this.powerLevel;
        if (this.powerLevel < 0) {
            this.powerLevel = 0;
            stop();
        }
    }

    private void killAllMobs() {
        DamageSource source = getWorld().getDamageSources().magic();
        // monsters
        for (EntityIMLiving mob : getWorld().getEntitiesByClass(EntityIMLiving.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            mob.damage(source, mob.getMaxHealth());
            mob.kill();
        }

        // wolves
        for (EntityIMWolf wolf : getWorld().getEntitiesByClass(EntityIMWolf.class, boundingBoxToRadius, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
            wolf.damage(source, wolf.getMaxHealth());
            wolf.kill();
        }
    }

    private boolean zapEnemy(int sfx) {
        if (mobList.isEmpty()) {
            return false;
        }

        if (!mobsSorted) {
            Collections.sort(mobList, ComparatorDistanceFrom.ofComparisonEntities(this));
        }
        EntityIMLiving mob = mobList.removeLast();
        mob.damage(mob.getDamageSources().magic(), 500);
        getWorld().spawnEntity(new EntityIMBolt(getWorld(), getPos().toCenterPos(), mob.getEyePos(), 15, sfx));
        return true;
    }

    private boolean resumeSpawnerContinuous() {
        try {
            mod_Invasion.tryGetInvasionPermission(this);
            float difficulty = 1 + powerLevel / 4500F;
            float tierLevel = 1 + powerLevel / 4500F;
            int timeSeconds = 240;
            Wave wave = waveBuilder.generateWave(difficulty, tierLevel, timeSeconds);
            this.mobsToKillInWave = ((int) (wave.getTotalMobAmount() * 0.8F));
            mod_Invasion.log("Original mobs to kill: " + mobsToKillInWave);
            lastMobsLeftInWave = mobsToKillInWave - waveSpawner.resumeFromState(wave, spawnerElapsedRestore, spawnRadius);
            mobsLeftInWave = lastMobsLeftInWave;
            return true;
        } catch (WaveSpawnerException e) {
            mod_Invasion.log("Error resuming spawner: " + e.getMessage());
            this.waveSpawner.stop();
            return false;
        } finally {
            mod_Invasion.setInvasionEnded(this);
        }
    }

    private boolean resumeSpawnerInvasion() {
        try {
            mod_Invasion.tryGetInvasionPermission(this);
            this.waveSpawner.resumeFromState(this.currentWave, this.spawnerElapsedRestore, this.spawnRadius);
            return true;
        } catch (WaveSpawnerException e) {
            InvasionMod.LOGGER.error("Error resuming spawner", e);
            stop();
            return false;
        } finally {
            mod_Invasion.setInvasionEnded(this);
        }
    }

    @Override
    protected void readNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        InvasionMod.log("Restoring TileEntityNexus from NBT");
        super.readNbt(compound, lookup);
        nexusItemStacks.readNbtList(compound.getList("Items", NbtElement.COMPOUND_TYPE), lookup);
        boundPlayers.clear();
        compound.getList("boundPlayers", NbtElement.COMPOUND_TYPE).forEach(tag -> {
            NbtCompound nbt = (NbtCompound)tag;
            boundPlayers.put(nbt.getUuid("uuid"), nbt.getLong("time"));
            InvasionMod.log("Added bound player: " + nbt.getUuid("uuid"));
        });

        activationTimer = compound.getInt("activationTimer");
        mode = compound.getInt("mode");
        currentWave = compound.getInt("currentWave");
        spawnRadius = compound.getInt("spawnRadius");
        waveSpawner.setRadius(spawnRadius);
        boundingBoxToRadius = computeSpawnArea();

        nexusLevel = compound.getInt("nexusLevel");
        hp = compound.getInt("hp");
        nexusKills = compound.getInt("nexusKills");
        generation = compound.getInt("generation");
        powerLevel = compound.getInt("powerLevel");
        lastPowerLevel = compound.getInt("lastPowerLevel");
        nextAttackTime = compound.getInt("nextAttackTime");
        daysToAttack = compound.getInt("daysToAttack");
        continuousAttack = compound.getBoolean("continuousAttack");
        activated = compound.getBoolean("activated");

        mod_Invasion.log("activationTimer = " + this.activationTimer);
        mod_Invasion.log("mode = " + this.mode);
        mod_Invasion.log("currentWave = " + this.currentWave);
        mod_Invasion.log("spawnRadius = " + this.spawnRadius);
        mod_Invasion.log("nexusLevel = " + this.nexusLevel);
        mod_Invasion.log("hp = " + this.hp);
        mod_Invasion.log("nexusKills = " + this.nexusKills);
        mod_Invasion.log("powerLevel = " + this.powerLevel);
        mod_Invasion.log("lastPowerLevel = " + this.lastPowerLevel);
        mod_Invasion.log("nextAttackTime = " + this.nextAttackTime);


        if (mode == 1 || mode == 3 || (mode == 2 && continuousAttack)) {
            InvasionMod.log("Nexus is active; flagging for restore");
            resumedFromNBT = true;
            spawnerElapsedRestore = compound.getLong("spawnerElapsed");
            InvasionMod.log("spawnerElapsed = " + spawnerElapsedRestore);
        }

        attackerAI.readFromNBT(compound);
    }

    @Override
    public void writeNbt(NbtCompound compound, RegistryWrapper.WrapperLookup lookup) {
        if (this.mode != 0) {
            mod_Invasion.setNexusUnloaded(this);
        }
        super.writeNbt(compound, lookup);
        compound.putInt("activationTimer", getActivationTimer());
        compound.putInt("currentWave", getCurrentWave());
        compound.putInt("spawnRadius", getSpawnRadius());
        compound.putInt("nexusLevel", getNexusLevel());
        compound.putInt("hp", getHp());
        compound.putInt("nexusKills", getNexusKills());
        compound.putInt("generation", getGeneration());
        compound.putLong("spawnerElapsed", waveSpawner.getElapsedTime());
        compound.putInt("mode", getMode());
        compound.putInt("powerLevel", getPowerLevel());
        compound.putInt("lastPowerLevel", lastPowerLevel);
        compound.putInt("nextAttackTime", nextAttackTime);
        compound.putInt("daysToAttack", daysToAttack);
        compound.putBoolean("continuousAttack", continuousAttack);
        compound.putBoolean("activated", isActive());
        compound.put("Items", nexusItemStacks.toNbtList(lookup));

        NbtList boundPlayers = new NbtList();
        for (Map.Entry<UUID, Long> entry : this.boundPlayers.entrySet()) {
            NbtCompound c = new NbtCompound();
            c.putUuid("uuid", entry.getKey());
            c.putLong("time", entry.getValue());
            boundPlayers.add(c);
        }
        compound.put("boundPlayers", boundPlayers);

        attackerAI.writeToNBT(compound);
    }
}