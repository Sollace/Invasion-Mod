package com.invasion.entity;

import com.invasion.InvSounds;
import com.invasion.block.BlockSpecial;
import com.invasion.block.InvBlocks;
import com.invasion.block.TileEntityNexus;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.world.event.GameEvent;

public class EntityIMBoulder extends PersistentProjectileEntity {
    public EntityIMBoulder(EntityType<? extends EntityIMBoulder> type, World world) {
        super(type, world);
        setStack(getDefaultItemStack());
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        // TODO: create an item for this
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return !getItemStack().isEmpty() && super.tryPickup(player);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        playSound(InvSounds.ENTITY_BOULDER_LAND, 1, 0.9F / (getRandom().nextFloat() * 0.2F + 0.9F));
        getWorld().emitGameEvent(this, GameEvent.HIT_GROUND, getBlockPos());
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);
        BlockState state = getWorld().getBlockState(hit.getBlockPos());
        if (state.isOf(InvBlocks.NEXUS_CORE) && getWorld().getBlockEntity(hit.getBlockPos()) instanceof TileEntityNexus nexus) {
            nexus.getNexus().damage(2);
        } else if (state.getHardness(getWorld(), hit.getBlockPos()) >= 0) {

            if (!state.isIn(BlockTags.WITHER_IMMUNE) && !state.isIn(BlockTags.DRAGON_IMMUNE)) {
                getWorld().emitGameEvent(this, GameEvent.HIT_GROUND, hit.getBlockPos());
                if (BlockSpecial.of(state) == BlockSpecial.DEFLECTION_1 && getRandom().nextInt(2) == 0) {
                    discard();
                    return;
                }
                if (getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    getWorld().createExplosion(this, getX(), getY(), getZ(), 2, ExplosionSourceType.BLOCK);
                }
            }

        }
    }
}