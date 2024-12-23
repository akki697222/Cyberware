package flaxbeard.cyberware.client.gui;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import org.lwjgl.opengl.GL11;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.client.gui.ContainerSurgery.SlotSurgery;
import flaxbeard.cyberware.client.render.ModelBox;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.SurgeryRemovePacket;

@SideOnly(Side.CLIENT)
public class GuiSurgery extends GuiContainer
{
	private static class GuiButtonSurgeryLocation extends GuiButton
	{
		private static final int buttonSize = 16;
		private float x3;
		private float y3;
		private float z3;
		private float xPos;
		private float yPos;
		
		public GuiButtonSurgeryLocation(int buttonId, float x3, float y3, float z3)
		{
			super(buttonId, 0, 0, buttonSize, buttonSize, "");
			this.x3 = x3;
			this.y3 = y3;
			this.z3 = z3;
			this.visible = false;
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if (visible)
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				
				float trans = 0.4F;
				if ( mouseX >= x
				  && mouseY >= y
				  && mouseX < x + buttonSize
				  && mouseY < y + buttonSize )
				{
					trans = 0.6F;
				}
				GlStateManager.color(1.0F, 1.0F, 1.0F, trans);

				
				mc.getTextureManager().bindTexture(SURGERY_GUI_TEXTURES);
				GlStateManager.translate(xPos, yPos, 0);
				drawTexturedModalRect(0, 0, 194, 0, width, height);

				GlStateManager.popMatrix();
			}
		}
	}
	
	private static class GuiButtonSurgery extends GuiButton
	{
		public GuiButtonSurgery(int buttonId, int x, int y, int xSize, int ySize)
		{
			super(buttonId, x, y, xSize, ySize, "");
		}
		
		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			// no operation
		}
	}
	
	private enum Type
	{
		BACK(176, 111, 18, 10),
		INDEX(176, 122, 12, 11);
		
		private int left;
		private int top;
		private int width;
		private int height;
		
		Type(int left, int top, int width, int height)
		{
			this.left = left;
			this.top = top;
			this.width = width;
			this.height = height;
		}
	}
	
	private static class InterfaceButton extends GuiButton
	{
		private final Type type;
		
		public InterfaceButton(int buttonId, int x, int y, Type type)
		{
			super(buttonId, x, y, type.width, type.height, "");
			this.type = type;
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if (visible)
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				float trans = 0.4F;
				boolean isHovering = mouseX >= x
				                  && mouseY >= y
				                  && mouseX < x + width
				                  && mouseY < y + height;
				if (isHovering) trans = 0.6F;
				
				GlStateManager.color(1.0F, 1.0F, 1.0F, trans);
				mc.getTextureManager().bindTexture(SURGERY_GUI_TEXTURES);
				
				drawTexturedModalRect(x, y, type.left + type.width, type.top, type.width, type.height);
				
				GlStateManager.color(1.0F, 1.0F, 1.0F, trans / 2F);
				drawTexturedModalRect(x, y, type.left, type.top, type.width, type.height);
				
				GlStateManager.popMatrix();
			}
		}
	}
	
	private static class PageConfiguration
	{
		private float rotation;
		private float x;
		private float y;
		private float scale;
		private float boxWidth;
		private float boxHeight;
		private float boxX;
		private float boxY;
		
		private PageConfiguration(float rotation, float x, float y, float scale)
		{
			this(rotation, x, y, scale, 0, 0, 0, 0);
		}
		
		private PageConfiguration(float rotation, float x, float y, float scale, float boxWidth, float boxHeight, float boxX, float boxY)
		{
			this.rotation = rotation;
			this.x = x;
			this.y = y;
			this.scale = scale;
			this.boxHeight = boxHeight;
			this.boxWidth = boxWidth;
			this.boxX = boxX;
			this.boxY = boxY;
		}

		public PageConfiguration copy()
		{
			return new PageConfiguration(rotation, x, y, scale, boxWidth, boxHeight, boxX, boxY);
		}

	}
	
	private static final ResourceLocation SURGERY_GUI_TEXTURES = new ResourceLocation(Cyberware.MODID + ":textures/gui/surgery.png");
	private static final ResourceLocation GREY_TEXTURE = new ResourceLocation(Cyberware.MODID + ":textures/gui/greypx.png");
	private static final ResourceLocation BLUE_TEXTURE = new ResourceLocation(Cyberware.MODID + ":textures/gui/bluepx.png");

	private final TileEntitySurgery surgery;
	
	private Entity skeleton;
	private ModelBox box;

	private float partialTicks;
	
	private GuiButtonSurgery[] bodyIcons = new GuiButtonSurgery[7];
	private InterfaceButton back;
	private InterfaceButton index;

	private GuiButtonSurgeryLocation[] headIcons = new GuiButtonSurgeryLocation[3];
	private GuiButtonSurgeryLocation[] torsoIcons = new GuiButtonSurgeryLocation[4];
	private GuiButtonSurgeryLocation[] crossSectionIcons = new GuiButtonSurgeryLocation[3];
	private GuiButtonSurgeryLocation[] armIcons = new GuiButtonSurgeryLocation[2];
	private GuiButtonSurgeryLocation[] legIcons = new GuiButtonSurgeryLocation[2];

	private PageConfiguration current;
	private PageConfiguration target;
	private PageConfiguration ease;
	
	private NonNullList<ItemStack> indexStacks;
	private int[] indexPages;
	private int[] indexNews;

	private int indexCount;
	
	private float lastTicks;
	private float addedRotate;
	private float oldRotate;
	
	private float transitionStart = 0;
	private float operationTime = 0;
	private float amountDone = 1;
	
	private float openTime = 0;
	
	private int page = 0;
	private boolean mouseDown;
	private int mouseDownX;
	private float[] lastDownX = new float[5];
	private float rotateVelocity = 0;
	
	private PageConfiguration[] configs = new PageConfiguration[25];
	List<SlotSurgery> visibleSlots = new ArrayList<>();
	private int parent;
	
	public GuiSurgery(InventoryPlayer inventoryPlayer, TileEntitySurgery surgery)
	{
		super(new ContainerSurgery(inventoryPlayer, surgery));
		
		this.surgery = surgery;
		this.ySize = 222;
		
		configs[0] = new PageConfiguration(0, 0, 0, 50, 35, 35, -50, 10);
		configs[1] = new PageConfiguration(50, 0, 210, 150, 0, 0, -150, 0);
		configs[2] = new PageConfiguration(15, 0, 100, 130, 0, 0, -150, 0);
		configs[3] = new PageConfiguration(-50, 0, 100, 130, 0, 0, -150, 0);
		configs[4] = new PageConfiguration(50, 0, 100, 130, 0, 0, -150, 0);
		configs[5] = new PageConfiguration(-70, 0, 10, 130, 0, 0, -150, 0);
		configs[6] = new PageConfiguration(70, 0, 10, 130, 0, 0, -150, 0);
		configs[7] = new PageConfiguration(0, 0, 0, 50, 170, 125, 0, 0);

		configs[11] = new PageConfiguration(160, 0, 300, 200);
		configs[12] = new PageConfiguration(5, 0, 330, 220);
		configs[13] = new PageConfiguration(5, 0, 330, 220);
		configs[14] = new PageConfiguration(-20, 0, 220, 210);
		configs[15] = new PageConfiguration(0, 0, 180, 180);
		configs[16] = new PageConfiguration(0, 0, 180, 180);
		configs[17] = new PageConfiguration(0, 0, 125, 180);
		configs[18] = new PageConfiguration(0, 0, 0, 50, 190, 180, 0, 0);
		configs[19] = new PageConfiguration(0, 0, 0, 50, 170, 180, 0, 0);
		configs[20] = new PageConfiguration(0, 0, 0, 50, 170, 180, 0, 0);

		configs[21] = new PageConfiguration(-70, 0, 180, 200);
		configs[22] = new PageConfiguration(-70, 0, 120, 220);
		
		configs[23] = new PageConfiguration(10, 0, 20, 200);
		configs[24] = new PageConfiguration(10, 0, -30, 220);

		current = ease = target = configs[0].copy();
	}
		
	@Override
	public void initGui()
	{
		super.initGui();
		int xLeft = (width - xSize) / 2;
		int yTop = (height - ySize) / 2;
		buttonList.add(bodyIcons[0] = new GuiButtonSurgery(1, xLeft + (xSize / 2) - 18, yTop + 8, 36, 27));
		buttonList.add(bodyIcons[1] = new GuiButtonSurgery(2, xLeft + (xSize / 2) - 13, yTop + 35, 26, 38));
		buttonList.add(bodyIcons[2] = new GuiButtonSurgery(3, xLeft + (xSize / 2) - 8 + 21, yTop + 35, 16, 38));
		buttonList.add(bodyIcons[3] = new GuiButtonSurgery(4, xLeft + (xSize / 2) - 8 - 21, yTop + 35, 16, 38));
		buttonList.add(bodyIcons[4] = new GuiButtonSurgery(5, xLeft + (xSize / 2) - 6 + 7, yTop + 73, 12, 39));
		buttonList.add(bodyIcons[5] = new GuiButtonSurgery(6, xLeft + (xSize / 2) - 6 - 7, yTop + 73, 12, 39));
		buttonList.add(back = new InterfaceButton(8, xLeft + xSize - 25, yTop + 5, Type.BACK));
		buttonList.add(index = new InterfaceButton(9, xLeft + xSize - 22, yTop + 5, Type.INDEX));
		back.visible = false;
		
		buttonList.add(bodyIcons[6] = new GuiButtonSurgery(7, 
				xLeft + (int) (xSize / 2 + configs[0].boxX - (configs[0].boxWidth / 2)),
				yTop + (int) ((125F / 2F) + 3F + configs[0].boxY - (configs[0].boxHeight / 2)),
				(int) configs[0].boxWidth, (int) configs[0].boxHeight)); // CAW

		buttonList.add(headIcons[0] = new GuiButtonSurgeryLocation(11, -2F, 19, 0));
		buttonList.add(headIcons[1] = new GuiButtonSurgeryLocation(12, 4F, 21, 2.F));
		buttonList.add(headIcons[2] = new GuiButtonSurgeryLocation(13, 4F, 21, -2F));
		buttonList.add(torsoIcons[0] = new GuiButtonSurgeryLocation(14, 1F, 8, -1F));
		buttonList.add(torsoIcons[1] = new GuiButtonSurgeryLocation(15, 0F, 9, -2F));
		buttonList.add(torsoIcons[2] = new GuiButtonSurgeryLocation(16, 0F, 9, 2F));
		buttonList.add(torsoIcons[3] = new GuiButtonSurgeryLocation(17, 0F, 13, 0F));
		buttonList.add(crossSectionIcons[0] = new GuiButtonSurgeryLocation(18, -12F, -8, -1F));
		buttonList.add(crossSectionIcons[1] = new GuiButtonSurgeryLocation(19, 12F, -1, 2F));
		buttonList.add(crossSectionIcons[2] = new GuiButtonSurgeryLocation(20, 3F, 5, 12F));
		buttonList.add(armIcons[0] = new GuiButtonSurgeryLocation(21, 0F, 10, -5.3F));
		buttonList.add(armIcons[1] = new GuiButtonSurgeryLocation(22, 0F, 16, -6.0F));
		buttonList.add(legIcons[0] = new GuiButtonSurgeryLocation(23, 0F, 1, -2.2F));
		buttonList.add(legIcons[1] = new GuiButtonSurgeryLocation(24, 0F, 6.4F, -2.2F));
		updateSurgerySlotsVisibility(true);
	}
	
	private void prepTransition(int time, int targetPage)
	{
		if (page == index.id)
		{					
			if (targetPage == 0)
			{
				back.visible = false;
				
				page = 0;
				showHideRelevantButtons(true);
				ease = current = configs[0].copy();

				return;
			}
			else
			{
				if ( targetPage >= 18
				  && targetPage <= 20 )
				{
					ease = current = configs[targetPage].copy();
					page = targetPage;
					showHideRelevantButtons(true);
					return;
				}
				else
				{
					if (time == 0)
					{
						ease = current = configs[targetPage].copy();
						page = targetPage;
						showHideRelevantButtons(true);
						return;
					}
					ease = current = configs[0].copy();
				}
			}


			
		}
		
		// INDEX
		if (targetPage == index.id)
		{
			showHideRelevantButtons(false);

			page = 9;
			parent = 0;
			
			back.visible = true;
			index.visible = false;
			
			indexStacks = NNLUtil.initListOfSize(40);
			indexPages = new int[5 * 8];
			indexNews = new int[5 * 8];

			indexCount = 0;
			for (int indexSurgeySlot = 0; indexSurgeySlot < surgery.slots.getSlots()
			                           && indexCount < indexStacks.size(); indexSurgeySlot++)
			{
				ItemStack playerStack = surgery.slotsPlayer.getStackInSlot(indexSurgeySlot);
				ItemStack surgeryStack = surgery.slots.getStackInSlot(indexSurgeySlot);
				
				int nu = 0;
				ItemStack draw = ItemStack.EMPTY;
				if (!surgeryStack.isEmpty())
				{
					draw = surgeryStack.copy();
					
					if (!playerStack.isEmpty())
					{
						if (CyberwareAPI.areCyberwareStacksEqual(playerStack, surgeryStack))
						{
							draw.grow(playerStack.getCount());
						}
						else
						{
							indexStacks.set(indexCount,playerStack.copy());
							EnumSlot slot = EnumSlot.values()[indexSurgeySlot / LibConstants.WARE_PER_SLOT];
							indexPages[indexCount] = slot.getSlotNumber();
							indexNews[indexCount] = 2;
							indexCount++;
							
							if (indexCount >= indexStacks.size())
							{
								break;
							}
						}
					}
					nu = 1;
				}
				else if ( !playerStack.isEmpty()
				       && !surgery.discardSlots[indexSurgeySlot] )
				{
					draw = playerStack.copy();
				}
				else if ( !playerStack.isEmpty()
				       && surgery.discardSlots[indexSurgeySlot] )
				{
					draw = playerStack.copy();
					nu = 2;
				}
				
				if (!draw.isEmpty())
				{
					indexStacks.set(indexCount, draw);
					EnumSlot slot = EnumSlot.values()[indexSurgeySlot / LibConstants.WARE_PER_SLOT];
					indexPages[indexCount] = slot.getSlotNumber();
					indexNews[indexCount] = nu;
					indexCount++;
				}
				
			}
			
			return;
		}
		
		transitionStart = ticksExisted() + partialTicks;

		current = ease;
		operationTime = amountDone * time;
		
		showHideRelevantButtons(false);
		page = targetPage;
		target = configs[page].copy();
		if (page == 0)
		{
			back.visible = false;
			//index.visible = true;
		}
		else
		{
			back.visible = true;
			index.visible = false;
		}
	}
	
	protected void actionPerformed(GuiButton button)
	{
		if (button.enabled)
		{
			// BACK
			if (button.id == back.id)
			{
				if ( page != 0
				  || ease.rotation != 0 )
				{
					int pageToGoTo = page <= 10 ? 0 : parent;
					prepTransition(20, pageToGoTo);
				}
				return;
			}
			
			openTime = 1;
			if (button.id > 10)
			{
				parent = page;
			}
			
			if (button.id == 4)
			{
				prepTransition(20, 3);
			}
			else if (button.id == 6)
			{
				prepTransition(20, 5);
			}
			else if (button.id == 13)
			{
				prepTransition(20, 12);
			}
			else if (button.id == 16)
			{
				prepTransition(20, 15);
			}
			else
			{
				prepTransition(20, button.id);
			}
		}
	}
	
	private void showHideRelevantButtons(boolean show)
	{
		GuiButton[] list = new GuiButton[0];
		
		switch(page)
		{
			case 0:
				list = bodyIcons;
				break;
			case 1:
				list = headIcons;
				break;
			case 2:
				list = torsoIcons;
				break;
			case 7:
				list = crossSectionIcons;
				break;
			case 5:
				list = legIcons;
				break;
			case 3:
				list = armIcons;
				break;
		}
		
		for (GuiButton guiButton : list) {
			guiButton.visible = show;
		}
		
		updateSurgerySlotsVisibility(show);
	}
	
	private void updateLocationButtons(float rot, float scale, float yOffset)
	{
		//SPECIAL CASE FOR GOING BACK TO MENU
		if (page == 0)
		{
			index.visible = true;
		}
		int xLeft = (width - xSize) / 2;
		int yTop = (height - ySize) / 2;
		
		GuiButtonSurgeryLocation[] list = new GuiButtonSurgeryLocation[0];
		
		switch(page)
		{
			case 1:
				list = headIcons;
				break;
			case 2:
				list = torsoIcons;
				break;
			case 7:
				list = crossSectionIcons;
				break;
			case 5:
				list = legIcons;
				break;
			case 3:
				list = armIcons;
				break;
		}
		
		if (page == 7)
		{
			rot += addedRotate;
		}
		
		float radRot = (float) Math.toRadians(rot);
		float sin = (float) Math.sin(radRot);
		float cos = -(float) Math.cos(radRot);
		float upDown = page == 7 ? (float) Math.sin(Math.toRadians(10)) : 0;
		
		for (GuiButtonSurgeryLocation guiButtonSurgeryLocation : list) {
			guiButtonSurgeryLocation.xPos = xLeft
			                              + sin * scale * guiButtonSurgeryLocation.x3 * 0.065F
			                              + cos * scale * guiButtonSurgeryLocation.z3 * 0.065F
			                              + xSize / 2F
			                              - 2.0F
			                              - guiButtonSurgeryLocation.width / 2F;
			guiButtonSurgeryLocation.yPos = -upDown * cos * scale * guiButtonSurgeryLocation.x3 * 0.065F
			                              +  upDown * sin * scale * guiButtonSurgeryLocation.z3 * 0.065F
			                              + yTop + 2 - yOffset
			                              + scale * guiButtonSurgeryLocation.y3 * 0.065F
			                              + 130 / 2F
			                              - guiButtonSurgeryLocation.height / 2F;
			guiButtonSurgeryLocation.x = Math.round(guiButtonSurgeryLocation.xPos);
			guiButtonSurgeryLocation.y = Math.round(guiButtonSurgeryLocation.yPos);
		}
	}
	
	private void drawSlots()
	{
		int xLeft = (width - xSize) / 2;
		int yTop = (height - ySize) / 2;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		
		int essence = (int) ((surgery.essence * 1F / surgery.maxEssence) * 49);
		int criticalEssence = (int) ((CyberwareConfig.CRITICAL_ESSENCE * 1F  / surgery.maxEssence) * 49);
		// TODO int warningEssence = (int) ((LibConstants.WARNING_ESSENCE * 1F  / surgery.maxEssence) * 49);
		int warningEssence = criticalEssence;
		zLevel = 200;
		
		mc.getTextureManager().bindTexture(SURGERY_GUI_TEXTURES);
		
		if (surgery.wrongSlot != -1)
		{
			float trans = 1.0F - ((ticksExisted() + partialTicks) - surgery.ticksWrong) / 10F;
			if (trans > 0)
			{
				GlStateManager.color(1.0F, 1.0F, 1.0F, trans);
	
				Slot slot = inventorySlots.inventorySlots.get(surgery.wrongSlot);
				drawTexturedModalRect(xLeft + slot.xPos - 5, yTop + slot.yPos - 5, 185, 61, 26, 26);		// Blue slot
			}
			else
			{
				surgery.wrongSlot = -1;
			}
		}
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		
		// Draw the less-transparent slot borders
		for (SlotSurgery pos : visibleSlots)
		{
			drawTexturedModalRect(xLeft + pos.xPos - 1, yTop + pos.yPos - 1, 176, 43, 18, 18);		// Blue slot
			drawTexturedModalRect(xLeft + pos.xPos - 1, yTop + pos.yPos - 1 - 26, 176, 18, 18, 25);	// Red 'slot'
		}
		
		if (page != index.id)
		{
			// Draw the solid part of the essence bar
			drawTexturedModalRect(xLeft + 5, yTop + 5 + (49 - essence), 176, 61 + (49 - essence), 9, Math.max(0, essence - warningEssence));
			
			drawTexturedModalRect(xLeft + 5, yTop + 5 + (49 - Math.min(warningEssence, essence)), 229, 61 + (49 - Math.min(warningEssence, essence)), 9, Math.max(0, Math.min(warningEssence, essence) - criticalEssence));
	
			drawTexturedModalRect(xLeft + 5, yTop + 5 + (49 - Math.min(criticalEssence, essence)), 220, 61 + (49 - Math.min(criticalEssence, essence)), 9, Math.max(0, Math.min(criticalEssence, essence)));

			// Draw the grey, emptied essence
			drawTexturedModalRect(xLeft + 5, yTop + 5, 211, 61, 9, 49 - essence);
		}
		else
		{
			zLevel = 50;
			for (int w = 0; w < 8; w++)
			{
				for (int h = 0; h < 5; h++)
				{
					drawTexturedModalRect(xLeft + (20 * w + 9) - 1, yTop + (20 * h + 50) - 1 - 26, 176, indexNews[w + h * 8] == 2 ? 18 : 43, 18, 18);
				}
			}
			
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.2F);
			for (int w = 0; w < 8; w++)
			{
				for (int h = 0; h < 5; h++)
				{
					drawTexturedModalRect(xLeft + (20 * w + 9) - 1, yTop + (20 * h + 50) - 1 - 26, 176 + 18, indexNews[w + h * 8] == 2 ? 18 : 43, 18, 18);
				}
			}
			zLevel = 500;
		}

		
		// Draw the more-transparent slot backs
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.2F);
		for (SlotSurgery pos : visibleSlots)
		{
			drawTexturedModalRect(xLeft + pos.xPos - 1, yTop + pos.yPos - 1, 176 + 18, 43, 18, 18);		// Blue slot
			drawTexturedModalRect(xLeft + pos.xPos - 1, yTop + pos.yPos - 1 - 26, 176 + 18, 18, 18, 18);	// Red 'slot'
		}
		
		List<String> missingSlots = new ArrayList<>();

		if (page != index.id)
		{
			if (surgery.essence < 0)
			{
				missingSlots.add(I18n.format("cyberware.gui.noEssence"));
			}
			else if (surgery.essence < CyberwareConfig.CRITICAL_ESSENCE)
			{
				missingSlots.add(I18n.format("cyberware.gui.criticalEssence"));
			}
			
			if (surgery.missingPower)
			{
				missingSlots.add(I18n.format("cyberware.gui.noPower"));
			}
			
			
			for (int index = 0; index < surgery.isEssentialMissing.length; index++)
			{
				EnumSlot slot = EnumSlot.values()[index / 2];
				
				if (slot.isSided())
				{
					if (index % 2 ==0)
					{
						if (surgery.isEssentialMissing[index])
						{
							missingSlots.add(I18n.format("cyberware.gui.missingEssential." + slot.getName() + ".left"));
						}
					}
					else
					{
						if (surgery.isEssentialMissing[index])
						{
							missingSlots.add(I18n.format("cyberware.gui.missingEssential." + slot.getName() + ".right"));
						}
					}
				}
				else if (index % 2 ==0)
				{
					if (surgery.isEssentialMissing[index])
					{
						missingSlots.add(I18n.format("cyberware.gui.missingEssential." + slot.getName()));
					}
				}
	
			}
			
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F);

			if (missingSlots.size() > 0)
			{
				drawTexturedModalRect(xSize - 23 + xLeft, 20 + yTop, 212, 43, 16, 16);
			}
		}
		
		zLevel = 0;

		GlStateManager.popMatrix();
	}
	
	private static ItemStackHandler lastLastInv = new ItemStackHandler(120);
	private static ItemStackHandler lastInv = new ItemStackHandler(120);

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		this.partialTicks = partialTicks;
		float time = ticksExisted() + partialTicks;
		
		// Only play animation if the player's body has changed since last opening
		if (openTime == 0)
		{
			boolean isEqual = true;
			ItemStackHandler newSlots = new ItemStackHandler(surgery.slotsPlayer.getSlots());
			for (int indexSlot = 0; indexSlot < surgery.slotsPlayer.getSlots(); indexSlot++)
			{
				ItemStack surgeryItem = surgery.slotsPlayer.getStackInSlot(indexSlot);
				ItemStack thisItem = lastLastInv.getStackInSlot(indexSlot);
				
				if (!ItemStack.areItemsEqual(surgeryItem, thisItem))
				{
					isEqual = false;
				}
				
				newSlots.setStackInSlot(indexSlot, surgeryItem);
			}
			if (!isEqual)
			{
				openTime = time;
				lastLastInv = lastInv;
				lastInv = newSlots;
			}
			else
			{
				openTime = 1;
			}
		}
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(SURGERY_GUI_TEXTURES);
		int xLeft = (width - xSize) / 2;
		int yTop = (height - ySize) / 2;
		
		zLevel = 0;
		drawTexturedModalRect(xLeft, yTop, 0, 0, xSize, ySize);
		zLevel = 0;
		
		World world = mc.world;
		if ( skeleton == null
		  || skeleton.isDead )
		{
			skeleton = new EntitySkeleton(world);
		}
		else
		{
			skeleton.world = world;
		}
		
		if (box == null)
		{
			box = new ModelBox();
		}
		
		GlStateManager.pushMatrix();

		// If doing a transition
		if (transitionStart != 0)
		{
			// Ensure we rotate the right way
			current.rotation = current.rotation % 360;
			
			if (Math.abs(current.rotation + 360 - target.rotation) < Math.abs(current.rotation - target.rotation))
			{
				current.rotation = current.rotation + 360;
			}
			else if (Math.abs(current.rotation - (target.rotation + 360)) < Math.abs(current.rotation - target.rotation))
			{
				current.rotation = current.rotation - 360;
			}
			
			amountDone = (time - transitionStart) / operationTime;
			ease = interpolate(amountDone, current, target);
			// If we're done, mark that we're done
			if (amountDone >= 1.0F)
			{
				transitionStart = 0;
				ease = target;
				
				showHideRelevantButtons(true);
			}
		}
		
		// Rotate the screen if the player drags (as long as we're not viewing slots)
		if ( mouseDown
		  && page <= 10 )
		{
			ease.rotation = oldRotate + (mouseX - mouseDownX)  % 360;
			for (int index = 1; index < 5; index++)
			{
				lastDownX[index] = lastDownX[index - 1];
			}
			lastDownX[0] = mouseX;
		}
		else
		{
			if (page > 10)
			{
				rotateVelocity = 0;
			}
			ease.rotation += rotateVelocity % 360;
			rotateVelocity *= 0.8F;
		}
		
		float endRotate = ease.rotation;
		
		if (page != index.id)
		{
			mc.getTextureManager().bindTexture(SURGERY_GUI_TEXTURES);
			
			float percentageSkele = Math.min(1.0F, (time - openTime) / 40F);
			if (percentageSkele < 1.0F)
			{
				ease.rotation = (float) (Math.sin(Math.PI * (percentageSkele) / 2F) * 360F);
			}
			
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			scissor(xLeft + 3, yTop + 3, 170, 125);
			
			// Scan line
			if (percentageSkele < 0.9F)
			{
				zLevel = 90;
				GlStateManager.pushMatrix();
	
				GlStateManager.translate(xLeft + xSize / 2 - 40, yTop + (int) (percentageSkele * 125F) + 2, 0F);
				drawTexturedModalRect(0, 0, 176, 110, 80, 1);
				GlStateManager.popMatrix();
				zLevel = 0;
			}
			
	
			if (ease.boxHeight >= 35)
			{
				// Draw the skin cross section and box
				GlStateManager.pushMatrix();
				
				float height = Math.min(125, ease.boxHeight);
				float width = Math.min(170, ease.boxWidth);
				mc.getTextureManager().bindTexture(GREY_TEXTURE);
		
				zLevel = page == 0 ? 90 : 70;
				GlStateManager.translate(xLeft + xSize / 2F + ease.boxX - (width / 2F),
				                         yTop + (125F / 2F) + 3F + ease.boxY - (height / 2F), 0F);
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(width, height, 1F);
				drawTexturedModalRect(0, 0, 0, 0, 1, 1);
				GlStateManager.popMatrix();
				
				mc.getTextureManager().bindTexture(BLUE_TEXTURE);
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(width, 1F, 1F);
				drawTexturedModalRect(0, 0, 0, 0, 1, 1);
				GlStateManager.translate(0F, height - 1F, 0F);
				drawTexturedModalRect(0, 0, 0, 0, 1, 1);
				GlStateManager.popMatrix();
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(1F, height, 1F);
				drawTexturedModalRect(0, 0, 0, 0, 1, 1);
				GlStateManager.translate(width - 1F, 0F, 0F);
				drawTexturedModalRect(0, 0, 0, 0, 1, 1);
				GlStateManager.popMatrix();
				
				GlStateManager.popMatrix();
				
				if (ease.boxHeight == 35)
				{
					GlStateManager.pushMatrix();
					
					// Draw the connectors for the box
					GlStateManager.translate(xLeft + xSize / 2F + ease.boxX - (ease.boxWidth / 2F),
					                         yTop + (125F / 2F) + 3F + ease.boxY - (ease.boxHeight / 2F), 0F);
		
					GlStateManager.pushMatrix();
					drawTexturedModalRect(0, 0, 0, 0, 1, 1);
					GlStateManager.translate((configs[0].boxWidth / 2F), -12F, 0F);
					GlStateManager.scale(1F, 12F, 1F);
					drawTexturedModalRect(0, 0, 0, 0, 1, 1);
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
					drawTexturedModalRect(0, 0, 0, 0, 1, 1);
					GlStateManager.translate((configs[0].boxWidth / 2F) + 1, -12F, 0F);
					GlStateManager.scale(25F, 1F, 1F);
					drawTexturedModalRect(0, 0, 0, 0, 1, 1);
					GlStateManager.popMatrix();
					
					GlStateManager.popMatrix();
				}
				
				zLevel = 0;
				
				ClientUtils.bindTexture( "cyberware:textures/models/skin" + ".png");
				
				if ( !mouseDown
				  && page < 10 )
				{
					addedRotate = (addedRotate + (ticksExisted() + partialTicks - lastTicks) * 2f) % 360;
				}
				lastTicks = ticksExisted() + partialTicks;
				renderModel(box, 
						xLeft + xSize / 2F + ease.boxX,
						yTop + (125F / 2F) + 3F + ease.boxY,
						(ease.boxHeight / 50F) * 40F, endRotate + addedRotate);
			
			}
			
			scissor(xLeft + 3, yTop + 3, 170, (int) (percentageSkele * 125));
			
			renderEntity(skeleton,
			             xLeft + xSize / 2F + ease.x,
			             yTop + 110 + ease.y,
			             ease.scale, endRotate, partialTicks);
			
			scissor(xLeft + 3, yTop + 3 + (int) (percentageSkele * 125), 170, 125 - (int) (percentageSkele * 125));
					
			EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
			
			float f = entityPlayer.renderYawOffset;
			float f1 = entityPlayer.rotationYaw;
			float f2 = entityPlayer.rotationPitch;
			float f3 = entityPlayer.prevRotationYawHead;
			float f4 = entityPlayer.rotationYawHead;
			
			entityPlayer.renderYawOffset = entityPlayer.rotationYaw = entityPlayer.rotationPitch = entityPlayer.prevRotationYawHead = 0;
			entityPlayer.rotationYaw = skeleton.rotationYaw;
			entityPlayer.rotationYawHead = skeleton.getRotationYawHead();
			float swingProgress = entityPlayer.swingProgress;
			entityPlayer.swingProgress = 0F;
		  
			renderEntity(entityPlayer,
			             xLeft + xSize / 2F + ease.x,
			             yTop + 115 + (ease.y) * (60F / 63F),
			             ease.scale * (57F / 50F),
			             ease.rotation  + (float) (5F * Math.sin((time) / 25F)),
			             partialTicks);
			
			entityPlayer.swingProgress = swingProgress;
			entityPlayer.renderYawOffset = f;
			entityPlayer.rotationYaw = f1;
			entityPlayer.rotationPitch = f2;
			entityPlayer.prevRotationYawHead = f3;
			entityPlayer.rotationYawHead = f4;
			
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
		
		updateLocationButtons(endRotate, ease.scale, ease.y);
		
		drawSlots();
		
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		int xLeft = (width - xSize) / 2;
		int yTop = (height - ySize) / 2;
		
		// Rotation
		if ( mouseButton == 0
		  && mouseX >= xLeft
		  && mouseX < xLeft + xSize
		  && mouseY >= yTop
		  && mouseY < yTop + 130 )
		{
			oldRotate = ease.rotation;
			mouseDown = true;
			mouseDownX = mouseX;
			for (int n = 0; n < 5; n++)
			{
				lastDownX[n] = mouseDownX;
			}
		}
		
		// Right click to go back
		if ( mouseButton == 1
		  && ( page != 0
		    || ease.rotation != 0 )
		  && getSlotAtPosition(mouseX, mouseY) == null
		  && mouseY < yTop + 130 )
		{
			int pageToGoTo = page <= 10 ? 0 : parent;
			prepTransition(20, pageToGoTo);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	// Taken from GuiContainer
	private Slot getSlotAtPosition(int x, int y)
	{
		for (int indexSlot = 0; indexSlot < inventorySlots.inventorySlots.size(); indexSlot++)
		{
			Slot slot = inventorySlots.inventorySlots.get(indexSlot);

			if (isMouseOverSlot(slot, x, y))
			{
				return slot;
			}
		}

		return null;
	}

	// Taken from GuiContainer
	private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY)
	{
		return isPointInRegion(slotIn.xPos, slotIn.yPos, 16, 16, mouseX, mouseY);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton)
	{
		// Make it spin! :D
		if (mouseButton == 0)
		{
			if (mouseDown)
			{
				mouseDown = false;
				rotateVelocity = (mouseX - lastDownX[4]);
				if (Math.abs(rotateVelocity) < 5)
				{
					rotateVelocity = 0;
				}
			}
		}
		super.mouseReleased(mouseX, mouseY, mouseButton);
	}
	
	private float ticksExisted()
	{
		return mc.player != null ? Minecraft.getMinecraft().player.ticksExisted : 0;
	}
	
	
	private static PageConfiguration interpolate(float amountDone, PageConfiguration start, PageConfiguration end)
	{
		return new PageConfiguration(
				ease(Math.min(1.0F, amountDone), start.rotation, end.rotation),
				ease(Math.min(1.0F, amountDone), start.x, end.x),
				ease(Math.min(1.0F, amountDone), start.y, end.y),
				ease(Math.min(1.0F, amountDone), start.scale, end.scale),
				ease(Math.min(1.0F, amountDone), start.boxWidth, end.boxWidth),
				ease(Math.min(1.0F, amountDone), start.boxHeight, end.boxHeight),
				ease(Math.min(1.0F, amountDone), start.boxX, end.boxX),
				ease(Math.min(1.0F, amountDone), start.boxY, end.boxY)
				);
	}
	
	// http://stackoverflow.com/a/8317722/1754640
	private static float ease(float percent, float startValue, float endValue)
	{
		endValue -= startValue;
		float total = 100;
		float elapsed = percent * total;
		
		if ((elapsed /= total / 2) < 1)
			return endValue / 2 * elapsed * elapsed + startValue;
		return -endValue / 2 * ((--elapsed) * (elapsed - 2) - 1) + startValue;
	}
	
	public void renderEntity(Entity entity, float x, float y, float scale, float rotation, float partialTicks)
	{
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 50.0F);
		GlStateManager.scale(-scale, scale, scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;
		Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableTexture2D();
	}
	
	public void renderModel(ModelBase model, float x, float y, float scale, float rotation)
	{
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 120F);
		GlStateManager.scale(-scale, scale, scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		float f1 = 0.7F;
		GlStateManager.glLightModel(2899, RenderHelper.setColorBuffer(f1, f1, f1, 1.0F));
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableTexture2D();
	}
	
	private void scissor(int x, int y, int xSize, int ySize)
	{
		ScaledResolution res = new ScaledResolution(mc);
		x = x * res.getScaleFactor();
		ySize = ySize * res.getScaleFactor();
		y = mc.displayHeight - (y * res.getScaleFactor()) - ySize;
		xSize = xSize * res.getScaleFactor();
		GL11.glScissor(x, y, xSize, ySize);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xLeft = (width - xSize) / 2;
		int yTop = (height - ySize) / 2;
		
		RenderHelper.enableGUIStandardItemLighting();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 900F);
		if ( page == 0
		  && transitionStart == 0 )
		{
			String s = "_" + Minecraft.getMinecraft().player.getName().toUpperCase();
			fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 115, 0x1DA9C1);
		}
		
		if (page == 9)
		{
			String s = I18n.format("cyberware.gui.installed");
			fontRenderer.drawString(s, 8, 9, 0x1DA9C1);
		}
		
		if (page != index.id)
		{
			String s = surgery.essence + " / " + surgery.maxEssence;
			fontRenderer.drawString(s, 18, 6, 0x1DA9C1);
		}
		
		GlStateManager.popMatrix();
		
		GlStateManager.enableBlend();

		zLevel = 500;
		itemRender.zLevel = 500;
		
		mc.getTextureManager().bindTexture(SURGERY_GUI_TEXTURES);
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		// Draw red 'slot' items and ghost items
		for (SlotSurgery pos : visibleSlots)
		{
			GlStateManager.pushMatrix();

			ItemStack stack = pos.getPlayerStack();
			// render red/current slot content
			itemRender.renderItemAndEffectIntoGUI(mc.player, stack, pos.xPos, pos.yPos - 26);
			
			if ( !stack.isEmpty()
			  && stack.getCount() > 1 )
			{
				FontRenderer fontRenderItem = stack.getItem().getFontRenderer(stack);
				if (fontRenderItem == null) fontRenderItem = fontRenderer;
				
				itemRender.renderItemOverlayIntoGUI(fontRenderItem, stack, pos.xPos, pos.yPos - 26, Integer.toString(stack.getCount()));
			}
			
			// render blue/target slot content
			if ( pos.getStack().isEmpty()
			  && !pos.slotDiscarded() )
			{
				itemRender.zLevel = 50;
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.colorMask(true, true, true, false);
				
				itemRender.renderItemAndEffectIntoGUI(mc.player, pos.getPlayerStack(), pos.xPos, pos.yPos);
				
				if ( !stack.isEmpty()
				  && stack.getCount() > 1 )
				{
					FontRenderer fontRenderItem = stack.getItem().getFontRenderer(stack);
					if (fontRenderItem == null) fontRenderItem = fontRenderer;
					
					itemRender.renderItemOverlayIntoGUI(fontRenderItem, stack, pos.xPos, pos.yPos, Integer.toString(stack.getCount()));
				}
				
				GlStateManager.colorMask(true, true, true, true);
				itemRender.zLevel = 500;
			}
			else if (CyberwareAPI.areCyberwareStacksEqual(stack, pos.getStack()))
			{
				FontRenderer fontRenderItem = stack.getItem().getFontRenderer(stack);
				if (fontRenderItem == null) fontRenderItem = fontRenderer;
				
				String str = pos.getStack().getCount() == 1 ? "+1" : "+";
				int width = pos.getStack().getCount() == 1 ? 0 : fontRenderItem.getStringWidth(Integer.toString(pos.getStack().getCount()));
				
				itemRender.renderItemOverlayIntoGUI(fontRenderItem, stack, pos.xPos - width, pos.yPos, str);
			}
			
			GlStateManager.popMatrix();
		}
		
		// draw index page
		if (page == index.id)
		{
			for (int zee = 0; zee < indexCount; zee++)
			{
				ItemStack draw = indexStacks.get(zee);
				
				int x = (zee % 8) * 20 + 9;
				int y = (zee / 8) * 20 + 24;
				itemRender.zLevel = 0;
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.colorMask(true, true, true, false);
				
				itemRender.renderItemAndEffectIntoGUI(mc.player, draw, x, y);
				
				FontRenderer fontRendererItem = draw.getItem().getFontRenderer(draw);
				if (fontRendererItem == null) fontRendererItem = fontRenderer;
				
				if ( !draw.isEmpty()
				  && draw.getCount() > 1 )
				{
					itemRender.renderItemOverlayIntoGUI(fontRendererItem, draw, x, y, Integer.toString(draw.getCount()));
				}
				
				int nu = indexNews[zee];
				if (nu == 1)
				{
					itemRender.renderItemOverlayIntoGUI(fontRendererItem, draw, x - 10, y - 10, "+");
				}
				else if (nu == 2)
				{
					itemRender.renderItemOverlayIntoGUI(fontRendererItem, draw, x - 10, y - 10, "-");
				}
				
				GlStateManager.colorMask(true, true, true, true);
				itemRender.zLevel = 500;
			}
		}
		
		List<String> missingSlots = new ArrayList<>();

		if (page != index.id)
		{
			if (surgery.essence < 0)
			{
				missingSlots.add(I18n.format("cyberware.gui.no_essence"));
			}
			else if (surgery.essence < CyberwareConfig.CRITICAL_ESSENCE)
			{
				missingSlots.add(I18n.format("cyberware.gui.critical_essence"));
			}
			
			if (surgery.missingPower)
			{
				missingSlots.add(I18n.format("cyberware.gui.no_power"));
			}
			
			for (int index = 0; index < surgery.isEssentialMissing.length; index++)
			{
				EnumSlot slot = EnumSlot.values()[index / 2];
				
				if (slot.isSided())
				{
					if (index % 2 == 0)
					{
						if (surgery.isEssentialMissing[index])
						{
							missingSlots.add(I18n.format("cyberware.gui.missing_essential." + slot.getName() + ".left"));
						}
					}
					else
					{
						if (surgery.isEssentialMissing[index])
						{
							missingSlots.add(I18n.format("cyberware.gui.missing_essential." + slot.getName() + ".right"));
						}
					}
				}
				else if (index % 2 == 0)
				{
					if (surgery.isEssentialMissing[index])
					{
						missingSlots.add(I18n.format("cyberware.gui.missing_essential." + slot.getName()));
					}
				}
			}
			
			GlStateManager.disableBlend();

			boolean ghost = false;
			boolean add = false;
			
			// See if a red 'slot' is hovered
			Slot slot = getSlotAtPosition(mouseX, mouseY + 26);
			if (!(slot instanceof SlotSurgery))
			{
				// Otherwise, see if a blue slot is hovered and a ghost item carries over
				ghost = true;
				slot = getSlotAtPosition(mouseX, mouseY);
				if ( slot != null
				  && !slot.getStack().isEmpty() )
				{
					slot = null;
				}
				
				if ( slot instanceof SlotSurgery
				  && ((SlotSurgery) slot).slotDiscarded() )
				{
					if (!((SlotSurgery) slot).getPlayerStack().isEmpty())
					{
						if (mc.player.inventory.getItemStack().isEmpty())
						{
							add = true;
						}
						else
						{
							slot = null;
						}
					}
				}
			}
			
			// Draw the tooltip if there is a red slot item or ghost item that needs one drawn
			if (slot instanceof SlotSurgery)
			{
				ItemStack stack = ((SlotSurgery) slot).getPlayerStack();
				if (add)
				{
					drawHoveringText(Collections.singletonList(I18n.format("cyberware.gui.add", I18n.format(stack.getTranslationKey() + ".name"))),
					                 mouseX - xLeft, mouseY - yTop, fontRenderer );
				}
				else
				{
					if (!stack.isEmpty())
					{
						renderToolTip(stack, mouseX - xLeft, mouseY - yTop, ghost ? 1 : 0);
					}
				}
			}
			
			if (missingSlots.size() > 0)
			{			
				if (isPointInRegion(xSize - 23, 20, 16, 16, mouseX, mouseY))
				{
					drawHoveringText(missingSlots, mouseX - xLeft, mouseY - yTop, fontRenderer);
				}
			}
		}
		else
		{
			// render tooltip & capture click event in index page
			for (int n = 0; n < indexCount; n++)
			{
				int x = (n % 8) * 20 + 9;
				int y = (n / 8) * 20 + 24;
				
				if (isPointInRegion(x - 1, y - 1, 18, 18, mouseX, mouseY))
				{
					renderToolTip(indexStacks.get(n), mouseX - xLeft, mouseY - yTop, 2 + indexNews[n]);
					if (mouseDown)
					{
						parent = index.id;
						int time = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) ? 0 : 30;
						prepTransition(time, indexPages[n]);
					}
				}
			}
		}
		
		if ( page != 0
		  && isPointInRegion(xSize - 25, 5, back.width, back.height, mouseX, mouseY) )
		{
			drawHoveringText(Collections.singletonList(I18n.format("cyberware.gui.back")),
			                 mouseX - xLeft, mouseY - yTop, fontRenderer );
		}
		else if ( page == 0
		       && isPointInRegion(xSize - 22, 5, index.width, index.height, mouseX, mouseY) )
		{
			drawHoveringText(Collections.singletonList(I18n.format("cyberware.gui.index")),
			                 mouseX - xLeft, mouseY - yTop, fontRenderer );
		}
		
		GlStateManager.disableBlend();

		zLevel = 0;
		itemRender.zLevel = 0;
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type)
	{
		if ( slotIn instanceof SlotSurgery
		  && !isSlotAccessible((SlotSurgery) slotIn) )
		{
			return;
		}
		
		if (slotIn instanceof SlotSurgery)
		{
			SlotSurgery surgerySlot = (SlotSurgery) slotIn;
			if ( surgerySlot.getStack().isEmpty()
			  && !surgerySlot.getPlayerStack().isEmpty()
			  && mc.player.inventory.getItemStack().isEmpty() )
			{
				int number = surgerySlot.slotNumber;
				
				ItemStack playerSlotItem = surgerySlot.getPlayerStack();
				if (surgerySlot.slotDiscarded())
				{
					if (!surgery.doesItemConflict(playerSlotItem, ((SlotSurgery) slotIn).slot, number % LibConstants.WARE_PER_SLOT))
					{
						surgerySlot.setDiscarded(false);
						surgery.enableDependsOn(playerSlotItem, ((SlotSurgery) slotIn).slot, number % LibConstants.WARE_PER_SLOT);
						CyberwarePacketHandler.INSTANCE.sendToServer(new SurgeryRemovePacket(surgery.getPos(), surgery.getWorld().provider.getDimension(), number, false));
					}
				}
				else
				{
					if (surgery.canDisableItem(playerSlotItem, ((SlotSurgery) slotIn).slot, number % LibConstants.WARE_PER_SLOT))
					{
						surgerySlot.setDiscarded(true);
						surgery.disableDependants(playerSlotItem, ((SlotSurgery) slotIn).slot, number % LibConstants.WARE_PER_SLOT);
						CyberwarePacketHandler.INSTANCE.sendToServer(new SurgeryRemovePacket(surgery.getPos(), surgery.getWorld().provider.getDimension(), number, true));
					}
				}

			}
		}
		
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
	}

	public boolean isSlotAccessible(@Nonnull SlotSurgery slot)
	{
		return page == slot.slot.getSlotNumber();
	}
	
	protected void updateSurgerySlotsVisibility(boolean show)
	{
		visibleSlots.clear();
		Iterator<Slot> iteratorSlots = inventorySlots.inventorySlots.iterator();
		
		Slot slot = iteratorSlots.next();
		while (slot instanceof SlotSurgery)
		{
			SlotSurgery slotSurgery = (SlotSurgery) slot;
			
			if ( show
			  && isSlotAccessible(slotSurgery) )
			{
				slotSurgery.xPos = slotSurgery.savedXPosition;
				slotSurgery.yPos = slotSurgery.savedYPosition;
				visibleSlots.add(slotSurgery);
			}
			else
			{
				slotSurgery.xPos = Integer.MIN_VALUE;
				slotSurgery.yPos = Integer.MIN_VALUE;
			}
			
			slot = iteratorSlots.next();
		}
	}

	protected void renderToolTip(@Nonnull ItemStack stack, int x, int y, int extras)
	{
		List<String> listTooltips = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

		for (int indexTooltip = 0; indexTooltip < listTooltips.size(); indexTooltip++)
		{
			if (indexTooltip == 0)
			{
				listTooltips.set(indexTooltip, stack.getItem().getForgeRarity(stack).getColor() + listTooltips.get(indexTooltip));
			}
			else
			{
				listTooltips.set(indexTooltip, TextFormatting.GRAY + listTooltips.get(indexTooltip));
			}
		}
		
		if (extras == 1)
		{
			listTooltips.add(1, I18n.format("cyberware.gui.remove"));
		}
		else if (extras >= 2)
		{
			listTooltips.add(1, I18n.format("cyberware.gui.click"));
			
			if (extras == 3)
			{
				listTooltips.set(0, listTooltips.get(0) + " " + I18n.format("cyberware.gui.added"));
			}
			else if (extras == 4)
			{
				listTooltips.set(0, listTooltips.get(0) + " " + I18n.format("cyberware.gui.removed"));
			}
		}

		FontRenderer frontRendererItem = stack.getItem().getFontRenderer(stack);
		drawHoveringText(listTooltips, x, y, (frontRendererItem == null ? fontRenderer : frontRendererItem));
	}
	
}
