package flaxbeard.cyberware.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBox extends ModelBase 
{
	public ModelRenderer box;

	public ModelBox()
	{
		textureWidth = 48;
		textureHeight = 21;
		
		box = new ModelRenderer(this, 0, 0);
		box.addBox(-6F, -4.5F, -6F, 12, 9, 12);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.box.render(f5);
	}
}
