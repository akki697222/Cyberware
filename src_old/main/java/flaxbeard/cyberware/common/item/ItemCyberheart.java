package flaxbeard.cyberware.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ItemCyberheart extends ItemCyberware
{

	public ItemCyberheart(String name, EnumSlot slot)
	{
		super(name, slot);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public boolean isEssential(ItemStack stack)
	{
		return true;		
	}
	
	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return CyberwareAPI.getCyberware(other).isEssential(other);
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (entityLivingBase.ticksExisted % 20 != 0) return;
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		ItemStack itemStackCyberheart = cyberwareUserData.getCyberware(getCachedStack(0));
		
		if (!itemStackCyberheart.isEmpty())
		{
			if (!cyberwareUserData.usePower(itemStackCyberheart, getPowerConsumption(itemStackCyberheart)))
			{
				entityLivingBase.attackEntityFrom(EssentialsMissingHandler.heartless, Integer.MAX_VALUE);
			}
			else if (entityLivingBase.isPotionActive(MobEffects.WEAKNESS))
			{
				entityLivingBase.removePotionEffect(MobEffects.WEAKNESS);
			}
		}
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return LibConstants.HEART_CONSUMPTION;
	}

}
