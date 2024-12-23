package flaxbeard.cyberware.client.render;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.entity.EntityCyberZombie;

public class RenderCyberZombie extends RenderZombie
{
	
	private static final ResourceLocation ZOMBIE = new ResourceLocation(Cyberware.MODID + ":textures/entity/cyberzombie.png");
	private static final ResourceLocation HIGHLIGHT = new ResourceLocation(Cyberware.MODID + ":textures/entity/cyberzombie_highlight.png");
	private static final ResourceLocation ZOMBIE_BRUTE = new ResourceLocation(Cyberware.MODID + ":textures/entity/cyberzombie_brute.png");
	private static final ResourceLocation HIGHLIGHT_BRUTE = new ResourceLocation(Cyberware.MODID + ":textures/entity/cyberzombie_brute_highlight.png");
	
	@SideOnly(Side.CLIENT)
	public static class LayerZombieHighlight<T extends EntityCyberZombie> implements LayerRenderer<T>
	{
		private final RenderCyberZombie czRenderer;
		
		public LayerZombieHighlight(RenderCyberZombie spiderRendererIn)
		{
			this.czRenderer = spiderRendererIn;
		}
		
		public void doRenderLayer(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
		{
			if (entitylivingbaseIn.isBrute())
			{
				this.czRenderer.bindTexture(HIGHLIGHT_BRUTE);
			}
			else
			{
				this.czRenderer.bindTexture(HIGHLIGHT);
			}
			GlStateManager.enableBlend();
			//GlStateManager.disableAlpha();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			
			if (entitylivingbaseIn.isInvisible())
			{
				GlStateManager.depthMask(false);
			}
			else
			{
				GlStateManager.depthMask(true);
			}
			
			int i = 61680;
			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.czRenderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			//i = entitylivingbaseIn.getBrightnessForRender(partialTicks);
			i = entitylivingbaseIn.getBrightnessForRender();
			j = i % 65536;
			k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
			//this.czRenderer.setLightmap(entitylivingbaseIn, partialTicks);
			this.czRenderer.setLightmap(entitylivingbaseIn);
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		}

		public boolean shouldCombineTextures()
		{
			return false;
		}
	}
	
	public RenderCyberZombie(RenderManager renderManagerIn)
	{
		super(renderManagerIn);
        layerRenderers.add(new LayerZombieHighlight(this));
	}
	
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull final EntityZombie entity)
	{
		EntityCyberZombie cz = (EntityCyberZombie) entity;
		if (cz.isBrute())
		{
			return ZOMBIE_BRUTE;
		}
		return ZOMBIE;
	}
	
	@Override
	protected void preRenderCallback(@Nonnull final EntityZombie zombie, float partialTickTime)
    {
		EntityCyberZombie cz = (EntityCyberZombie) zombie;
        if (cz.height == (1.95F * 1.2F))
        {
            GlStateManager.scale(1.2F, 1.2F, 1.2F);
        }
    }
	
}