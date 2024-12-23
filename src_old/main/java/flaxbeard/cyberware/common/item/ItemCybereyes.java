package flaxbeard.cyberware.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ItemCybereyes extends ItemCyberware
{

	private static boolean isBlind;
	
	public ItemCybereyes(String name, EnumSlot slot)
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
	
	@SubscribeEvent
	public void handleBlindnessImmunity(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (!entityLivingBase.isPotionActive(MobEffects.BLINDNESS)) return;
		
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		
		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(0)))
		{
			entityLivingBase.removePotionEffect(MobEffects.BLINDNESS);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleMissingEssentials(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (entityLivingBase.ticksExisted % 20 != 0) return;
		
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		
		ItemStack itemStackCybereye = cyberwareUserData.getCyberware(getCachedStack(0));
		if (!itemStackCybereye.isEmpty())
		{
			boolean isPowered = cyberwareUserData.usePower(itemStackCybereye, getPowerConsumption(itemStackCybereye));
			if ( entityLivingBase.world.isRemote
			  && entityLivingBase == Minecraft.getMinecraft().player )
			{
				isBlind = !isPowered;
			}
		}
		else if ( entityLivingBase.world.isRemote
		       && entityLivingBase == Minecraft.getMinecraft().player )
		{
			isBlind = false;
		}
		
		if (isBlind)
		{
			entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40));
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void overlayPre(RenderGameOverlayEvent.Pre event)
	{
		if (event.getType() == ElementType.ALL)
		{
			EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
			
			if (isBlind && !entityPlayer.isCreative())
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
				Minecraft.getMinecraft().getTextureManager().bindTexture(EssentialsMissingHandler.BLACK_PX);
				ClientUtils.drawTexturedModalRect(0, 0, 0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
				GlStateManager.popMatrix();
			}
		}
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return LibConstants.CYBEREYES_CONSUMPTION;
	}
}
