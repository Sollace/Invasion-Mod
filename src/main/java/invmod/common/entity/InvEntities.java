package invmod.common.entity;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import invmod.common.ConfigInvasion;
import invmod.common.InvasionMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeKeys;

public interface InvEntities {
    EntityType<EntityIMSkeleton> SKELETON = register("skeleton", EntityType.Builder.<EntityIMSkeleton>create(EntityIMSkeleton::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.99F).eyeHeight(1.74F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMZombie> ZOMBIE = register("zombie", EntityType.Builder.<EntityIMZombie>create(EntityIMZombie::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMZombiePigman> ZOMBIE_PIGMAN = register("pigman", EntityType.Builder.<EntityIMZombiePigman>create(EntityIMZombiePigman::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMPigEngy> PIGMAN_ENGINEER = register("pigman_engineer", EntityType.Builder.<EntityIMPigEngy>create(EntityIMPigEngy::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMCreeper> CREEPER = register("creeper", EntityType.Builder.<EntityIMCreeper>create(EntityIMCreeper::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.7F).maxTrackingRange(8));
    EntityType<EntityIMSpider> SPIDER = register("spider", EntityType.Builder.<EntityIMSpider>create(EntityIMSpider::new, SpawnGroup.MONSTER)
            .dimensions(1.4F, 0.9F).eyeHeight(0.65F).passengerAttachments(0.765F).maxTrackingRange(8));
    EntityType<EntityIMThrower> THROWER = register("thrower", EntityType.Builder.<EntityIMThrower>create(EntityIMThrower::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.99F).eyeHeight(1.74F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMBurrower> BURROWER = register("burrower", EntityType.Builder.<EntityIMBurrower>create(EntityIMBurrower::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.99F).eyeHeight(1.74F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMImp> IMP = register("imp", EntityType.Builder.<EntityIMImp>create(EntityIMImp::new, SpawnGroup.MONSTER)
            .dimensions(0.6F, 1.99F).eyeHeight(1.74F).vehicleAttachment(-0.7F).maxTrackingRange(8));
    EntityType<EntityIMWolf> WOLF = register("wolf", EntityType.Builder.<EntityIMWolf>create(EntityIMWolf::new, SpawnGroup.CREATURE)
            .dimensions(0.6F, 0.85F).eyeHeight(0.68F).passengerAttachments(new Vec3d(0.0, 0.81875, -0.0625)).maxTrackingRange(10));

    EntityType<EntityIMEgg> SPIDER_EGG = register("spider_egg", EntityType.Builder.<EntityIMEgg>create(EntityIMEgg::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.8F).eyeHeight(0.5F).passengerAttachments(new Vec3d(0.0, 0.81875, -0.0625)).maxTrackingRange(10));

    EntityType<EntityIMTrap> TRAP = register("trap", EntityType.Builder.<EntityIMTrap>create(EntityIMTrap::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.28F).maxTrackingRange(10));

    @Deprecated
    EntityType<EntitySFX> SFX = register("sfx", EntityType.Builder.<EntitySFX>create(EntitySFX::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F).maxTrackingRange(8));
    EntityType<EntityIMSpawnProxy> SPAWN_PROXY = register("spawn_proxy", EntityType.Builder.<EntityIMSpawnProxy>create(EntityIMSpawnProxy::new, SpawnGroup.MONSTER)
            .dimensions(0.5F, 0.5F).maxTrackingRange(8));
    EntityType<EntityIMBolt> BOLT = register("bolt", EntityType.Builder.<EntityIMBolt>create(EntityIMBolt::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F).maxTrackingRange(8));
    EntityType<EntityIMBoulder> BOULDER = register("boulder", EntityType.Builder.<EntityIMBoulder>create(EntityIMBoulder::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F).maxTrackingRange(8));
    EntityType<EntityIMPrimedTNT> TNT = register("tnt", EntityType.Builder.<EntityIMPrimedTNT>create(EntityIMPrimedTNT::new, SpawnGroup.MISC)
            .makeFireImmune().dimensions(0.98F, 0.98F).eyeHeight(0.15F).maxTrackingRange(10).trackingTickInterval(10));

    RegistryKey<EntityType<?>> BIRD = RegistryKey.of(RegistryKeys.ENTITY_TYPE, InvasionMod.id("bird"));
    RegistryKey<EntityType<?>> GIANT_BIRD = RegistryKey.of(RegistryKeys.ENTITY_TYPE, InvasionMod.id("giant_bird"));

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, InvasionMod.id(name), builder.build());
    }

    static void bootstrap() {
        ConfigInvasion config = InvasionMod.getConfig();

        if (config.debugMode) {
            register("bird", EntityType.Builder.<EntityIMBird>create(EntityIMBird::new, SpawnGroup.MONSTER)
                    .dimensions(1, 1).maxTrackingRange(10).trackingTickInterval(10));
            register("giant_bird", EntityType.Builder.<EntityIMGiantBird>create(EntityIMGiantBird::new, SpawnGroup.MONSTER)
                    .dimensions(1.9F, 2.8F).maxTrackingRange(10).trackingTickInterval(10));
        }

        if (config.nightSpawnsEnabled) {
            BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER), SPAWN_PROXY.getSpawnGroup(), SPAWN_PROXY, config.nightMobSpawnChance, 1, 1);
            BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.ZOMBIE), ZOMBIE.getSpawnGroup(), ZOMBIE, 1, 1, 1);
            BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.SPIDER), SPIDER.getSpawnGroup(), SPIDER, 1, 1, 1);
            BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.SKELETON), SKELETON.getSpawnGroup(), SKELETON, 1, 1, 1);
        }

        if (config.maxNightMobs != 70) {
            try {
                // TODO: Use a mixin for this. Reflection is slow and won't work in an obfuscated environment
                Field field = SpawnGroup.class.getDeclaredField("capacity");
                field.setAccessible(true);
                field.set(SpawnGroup.MONSTER, config.maxNightMobs);
            } catch (Exception e) {
                InvasionMod.LOGGER.error("Error whilst updating max hostile entity cap", e);
            }
        }
    }
}
