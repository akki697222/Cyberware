package flaxbeard.cyberware.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelScanner extends ModelBase
{
	public ModelRenderer bar;
	public ModelRenderer bar2;

	public ModelRenderer scanner;
	public ModelRenderer beam;

	public ModelScanner()
	{
		this.textureWidth = 34;
		this.textureHeight = 10;
		
		this.bar = new ModelRenderer(this, 0, 0);
		this.bar.addBox(-8F, 7F, -7F, 16, 1, 1);
		this.bar2 = new ModelRenderer(this, 0, 0);
		this.bar2.addBox(-8F, 5F, -7F, 16, 1, 1);
		this.bar.addChild(bar2);
		
		this.scanner = new ModelRenderer(this, 0, 2);
		this.scanner.addBox(-7F, 2F, -8F, 3, 5, 3);
		
		this.beam = new ModelRenderer(this, 12, 2);
		this.beam.addBox(-6F, -2F, -7F, 1, 4, 1);
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		this.bar.render(scale);
	}
	
	public void renderScanner(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		this.scanner.render(scale);
	}
	
	public void renderBeam(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		this.beam.render(scale);
	}
	
	
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
