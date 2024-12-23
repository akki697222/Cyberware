package flaxbeard.cyberware.api.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

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
