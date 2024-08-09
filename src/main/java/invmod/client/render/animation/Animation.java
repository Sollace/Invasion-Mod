package invmod.client.render.animation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

public record Animation<T extends Enum<T>>(
        Class<T> skeletonType,
        float animationPeriod,
        float baseSpeed,
        EnumMap<T, List<KeyFrame>> keyframes,
        List<AnimationPhaseInfo> phases) {
    public float getAnimationPeriod() {
        return this.animationPeriod;
    }

    public float getBaseSpeed() {
        return this.baseSpeed;
    }

    public List<AnimationPhaseInfo> getAnimationPhases() {
        return Collections.unmodifiableList(this.phases);
    }

    public Class<T> getSkeletonType() {
        return this.skeletonType;
    }

    public List<KeyFrame> getKeyFramesFor(T skeletonPart) {
        return this.keyframes.getOrDefault(skeletonPart, List.of());
    }

    public ModelAnimator<T> createAnimator(Map<T, ModelPart> parts) {
        return new ModelAnimator<>(parts, this);
    }

    public <E extends Entity, K extends AnimationController> K createState(E entity, AnimationAction initialAction, BiFunction<E, AnimationState<T>, K> controllerFactory) {
        return controllerFactory.apply(entity, new AnimationState<>(this).setNewAction(initialAction));
    }
}