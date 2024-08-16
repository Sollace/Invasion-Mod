package com.invasion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.EntityIMLiving;
import com.invasion.nexus.Combatant;
import com.invasion.nexus.IEntityIMPattern;
import com.invasion.nexus.wave.IMWaveBuilder;
import com.invasion.util.ISelect;
import com.invasion.util.RandomSelectionPool;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ConfigInvasion extends Config {
    private static final Map<String, Integer> DEFAULT_MOB_HEALTHS = Util.make(new HashMap<>(), m -> {
        m.put("IMCreeper-T1", 20);
        m.put("IMVulture-T1", 20);
        m.put("IMImp-T1", 20);
        m.put("IMPigManEngineer-T1", 20);
        m.put("IMSkeleton-T1", 20);
        m.put("IMSpider-T1-Spider", 18);
        m.put("IMSpider-T1-Baby-Spider", 2);
        m.put("IMSpider-T2-Jumping-Spider", 18);
        m.put("IMSpider-T2-Mother-Spider", 23);
        m.put("IMThrower-T1", 50);
        m.put("IMThrower-T2", 70);
        m.put("IMZombie-T1", 20);
        m.put("IMZombie-T2", 30);
        m.put("IMZombie-T3", 65);
        m.put("IMZombiePigman-T1", 20);
        m.put("IMZombiePigman-T2", 30);
        m.put("IMZombiePigman-T3", 65);
    });
    private static final String[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS = {
            "zombie_t1_any", "zombie_t2_any_basic",
            "zombie_t2_plain", "zombie_t2_tar", "zombie_t2_pigman", "zombie_t3_any",
            "zombiePigman_t1_any", "zombiePigman_t2_any", "zombiePigman_t3_any",
            "spider_t1_any", "spider_t2_any", "pigengy_t1_any",
            "skeleton_t1_any",
            "thrower_t1", "thrower_t2",
            "creeper_t1_basic",
            "imp_t1"
    };
    private static final float[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS = {
            1, 1, 0, 0, 0, 0, 0, 0, 0, 0.5F, 0, 0, 0, 0, 0, 0, 0
    };
    private static final boolean DEFAULT_CRAFT_ITEMS_ENABLED = true;
    private static final boolean DEFAULT_NIGHT_SPAWNS_ENABLED = false;
    private static final int DEFAULT_MIN_CONT_MODE_DAYS = 2;
    private static final int DEFAULT_MAX_CONT_MODE_DAYS = 3;
    private static final int DEFAULT_NIGHT_MOB_SIGHT_RANGE = 20;
    private static final int DEFAULT_NIGHT_MOB_SENSE_RANGE = 8;
    private static final int DEFAULT_NIGHT_MOB_SPAWN_CHANCE = 30;
    private static final int DEFAULT_NIGHT_MOB_MAX_GROUP_SIZE = 3;
    private static final int DEFAULT_NIGHT_MOB_LIMIT_OVERRIDE = 70;
    private static final float DEFAULT_NIGHT_MOB_STATS_SCALING = 1.0F;
    private static final boolean DEFAULT_NIGHT_MOBS_BURN = true;

    private final Map<Identifier, Float> strengthOverrides = new HashMap<>();

    public boolean enableLog;
    public boolean debugMode;
    public boolean destructedBlocksDrop = true;
    public boolean updateNotifications;

    @Deprecated
    public boolean craftItemsEnabled = DEFAULT_CRAFT_ITEMS_ENABLED;
    public int minContinuousModeDays = DEFAULT_MIN_CONT_MODE_DAYS;
    public int maxContinuousModeDays = DEFAULT_MAX_CONT_MODE_DAYS;

    public boolean nightSpawnsEnabled = DEFAULT_NIGHT_SPAWNS_ENABLED;
    public int nightMobSightRange = DEFAULT_NIGHT_MOB_SIGHT_RANGE;
    public int nightMobSenseRange = DEFAULT_NIGHT_MOB_SENSE_RANGE;
    public int nightMobSpawnChance = DEFAULT_NIGHT_MOB_SPAWN_CHANCE;
    public int nightMobMaxGroupSize = DEFAULT_NIGHT_MOB_MAX_GROUP_SIZE;
    public int maxNightMobs = DEFAULT_NIGHT_MOB_LIMIT_OVERRIDE;
    public boolean nightMobsBurnInDay = DEFAULT_NIGHT_MOBS_BURN;

    private final Map<String, Integer> mobHealthNightspawn = new HashMap<>();
    private final Map<String, Integer> mobHealthInvasion = new HashMap<>();

    @Nullable
    private ISelect<IEntityIMPattern> spawnPool;

    public Optional<Float> getBlockStrength(Block block) {
        return Optional.ofNullable(strengthOverrides.get(Registries.BLOCK.getId(block)));
    }

    public Optional<Float> getBlockCost(Block block) {
        return getBlockStrength(block).map(strength -> 1 + strength * 0.4F);
    }

    public int getHealth(String mobName, boolean nightTime) {
        return (nightTime ? mobHealthNightspawn : mobHealthInvasion).getOrDefault(mobName, DEFAULT_MOB_HEALTHS.getOrDefault(mobName, 20));
    }

    public int getHealth(Combatant mob) {
        // TODO:
        return getHealth(mob.getLegacyName(), !mob.hasNexus());
    }

    public synchronized ISelect<IEntityIMPattern> getSpawnPool() {
        if (spawnPool == null) {
            spawnPool = loadSpawnPool();
        }
        return spawnPool;
    }

    @Override
    public void loadConfig(File file) {
        super.loadConfig(file);
        mobHealthNightspawn.clear();
        mobHealthInvasion.clear();
        keySet().forEach(key -> {
            if (key.startsWith("block-") && key.endsWith("-strength")) {
                Identifier id = Identifier.tryParse(key.split("-")[1]);
                if (id != null) {
                    float strength = getPropertyValueFloat(key, 0);
                    if (strength > 0) {
                        strengthOverrides.put(id, strength);
                    }
                }
            }
            if (key.endsWith("-nightSpawn-health")) {
                String mobName = key.replace("-nightSpawn-health", "");
                int health = getPropertyValueInt(key, 0);
                if (health > 0) {
                    mobHealthNightspawn.put(mobName, health);
                }
            }
            if (key.endsWith("-invasionSpawn-health")) {
                String mobName = key.replace("-invasionSpawn-health", "");
                int health = getPropertyValueInt(key, 0);
                if (health > 0) {
                    mobHealthInvasion.put(mobName, health);
                }
            }
        });

        enableLog = getPropertyValueBoolean("enable-log-file", false);
        destructedBlocksDrop = getPropertyValueBoolean("destructed-blocks-drop", true);
        updateNotifications = getPropertyValueBoolean("update-messages-enabled", false);
        // soundsEnabled = configInvasion.getPropertyValueBoolean("sounds-enabled", true);
        craftItemsEnabled = getPropertyValueBoolean("craft-items-enabled", true);
        debugMode = getPropertyValueBoolean("debug", false);

        minContinuousModeDays = getPropertyValueInt("min-days-to-attack", 2);
        maxContinuousModeDays = getPropertyValueInt("max-days-to-attack", 3);

        nightSpawnsEnabled = getPropertyValueBoolean("night-spawns-enabled", false);
        nightMobSightRange = getPropertyValueInt("night-mob-sight-range", 20);
        nightMobSenseRange = getPropertyValueInt("night-mob-sense-range", 12);
        nightMobSpawnChance = getPropertyValueInt("night-mob-spawn-chance", 30);
        nightMobMaxGroupSize = getPropertyValueInt("night-mob-max-group-size", 3);
        maxNightMobs = getPropertyValueInt("mob-limit-override", 70);
        nightMobsBurnInDay = getPropertyValueBoolean("night-mobs-burn-in-day", true);
        spawnPool = loadSpawnPool();
        saveConfig(file);
    }

    private ISelect<IEntityIMPattern> loadSpawnPool() {
        RandomSelectionPool<IEntityIMPattern> pool = new RandomSelectionPool<>();
        if (DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length == DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS.length) {
            for (int i = 0; i < DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length; i++) {
                String pattern = getPropertyValueString("nm-spawnpool1-slot" + (1 + i), DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS[i]);
                float weight = getPropertyValueFloat("nm-spawnpool1-slot" + (1 + i) + "-weight", DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS[i]);

                if (IMWaveBuilder.isPatternNameValid(pattern)) {
                    InvasionMod.log("Added entry for pattern 1 slot " + (i + 1));
                    pool.addEntry(IMWaveBuilder.getPattern(pattern), weight);
                } else {
                    InvasionMod.LOGGER.warn("Pattern 1 slot " + (i + 1) + " in config not recognized. Proceeding as blank.");
                    setProperty("nm-spawnpool1-slot" + (1 + i), "none");
                }
            }
        } else {
            InvasionMod.LOGGER.warn("Mob pattern table element mismatch. Ensure each slot has a probability weight");
        }
        return pool;
    }

    private void saveConfig(File saveFile) {
        try (var writer = new BufferedWriter(new FileWriter(saveFile))) {
            writeLine(writer, "# Invasion Mod config");
            writeLine(writer, "# Delete this file to restore defaults");
            writer.newLine();
            writeLine(writer, "# General settings and IDs");
            writeProperty(writer, "update-messages-enabled", "Update-messages-enabled is currently unused");
            writeProperty(writer, "destructed-blocks-drop");
            writeProperty(writer, "enable-log-file");
            writeProperty(writer, "craft-items-enabled");
            writeProperty(writer, "guiID-Nexus");
            if (debugMode) {
                writeProperty(writer, "debug");
            }

            writeLine(writer, "# Nexus Continuous Mode");
            writeProperty(writer, "min-days-to-attack");
            writeProperty(writer, "max-days-to-attack");
            writeLine(writer, "# Mob health during invasion");

            for (var pairs : mobHealthInvasion.entrySet()) {
                writeProperty(writer, pairs.getKey().toString());
            }
            writer.newLine();
            writeLine(writer, "# Mob health during invasion (at night)");
            for (var pairs : mobHealthNightspawn.entrySet()) {
                writeProperty(writer, pairs.getKey().toString());
            }
            writer.newLine();

            // Block strength options
            writeLine(writer, "# Block strengths");
            writeLine(writer, "# Add entries here for other mods' blocks");
            writeLine(writer, "# Reference values: minecraft:dirt=3.125, minecraft:gravel=2.5, minecraft:obsidian=7.7, minecraft:stone=5.5 (plus up to 50% from special)");
            writeLine(writer, "# Format:  block-<namespace>:<id>-strength=<strength>");
            if (strengthOverrides.size() == 0) {
                writeLine(writer, "# First example, reinforced stone from IC2 (remove comment symbol '#')");
                writeLine(writer, "# block231-strength=10.5");
            } else {
                for (var entry : strengthOverrides.entrySet()) {
                    writer.write(entry.getKey() + "-strength=" + entry.getValue());
                    writer.newLine();
                }
            }
            writer.newLine();

            writeLine(writer, "# Nighttime mob spawning behaviour (does not affect the nexus)");
            writeProperty(writer, "mob-limit-override", "mob-limit-override: The maximum number of randomly spawned mobs that may exist in the world. This applies to ALL of minecraft (default: 70)");
            writeProperty(writer, "night-spawns-enabled", "night-spawns-enabled: Currently does not remove any default mobs, only adds new spawns");
            writeProperty(writer, "night-mob-spawn-chance", "night-mob-spawn-chance: Higher number means mobs are more common");
            writeProperty(writer, "night-mob-max-group-size", "night-mob-group-size: The maximum number of mobs that may spawn together");
            writeProperty(writer, "night-mob-sight-range", "night-mob-sight-range: How far mobs can see a player from");
            writeProperty(writer, "night-mob-sense-range", "night-mob-sense-range: How far mobs can smell a player (trough walls)");
            writer.newLine();

            writeLine(writer, "# Nightime mob spawning tables (also does not affect the nexus)");
            writeLine(writer, "# A spawnpool contains mobs that can possibly spawn, and the probability weight of them spawning.");
            writeLine(writer, "# Expenation: zombie_t2_any_basic has all T2, zombie_t2_plain excludes tar zombies");
            for (int i = 0; i < DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length; i++) {
                writeProperty(writer, "nm-spawnpool1-slot" + (1 + i));
                writeProperty(writer, "nm-spawnpool1-slot" + (1 + i) + "-weight");
            }
            writer.flush();
        } catch (IOException e) {
            InvasionMod.LOGGER.error("Could not save config", e);
        }
    }
}