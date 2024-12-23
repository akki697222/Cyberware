package flaxbeard.cyberware.api.item;

import net.minecraft.world.item.ItemStack;

public interface ICyberwareTabItem
{
    enum EnumCategory
    {
        BLOCKS,
        BODYPARTS,
        EYES,
        CRANIUM,
        HEART,
        LUNGS,
        LOWER_ORGANS,
        SKIN,
        MUSCLE,
        BONE,
        ARM,
        HAND,
        LEG,
        FOOT;
    }

    EnumCategory getCategory(ItemStack stack);
}
