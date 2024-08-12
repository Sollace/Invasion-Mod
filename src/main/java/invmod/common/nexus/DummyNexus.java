package invmod.common.nexus;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.ai.AttackerAI;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class DummyNexus implements INexusAccess {
    private World world;

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void attackNexus(int damage) {
    }

    @Override
    public void registerMobDied() {
    }

    @Override
    public boolean isActivating() {
        return false;
    }

    @Override
    public int getMode() {
        return 0;
    }


    @Override
    public int getSpawnRadius() {
        return 45;
    }

    @Override
    public int getCurrentWave() {
        return 1;
    }

    @Override
    public int getXCoord() {
        return 0;
    }

    @Override
    public int getYCoord() {
        return 0;
    }

    @Override
    public int getZCoord() {
        return 0;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public List<EntityIMLiving> getMobList() {
        return List.of();
    }

    @Override
    public void askForRespawn(EntityIMLiving entity) {
    }

    @Override
    public AttackerAI getAttackerAI() {
        return null;
    }

    @Override
    public Map<UUID, Long> getBoundPlayers() {
        return Map.of();
    }

    @Override
    public void sendMessage(Formatting color, String translationKey, Object... params) {
    }

    @Override
    public boolean forceStart(int wave) {
        return true;
    }

    @Override
    public void stop(boolean killEnemies) {
    }

    @Override
    public boolean setSpawnRadius(int radius) {
        return true;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public List<Text> getStatus() {
        return List.of();
    }
}