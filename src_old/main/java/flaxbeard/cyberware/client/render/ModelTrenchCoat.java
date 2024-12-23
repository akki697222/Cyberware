package flaxbeard.cyberware.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelTrenchCoat extends ModelBiped
{
	public ModelRenderer bottomThing;
	private ModelBiped modelBaseParent;
	
	public ModelTrenchCoat(float modelSize)
	{
		super(modelSize);
		bottomThing = new ModelRenderer(this, 16, 0);
		bottomThing.addBox(-4.0F, 0F, -1.7F, 8, 12, 4, modelSize);
		bottomThing.setRotationPoint(0, 12.0F, 0.0F);
	}
	
	public void setDefaultModel(ModelBiped modelBiped)
	{
		modelBaseParent = modelBiped;
	}
	
	@Override
	public void setModelAttributes(ModelBase modelBase)
	{
		super.setModelAttributes(modelBase);
		modelBaseParent.setModelAttributes(modelBase);
	}
	
	@Override
	public void setLivingAnimations(EntityLivingBase entityLivingBase, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.setLivingAnimations(entityLivingBase, limbSwing, limbSwingAmount, partialTickTime);
		modelBaseParent.setLivingAnimations(entityLivingBase, limbSwing, limbSwingAmount, partialTickTime);
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		modelBaseParent.setVisible(visible);
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, @Nullable Entity entity)
	{
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
		modelBaseParent.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
		
		bottomThing.setRotationPoint(0, bipedLeftLeg.rotationPointY, bipedLeftLeg.rotationPointZ);
		bottomThing.rotateAngleX = Math.max(bipedLeftLeg.rotateAngleX, bipedRightLeg.rotateAngleX) + 0.055F;
	}
	
	@Override
	public void render(@Nonnull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		// don't call super as we use a single model for all entities
		// super.render(entity, limbSwing, limbSwingAmount, ageInTicks,  netHeadYaw, headPitch, scale);
		setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		
		// transfer properties, wrap to original
		modelBaseParent.swingProgress = swingProgress;
		modelBaseParent.bipedHead.showModel     = bipedHead.showModel    ;
		modelBaseParent.bipedHeadwear.showModel = bipedHeadwear.showModel;
		modelBaseParent.bipedBody.showModel     = bipedBody.showModel    ;
		modelBaseParent.bipedRightArm.showModel = bipedRightArm.showModel;
		modelBaseParent.bipedLeftArm.showModel  = bipedLeftArm.showModel ;
		modelBaseParent.bipedRightLeg.showModel = bipedRightLeg.showModel;
		modelBaseParent.bipedLeftLeg.showModel  = bipedLeftLeg.showModel ;
		modelBaseParent.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		
		// add the bottom part of the trench coat
		GlStateManager.pushMatrix();
		
		if (isChild)
		{
			float factor = 0.5F;
			GlStateManager.scale(factor, factor, factor);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
		}
		else
		{
			if (entity.isSneaking())
			{
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			}
		}
		bottomThing.render(scale);
		
		GlStateManager.popMatrix();
	}
}
