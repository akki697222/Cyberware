package flaxbeard.cyberware.common.handler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.input.Mouse;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.client.ClientUtils;

public class CreativeMenuHandler
{
	private static class CEXButton extends GuiButton
	{
		public final int offset;
		public final int baseX;
		public final int baseY;
		
		public CEXButton(int buttonId, int x, int y, int offset)
		{
			super(buttonId, x, y, 21, 21, "");
			this.offset = offset;
			this.baseX = this.x;
			this.baseY = this.y;
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if (this.visible)
			{
				boolean down = Mouse.isButtonDown(0);
				boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				
				mc.getTextureManager().bindTexture(CEX_GUI_TEXTURES);

				boolean isDown = (down && flag) || pageSelected == offset;
	
				int i = 4;
				int j = 8;
				if (isDown)
				{
					i = 29;
					j = 0;
				}
				
				j += offset * (isDown ? 18 : 23);
				this.drawTexturedModalRect(this.x, this.y, i, j, 18, 18);
			}
		}
	}
	
	public static CreativeMenuHandler INSTANCE = new CreativeMenuHandler();
	
	private static final ResourceLocation CEX_GUI_TEXTURES = new ResourceLocation(Cyberware.MODID + ":textures/gui/creative_expansion.png");
	private Minecraft mc = Minecraft.getMinecraft();
	public static int pageSelected = 1;
	private static CEXButton salvaged;
	private static CEXButton manufactured;
	
	@SubscribeEvent
	public void handleButtons(InitGuiEvent event)
	{
		if (event.getGui() instanceof GuiContainerCreative)
		{
			GuiContainerCreative gui = (GuiContainerCreative) event.getGui();

			int i = (gui.width - 136) / 2;
			int j = (gui.height - 195) / 2;
			
			List<GuiButton> buttons = event.getButtonList();
			buttons.add(salvaged     = new CEXButton(355, i + 166 + 4, j + 30 + 8, 0));
			buttons.add(manufactured = new CEXButton(356, i + 166 + 4, j + 30 + 31, 1));
			
			if (isCorrectGui(event.getGui()))
			{
				salvaged.visible = false;
				manufactured.visible = false;
			}
			event.setButtonList(buttons);
		}
	}
	
	@SubscribeEvent
	public void handleTooltips(DrawScreenEvent.Post event)
	{
		if (isCorrectGui(event.getGui()))
		{
			int mouseX = event.getMouseX();
			int mouseY = event.getMouseY();
			GuiContainerCreative gui = (GuiContainerCreative) event.getGui();
			int i = (gui.width - 136) / 2;
			int j = (gui.height - 195) / 2;
			if (isPointInRegion(i, j, salvaged.x - i, 29 + 8, 18, 18, mouseX, mouseY))
			{
				ClientUtils.drawHoveringText(gui,
				                             Collections.singletonList(I18n.format(CyberwareAPI.QUALITY_SCAVENGED.getUnlocalizedName())),
				                             mouseX, mouseY,
				                             mc.getRenderManager().getFontRenderer());
			}
			
			if (isPointInRegion(i, j, manufactured.x - i, 29 + 8 + 23, 18, 18, mouseX, mouseY))
			{
				ClientUtils.drawHoveringText(gui,
				                             Collections.singletonList(I18n.format(CyberwareAPI.QUALITY_MANUFACTURED.getUnlocalizedName())),
				                             mouseX, mouseY,
				                             mc.getRenderManager().getFontRenderer());
			}
		}
	}
	
	@SubscribeEvent
	public void handleCreativeInventory(BackgroundDrawnEvent event)
	{
		if (isCorrectGui(event.getGui()))
		{
			GuiContainerCreative gui = (GuiContainerCreative) event.getGui();
			int i = (gui.width - 136) / 2;
			int j = (gui.height - 195) / 2;
			
			int xSize = 29;
			int ySize = 129;
			
			int xOffset = 0;
			boolean hasVisibleEffect = false;
			for(PotionEffect potioneffect : mc.player.getActivePotionEffects())
			{
				Potion potion = potioneffect.getPotion();
				if(potion.shouldRender(potioneffect)) {
					hasVisibleEffect = true; break;
				}
			}
			if (!this.mc.player.getActivePotionEffects().isEmpty() && hasVisibleEffect)
			{
				xOffset = 59;
			}
			salvaged.x = salvaged.baseX + xOffset;
			manufactured.x = manufactured.baseX + xOffset;

			
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.getTextureManager().bindTexture(CEX_GUI_TEXTURES);
			gui.drawTexturedModalRect(i + 166 + xOffset, j + 30, 0, 0, xSize, ySize);
			
			salvaged.visible = true;
			manufactured.visible = true;
		}
		else
		{
			if (salvaged != null) salvaged.visible = false;
			if (manufactured != null) manufactured.visible = false;
		}
	}
	
	@SubscribeEvent
	public void handleButtonClick(ActionPerformedEvent event)
	{
		if (isCorrectGui(event.getGui()))
		{
			GuiContainerCreative gui = (GuiContainerCreative) event.getGui();

			if (event.getButton().id == salvaged.id)
			{
				pageSelected = salvaged.offset;
			}
			else if (event.getButton().id == manufactured.id)
			{
				pageSelected = manufactured.offset;
			}
			
			// force a refresh of the page
			// note: this only called client side, when clicking, hence there's no need to cache it
			gui.setCurrentCreativeTab(Cyberware.creativeTab);
		}
	}

	private boolean isCorrectGui(GuiScreen gui)
	{
		if (gui instanceof GuiContainerCreative)
		{
			return ((GuiContainerCreative) gui).getSelectedTabIndex() == Cyberware.creativeTab.getIndex();
		}
		return false;
	}
	
	private boolean isPointInRegion(int i, int j, int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
	{
		pointX = pointX - i;
		pointY = pointY - j;
		return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
	}
}
