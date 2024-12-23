package flaxbeard.cyberware.client;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.client.render.ModelTrenchCoat;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.TriggerActiveAbilityPacket;

public class ClientUtils
{
	
	@SideOnly(Side.CLIENT)
	public static final ModelTrenchCoat modelTrenchCoat = new ModelTrenchCoat(0.51F);
	
	private static final float TEXTURE_SCALE = 1.0F / 256; 
	public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x        , y + height, 0.0F).tex((textureX        ) * TEXTURE_SCALE, (textureY + height) * TEXTURE_SCALE).endVertex();
		bufferBuilder.pos(x + width, y + height, 0.0F).tex((textureX + width) * TEXTURE_SCALE, (textureY + height) * TEXTURE_SCALE).endVertex();
		bufferBuilder.pos(x + width, y         , 0.0F).tex((textureX + width) * TEXTURE_SCALE, (textureY         ) * TEXTURE_SCALE).endVertex();
		bufferBuilder.pos(x        , y         , 0.0F).tex((textureX        ) * TEXTURE_SCALE, (textureY         ) * TEXTURE_SCALE).endVertex();
		tessellator.draw();
	}
	
	private static HashMap<String, ResourceLocation> textures = new HashMap<>();

	public static void bindTexture(String string)
	{
		if (!textures.containsKey(string))
		{
			textures.put(string, new ResourceLocation(string));
			Cyberware.logger.info("Registering new ResourceLocation: " + string);
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(textures.get(string));
	}
	
	public static void drawHoveringText(GuiScreen gui, List<String> textLines, int x, int y, FontRenderer font)
	{
		net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(textLines, x, y, gui.width, gui.height, -1, font);
	}
	
	
	public static void useActiveItemClient(Entity entity, ItemStack stack)
	{
		CyberwarePacketHandler.INSTANCE.sendToServer(new TriggerActiveAbilityPacket(stack));
		CyberwareAPI.useActiveItem(entity, stack);
	}
}
