package flaxbeard.cyberware.client.gui;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import flaxbeard.cyberware.api.item.IBlueprint;
import flaxbeard.cyberware.client.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.EngineeringDestroyPacket;
import flaxbeard.cyberware.common.network.EngineeringSwitchArchivePacket;

@SideOnly(Side.CLIENT)
public class GuiEngineeringTable extends GuiContainer
{
	
	private static class SmashButton extends GuiButton
	{
		public SmashButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3)
		{
			super(p_i46316_1_, p_i46316_2_, p_i46316_3, 21, 21, "");
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if (visible)
			{
				boolean down = Mouse.isButtonDown(0);
				boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
				
				mc.getTextureManager().bindTexture(ENGINEERING_GUI_TEXTURES);
				
				int i = 39;
				int j = 34;
				if (down && flag)
				{
					i = 0;
					j = 166;
				}
				drawTexturedModalRect(x, y, i, j, 21, 21);
			}
		}
	}
	
	private static class NextPageButton extends GuiButton
	{
		private final boolean isForward;
	
		public NextPageButton(int buttonId, int x, int y, boolean isForward)
		{
			super(buttonId, x, y, 23, 13, "");
			this.isForward = isForward;
		}

		@Override
		public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if (visible)
			{
				boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				mc.getTextureManager().bindTexture(ENGINEERING_GUI_TEXTURES);
				int i = 21;
				int j = 166;
	
				if (flag)
				{
					i += 23;
				}
	
				if (!isForward)
				{
					j += 13;
				}
	
				drawTexturedModalRect(x, y, i, j, 23, 13);
			}
		}
	}
	
	private static final ResourceLocation ENGINEERING_GUI_TEXTURES = new ResourceLocation(Cyberware.MODID + ":textures/gui/engineering.png");

	private InventoryPlayer playerInventory;

	private TileEntityEngineeringTable tileEntityEngineeringTable;

	private GuiButton next;
	private GuiButton prev;
	private GuiButton nextC;
	private GuiButton prevC;
	private final int offset;

	public GuiEngineeringTable(InventoryPlayer playerInventory, TileEntityEngineeringTable tileEntityEngineeringTable)
	{
		super(new ContainerEngineeringTable(Minecraft.getMinecraft().player.getCachedUniqueIdString(), playerInventory, tileEntityEngineeringTable));
		this.playerInventory = playerInventory;
		this.tileEntityEngineeringTable = tileEntityEngineeringTable;
		
		if (archive() != null)
		{
			xSize += 65;
		}
		if (componentBox() != null)
		{
			xSize += 65;
			offset = 65;
		}
		else
		{
			offset = 0;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		String s = tileEntityEngineeringTable.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, offset + 8, 6, 4210752);
		fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(), offset + 8, ySize - 96 + 2, 4210752);
		
		next.visible = prev.visible = (archive() != null && ((ContainerEngineeringTable) inventorySlots).archiveList.size() > 1);
		nextC.visible = prevC.visible = (componentBox() != null && ((ContainerEngineeringTable) inventorySlots).componentBoxList.size() > 1);

		inventorySlots.canInteractWith(mc.player);
		
		if (archive() != null)
		{
			String ogName = archive().getDisplayName().getUnformattedText();
			
			String name = ogName.substring(0, Math.min(9, ogName.length())).trim();
			if (ogName.length() > 9)
			{
				name += "...";
			}
			
			if (ogName.length() <= 11)
			{
				name = ogName.substring(0, Math.min(11, ogName.length())).trim();
			}
			
			fontRenderer.drawString(name, offset + 180, 10, 4210752);
		}
		
		Object cb = componentBox();
		if (cb != null)
		{
			String ogName;
			if (cb instanceof TileEntityComponentBox)
			{
				ogName = ((TileEntityComponentBox) cb).getDisplayName().getUnformattedText();
			}
			else
			{
				ogName = name();
			}
			
			String name = ogName.substring(0, Math.min(9, ogName.length())).trim();
			if (ogName.length() > 9)
			{
				name += "...";
			}
			
			if (ogName.length() <= 11)
			{
				name = ogName.substring(0, Math.min(11, ogName.length())).trim();
			}
			
			fontRenderer.drawString(name, 7, 10, 4210752);
		}
		
		if (isPointInRegion(offset + 39, 34, 21, 21, mouseX, mouseY))
		{
			String[] tooltip;
			if (!tileEntityEngineeringTable.slots.getStackInSlot(1).isEmpty())
			{
				float chance = CyberwareConfig.ENGINEERING_CHANCE;
				if (!tileEntityEngineeringTable.slots.getStackInSlot(0).isEmpty()
				    && tileEntityEngineeringTable.slots.getStackInSlot(0).isItemStackDamageable() )
				{
					chance = Math.min(100F, CyberwareConfig.ENGINEERING_CHANCE * 5F * (1F - (tileEntityEngineeringTable.slots.getStackInSlot(0).getItemDamage() * 1F / tileEntityEngineeringTable.slots.getStackInSlot(0).getMaxDamage())));
				}
				tooltip = new String[] { I18n.format("cyberware.gui.destroy"),
				                         I18n.format("cyberware.gui.destroy_chance", Math.round(chance * 100F) / 100F + "%") };
			}
			else
			{
				tooltip = new String[] { I18n.format("cyberware.gui.destroy") };
			}
			drawHoveringText(Arrays.asList(tooltip), mouseX - i, mouseY - j, fontRenderer);
		}
		
		if (isPointInRegion(offset + 15, 20, 16, 16, mouseX, mouseY) && tileEntityEngineeringTable.slots.getStackInSlot(0).isEmpty())
		{
			drawHoveringText(Arrays.asList(I18n.format("cyberware.gui.to_destroy")), mouseX - i, mouseY - j, fontRenderer);
		}
		if (isPointInRegion(offset + 15, 53, 16, 16, mouseX, mouseY) && tileEntityEngineeringTable.slots.getStackInSlot(1).isEmpty())
		{
			drawHoveringText(Arrays.asList(I18n.format("cyberware.gui.paper")), mouseX - i, mouseY - j, fontRenderer);
		}
		if (isPointInRegion(offset + 115, 53, 16, 16, mouseX, mouseY) && tileEntityEngineeringTable.slots.getStackInSlot(8).isEmpty())
		{
			drawHoveringText(Arrays.asList(I18n.format("cyberware.gui.blueprint")), mouseX - i, mouseY - j, fontRenderer);
		}

		GlStateManager.pushMatrix();
		ShaderUtil.alpha(0.35F);
		ItemStack blueprintStack = tileEntityEngineeringTable.slots.getStackInSlot(8);
		if ( !blueprintStack.isEmpty()
		  && blueprintStack.getItem() instanceof IBlueprint )
		{
			IBlueprint blueprint = (IBlueprint) blueprintStack.getItem();
			NonNullList<ItemStack> nnlReq = blueprint.getRequirementsForDisplay(blueprintStack);
			ItemStack[] requiredItems = nnlReq.toArray(new ItemStack[nnlReq.size()]);

			for (int index = 0; index < requiredItems.length; index++)
			{
				requiredItems[index] = requiredItems[index].copy();
			}
			if (requiredItems.length!=0)
			{
				for (ItemStack requiredItem : requiredItems)
				{
					if (!requiredItem.isEmpty())
					{
						for (int indexSlot = 2; indexSlot < 8; indexSlot++) {
							ItemStack crafting = tileEntityEngineeringTable.slots.getStackInSlot(indexSlot);
							if (!crafting.isEmpty()
							  && crafting.getItem() == requiredItem.getItem()
							  && crafting.getItemDamage() == requiredItem.getItemDamage()
							  && ( !requiredItem.hasTagCompound()
							    || ItemStack.areItemStackTagsEqual(requiredItem, crafting) ) ) {
								requiredItem.setCount(Math.max(0, requiredItem.getCount() - crafting.getCount()));
							}
						}
					}
				}

				List<ItemStack> toRender = new ArrayList<>();
				for (ItemStack required : requiredItems) {
					if (required.getCount() > 0) {
						toRender.add(required);
					}
				}

				int index = 0;
				for (int indexSlot = 2; indexSlot < 8 && index < toRender.size(); indexSlot++)
				{
					if (tileEntityEngineeringTable.slots.getStackInSlot(indexSlot) == ItemStack.EMPTY)
					{
						itemRender.renderItemAndEffectIntoGUI(mc.player, toRender.get(index), offset + 71 + 18 * (indexSlot % 2), -1 + 18 * (indexSlot / 2));

						FontRenderer font = toRender.get(index).getItem().getFontRenderer(toRender.get(index));
						if (font == null) font = fontRenderer;

						itemRender.renderItemOverlayIntoGUI(font, toRender.get(index), offset + 71 + 18 * (indexSlot % 2), -1 + 18 * (indexSlot / 2),
						                                    "+" + toRender.get(index).getCount());

						index++;
					}
				}
			}
		}
		ShaderUtil.releaseShader();
		GlStateManager.popMatrix();

		if (archive() != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 100F);

			TileEntityBlueprintArchive archive = archive();
			for (int h = 0; h < archive.slots.getSlots(); h++)
			{
				ItemStack item = archive.slots.getStackInSlot(h);

				if (item != ItemStack.EMPTY && item.getItem() instanceof IBlueprint)
				{
					IBlueprint blueprint = (IBlueprint) item.getItem();
					ItemStack prod = blueprint.getIconForDisplay(item);
					itemRender.renderItemAndEffectIntoGUI(mc.player, prod, offset + 181 + 18 * (h % 3), 22 + 18 * (h / 3));
				}
			}
			
			GlStateManager.popMatrix();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(ENGINEERING_GUI_TEXTURES);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(offset + i, j, 0, 0, archive() == null ? 176 : 241, ySize);
		
		if (componentBox() != null)
		{
			mc.getTextureManager().bindTexture(GuiComponentBox.BOX_GUI_TEXTURE);
			drawTexturedModalRect(i, j, 176, 0, 65, ySize);
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		buttonList.add(new SmashButton(0, offset + i + 39, j + 34));

		buttonList.add(next = new NextPageButton(1, offset + i + 180, j + 131, false));
		buttonList.add(prev = new NextPageButton(2, offset + i + 216, j + 131, true));
		buttonList.add(nextC = new NextPageButton(3, i + 7, j + 131, false));
		buttonList.add(prevC = new NextPageButton(4, i + 43, j + 131, true));

		next.visible = prev.visible = (archive() != null && ((ContainerEngineeringTable) inventorySlots).archiveList.size() > 1);
		nextC.visible = prevC.visible = (componentBox() != null && ((ContainerEngineeringTable) inventorySlots).componentBoxList.size() > 1);

	}
	
	protected void actionPerformed(GuiButton button)
	{
		if (button.id == 0)
		{
			CyberwarePacketHandler.INSTANCE.sendToServer(new EngineeringDestroyPacket(tileEntityEngineeringTable.getPos(), tileEntityEngineeringTable.getWorld().provider.getDimension()));
		}
		
		if (button.id == 2)
		{
			nextArchive();
		}
		
		if (button.id == 1)
		{
			prevArchive();
		}
		
		if (button.id == 4)
		{
			nextComponentBox();
		}
		
		if (button.id == 3)
		{
			prevComponentBox();
		}
	}
	
	private TileEntityBlueprintArchive archive()
	{
		return ((ContainerEngineeringTable) inventorySlots).archive;
	}
	
	
	private Object componentBox()
	{
		return ((ContainerEngineeringTable) inventorySlots).componentBox;
	}
	
	private String name()
	{
		ContainerEngineeringTable table = ((ContainerEngineeringTable) inventorySlots);
		if (table.componentBox instanceof Integer)
		{
			ItemStack stack = table.playerInv.mainInventory.get((Integer) table.componentBox);
			if (!stack.isEmpty())
			{
				return stack.getDisplayName();
			}
		}

		return "";
	}
	
	private void nextComponentBox()
	{
		CyberwarePacketHandler.INSTANCE.sendToServer(new EngineeringSwitchArchivePacket(tileEntityEngineeringTable.getPos(), mc.player, true, true));

		((ContainerEngineeringTable) inventorySlots).nextComponentBox();
		//tileEntityEngineeringTable.lastPlayerArchive.put(mc.thePlayer.getCachedUniqueIdString(), archive().getPos());
	}
	
	private void prevComponentBox()
	{
		CyberwarePacketHandler.INSTANCE.sendToServer(new EngineeringSwitchArchivePacket(tileEntityEngineeringTable.getPos(), mc.player, false, true));

		((ContainerEngineeringTable) inventorySlots).prevComponentBox();
		//tileEntityEngineeringTable.lastPlayerArchive.put(mc.thePlayer.getCachedUniqueIdString(), archive().getPos());
	}
	
	private void nextArchive()
	{
		CyberwarePacketHandler.INSTANCE.sendToServer(new EngineeringSwitchArchivePacket(tileEntityEngineeringTable.getPos(), mc.player, true, false));

		((ContainerEngineeringTable) inventorySlots).nextArchive();
		tileEntityEngineeringTable.lastPlayerArchive.put(mc.player.getCachedUniqueIdString(), archive().getPos());
	}
	
	private void prevArchive()
	{
		CyberwarePacketHandler.INSTANCE.sendToServer(new EngineeringSwitchArchivePacket(tileEntityEngineeringTable.getPos(), mc.player, false, false));

		((ContainerEngineeringTable) inventorySlots).prevArchive();
		tileEntityEngineeringTable.lastPlayerArchive.put(mc.player.getCachedUniqueIdString(), archive().getPos());
	}
}
