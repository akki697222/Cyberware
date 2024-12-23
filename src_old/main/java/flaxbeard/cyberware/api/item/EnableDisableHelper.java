package flaxbeard.cyberware.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import flaxbeard.cyberware.api.CyberwareAPI;

public class EnableDisableHelper
{
	public static final String ENABLED_STR = "~enabled";
	
	public static boolean isEnabled(ItemStack stack)
	{
		if (stack.isEmpty()) return false;

		NBTTagCompound tagCompound = CyberwareAPI.getCyberwareNBT(stack);
		if (!tagCompound.hasKey(ENABLED_STR))
		{
			return true;
		}
		
		return tagCompound.getBoolean(ENABLED_STR);
	}
	
	public static void toggle(ItemStack stack)
	{
		NBTTagCompound tagCompound = CyberwareAPI.getCyberwareNBT(stack);
		if (isEnabled(stack))
		{
			tagCompound.setBoolean(ENABLED_STR, false);
		}
		else
		{
			tagCompound.removeTag(ENABLED_STR);
		}
	}
	
	public static String getUnlocalizedLabel(ItemStack stack)
	{
		if (isEnabled(stack))
		{
			return "cyberware.gui.active.disable";
		}
		else
		{
			return "cyberware.gui.active.enable";
		}
	}
}
