package invmod.common.nexus;

import invmod.common.mod_Invasion;
import invmod.common.util.FiniteSelectionPool;
import invmod.common.util.ISelect;
import invmod.common.util.RandomSelectionPool;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IMWaveBuilder {
    public static final int WAVES_DEFINED = 11;
    private static final float ZOMBIE_T1_WEIGHT = 1.0F;
    private static final float ZOMBIE_T2_WEIGHT = 2.0F;
    private static final float SPIDER_T1_WEIGHT = 1.0F;
    private static final float SPIDER_T2_WEIGHT = 2.0F;
    private static final Map<String, IEntityIMPattern> commonPatterns = Util.make(new HashMap<>(), commonPatterns -> {
        commonPatterns.put("zombie_t1_any", new EntityPattern(IMEntityType.ZOMBIE).addTier(1, 1.0F).addFlavour(0, 3.0F).addFlavour(1, 1.0F));
        commonPatterns.put("zombie_t2_any_basic", new EntityPattern(IMEntityType.ZOMBIE).addTier(2, 1.0F).addFlavour(0, 2.0F).addFlavour(1, 1.0F).addFlavour(2, 0.4F));
        commonPatterns.put("zombie_t2_plain", new EntityPattern(IMEntityType.ZOMBIE).addTier(2, 1.0F).addFlavour(0, 1.0F));
        commonPatterns.put("zombie_t2_tar", new EntityPattern(IMEntityType.ZOMBIE).addTier(2, 1.0F).addFlavour(2, 1.0F).addTexture(5, 1.0F));
        commonPatterns.put("zombie_t3_any", new EntityPattern(IMEntityType.ZOMBIE).addTier(3, 1.0F).addTexture(0, 1.0F));
        commonPatterns.put("zombiePigman_t1_any", new EntityPattern(IMEntityType.ZOMBIEPIGMAN).addTier(1, 1.0F).addFlavour(0, 1.0F));
        commonPatterns.put("zombiePigman_t2_any", new EntityPattern(IMEntityType.ZOMBIEPIGMAN).addTier(2, 1.0F).addFlavour(0, 1.0F));
        commonPatterns.put("zombiePigman_t3_any", new EntityPattern(IMEntityType.ZOMBIEPIGMAN).addTier(3, 1.0F).addFlavour(0, 1.0F));
        commonPatterns.put("spider_t1_any", new EntityPattern(IMEntityType.SPIDER).addTier(1, 1.0F));
        commonPatterns.put("spider_t2_any", new EntityPattern(IMEntityType.SPIDER).addTier(2, 1.0F).addFlavour(0, 1.0F).addFlavour(1, 1.0F));
        commonPatterns.put("pigengy_t1_any", new EntityPattern(IMEntityType.PIG_ENGINEER).addTier(1, 1.0F));
        commonPatterns.put("skeleton_t1_any", new EntityPattern(IMEntityType.SKELETON).addTier(1, 1.0F));
        commonPatterns.put("thrower_t1", new EntityPattern(IMEntityType.THROWER).addTier(1, 1.0F));
        commonPatterns.put("thrower_t2", new EntityPattern(IMEntityType.THROWER).addTier(2, 1.0F));
        commonPatterns.put("burrower", new EntityPattern(IMEntityType.BURROWER).addTier(1, 1.0F));
        commonPatterns.put("creeper_t1_basic", new EntityPattern(IMEntityType.CREEPER).addTier(1, 1.0F));
        commonPatterns.put("imp_t1", new EntityPattern(IMEntityType.IMP).addTier(1, 1.0F));
    });
    private Random rand;

    public IMWaveBuilder() {
        this.rand = new Random();
    }

    public Wave generateWave(float difficulty, float tierLevel, int lengthSeconds) {
        float basicMobsPerSecond = 0.12F * difficulty;
        int numberOfGroups = 7;
        int numberOfBigGroups = 1;
        float proportionInGroups = 0.5F;
        int mobsPerGroup = Math.round(
                proportionInGroups * basicMobsPerSecond * lengthSeconds / (numberOfGroups + numberOfBigGroups * 2));
        int mobsPerBigGroup = mobsPerGroup * 2;
        int remainingMobs = (int) (basicMobsPerSecond * lengthSeconds) - mobsPerGroup * numberOfGroups - mobsPerBigGroup * numberOfBigGroups;
        int mobsPerSteady = Math.round(0.7F * remainingMobs / numberOfGroups);
        int extraMobsForFinale = Math.round(0.3F * remainingMobs);
        int extraMobsForCleanup = (int) (basicMobsPerSecond * lengthSeconds * 0.2F);
        float timeForGroups = 0.5F;
        int groupTimeInterval = (int) (lengthSeconds * 1000 * timeForGroups / (numberOfGroups + numberOfBigGroups * 3));
        int steadyTimeInterval = (int) (lengthSeconds * 1000 * (1.0F - timeForGroups) / numberOfGroups);

        int time = 0;
        List<WaveEntry> entryList = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            if (this.rand.nextInt(2) == 0) {
                entryList.add(new WaveEntry(time, time + 3500, mobsPerGroup, 500, generateGroupPool(tierLevel), 25, 3));
                entryList.add(new WaveEntry(time += groupTimeInterval, time += steadyTimeInterval, mobsPerSteady, 2000, generateSteadyPool(tierLevel), 160, 5));
            } else {
                entryList.add(new WaveEntry(time, time += steadyTimeInterval, mobsPerSteady, 2000, generateSteadyPool(tierLevel), 160, 5));
                entryList.add(new WaveEntry(time, time + 5000, mobsPerGroup, 500, generateGroupPool(tierLevel), 25, 3));
                time += groupTimeInterval;
            }
        }

        time = (int) (time + groupTimeInterval * 0.75D);
        FiniteSelectionPool<IEntityIMPattern> finaleGroup = new FiniteSelectionPool<>();
        finaleGroup.addEntry(getPattern("thrower_t1"), mobsPerBigGroup / 5);
        generateGroupPool(tierLevel + 0.5F, finaleGroup, mobsPerBigGroup);
        WaveEntry finale = new WaveEntry(time, time + 8000, mobsPerBigGroup + mobsPerBigGroup / 7, 500, finaleGroup, 45,
                3);
        finale.addAlert("invmod.message.wave.grouplarge", 0);

        entryList.add(finale);
        entryList.add(new WaveEntry(time + 5000, (int) (time + groupTimeInterval * 2.25F), extraMobsForFinale / 2, 500, generateSteadyPool(tierLevel), 160, 5));
        entryList.add(new WaveEntry(time + 5000, (int) (time + groupTimeInterval * 2.25F), extraMobsForFinale / 2, 500, generateSteadyPool(tierLevel), 160, 5));
        entryList.add(new WaveEntry(time + 5000, (int) (time + groupTimeInterval * 2.25F), extraMobsForFinale / 2, 500, generateSteadyPool(tierLevel), 160, 5));
        entryList.add(new WaveEntry(time + 15000, (int) (time + 10000 + groupTimeInterval * 2.25F), extraMobsForCleanup, 500, generateSteadyPool(tierLevel)));
        time = (int) (time + groupTimeInterval * 2.25D);

        return new Wave(time + 16000, groupTimeInterval * 3, entryList);
    }

    private ISelect<IEntityIMPattern> generateGroupPool(float tierLevel) {
        return generateGroupPool(tierLevel, new RandomSelectionPool<>(), 6.0F);
    }

    private void generateGroupPool(float tierLevel, FiniteSelectionPool<IEntityIMPattern> startPool, int amount) {
        startPool.addEntry(generateGroupPool(tierLevel, new RandomSelectionPool<>(), 6.0F), amount);
    }

    private RandomSelectionPool<IEntityIMPattern> generateGroupPool(float tierLevel, RandomSelectionPool<IEntityIMPattern> startPool, float weight) {
        float[] weights = new float[6];
        for (int i = 0; i < 6; i++) {
            if (tierLevel - i * 0.5F > 0.0F) {
                weights[i] = (tierLevel - i <= 1.0F ? tierLevel - i * 0.5F : 1.0F);
            }
        }
        RandomSelectionPool<IEntityIMPattern> zombiePool = new RandomSelectionPool<>();
        zombiePool.addEntry(getPattern("zombie_t1_any"), ZOMBIE_T1_WEIGHT * weights[0]);
        zombiePool.addEntry(getPattern("zombie_t2_any_basic"), ZOMBIE_T2_WEIGHT * weights[2]);
        zombiePool.addEntry(getPattern("zombiePigman_t1_any"), ZOMBIE_T1_WEIGHT * weights[3]);

        RandomSelectionPool<IEntityIMPattern> spiderPool = new RandomSelectionPool<>();
        spiderPool.addEntry(getPattern("spider_t1_any"), SPIDER_T1_WEIGHT * weights[0]);
        spiderPool.addEntry(getPattern("spider_t2_any"), SPIDER_T2_WEIGHT * weights[2]);

        RandomSelectionPool<IEntityIMPattern> basicPool = new RandomSelectionPool<>();
        basicPool.addEntry(zombiePool, 3.1F);
        basicPool.addEntry(spiderPool, 0.7F);
        basicPool.addEntry(getPattern("skeleton_t1_any"), 0.8F);

        RandomSelectionPool<IEntityIMPattern> specialPool = new RandomSelectionPool<>();
        specialPool.addEntry(getPattern("pigengy_t1_any"), 4.0F);
        specialPool.addEntry(getPattern("thrower_t1"), 1.1F * weights[4]);
        specialPool.addEntry(getPattern("zombie_t3_any"), 1.1F * weights[5]);
        specialPool.addEntry(getPattern("creeper_t1_basic"), 0.7F * weights[3]);

        startPool.addEntry(basicPool, weight * 0.8333333F);
        startPool.addEntry(specialPool, weight * 0.1666667F);
        return startPool;
    }

    private ISelect<IEntityIMPattern> generateSteadyPool(float tierLevel) {
        float[] weights = new float[6];
        for (int i = 0; i < 6; i++) {
            if (tierLevel - i * 0.5F > 0.0F) {
                weights[i] = (tierLevel - i <= 1.0F ? tierLevel - i * 0.5F : 1.0F);
            }
        }

        return new RandomSelectionPool<IEntityIMPattern>()
            .addEntry(new RandomSelectionPool<IEntityIMPattern>()
                    .addEntry(new RandomSelectionPool<IEntityIMPattern>()
                            .addEntry(getPattern("zombie_t1_any"), ZOMBIE_T1_WEIGHT * weights[0])
                            .addEntry(getPattern("zombie_t2_any_basic"), ZOMBIE_T2_WEIGHT * weights[2])
                            .addEntry(getPattern("zombiePigman_t1_any"), ZOMBIE_T1_WEIGHT * weights[3]), 3.1F)
                    .addEntry(new RandomSelectionPool<IEntityIMPattern>()
                            .addEntry(getPattern("spider_t1_any"), SPIDER_T1_WEIGHT * weights[0])
                            .addEntry(getPattern("spider_t2_any"), SPIDER_T2_WEIGHT * weights[2]), 0.7F)
                    .addEntry(getPattern("skeleton_t1_any"), 0.8F), 9.0F)
            .addEntry(new RandomSelectionPool<IEntityIMPattern>()
                    .addEntry(getPattern("pigengy_t1_any"), 3.0F)
                    .addEntry(getPattern("zombie_t3_any"), 1.1F * weights[5])
                    .addEntry(getPattern("creeper_t1_basic"), 0.8F * weights[3]), 1.0F);
    }

    public static IEntityIMPattern getPattern(String s) {
        if (commonPatterns.containsKey(s)) {
            return commonPatterns.get(s);
        }

        mod_Invasion.log("Non-existing pattern name in wave definition: " + s);
        return commonPatterns.get("zombie_t1_any");
    }

    public static boolean isPatternNameValid(String s) {
        return commonPatterns.containsKey(s);
    }

    public Wave generateWave(int waveNumber, int difficulty) {
        return null;
    }

    public static Wave generateMainInvasionWave(int waveNumber) {
        if (waveNumber > 11) {
            return generateExtendedWave(waveNumber);
        }
        List<WaveEntry> entryList = new ArrayList<>();
        switch (waveNumber) {
        case 1:
            return new Wave(110000, 15000, List.of(
                    new WaveEntry(0, 90000, 8, 2000, new RandomSelectionPool<IEntityIMPattern>()
                            .addEntry(getPattern("zombie_t1_any"), 3.0F)
                            .addEntry(getPattern("spider_t1_any"), 1.0F)),
                    new WaveEntry(70000, 73000, 3, 500, new FiniteSelectionPool<IEntityIMPattern>()
                            .addEntry(getPattern("pigengy_t1_any"), 1)
                            .addEntry(getPattern("zombie_t1_any"), 2), 25, 3)
            ));
        case 2:
            RandomSelectionPool wave2BasePool = new RandomSelectionPool();
            wave2BasePool.addEntry(getPattern("zombie_t1_any"), 3.0F);
            wave2BasePool.addEntry(getPattern("spider_t1_any"), 1.0F);
            wave2BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave2BasePool.addEntry(getPattern("zombiePigman_t1_any"), 0.5F);
            wave2BasePool.addEntry(getPattern("creeper_t1_basic"), 0.038F);
            WaveEntry wave2Base = new WaveEntry(0, 50000, 5, 2000, wave2BasePool, 110, 5);
            entryList.add(wave2Base);

            WaveEntry wave2Base2 = new WaveEntry(50000, 100000, 5, 2000, wave2BasePool.clone(), 110, 5);
            entryList.add(wave2Base2);

            RandomSelectionPool wave2SpecialPool = new RandomSelectionPool();
            wave2SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1.0F);
            WaveEntry wave2Special = new WaveEntry(20000, 23000, 1, 500, wave2SpecialPool);
            entryList.add(wave2Special);

            FiniteSelectionPool wave2BurstPool = new FiniteSelectionPool();
            wave2BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave2BurstPool.addEntry(getPattern("zombie_t1_any"), 2);
            WaveEntry wave2Burst = new WaveEntry(65000, 68000, 3, 500, wave2BurstPool, 25, 2);
            entryList.add(wave2Burst);
            return new Wave(120000, 15000, entryList);
        case 3:
            RandomSelectionPool wave3BasePool = new RandomSelectionPool();
            wave3BasePool.addEntry(getPattern("zombie_t1_any"), 3.0F);
            wave3BasePool.addEntry(getPattern("spider_t1_any"), 1.0F);
            wave3BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave3BasePool.addEntry(getPattern("creeper_t1_basic"), 0.04F);
            WaveEntry wave3Base1 = new WaveEntry(0, 30000, 6, 2000, wave3BasePool, 45, 3);
            entryList.add(wave3Base1);

            WaveEntry wave3Base2 = new WaveEntry(80000, 100000, 5, 2000, wave3BasePool.clone(), 45, 3);
            entryList.add(wave3Base2);

            RandomSelectionPool wave3SpecialPool = new RandomSelectionPool();
            wave3SpecialPool.addEntry(getPattern("spider_t2_any"), 1.0F);
            WaveEntry wave3Special = new WaveEntry(10000, 12000, 1, 500, wave3SpecialPool);
            entryList.add(wave3Special);

            FiniteSelectionPool wave3BurstPool = new FiniteSelectionPool();
            wave3BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave3BurstPool.addEntry(getPattern("zombie_t1_any"), 1);
            wave3BurstPool.addEntry(getPattern("zombie_t2_plain"), 1);
            wave3BurstPool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave3BurstPool.addEntry(getPattern("spider_t1_any"), 1);
            wave3BurstPool.addEntry(getPattern("creeper_t1_basic"), 1);
            WaveEntry wave3Burst = new WaveEntry(50000, 55000, 5, 500, wave3BurstPool, 25, 6);
            wave3Burst.addAlert("invmod.message.wave.groupsmall", 0);
            entryList.add(wave3Burst);
            return new Wave(120000, 18000, entryList);
        case 4:
            RandomSelectionPool wave4BasePool = new RandomSelectionPool();
            wave4BasePool.addEntry(getPattern("zombie_t1_any"), 3.0F);
            wave4BasePool.addEntry(getPattern("spider_t1_any"), 1.0F);
            wave4BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave4BasePool.addEntry(getPattern("creeper_t1_basic"), 0.058F);
            WaveEntry wave4Base1 = new WaveEntry(0, 50000, 6, 2000, wave4BasePool, 110, 5);
            entryList.add(wave4Base1);

            WaveEntry wave4Base2 = new WaveEntry(50000, 100000, 6, 2000, wave4BasePool.clone(), 110, 5);
            entryList.add(wave4Base2);

            FiniteSelectionPool wave4SpecialPool = new FiniteSelectionPool();
            wave4SpecialPool.addEntry(getPattern("spider_t2_any"), 1);
            wave4SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
            WaveEntry wave4Special = new WaveEntry(0, 90000, 3, 500, wave4SpecialPool);
            entryList.add(wave4Special);

            FiniteSelectionPool wave4BurstPool = new FiniteSelectionPool();
            wave4BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave4BurstPool.addEntry(getPattern("zombie_t2_any_basic"), 1);
            wave4BurstPool.addEntry(getPattern("zombiePigman_t2_any"), 1);
            WaveEntry wave4Burst = new WaveEntry(70000, 75000, 2, 500, wave4BurstPool, 25, 2);
            entryList.add(wave4Burst);
            return new Wave(120000, 18000, entryList);
        case 5:
            RandomSelectionPool wave5BasePool = new RandomSelectionPool();
            wave5BasePool.addEntry(getPattern("zombie_t1_any"), 3.0F);
            wave5BasePool.addEntry(getPattern("spider_t1_any"), 1.0F);
            wave5BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave5BasePool.addEntry(getPattern("creeper_t1_basic"), 0.054F);
            WaveEntry wave5Base1 = new WaveEntry(0, 40000, 6, 2000, wave5BasePool, 110, 5);
            entryList.add(wave5Base1);

            WaveEntry wave5Base2 = new WaveEntry(40000, 80000, 6, 2000, wave5BasePool.clone(), 110, 5);
            entryList.add(wave5Base2);

            RandomSelectionPool wave5SpecialPool = new RandomSelectionPool();
            wave5SpecialPool.addEntry(getPattern("spider_t2_any"), 1.0F);
            wave5SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1.0F);
            wave5SpecialPool.addEntry(getPattern("zombiePigman_t1_any"), 0.50F);
            WaveEntry wave5Special = new WaveEntry(0, 80000, 3, 500, wave5SpecialPool);
            entryList.add(wave5Special);

            FiniteSelectionPool wave5BurstPool = new FiniteSelectionPool();
            wave5BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave5BurstPool.addEntry(getPattern("zombie_t1_any"), 3);
            wave5BurstPool.addEntry(getPattern("zombie_t2_any_basic"), 1);
            wave5BurstPool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave5BurstPool.addEntry(getPattern("spider_t2_any"), 1);
            wave5BurstPool.addEntry(getPattern("thrower_t1"), 1);
            WaveEntry wave5Burst = new WaveEntry(115000, 118000, 8, 500, wave5BurstPool, 35, 5);
            wave5Burst.addAlert("invmod.message.wave.grouplarge", 0);
            entryList.add(wave5Burst);

            FiniteSelectionPool wave5FinalePool = new FiniteSelectionPool();
            wave5FinalePool.addEntry(getPattern("zombie_t1_any"), 3);
            wave5FinalePool.addEntry(getPattern("zombie_t2_any_basic"), 1);
            wave5FinalePool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave5FinalePool.addEntry(getPattern("spider_t2_any"), 1);
            wave5FinalePool.addEntry(getPattern("spider_t1_any"), 1);
            WaveEntry wave5Finale = new WaveEntry(135000, 165000, 7, 500, wave5FinalePool);
            entryList.add(wave5Finale);
            return new Wave(130000, 80000, entryList);
        case 6:
            RandomSelectionPool wave6BasePool = new RandomSelectionPool();
            wave6BasePool.addEntry(getPattern("zombie_t1_any"), 2.0F);
            wave6BasePool.addEntry(getPattern("zombie_t2_any_basic"), 1.0F);
            wave6BasePool.addEntry(getPattern("spider_t1_any"), 0.7F);
            wave6BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave6BasePool.addEntry(getPattern("creeper_t1_basic"), 0.064F);
            WaveEntry wave6Base1 = new WaveEntry(0, 50000, 7, 2000, wave6BasePool, 110, 5);
            entryList.add(wave6Base1);

            WaveEntry wave6Base2 = new WaveEntry(50000, 100000, 6, 2000, wave6BasePool.clone(), 110, 5);
            entryList.add(wave6Base2);

            FiniteSelectionPool wave6SpecialPool = new FiniteSelectionPool();
            wave6SpecialPool.addEntry(getPattern("spider_t2_any"), 1);
            wave6SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
            WaveEntry wave6Special = new WaveEntry(0, 90000, 2, 500, wave6SpecialPool);
            entryList.add(wave6Special);

            FiniteSelectionPool wave6BurstPool = new FiniteSelectionPool();
            wave6BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave6BurstPool.addEntry(getPattern("zombie_t2_any_basic"), 2);
            wave6BurstPool.addEntry(getPattern("zombie_t1_any"), 1);
            wave6BurstPool.addEntry(getPattern("zombiePigman_t1_any"), 1);
            WaveEntry wave6Burst = new WaveEntry(70000, 75000, 4, 500, wave6BurstPool, 25, 2);
            entryList.add(wave6Burst);
            return new Wave(110000, 25000, entryList);
        case 7:
            RandomSelectionPool wave7BasePool = new RandomSelectionPool();
            wave7BasePool.addEntry(getPattern("zombie_t1_any"), 2.0F);
            wave7BasePool.addEntry(getPattern("zombie_t2_any_basic"), 1.0F);
            wave7BasePool.addEntry(getPattern("spider_t1_any"), 0.7F);
            wave7BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave7BasePool.addEntry(getPattern("creeper_t1_basic"), 0.064F);
            WaveEntry wave7Base1 = new WaveEntry(0, 30000, 7, 2000, wave7BasePool, 45, 5);
            entryList.add(wave7Base1);

            FiniteSelectionPool wave7SpecialPool = new FiniteSelectionPool();
            wave7SpecialPool.addEntry(getPattern("spider_t2_any"), 1);
            wave7SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave7SpecialPool.addEntry(getPattern("thrower_t1"), 1);
            WaveEntry wave7Special = new WaveEntry(0, 60000, 3, 500, wave7SpecialPool);
            entryList.add(wave7Special);

            FiniteSelectionPool wave7BurstPool = new FiniteSelectionPool();
            wave7BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave7BurstPool.addEntry(getPattern("zombie_t2_any_basic"), 2);
            wave7BurstPool.addEntry(getPattern("zombie_t1_any"), 1);
            wave7BurstPool.addEntry(getPattern("zombiePigman_t1_any"), 1);
            wave7BurstPool.addEntry(getPattern("spider_t2_any"), 1);
            WaveEntry wave7Burst = new WaveEntry(65000, 67000, 5, 500, wave7BurstPool, 45, 2);
            entryList.add(wave7Burst);

            FiniteSelectionPool wave7Burst2Pool = new FiniteSelectionPool();
            wave7Burst2Pool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave7Burst2Pool.addEntry(getPattern("zombiePigman_t1_any"), 3);
            WaveEntry wave7Burst2 = new WaveEntry(95000, 97000, 4, 500, wave7Burst2Pool, 45, 2);
            entryList.add(wave7Burst2);

            return new Wave(120000, 36000, entryList);
        case 8:
            RandomSelectionPool wave8BasePool = new RandomSelectionPool();
            wave8BasePool.addEntry(getPattern("zombie_t1_any"), 2.0F);
            wave8BasePool.addEntry(getPattern("zombie_t2_any_basic"), 1.5F);
            wave8BasePool.addEntry(getPattern("spider_t1_any"), 0.7F);
            wave8BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave8BasePool.addEntry(getPattern("creeper_t1_basic"), 0.064F);
            WaveEntry wave8Base1 = new WaveEntry(0, 35000, 7, 2000, wave8BasePool, 110, 5);
            entryList.add(wave8Base1);

            WaveEntry wave8Base2 = new WaveEntry(80000, 110000, 4, 2000, wave8BasePool.clone(), 110, 5);
            entryList.add(wave8Base2);

            FiniteSelectionPool wave8SpecialPool = new FiniteSelectionPool();
            wave8SpecialPool.addEntry(getPattern("spider_t2_any"), 1);
            wave8SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
            WaveEntry wave8Special = new WaveEntry(0, 90000, 2, 500, wave8SpecialPool);
            entryList.add(wave8Special);

            FiniteSelectionPool wave8BurstPool = new FiniteSelectionPool();
            wave8BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave8BurstPool.addEntry(getPattern("zombie_t2_any_basic"), 3);
            wave8BurstPool.addEntry(getPattern("zombie_t1_any"), 2);
            wave8BurstPool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave8BurstPool.addEntry(getPattern("creeper_t1_basic"), 1);
            WaveEntry wave8Burst = new WaveEntry(60000, 63000, 8, 500, wave8BurstPool, 25, 2);
            wave8Burst.addAlert("invmod.message.wave.group", 0);
            entryList.add(wave8Burst);
            return new Wave(110000, 30000, entryList);
        case 9:
            RandomSelectionPool wave9BasePool = new RandomSelectionPool();
            wave9BasePool.addEntry(getPattern("zombie_t1_any"), 2.0F);
            wave9BasePool.addEntry(getPattern("zombie_t2_any_basic"), 2.0F);
            wave9BasePool.addEntry(getPattern("spider_t1_any"), 0.7F);
            wave9BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave9BasePool.addEntry(getPattern("creeper_t1_basic"), 0.074F);
            WaveEntry wave9Base1 = new WaveEntry(0, 30000, 7, 2000, wave9BasePool, 45, 5);
            entryList.add(wave9Base1);

            FiniteSelectionPool wave9SpecialPool = new FiniteSelectionPool();
            wave9SpecialPool.addEntry(getPattern("spider_t2_any"), 1);
            wave9SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
            WaveEntry wave9Special = new WaveEntry(0, 90000, 3, 500, wave9SpecialPool);
            entryList.add(wave9Special);

            FiniteSelectionPool wave9BurstPool = new FiniteSelectionPool();
            wave9BurstPool.addEntry(getPattern("pigengy_t1_any"), 1);
            wave9BurstPool.addEntry(getPattern("zombiePigman_t1_any"), 3);
            wave9BurstPool.addEntry(getPattern("zombie_t1_any"), 1);
            wave9BurstPool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave9BurstPool.addEntry(getPattern("zombie_t3_any"), 1);
            WaveEntry wave9Burst = new WaveEntry(65000, 67000, 6, 500, wave9BurstPool, 25, 3);
            entryList.add(wave9Burst);

            FiniteSelectionPool wave9Burst2Pool = new FiniteSelectionPool();
            wave9Burst2Pool.addEntry(getPattern("zombie_t2_any_basic"), 2);
            wave9Burst2Pool.addEntry(getPattern("zombie_t1_any"), 3);
            wave9Burst2Pool.addEntry(getPattern("spider_t2_any"), 1);
            WaveEntry wave9Burst2 = new WaveEntry(95000, 97000, 6, 500, wave9Burst2Pool, 45, 2);
            entryList.add(wave9Burst2);

            return new Wave(120000, 35000, entryList);
        case 10:
            RandomSelectionPool wave10BasePool = new RandomSelectionPool();
            wave10BasePool.addEntry(getPattern("zombie_t1_any"), 1.5F);
            wave10BasePool.addEntry(getPattern("zombie_t2_any_basic"), 2.2F);
            wave10BasePool.addEntry(getPattern("zombiePigman_t1_any"), 0.7F);
            wave10BasePool.addEntry(getPattern("spider_t1_any"), 0.7F);
            wave10BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave10BasePool.addEntry(getPattern("creeper_t1_basic"), 0.084F);
            WaveEntry wave10Base1 = new WaveEntry(0, 40000, 9, 2000, wave10BasePool, 110, 5);
            entryList.add(wave10Base1);

            WaveEntry wave10Base2 = new WaveEntry(40000, 80000, 7, 2000, wave10BasePool.clone(), 110, 5);
            entryList.add(wave10Base2);

            RandomSelectionPool wave10SpecialPool = new RandomSelectionPool();
            wave10SpecialPool.addEntry(getPattern("spider_t2_any"), 1.0F);
            wave10SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1.0F);
            WaveEntry wave10Special = new WaveEntry(0, 80000, 3, 500, wave10SpecialPool);
            entryList.add(wave10Special);

            FiniteSelectionPool wave10BurstPool = new FiniteSelectionPool();
            wave10BurstPool.addEntry(getPattern("pigengy_t1_any"), 2);
            wave10BurstPool.addEntry(getPattern("zombie_t1_any"), 2);
            wave10BurstPool.addEntry(getPattern("zombie_t2_any_basic"), 2);
            wave10BurstPool.addEntry(getPattern("zombiePigman_t1_any"), 3);
            wave10BurstPool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave10BurstPool.addEntry(getPattern("spider_t2_any"), 1);
            wave10BurstPool.addEntry(getPattern("thrower_t1"), 1);
            WaveEntry wave10Burst = new WaveEntry(125000, 128000, 12, 500, wave10BurstPool, 35, 5);
            wave10Burst.addAlert("invmod.message.wave.grouplarge", 0);
            entryList.add(wave10Burst);

            FiniteSelectionPool wave10FinalePool = new FiniteSelectionPool();
            wave10FinalePool.addEntry(getPattern("zombie_t1_any"), 2);
            wave10FinalePool.addEntry(getPattern("zombie_t2_any_basic"), 2);
            wave10FinalePool.addEntry(getPattern("skeleton_t1_any"), 1);
            wave10FinalePool.addEntry(getPattern("spider_t2_any"), 1);
            wave10FinalePool.addEntry(getPattern("spider_t1_any"), 2);
            WaveEntry wave10Finale = new WaveEntry(152000, 170000, 7, 500, wave10FinalePool);
            entryList.add(wave10Finale);
            return new Wave(172000, 60000, entryList);
        case 11:
            RandomSelectionPool wave11BasePool = new RandomSelectionPool();
            wave11BasePool.addEntry(getPattern("zombie_t1_any"), 1.5F);
            wave11BasePool.addEntry(getPattern("zombie_t2_any_basic"), 2.2F);
            wave11BasePool.addEntry(getPattern("zombie_t3_any"), 0.185F);
            wave11BasePool.addEntry(getPattern("zombiePigman_t1_any"), 0.8F);
            wave11BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
            wave11BasePool.addEntry(getPattern("thrower_t1"), 0.1F);
            wave11BasePool.addEntry(getPattern("creeper_t1_basic"), 0.064F);
            WaveEntry wave11Base1 = new WaveEntry(0, 30000, 7, 2000, wave11BasePool, 45, 5);
            entryList.add(wave11Base1);

            FiniteSelectionPool wave11SpecialPool = new FiniteSelectionPool();
            wave11SpecialPool.addEntry(getPattern("spider_t2_any"), 1);
            wave11SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
            WaveEntry wave11Special = new WaveEntry(0, 90000, 3, 500, wave11SpecialPool);
            entryList.add(wave11Special);

            RandomSelectionPool wave11BurstPool = new RandomSelectionPool();
            wave11BurstPool.addEntry(getPattern("pigengy_t1_any"), 1.0F);
            wave11BurstPool.addEntry(getPattern("zombiePigman_t1_any"), 2.0F);
            wave11BurstPool.addEntry(getPattern("zombie_t2_any"), 3.0F);
            wave11BurstPool.addEntry(getPattern("zombie_t1_any"), 1.0F);
            wave11BurstPool.addEntry(getPattern("skeleton_t1_any"), 1.0F);
            wave11BurstPool.addEntry(getPattern("thrower_t1"), 0.8F);
            wave11BurstPool.addEntry(getPattern("creeper_t1_basic"), 0.8F);
            WaveEntry wave11Burst = new WaveEntry(65000, 67000, 7, 500, wave11BurstPool, 25, 3);
            entryList.add(wave11Burst);

            FiniteSelectionPool wave11Burst2Pool = new FiniteSelectionPool();
            wave11Burst2Pool.addEntry(getPattern("zombie_t2_any_basic"), 2);
            wave11Burst2Pool.addEntry(getPattern("zombie_t1_any"), 3);
            wave11Burst2Pool.addEntry(getPattern("spider_t2_any"), 1);
            WaveEntry wave11Burst2 = new WaveEntry(95000, 97000, 6, 500, wave11Burst2Pool, 45, 2);
            entryList.add(wave11Burst2);

            return new Wave(120000, 35000, entryList);
        }
        return null;
    }

    private static Wave generateExtendedWave(int waveNumber) {
        float mobScale = (float) Math.pow(1.090000033378601D, waveNumber - 11);
        float timeScale = 1.0F + (waveNumber - 11) * 0.04F;
        ArrayList entryList = new ArrayList();
        RandomSelectionPool wave11BasePool = new RandomSelectionPool();
        wave11BasePool.addEntry(getPattern("zombie_t1_any"), 1.5F);
        wave11BasePool.addEntry(getPattern("zombie_t2_any_basic"), 2.2F);
        wave11BasePool.addEntry(getPattern("zombie_t3_any"), 0.26F);
        wave11BasePool.addEntry(getPattern("zombiePigman_t1_any"), 0.8F);
        wave11BasePool.addEntry(getPattern("zombiePigman_t2_any"), 0.5F);
        wave11BasePool.addEntry(getPattern("zombiePigman_t2_any"), 0.054F);
        wave11BasePool.addEntry(getPattern("skeleton_t1_any"), 0.7F);
        wave11BasePool.addEntry(getPattern("thrower_t1"), 0.18F);
        wave11BasePool.addEntry(getPattern("thrower_t2"), 0.054F);
        wave11BasePool.addEntry(getPattern("creeper_t1_basic"), 0.054F);
        wave11BasePool.addEntry(getPattern("imp_t1"), 0.4F);
        WaveEntry wave11Base1 = new WaveEntry(0, (int) (timeScale * 30000.0F), (int) (mobScale * 7.0F), 2000,
                wave11BasePool, 45, 5);
        entryList.add(wave11Base1);

        FiniteSelectionPool wave11SpecialPool = new FiniteSelectionPool();
        wave11SpecialPool.addEntry(getPattern("spider_t2_any"), 2);
        wave11SpecialPool.addEntry(getPattern("pigengy_t1_any"), 1);
        WaveEntry wave11Special = new WaveEntry(0, (int) (timeScale * 90000.0F), (int) (mobScale * 3.0F), 500,
                wave11SpecialPool);
        entryList.add(wave11Special);

        RandomSelectionPool wave11BurstPool = new RandomSelectionPool();
        wave11BurstPool.addEntry(getPattern("zombiePigman_t1_any"), 1.5F);
        wave11BurstPool.addEntry(getPattern("zombiePigman_t2_any"), 0.7F);
        wave11BurstPool.addEntry(getPattern("zombiePigman_t3_any"), 0.35F);
        wave11BurstPool.addEntry(getPattern("zombie_t2_any"), 1.5F);
        wave11BurstPool.addEntry(getPattern("spider_t2_any"), 1.0F);
        wave11BurstPool.addEntry(getPattern("zombie_t1_any"), 1.0F);
        wave11BurstPool.addEntry(getPattern("skeleton_t1_any"), 1.0F);
        wave11BurstPool.addEntry(getPattern("thrower_t1"), 0.5F);
        wave11BurstPool.addEntry(getPattern("thrower_t2"), 0.42F);
        wave11BurstPool.addEntry(getPattern("zombie_t3_any"), 0.5F);
        wave11BurstPool.addEntry(getPattern("creeper_t1_basic"), 0.42F);
        wave11BurstPool.addEntry(getPattern("imp_t1"), 0.4F);
        WaveEntry wave11Burst = new WaveEntry((int) (timeScale * 65000.0F), (int) (timeScale * 67000.0F),
                (int) (mobScale * 7.0F), 500, wave11BurstPool, 25, 3);
        entryList.add(wave11Burst);

        FiniteSelectionPool wave11Burst2Pool = new FiniteSelectionPool();
        wave11Burst2Pool.addEntry(getPattern("zombie_t2_any_basic"), 2);
        wave11Burst2Pool.addEntry(getPattern("zombie_t1_any"), 3);
        wave11Burst2Pool.addEntry(getPattern("spider_t2_any"), 1);
        WaveEntry wave11Burst2 = new WaveEntry((int) (timeScale * 95000.0F), (int) (timeScale * 97000.0F),
                (int) (mobScale * 6.0F), 500, wave11Burst2Pool, 45, 2);
        entryList.add(wave11Burst2);

        return new Wave((int) (timeScale * 120000.0F), (int) (timeScale * 35000.0F), entryList);
    }
}