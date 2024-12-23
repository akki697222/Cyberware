package flaxbeard.cyberware.common.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.client.render.RenderCyberlimbHand;
import flaxbeard.cyberware.client.render.RenderPlayerCyberware;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import flaxbeard.cyberware.common.item.ItemSkinUpgrade;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class EssentialsMissingHandlerClient
{
	
	public static final EssentialsMissingHandlerClient INSTANCE = new EssentialsMissingHandlerClient();
	
	@SideOnly(Side.CLIENT)
	private static final RenderPlayerCyberware renderSmallArms = new RenderPlayerCyberware(Minecraft.getMinecraft().getRenderManager(), true);
	
	@SideOnly(Side.CLIENT)
	public static final RenderPlayerCyberware renderLargeArms = new RenderPlayerCyberware(Minecraft.getMinecraft().getRenderManager(), false);
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void handleMissingSkin(RenderPlayerEvent.Pre event)
	{
		if (!CyberwareConfig.ENABLE_CUSTOM_PLAYER_MODEL) return;
		
		EntityPlayer entityPlayer = event.getEntityPlayer();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;
		
		boolean hasLeftLeg = cyberwareUserData.hasEssential(EnumSlot.LEG, EnumSide.LEFT);
		boolean hasRightLeg = cyberwareUserData.hasEssential(EnumSlot.LEG, EnumSide.RIGHT);
		boolean hasLeftArm = cyberwareUserData.hasEssential(EnumSlot.ARM, EnumSide.LEFT);
		boolean hasRightArm = cyberwareUserData.hasEssential(EnumSlot.ARM, EnumSide.RIGHT);
		
		boolean robotLeftArm  = cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM ));
		boolean robotRightArm = cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM));
		boolean robotLeftLeg  = cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG ));
		boolean robotRightLeg = cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG));
		
		RenderPlayer renderPlayer = event.getRenderer();
		
		if (!(renderPlayer instanceof RenderPlayerCyberware))
		{
			boolean useSmallArms = renderPlayer.smallArms;
			RenderPlayerCyberware renderToUse = useSmallArms ? renderSmallArms : renderLargeArms;
			
			boolean hasNoSkin = !cyberwareUserData.hasEssential(EnumSlot.SKIN);
			if (hasNoSkin)
			{
				event.setCanceled(true);
				renderToUse.doMuscles = true;
			}
			
			boolean hasNoLegs = !hasRightLeg && !hasLeftLeg;
			if (hasNoLegs)
			{
				// Hide pants + shoes
				if (!pants.containsKey(entityPlayer.getEntityId()))
				{
					pants.put(entityPlayer.getEntityId(), entityPlayer.inventory.armorInventory.set(EntityEquipmentSlot.LEGS.getIndex(), ItemStack.EMPTY));
				}
				if (!shoes.containsKey(entityPlayer.getEntityId()))
				{
					shoes.put(entityPlayer.getEntityId(), entityPlayer.inventory.armorInventory.set(EntityEquipmentSlot.FEET.getIndex(), ItemStack.EMPTY));
				}
			}
			
			if ( !hasRightLeg  || !hasLeftLeg  || !hasRightArm  || !hasLeftArm
			  || robotRightLeg || robotLeftLeg || robotRightArm || robotLeftArm )
			{
				event.setCanceled(true);
				
				boolean leftArmRusty  = robotLeftArm  && CyberwareContent.cyberlimbs.getQuality(cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM ))) == CyberwareAPI.QUALITY_SCAVENGED;
				boolean rightArmRusty = robotRightArm && CyberwareContent.cyberlimbs.getQuality(cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM))) == CyberwareAPI.QUALITY_SCAVENGED;
				boolean leftLegRusty  = robotLeftLeg  && CyberwareContent.cyberlimbs.getQuality(cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG ))) == CyberwareAPI.QUALITY_SCAVENGED;
				boolean rightLegRusty = robotRightLeg && CyberwareContent.cyberlimbs.getQuality(cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG))) == CyberwareAPI.QUALITY_SCAVENGED;
				
				// Human/body pass
				renderToUse.doRobo = false;
				renderToUse.doRusty = false;
				renderToUse.doRender((AbstractClientPlayer) entityPlayer, event.getX(), event.getY() - (hasNoLegs ? (11F / 16F) : 0), event.getZ(), entityPlayer.rotationYaw, event.getPartialRenderTick());
				
				if (!cyberwareUserData.isCyberwareInstalled(CyberwareContent.skinUpgrades.getCachedStack(ItemSkinUpgrade.META_SYNTHETIC_SKIN)))
				{
					ModelPlayer mainModel = renderToUse.getMainModel();
					mainModel.bipedBody.isHidden = true;
					mainModel.bipedHead.isHidden = true;
					
					// Manufactured 'ware pass
					mainModel.bipedLeftArm.isHidden = !(robotLeftArm && !leftArmRusty);
					mainModel.bipedRightArm.isHidden = !(robotRightArm && !rightArmRusty);
					mainModel.bipedLeftLeg.isHidden = !(robotLeftLeg && !leftLegRusty);
					mainModel.bipedRightLeg.isHidden = !(robotRightLeg && !rightLegRusty);
					
					if ( !mainModel.bipedLeftArm.isHidden
					  || !mainModel.bipedRightArm.isHidden
					  || !mainModel.bipedLeftLeg.isHidden
					  || !mainModel.bipedRightLeg.isHidden )
					{
						renderToUse.doRobo = true;
						renderToUse.doRusty = false;
						renderToUse.doRender((AbstractClientPlayer) entityPlayer, event.getX(), event.getY() - (hasNoLegs ? (11F / 16F) : 0), event.getZ(), entityPlayer.rotationYaw, event.getPartialRenderTick());
					}
					
					// Rusty 'ware pass
					mainModel.bipedLeftArm.isHidden = !leftArmRusty;
					mainModel.bipedRightArm.isHidden = !rightArmRusty;
					mainModel.bipedLeftLeg.isHidden = !leftLegRusty;
					mainModel.bipedRightLeg.isHidden = !rightLegRusty;
					
					if ( !mainModel.bipedLeftArm.isHidden
					  || !mainModel.bipedRightArm.isHidden
					  || !mainModel.bipedLeftLeg.isHidden
					  || !mainModel.bipedRightLeg.isHidden )
					{
						renderToUse.doRobo = true;
						renderToUse.doRusty = true;
						renderToUse.doRender((AbstractClientPlayer) entityPlayer, event.getX(), event.getY() - (hasNoLegs ? (11F / 16F) : 0), event.getZ(), entityPlayer.rotationYaw, event.getPartialRenderTick());
					}
					
					// restore defaults
					mainModel.bipedBody.isHidden = false;
					mainModel.bipedHead.isHidden = false;
					mainModel.bipedLeftArm.isHidden = false;
					mainModel.bipedRightArm.isHidden = false;
					mainModel.bipedLeftLeg.isHidden = false;
					mainModel.bipedRightLeg.isHidden = false;
				}
			}
			else if (hasNoSkin)
			{
				renderToUse.doRender((AbstractClientPlayer) entityPlayer, event.getX(), event.getY(), event.getZ(), entityPlayer.rotationYaw, event.getPartialRenderTick());
			}
			
			if (hasNoSkin)
			{
				renderToUse.doMuscles = false;
			}
		}
		
		if (!hasLeftLeg)
		{
			renderPlayer.getMainModel().bipedLeftLeg.isHidden = true;
		}
		
		if (!hasRightLeg)
		{
			renderPlayer.getMainModel().bipedRightLeg.isHidden = true;
		}
		
		if (!hasLeftArm)
		{
			renderPlayer.getMainModel().bipedLeftArm.isHidden = true;
			
			// Hide the main or offhand item if no arm there
			if (mc.gameSettings.mainHand == EnumHandSide.LEFT)
			{
				if ( !mainHand.containsKey(entityPlayer.getEntityId())
				  && InventoryPlayer.isHotbar(entityPlayer.inventory.currentItem) )
				{
					mainHand.put(entityPlayer.getEntityId(), entityPlayer.inventory.mainInventory.set(entityPlayer.inventory.currentItem, ItemStack.EMPTY));
				}
			}
			else
			{
				if (!offHand.containsKey(entityPlayer.getEntityId()))
				{
					offHand.put(entityPlayer.getEntityId(), entityPlayer.inventory.offHandInventory.set(0, ItemStack.EMPTY));
				}
			}
		}
		
		if (!hasRightArm)
		{
			renderPlayer.getMainModel().bipedRightArm.isHidden = true;
			
			// Hide the main or offhand item if no arm there
			if (mc.gameSettings.mainHand == EnumHandSide.RIGHT)
			{
				if ( !mainHand.containsKey(entityPlayer.getEntityId())
				  && InventoryPlayer.isHotbar(entityPlayer.inventory.currentItem) )
				{
					mainHand.put(entityPlayer.getEntityId(), entityPlayer.inventory.mainInventory.set(entityPlayer.inventory.currentItem, ItemStack.EMPTY));
				}
			}
			else
			{
				if (!offHand.containsKey(entityPlayer.getEntityId()))
				{
					offHand.put(entityPlayer.getEntityId(), entityPlayer.inventory.offHandInventory.set(0, ItemStack.EMPTY));
				}
			}
		}
	}
	
	private static Map<Integer, ItemStack> mainHand = new HashMap<>();
	private static Map<Integer, ItemStack> offHand = new HashMap<>();
	
	private static Map<Integer, ItemStack> pants = new HashMap<>();
	private static Map<Integer, ItemStack> shoes = new HashMap<>();

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void handleMissingSkin(RenderPlayerEvent.Post event)
	{
		if (!CyberwareConfig.ENABLE_CUSTOM_PLAYER_MODEL) return;
		
		event.getRenderer().getMainModel().bipedLeftArm.isHidden = false;
		event.getRenderer().getMainModel().bipedRightArm.isHidden = false;
		event.getRenderer().getMainModel().bipedLeftLeg.isHidden = false;
		event.getRenderer().getMainModel().bipedRightLeg.isHidden = false;
		
		EntityPlayer entityPlayer = event.getEntityPlayer();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData != null)
		{
			if (pants.containsKey(entityPlayer.getEntityId()))
			{
				entityPlayer.inventory.armorInventory.set(EntityEquipmentSlot.LEGS.getIndex(), pants.remove(entityPlayer.getEntityId()));
			}
			
			if (shoes.containsKey(entityPlayer.getEntityId()))
			{
				entityPlayer.inventory.armorInventory.set(EntityEquipmentSlot.FEET.getIndex(), shoes.remove(entityPlayer.getEntityId()));
			}
			
			if (mainHand.containsKey(entityPlayer.getEntityId()))
			{
				entityPlayer.inventory.mainInventory.set(entityPlayer.inventory.currentItem, mainHand.remove(entityPlayer.getEntityId()));
			}
			
			if (offHand.containsKey(entityPlayer.getEntityId()))
			{
				entityPlayer.inventory.offHandInventory.set(0, offHand.remove(entityPlayer.getEntityId()));
			}
			
			if (!cyberwareUserData.hasEssential(EnumSlot.ARM, EnumSide.LEFT))
			{
				event.getRenderer().getMainModel().bipedLeftArm.isHidden = false;
			}
			
			if (!cyberwareUserData.hasEssential(EnumSlot.ARM, EnumSide.RIGHT))
			{
				event.getRenderer().getMainModel().bipedRightArm.isHidden = false;
			}
		}
	}
	
	private static boolean missingArm = false;
	private static boolean missingSecondArm = false;
	private static boolean hasRoboLeft = false;
	private static boolean hasRoboRight = false;
	private static EnumHandSide oldHand;
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void handleMissingEssentials(LivingUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (entityLivingBase != Minecraft.getMinecraft().player) return;
		
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData != null)
		{
			GameSettings settings = Minecraft.getMinecraft().gameSettings;
			boolean stillMissingArm = false;
			boolean stillMissingSecondArm = false;
			
			boolean leftUnpowered = false;
			ItemStack armLeft = cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM));
			if (!armLeft.isEmpty() && !ItemCyberlimb.isPowered(armLeft))
			{
				leftUnpowered = true;
			}
			
			boolean rightUnpowered = false;
			ItemStack armRight = cyberwareUserData.getCyberware(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM));
			if (!armRight.isEmpty() && !ItemCyberlimb.isPowered(armRight))
			{
				rightUnpowered = true;
			}
			
			boolean hasSkin = cyberwareUserData.isCyberwareInstalled(CyberwareContent.skinUpgrades.getCachedStack(ItemSkinUpgrade.META_SYNTHETIC_SKIN));
			hasRoboLeft = !armLeft.isEmpty() && !hasSkin;
			hasRoboRight = !armRight.isEmpty() && !hasSkin;
			boolean hasRightArm = cyberwareUserData.hasEssential(EnumSlot.ARM, EnumSide.RIGHT) && !rightUnpowered;
			boolean hasLeftArm = cyberwareUserData.hasEssential(EnumSlot.ARM, EnumSide.LEFT) && !leftUnpowered;
			
			if (!hasRightArm)
			{
				if (settings.mainHand != EnumHandSide.LEFT)
				{
					oldHand = settings.mainHand;
					settings.mainHand = EnumHandSide.LEFT;
					settings.sendSettingsToServer();
				}
				
				missingArm = true;
				stillMissingArm = true;
				
				if (!hasLeftArm)
				{
					missingSecondArm = true;
					stillMissingSecondArm = true;
				}
			}
			else if (!hasLeftArm)
			{
				if (settings.mainHand != EnumHandSide.RIGHT)
				{
					oldHand = settings.mainHand;
					settings.mainHand = EnumHandSide.RIGHT;
					settings.sendSettingsToServer();
				}
				
				missingArm = true;
				stillMissingArm = true;
			}

			if (!stillMissingArm)
			{
				missingArm = false;
				if (oldHand != null)
				{
					settings.mainHand = oldHand;
					settings.sendSettingsToServer();
					oldHand = null;
				}
			}

			if (!stillMissingSecondArm)
			{
				missingSecondArm = false;
			}
		}
	}
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void handleRenderHand(RenderHandEvent event)
	{
		if (!CyberwareConfig.ENABLE_CUSTOM_PLAYER_MODEL || FMLClientHandler.instance().hasOptifine()) return;
		
		if (missingArm || missingSecondArm || hasRoboLeft || hasRoboRight)
		{
			float partialTicks = event.getPartialTicks();
			EntityRenderer entityRenderer = mc.entityRenderer;
			event.setCanceled(true);
			
			boolean isSleeping = mc.getRenderViewEntity() instanceof EntityLivingBase
			                  && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

			if ( mc.gameSettings.thirdPersonView == 0
			  && !isSleeping
			  && !mc.gameSettings.hideGUI
			  && !mc.playerController.isSpectator() )
			{
				entityRenderer.enableLightmap();
				renderItemInFirstPerson(partialTicks);
				entityRenderer.disableLightmap();
			}
		}
	}

	public static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
		return first != null ? first : checkNotNull(second);
	}

	private void renderItemInFirstPerson(float partialTicks)
	{
		ItemRenderer itemRenderer = mc.getItemRenderer();
		AbstractClientPlayer abstractclientplayer = mc.player;
		float swingProgress = abstractclientplayer.getSwingProgress(partialTicks);

		EnumHand enumhand = firstNonNull(abstractclientplayer.swingingHand, EnumHand.MAIN_HAND);

		float rotationPitch = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
		float rotationYaw = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
		boolean doRenderMainHand = true;
		boolean doRenderOffHand = true;

		if (abstractclientplayer.isHandActive())
		{
			ItemStack itemstack = abstractclientplayer.getActiveItemStack();

			if (!itemstack.isEmpty() && itemstack.getItem() == Items.BOW) //Forge: Data watcher can desync and cause this to NPE...
			{
				EnumHand enumhand1 = abstractclientplayer.getActiveHand();
				doRenderMainHand = enumhand1 == EnumHand.MAIN_HAND;
				doRenderOffHand = !doRenderMainHand;
			}
		}

		rotateArroundXAndY(rotationPitch, rotationYaw);
		setLightmap();
		rotateArm(partialTicks);
		GlStateManager.enableRescaleNormal();

		RenderCyberlimbHand.INSTANCE.itemStackMainHand = itemRenderer.itemStackMainHand;
		RenderCyberlimbHand.INSTANCE.itemStackOffHand = itemRenderer.itemStackOffHand;

		if (doRenderMainHand && !missingSecondArm)
		{
			float f3 = enumhand == EnumHand.MAIN_HAND ? swingProgress : 0.0F;
			float f5 = 1.0F - (itemRenderer.prevEquippedProgressMainHand + (itemRenderer.equippedProgressMainHand - itemRenderer.prevEquippedProgressMainHand) * partialTicks);
			RenderCyberlimbHand.INSTANCE.leftRobot = hasRoboLeft;
			RenderCyberlimbHand.INSTANCE.rightRobot = hasRoboRight;
			RenderCyberlimbHand.INSTANCE.renderItemInFirstPerson(abstractclientplayer, partialTicks, rotationPitch, EnumHand.MAIN_HAND, f3, itemRenderer.itemStackMainHand, f5);
		}

		if (doRenderOffHand && !missingArm)
		{
			float f4 = enumhand == EnumHand.OFF_HAND ? swingProgress : 0.0F;
			float f6 = 1.0F - (itemRenderer.prevEquippedProgressOffHand + (itemRenderer.equippedProgressOffHand - itemRenderer.prevEquippedProgressOffHand) * partialTicks);
			RenderCyberlimbHand.INSTANCE.leftRobot = hasRoboLeft;
			RenderCyberlimbHand.INSTANCE.rightRobot = hasRoboRight;
			RenderCyberlimbHand.INSTANCE.renderItemInFirstPerson(abstractclientplayer, partialTicks, rotationPitch, EnumHand.OFF_HAND, f4, itemRenderer.itemStackOffHand, f6);
		}

		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
	}
	
	private void rotateArroundXAndY(float angle, float angleY)
	{
		GlStateManager.pushMatrix();
		GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(angleY, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}
	
	private void setLightmap()
	{
		EntityPlayer entityPlayer = mc.player;
		int i = mc.world.getCombinedLight(new BlockPos(entityPlayer.posX, entityPlayer.posY + (double)entityPlayer.getEyeHeight(), entityPlayer.posZ), 0);
		float f = (float)(i & 65535);
		float f1 = (float)(i >> 16);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
	}
	
	private void rotateArm(float p_187458_1_)
	{
		EntityPlayerSP entityPlayerSP = mc.player;
		float f = entityPlayerSP.prevRenderArmPitch + (entityPlayerSP.renderArmPitch - entityPlayerSP.prevRenderArmPitch) * p_187458_1_;
		float f1 = entityPlayerSP.prevRenderArmYaw + (entityPlayerSP.renderArmYaw - entityPlayerSP.prevRenderArmYaw) * p_187458_1_;
		GlStateManager.rotate((entityPlayerSP.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate((entityPlayerSP.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
	}
	
	@SubscribeEvent
	public void handleWorldUnload(WorldEvent.Unload event)
	{
		if (missingArm)
		{
			GameSettings settings = Minecraft.getMinecraft().gameSettings;
			missingArm = false;
			settings.mainHand = oldHand;
		}
	}
}
