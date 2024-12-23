package flaxbeard.cyberware.api;

import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.integration.CyberwareMatterOverdriveCheck;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.items.ItemStackHandler;

public class CyberwareSurgeryEvent extends EntityEvent
{
	
	public CyberwareSurgeryEvent(EntityLivingBase entityLivingBase)
	{
		super(entityLivingBase);
	}
	
	/**
	 * Fired when the Surgery Chamber starts the process of altering an entities installed Cyberware
	 * Changing inventories isn't supported.
	 * Cancel to prevent any changes
	 */
	@Cancelable
	public static class Pre extends CyberwareSurgeryEvent
	{
		public ItemStackHandler inventoryActual;
		public ItemStackHandler inventoryTarget;
		
		public Pre(EntityLivingBase entityLivingBase, ItemStackHandler inventoryActual, ItemStackHandler inventoryTarget)
		{
			super(entityLivingBase);
			
			this.inventoryActual = new ItemStackHandler(120);
			this.inventoryActual.deserializeNBT(inventoryActual.serializeNBT());
			this.inventoryTarget = new ItemStackHandler(120);
			this.inventoryTarget.deserializeNBT(inventoryTarget.serializeNBT());
			
			if (isAndroid(entityLivingBase)){
				setCanceled(true);
			}
		}

		private boolean isAndroid(EntityLivingBase entityLivingBase){
			if ( CyberwareConfig.INT_MATTER_OVERDRIVE
			  && Loader.isModLoaded("matteroverdrive")
			  && entityLivingBase instanceof EntityPlayer ){
				return CyberwareMatterOverdriveCheck.isPlayerAndroid((EntityPlayer)entityLivingBase);
			}
			return false;
		}
		
		public ItemStackHandler getActualCyberwares()
		{
			return inventoryActual;
		}
		
		public ItemStackHandler getTargetCyberwares()
		{
			return inventoryTarget;
		}
	}
	
	/**
	 * Fired when the Surgery Chamber finishes the process of altering an entities installed Cyberware
	 */
	public static class Post extends CyberwareSurgeryEvent
	{
		public Post(EntityLivingBase entityLivingBase)
		{
			super(entityLivingBase);
		}
	}
}
