package flaxbeard.cyberware.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBeaconLarge extends ModelBase
{
	public ModelRenderer bar1;
	public ModelRenderer bar2;
	public ModelRenderer bar3;
	public ModelRenderer bar4;
	public ModelRenderer base;
	
	public ModelRenderer[] crossbars;
	
	public ModelBeaconLarge()
	{
		textureWidth = 128;
		textureHeight = 256;
		
		float angle = 173.8F;
		
		bar1 = new ModelRenderer(this, 0, 0);
		bar1.addBox(-1.5F, 8F, -2F, 3, 163, 3);
		bar1.rotateAngleY = (float) Math.toRadians(45F);
		bar1.rotateAngleX = (float) Math.toRadians(angle);
		
		bar2 = new ModelRenderer(this, 0, 0);
		bar2.addBox(-1.5F, 8F, -2F, 3, 163, 3);
		bar2.rotateAngleY = (float) Math.toRadians(135F);
		bar2.rotateAngleX = (float) Math.toRadians(angle);
		
		bar3 = new ModelRenderer(this, 0, 0);
		bar3.addBox(-1.5F, 8F, -2F, 3, 163, 3);
		bar3.rotateAngleY = (float) Math.toRadians(-45F);
		bar3.rotateAngleX = (float) Math.toRadians(angle);
		
		bar4 = new ModelRenderer(this, 0, 0);
		bar4.addBox(-1.5F, 8F, -2F, 3, 163, 3);
		bar4.rotateAngleY = (float) Math.toRadians(-135F);
		bar4.rotateAngleX = (float) Math.toRadians(angle);
		
		float hPercent = (float) -Math.cos(Math.toRadians(angle));
		float wPercent = (float) Math.sin(Math.toRadians(angle));

		int num = 6;
		
		float progressChg = 25F;
		crossbars = new ModelRenderer[num * 4];
		float x;
		float y;
		float z;
		float progress = 10F + progressChg;
		float pi4 = (float) Math.PI / 4F;
		for (int i = 0; i < num; i++)
		{
			x = (float) Math.ceil(.3F + -wPercent * progress * pi4);
			z = -1.6F + -wPercent * progress * (pi4 - .1F);

			y = -hPercent * progress;
			
			ModelRenderer bar = new ModelRenderer(this, 12, 0);
			bar.addBox(x, y, z, (int) (-x * 2F), 2, 2);
			crossbars[i * 4] = bar;
			
			ModelRenderer bar2 = new ModelRenderer(this, 12, 0);
			bar2.addBox(x, y, -z - 2F, (int) (-x * 2F), 2, 2);
			crossbars[i * 4 + 1] = bar2;
			
			
			ModelRenderer bar3 = new ModelRenderer(this, 12, 0);
			bar3.addBox(z, y, x, 2, 2, (int) (-x * 2F));
			crossbars[i * 4 + 2] = bar3;
			
			
			ModelRenderer bar4 = new ModelRenderer(this, 12, 0);
			bar4.addBox(-z - 2F, y, x, 2, 2, (int) (-x * 2F));
			crossbars[i * 4 + 3] = bar4;
			
			progress += progressChg;
			
		}
		
		textureWidth = 256;
		textureHeight = 64;
		
		base = new ModelRenderer(this, 0, 0);
		base.addBox(-24F, -168F, -24F, 48, 4, 48);
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		bar1.render(scale);
		bar2.render(scale);
		bar3.render(scale);
		bar4.render(scale);
		base.render(scale);
		
		for (ModelRenderer bar : crossbars)
		{
			if (bar != null)
			{
				bar.render(scale);
			}
		}
	}
	
	public void renderBase(Entity entity, float f, float f1, float f2, float f3, float f4, float scale)
	{
		base.render(scale);
	}
	
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
