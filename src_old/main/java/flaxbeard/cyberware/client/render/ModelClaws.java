package flaxbeard.cyberware.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelClaws extends ModelBase
{
    public ModelRenderer claw1;
    public ModelRenderer claw2;
    public ModelRenderer claw3;
    
    public ModelClaws(float modelSize)
    {
		textureWidth = 64;
		textureHeight = 64;
		
        claw1 = new ModelRenderer(this, 0, 0);
        claw1.addBox(-2.5F, 10.0F, -1.8F, 1, 7, 1, modelSize);
        claw1.setRotationPoint(-5.0F, 2.0F, 0.0F);
        
        claw2 = new ModelRenderer(this, 0, 0);
        claw2.addBox(-2.5F, 10.0F, -0.3F, 1, 7, 1, modelSize);
        claw1.addChild(claw2);
        
        claw3 = new ModelRenderer(this, 0, 0);
        claw3.addBox(-2.5F, 10.0F, 1.2F, 1, 7, 1, modelSize);
        claw1.addChild(claw3);
    }
    
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.pushMatrix();
        
        if (entity.isSneaking())
        {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        
        claw1.render(scale);
        
        GlStateManager.popMatrix();
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity)
    {
      
    }
    
}