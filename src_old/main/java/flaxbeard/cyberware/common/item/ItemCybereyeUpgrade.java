package flaxbeard.cyberware.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class ItemCybereyeUpgrade extends ItemCyberware implements IMenuItem, IHudjack
{
	
	public static final int META_NIGHT_VISION               = 0;
	public static final int META_UNDERWATER_VISION          = 1;
	public static final int META_HUDJACK                    = 2;
	public static final int META_TARGETING                  = 3;
	public static final int META_ZOOM                       = 4;
	
	public ItemCybereyeUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		if (stack.getItemDamage() == META_TARGETING)
		{
			return NNLUtil.fromArray(new ItemStack[][] { 
					new ItemStack[] { CyberwareContent.cybereyes.getCachedStack(0) }, 
					new ItemStack[] { getCachedStack(META_HUDJACK) }});
		}
		
		return NNLUtil.fromArray(new ItemStack[][] { 
				new ItemStack[] { CyberwareContent.cybereyes.getCachedStack(0) }});
	}
	
	private static int cache_tickExisted = -1;
	private static boolean cache_isHighlighting = false;
	private static AxisAlignedBB cache_aabbHighlight = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	private static List<EntityLivingBase> entitiesInRange = new ArrayList<>(16);
	private static final float HIGHLIGHT_RANGE = 25F;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void handleHighlight(RenderTickEvent event)
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		if (entityPlayer == null) return;
		
		if (entityPlayer.ticksExisted != cache_tickExisted)
		{
			cache_tickExisted = entityPlayer.ticksExisted;
			
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData == null) return;
			ItemStack itemStackTargeting = cyberwareUserData.getCyberware(getCachedStack(META_TARGETING));
			cache_isHighlighting = !itemStackTargeting.isEmpty()
			                    && EnableDisableHelper.isEnabled(itemStackTargeting);
			if (cache_isHighlighting)
			{
				cache_aabbHighlight = new AxisAlignedBB(entityPlayer.posX - HIGHLIGHT_RANGE, entityPlayer.posY - HIGHLIGHT_RANGE, entityPlayer.posZ - HIGHLIGHT_RANGE,
				                                        entityPlayer.posX + entityPlayer.width + HIGHLIGHT_RANGE, entityPlayer.posY + entityPlayer.height + HIGHLIGHT_RANGE, entityPlayer.posZ + entityPlayer.width + HIGHLIGHT_RANGE);
			}
		}
		
		if (cache_isHighlighting)
		{
			if (event.phase == Phase.START)
			{
				entitiesInRange.clear();
				List<EntityLivingBase> entityLivingBases = entityPlayer.world.getEntitiesWithinAABB(EntityLivingBase.class, cache_aabbHighlight);
				double rangeSq = HIGHLIGHT_RANGE * HIGHLIGHT_RANGE;
				for (EntityLivingBase entityLivingBase : entityLivingBases)
				{
					if ( entityPlayer.getDistanceSq(entityLivingBase) <= rangeSq
					  && entityLivingBase != entityPlayer
					  && !entityLivingBase.isGlowing() )
					{
						entityLivingBase.setGlowing(true);
						entitiesInRange.add(entityLivingBase);
					}
				}
			}
			else if (event.phase == Phase.END)
			{
				for (EntityLivingBase entityLivingBase : entitiesInRange)
				{
					entityLivingBase.setGlowing(false);
				}
				entitiesInRange.clear();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void handleFog(FogDensity event)
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;
		
		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_UNDERWATER_VISION)))
		{
			if (entityPlayer.isInsideOfMaterial(Material.WATER))
			{
				event.setDensity(0.01F);
				event.setCanceled(true);
			}
			else if (entityPlayer.isInsideOfMaterial(Material.LAVA))
			{
				event.setDensity(0.7F);
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleNightVision(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		ItemStack itemStackNightVision = cyberwareUserData.getCyberware(getCachedStack(META_NIGHT_VISION));
		
		if ( !itemStackNightVision.isEmpty()
		  && EnableDisableHelper.isEnabled(itemStackNightVision) )
		{
			entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 53, true, false));
		}
		else
		{
			PotionEffect effect = entityLivingBase.getActivePotionEffect(MobEffects.NIGHT_VISION);
			if (effect != null && effect.getAmplifier() == 53)
			{
				entityLivingBase.removePotionEffect(MobEffects.NIGHT_VISION);
			}
		}
	}
	
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void handleWaterVision(RenderBlockOverlayEvent event)
	{
		EntityPlayer entityPlayer = event.getPlayer();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return;
		ItemStack itemStackUnderwaterVision = cyberwareUserData.getCyberware(getCachedStack(META_UNDERWATER_VISION));
		
		if ( !itemStackUnderwaterVision.isEmpty()
		  && ( event.getBlockForOverlay().getMaterial() == Material.WATER
		    || event.getBlockForOverlay().getMaterial() == Material.LAVA ) )
		{
			event.setCanceled(true);
		}
	}
	
	private static boolean inUse = false;
	private static boolean wasInUse = false;

	private static int zoomSettingOn = 0;
	private static float fov = 0F;
	private static float sensitivity = 0F;
	private static EntityPlayer player = null;

	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tickStart(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (event.phase == Phase.START)
		{
			wasInUse = inUse;
				
			EntityPlayer entityPlayer = mc.player;
			
			if (!inUse && !wasInUse)
			{
				fov = mc.gameSettings.fovSetting;
				sensitivity = mc.gameSettings.mouseSensitivity;
			}
			
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if ( cyberwareUserData != null
			  && cyberwareUserData.isCyberwareInstalled(getCachedStack(META_ZOOM)) )
			{
				player = entityPlayer;

				if (mc.gameSettings.thirdPersonView == 0)
				{
					switch (zoomSettingOn)
					{
					case 0:
						mc.gameSettings.fovSetting = fov;
						mc.gameSettings.mouseSensitivity = sensitivity;
						break;
						
					case 1:
						mc.gameSettings.fovSetting = fov;
						mc.gameSettings.mouseSensitivity = sensitivity;
						int i = 0;
						while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 2.0F)) > 2.5F && i < 200)
						{
							mc.gameSettings.fovSetting -= 2.5F;
							mc.gameSettings.mouseSensitivity -= 0.01F;
							i++;
						}
						break;
						
					case 2:
						mc.gameSettings.fovSetting = fov;
						mc.gameSettings.mouseSensitivity = sensitivity;
						i = 0;
						while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 5.0F)) > 2.5F && i < 200)
						{
							mc.gameSettings.fovSetting -= 2.5F;
							mc.gameSettings.mouseSensitivity -= 0.01F;
							i++;
						}
						break;
						
					case 3:
						mc.gameSettings.fovSetting = fov;
						mc.gameSettings.mouseSensitivity = sensitivity;
						i = 0;
						while (Math.abs((mc.gameSettings.fovSetting - ((fov + 5F)) / 12.0F)) > 2.5F && i < 200)
						{
							mc.gameSettings.fovSetting -= 2.5F;
							mc.gameSettings.mouseSensitivity -= 0.01F;
							i++;
						}
						break;
					}
				}
				
			}
			else
			{
				zoomSettingOn = 0;
			}
			inUse = zoomSettingOn != 0;

			if (!inUse && wasInUse)
			{
				mc.gameSettings.fovSetting = fov;
				mc.gameSettings.mouseSensitivity = sensitivity;
			}

		}
	}
	
	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.getItemDamage() == META_NIGHT_VISION
		    || stack.getItemDamage() == META_HUDJACK
		    || stack.getItemDamage() == META_TARGETING
		    || stack.getItemDamage() == META_ZOOM;
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		if (stack.getItemDamage() == META_ZOOM)
		{
			if (entity == player)
			{
				if (entity.isSneaking())
				{
					zoomSettingOn = (zoomSettingOn + 4 - 1) % 4;
				}
				else
				{
					zoomSettingOn = (zoomSettingOn + 1) % 4;
				}
			}
			return;
		}
		EnableDisableHelper.toggle(stack);
	}

	@Override
	public String getUnlocalizedLabel(ItemStack stack)
	{
		if (stack.getItemDamage() == META_ZOOM)
		{
			return "cyberware.gui.active.zoom";
		}
		return EnableDisableHelper.getUnlocalizedLabel(stack);
	}

	private static final float[] f = new float[] { 1.0F, 0.0F, 0.0F };
	
	@Override
	public float[] getColor(ItemStack stack)
	{
		if (stack.getItemDamage() == META_ZOOM)
		{
			return null;
		}
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}

	@Override
	public boolean isActive(ItemStack stack)
	{
		return stack.getItemDamage() == META_HUDJACK
		    && EnableDisableHelper.isEnabled(stack);
	}
}
