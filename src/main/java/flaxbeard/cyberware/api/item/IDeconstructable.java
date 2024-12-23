package flaxbeard.cyberware.api.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IDeconstructable
{
    boolean canDestroy(ItemStack stack);
    NonNullList<ItemStack> getComponents(ItemStack stack);
}
