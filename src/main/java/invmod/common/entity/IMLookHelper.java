package invmod.common.entity;

import java.util.Optional;

import net.minecraft.entity.ai.control.LookControl;

public class IMLookHelper extends LookControl {
    public IMLookHelper(EntityIMLiving entity) {
        super(entity);
    }

    @Override
    protected boolean shouldStayHorizontal() {
        return false;
    }

    @Override
    protected Optional<Float> getTargetPitch() {
        return super.getTargetPitch().map(pitch -> pitch + 40);
    }

    @Override
    protected Optional<Float> getTargetYaw() {
        return super.getTargetYaw().map(yaw -> Math.abs(yaw) > 100 ? 0 : yaw / 6F);
    }
}