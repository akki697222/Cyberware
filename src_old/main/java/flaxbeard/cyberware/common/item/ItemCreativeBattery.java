package flaxbeard.cyberware.common.item;

import net.minecraft.item.ItemStack;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ISpecialBattery;

public class ItemCreativeBattery extends ItemCyberware implements ISpecialBattery
{

	public ItemCreativeBattery(String name, EnumSlot slot)
	{
		super(name, slot);
	}

	@Override
	public int add(ItemStack battery, ItemStack power, int amount, boolean simulate)
	{
		return amount;
	}

	@Override
	public int extract(ItemStack battery, int amount, boolean simulate)
	{
		return amount;
	}

	@Override
	public int getStoredEnergy(ItemStack battery)
	{
		return 999999;
	}
	
	@Override
	public int getCapacity(ItemStack battery)
	{
		return 999999;
	}
	
	@Override
	public boolean canHoldQuality(ItemStack stack, Quality quality)
	{
		return quality == CyberwareAPI.QUALITY_MANUFACTURED;
	}

}
