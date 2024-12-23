package flaxbeard.cyberware.api;

import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CyberwareSurgeryEvent extends EntityEvent {
    public CyberwareSurgeryEvent(LivingEntity livingEntity) {
        super(livingEntity);
    }

    /**
     * Fired when the Surgery Chamber starts the process of altering an entities installed Cyberware
     * Changing inventories isn't supported.
     * Cancel to prevent any changes
     */
    public static class Pre extends CyberwareSurgeryEvent {
        public ItemStackHandler inventoryActual;
        public ItemStackHandler inventoryTarget;

        public Pre(LivingEntity livingEntity, ItemStackHandler inventoryActual, ItemStackHandler inventoryTarget) {
            super(livingEntity);
            //TODO Providerが何か、ItemStackHandlerの仕様更新について調べる
            this.inventoryActual = new ItemStackHandler(120);

        }

        public ItemStackHandler getActualCyberwares() {
            return inventoryActual;
        }

        public ItemStackHandler getTargetCyberwares() {
            return inventoryTarget;
        }
    }

    /**
     * Fired when the Surgery Chamber finishes the process of altering an entities installed Cyberware
     */
    public static class Post extends CyberwareSurgeryEvent
    {
        public Post(LivingEntity livingEntity)
        {
            super(livingEntity);
        }
    }
}
