package flaxbeard.cyberware.client.gui.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.hud.HudElementBase;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.handler.HudHandler;

public class MissingPowerDisplay extends HudElementBase
{
	private static final List<ItemStack> exampleStacks = new ArrayList<>();
	static
	{
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
		exampleStacks.add(new ItemStack(CyberwareContent.cybereyes));
	}
	
	public MissingPowerDisplay()
	{
		super("cyberware:missing_power");
		setDefaultX(-15);
		setDefaultY(35);
		setWidth(16 + 20);
		setHeight(18 * 8);
	}
	
	@Override
	public void renderElement(int x, int y, EntityPlayer entityPlayer, ScaledResolution resolution, boolean isHUDjackAvailable, boolean isConfigOpen, float partialTicks)
	{
		if ( isHidden()
		  || !isHUDjackAvailable ) {
			return;
		}
		
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;
		
		boolean isRightAnchored = getHorizontalAnchor() == EnumAnchorHorizontal.RIGHT;
		float currTime = entityPlayer.ticksExisted + partialTicks;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
		
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		List<ItemStack> stacksPowerOutage = isConfigOpen ? exampleStacks : cyberwareUserData.getPowerOutages();
		List<Integer> timesPowerOutage = cyberwareUserData.getPowerOutageTimes();
		List<Integer> indexesElapsed = new ArrayList<>();
		float zLevelSaved = renderItem.zLevel;
		renderItem.zLevel = -300;
		int xPosition = x - 1 + (isRightAnchored ? 0 : 20);
		int yPosition = y;
		for (int index = stacksPowerOutage.size() - 1; index >= 0; index--)
		{
			ItemStack stack = stacksPowerOutage.get(index);
			if (!stack.isEmpty())
			{
				int time = (int) currTime;
				if (isConfigOpen)
				{
					if (index == 0)
					{
						time = (int) (currTime - 20 - (entityPlayer.ticksExisted % 40));
					}
				}
				else
				{
					time = timesPowerOutage.get(index);
				}
				
				if (entityPlayer.ticksExisted - time < 50)
				{
					double percentVisible = Math.max(0F, (currTime - time - 20) / 30F);
					float xOffset = (float) (20F * Math.sin(percentVisible * Math.PI / 2F));
					
					GlStateManager.pushMatrix();
					GlStateManager.translate(isRightAnchored ? xOffset : -xOffset, 0.0F, 0.0F);
					
					fontRenderer.drawStringWithShadow("!", xPosition + 14, yPosition + 8, 0xFF0000);
					
					RenderHelper.enableStandardItemLighting();
					renderItem.renderItemAndEffectIntoGUI(stack, xPosition, yPosition);
					RenderHelper.disableStandardItemLighting();
					
					GlStateManager.popMatrix();
					yPosition += 18;
				}
				else if (!isConfigOpen)
				{
					indexesElapsed.add(index);
				}
			}
		}
		renderItem.zLevel = zLevelSaved;
		
		for (int indexElapsed : indexesElapsed)
		{
			stacksPowerOutage.remove(indexElapsed);
			timesPowerOutage.remove(indexElapsed);
		}
		
		GlStateManager.popMatrix();
	}
}
