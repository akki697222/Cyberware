package flaxbeard.cyberware.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IBlueprint
{
	public ItemStack getResult(ItemStack stack, NonNullList<ItemStack> items);
	public NonNullList<ItemStack> consumeItems(ItemStack stack, NonNullList<ItemStack> items);
	default ItemStack getIconForDisplay(ItemStack stack)
	{
		return ItemStack.EMPTY;
	}
	default NonNullList<ItemStack> getRequirementsForDisplay(ItemStack stack)
	{
		return NonNullList.create();
	}
}
