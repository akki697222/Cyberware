package flaxbeard.cyberware.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityEvent;

import javax.annotation.Nonnull;

public class CyberwareUpdateEvent extends EntityEvent {
    private final LivingEntity livingEntity;
    private final ICyberwareUserData cyberwareUserData;

    public CyberwareUpdateEvent(@Nonnull LivingEntity livingEntity, @Nonnull ICyberwareUserData cyberwareUserData) {
        super(livingEntity);
        this.livingEntity = livingEntity;
        this.cyberwareUserData = cyberwareUserData;
    }

    @Nonnull
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    @Nonnull
    public ICyberwareUserData getCyberwareUserData() {
        return cyberwareUserData;
    }
}
