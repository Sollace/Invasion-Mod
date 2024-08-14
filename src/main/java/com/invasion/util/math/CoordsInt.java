package com.invasion.util.math;

import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public final class CoordsInt {
    private CoordsInt() {}
    public static final Direction[] CARDINAL_DIRECTIONS = { Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH };

    public static final List<BlockPos> ZERO = List.of();
    public static final List<Vec3i> OFFSET_ADJACENT = List.of(
            new BlockPos( 1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos( 0, 0, 1),
            new BlockPos( 0, 0, -1)
    );
    public static final List<Vec3i> OFFSET_ADJACENT_2 = List.of(
            new BlockPos( 2, 0, 0),
            new BlockPos( 2, 0, 1),
            new BlockPos(-1, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos( 1, 0, 2),
            new BlockPos( 0, 0, 2),
            new BlockPos( 0, 0, -1),
            new BlockPos( 1, 0, -1)
    );
    public static final List<Vec3i> OFFSET_RING = List.of(
            new BlockPos( 1, 0, 1),
            new BlockPos( 0, 0, 1),
            new BlockPos(-1, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(-1, 0,-1),
            new BlockPos( 0, 0,-1),
            new BlockPos( 1, 0, 0)
    );

    public static double getInclination(BlockPos from, BlockPos to) {
        BlockPos delta = from.subtract(to);
        if (delta.getY() <= 0) {
            return 0;
        }
        return (delta.getY() + 8) / (Math.sqrt(MathHelper.square(delta.getX()) + MathHelper.square(delta.getZ())) + MathHelper.EPSILON);
    }
}