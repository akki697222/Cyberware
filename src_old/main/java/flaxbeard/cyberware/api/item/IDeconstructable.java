package flaxbeard.cyberware.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IDeconstructable
{
	public boolean canDestroy(ItemStack stack);
	public NonNullList<ItemStack> getComponents(ItemStack stack);
}
