package flaxbeard.cyberware.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface IMenuItem
{
    boolean hasMenu(ItemStack stack);
    void use(Entity entity, ItemStack stack);
    String getUnlocalizedLabel(ItemStack stack);
    float[] getColor(ItemStack stack);
}
