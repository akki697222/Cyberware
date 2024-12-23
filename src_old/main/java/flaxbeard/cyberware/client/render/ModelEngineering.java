package flaxbeard.cyberware.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelEngineering extends ModelBase
{
	public ModelRenderer head;
	public ModelRenderer bar;
	
	public ModelEngineering()
	{
		textureWidth = 24;
		textureHeight = 17;
		
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-3F, -2F, -3F, 6, 2, 6);
		bar = new ModelRenderer(this, 0, 8);
		bar.addBox(-1F, 0F, -1F, 2, 7, 2);
		head.addChild(bar);
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		head.render(scale);
	}
	
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
