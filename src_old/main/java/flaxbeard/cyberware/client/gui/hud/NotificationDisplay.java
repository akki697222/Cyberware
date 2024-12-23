package flaxbeard.cyberware.client.gui.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.hud.HudElementBase;
import flaxbeard.cyberware.api.hud.INotification;
import flaxbeard.cyberware.api.hud.NotificationInstance;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import flaxbeard.cyberware.common.handler.HudHandler;

public class NotificationDisplay extends HudElementBase
{
	
	public NotificationDisplay()
	{
		super("cyberware:notification");
		setDefaultX(5);
		setDefaultY(5 - 20);
		setWidth(5 * 18);
		setHeight(14 + 20 + 4);
		setDefaultVerticalAnchor(EnumAnchorVertical.BOTTOM);
	}
	
	private static int cache_tickExisted = -1;
	private static int cache_tierRadio = -1;
	private static boolean cache_isWearingLightArmor = false;
	private static final NotificationInstance[] examples = new NotificationInstance[] {
			new NotificationInstance(0, new NotificationArmor(true)),
			new NotificationInstance(0, new NotificationArmor(false)),
			new NotificationInstance(0, new NotificationArmor(true)),
			new NotificationInstance(0, new NotificationArmor(false))
	};

	@Override
	public void renderElement(int x, int y, EntityPlayer entityPlayer, ScaledResolution resolution, boolean isHUDjackAvailable, boolean isConfigOpen, float partialTicks)
	{
		if ( isHidden()
		  || !isHUDjackAvailable ) {
			return;
		}
		
		boolean isTopAnchored = getVerticalAnchor() == EnumAnchorVertical.TOP;
		boolean isRightAnchored = getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT;
		
		float currTime = entityPlayer.ticksExisted + partialTicks;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
		
		if (entityPlayer.ticksExisted != cache_tickExisted)
		{
			cache_tickExisted = entityPlayer.ticksExisted;
			boolean wasWearingLightArmor = cache_isWearingLightArmor;
			cache_isWearingLightArmor = ArmorClass.isWearingLightOrNone(entityPlayer);
			if (cache_isWearingLightArmor != wasWearingLightArmor)
			{
				HudHandler.addNotification(new NotificationInstance(currTime, new NotificationArmor(cache_isWearingLightArmor)));
			}
			
			int tierRadioPrevious = cache_tierRadio;
			cache_tierRadio = TileEntityBeacon.isInRange(entityPlayer.world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);
			if (cache_tierRadio != tierRadioPrevious)
			{
				HudHandler.addNotification(new NotificationInstance(currTime, new NotificationRadio(cache_tierRadio)));
			}
		}
		
		// Render some placeholder notifications if the Hud config GUI is open so that the player can see what it'll look like in use
		if (isConfigOpen)
		{
			for (int indexNotification = 0; indexNotification < examples.length; indexNotification++)
			{
				NotificationInstance notificationInstance = examples[indexNotification];
				INotification notification = notificationInstance.getNotification();
				double percentVisible = 0F;
				if (indexNotification == 0)
				{
					percentVisible = (entityPlayer.ticksExisted % 40F) / 40F;
				}

				float yOffset = (float) (20F * Math.sin(percentVisible * Math.PI / 2F));
				
				GlStateManager.pushMatrix();
				GlStateManager.color(1.0F, 1.0F, 1.0F);
				GlStateManager.translate(0F, isTopAnchored ? -yOffset : yOffset, 0F);
				int index = (examples.length - 1) - indexNotification;
				int xPos = isRightAnchored ? (x + getWidth() - ((index + 1) * 18)) : (x + index * 18);
				notification.render(xPos, y + (isTopAnchored ? 20 : 0));
				GlStateManager.popMatrix();
			}
		}
		else
		{
			List<NotificationInstance> notificationsElapsed = new ArrayList<>();
			for (int indexNotification = 0; indexNotification < HudHandler.notifications.size(); indexNotification++)
			{
				NotificationInstance notificationInstance = HudHandler.notifications.get(indexNotification);
				INotification notification = notificationInstance.getNotification();
				if (currTime - notificationInstance.getCreatedTime() < notification.getDuration() + 25)
				{
					double percentVisible = Math.max(0F, (currTime - notificationInstance.getCreatedTime() - notification.getDuration()) / 30F);
	
					float yOffset = (float) (20F * Math.sin(percentVisible * Math.PI / 2F));
					
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0F, 1.0F, 1.0F);
					GlStateManager.translate(0F, isTopAnchored ? -yOffset : yOffset, 0F);
					int index = (HudHandler.notifications.size() - 1) - indexNotification;
					int xPos = isRightAnchored ? (x + getWidth() - ((index + 1) * 18)) : (x + index * 18);
					notification.render(xPos, y + (isTopAnchored ? 20 : 0));
					GlStateManager.popMatrix();
				}
				else
				{
					notificationsElapsed.add(notificationInstance);
				}
			}
			
			for (NotificationInstance notificationInstance : notificationsElapsed)
			{
				HudHandler.notifications.remove(notificationInstance);
			}
		}
		
		// GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}
	
	@SideOnly(Side.CLIENT)
	private static class NotificationArmor implements INotification
	{
		private boolean light;
		
		private NotificationArmor(boolean light)
		{
			this.light = light;
		}

		@Override
		public void render(int x, int y)
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
			GlStateManager.pushMatrix();
			float[] color = CyberwareAPI.getHUDColor();
			GlStateManager.color(color[0], color[1], color[2]);
			ClientUtils.drawTexturedModalRect(x, y + 1, 0, 25, 15, 14);
			GlStateManager.popMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F);

			if (light)
			{
				ClientUtils.drawTexturedModalRect(x + 9, y + 1 + 7, 15, 25, 7, 9);
			}
			else
			{
				ClientUtils.drawTexturedModalRect(x + 8, y + 1 + 7, 22, 25, 8, 9);
			}
		}

		@Override
		public int getDuration()
		{
			return 20;
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static class NotificationRadio implements INotification
	{
		private int tier;
		
		private NotificationRadio(int tier)
		{
			this.tier = tier;
		}

		@Override
		public void render(int x, int y)
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
			if (tier > 0)
			{
				GlStateManager.pushMatrix();
				float[] color = CyberwareAPI.getHUDColor();
				GlStateManager.color(color[0], color[1], color[2]);
				ClientUtils.drawTexturedModalRect(x, y + 1, 13, 39, 15, 14);
				GlStateManager.popMatrix();
				
				String textRadioTier = tier == 1 ? I18n.format("cyberware.gui.radio_internal") : Integer.toString(tier - 1);
				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
				fontRenderer.drawStringWithShadow(textRadioTier, x + 15 - fontRenderer.getStringWidth(textRadioTier), y + 9, 0xFFFFFF);
			}
			else
			{
				float[] color = CyberwareAPI.getHUDColor();
				GlStateManager.color(color[0], color[1], color[2]);
				ClientUtils.drawTexturedModalRect(x, y + 1, 28, 39, 15, 14);
			}
		}

		@Override
		public int getDuration()
		{
			return 40;
		}
	}
}
