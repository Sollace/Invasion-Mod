package com.invasion.entity.pathfinding;

import com.invasion.entity.EntityIMLiving;
import com.invasion.util.math.PosRotate3D;

public abstract class NavigatorParametric extends NavigatorIM {
    protected double minMoveToleranceSq = 21;
    protected int timeParam;

    public NavigatorParametric(EntityIMLiving entity, IPathSource pathSource) {
        super(entity, pathSource);
    }

    @Override
    public void onUpdateNavigation() {
        totalTicks++;
        if (noPath() || waitingForNotify) {
            return;
        }
        if (nodeActionFinished) {
            int pathIndex = path.getCurrentPathIndex();
            pathFollow(timeParam + 1);
            doMovementTo(timeParam);

            if (path.getCurrentPathIndex() != pathIndex) {
                ticksStuck = 0;
                if (activeNode.action != PathAction.NONE) {
                    nodeActionFinished = false;
                }
            }
        }
        if (nodeActionFinished) {
            if (!isPositionClear(activeNode.pos, theEntity)) {
                if (theEntity.onPathBlocked(path, this)) {
                    setDoingTaskAndHold();
                } else {
                    clearPath();
                }
            }
        } else {
            handlePathAction();
        }
    }

    protected void doMovementTo(int time) {
        PosRotate3D movePos = entityPositionAtParam(time);
        theEntity.getMoveControl().moveTo(movePos.position().x, movePos.position().y, movePos.position().z, 1);

        if (Math.abs(theEntity.squaredDistanceTo(movePos.position())) < minMoveToleranceSq) {
            timeParam = time;
            ticksStuck--;
        } else {
            ticksStuck++;
        }
    }

    protected abstract PosRotate3D entityPositionAtParam(int paramInt);

    protected abstract boolean isReadyForNextNode(int paramInt);

    protected void pathFollow(int time) {
        int nextIndex = path.getCurrentPathIndex() + 1;
        if (isReadyForNextNode(time)) {
            if (nextIndex < path.getCurrentPathLength()) {
                timeParam = 0;
                path.setCurrentPathIndex(nextIndex);
                activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
            }
        } else {
            timeParam = time;
        }
    }

    @Override
    protected void pathFollow() {
        pathFollow(0);
    }
}