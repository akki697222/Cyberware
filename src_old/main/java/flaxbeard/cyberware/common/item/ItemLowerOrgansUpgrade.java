package flaxbeard.cyberware.common.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ItemLowerOrgansUpgrade extends ItemCyberware implements IMenuItem
{

	public static final int META_LIVER_FILTER              = 0;
	public static final int META_METABOLIC_GENERATOR       = 1;
	public static final int META_BATTERY                   = 2;
	public static final int META_ADRENALINE_PUMP           = 3;
	
	public ItemLowerOrgansUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private static Map<UUID, Collection<PotionEffect>> mapPotions = new HashMap<>();

	@SubscribeEvent
	public void handleEatFoodTick(LivingEntityUseItemEvent.Tick event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (!(entityLivingBase instanceof EntityPlayer)) return;
		EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;
		ItemStack stack = event.getItem();
		
		if ( !stack.isEmpty()
		  && ( stack.getItem().getItemUseAction(stack) == EnumAction.EAT
		    || stack.getItem().getItemUseAction(stack) == EnumAction.DRINK ) )
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if ( cyberwareUserData != null
			  && cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LIVER_FILTER)))
			{
				mapPotions.put(entityPlayer.getUniqueID(), new ArrayList<>(entityPlayer.getActivePotionEffects()));
			}
		}
	}
	
	@SubscribeEvent
	public void handleEatFoodEnd(LivingEntityUseItemEvent.Finish event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (!(entityLivingBase instanceof EntityPlayer)) return;
		EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;
		ItemStack stack = event.getItem();
		
		if ( !stack.isEmpty()
		  && ( stack.getItem().getItemUseAction(stack) == EnumAction.EAT
		    || stack.getItem().getItemUseAction(stack) == EnumAction.DRINK ) )
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if ( cyberwareUserData != null
			  && cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LIVER_FILTER)))
			{
				Collection<PotionEffect> potionEffectsRemoved = new ArrayList<>(entityPlayer.getActivePotionEffects());
				for (PotionEffect potionEffect : potionEffectsRemoved)
				{
					if (potionEffect.getPotion().isBadEffect())
					{
						entityPlayer.removePotionEffect(potionEffect.getPotion());
					}
				}
				
				Collection<PotionEffect> potionEffectsToAdd = mapPotions.get(entityPlayer.getUniqueID());
				if (potionEffectsToAdd != null)
				{
					for (PotionEffect potionEffectToAdd : potionEffectsToAdd)
					{
						for (PotionEffect potionEffectRemoved : potionEffectsRemoved)
						{
							if (potionEffectRemoved.getPotion() == potionEffectToAdd.getPotion())
							{
								entityPlayer.addPotionEffect(potionEffectToAdd);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (entityLivingBase.ticksExisted % 20 != 0) return;
		
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		
		ItemStack itemStackMetabolicGenerator = cyberwareUserData.getCyberware(getCachedStack(META_METABOLIC_GENERATOR));
		if ( !itemStackMetabolicGenerator.isEmpty()
		  && EnableDisableHelper.isEnabled(itemStackMetabolicGenerator)
		  && !cyberwareUserData.isAtCapacity(itemStackMetabolicGenerator, getPowerProduction(itemStackMetabolicGenerator)) )
		{
			if (entityLivingBase instanceof EntityPlayer)
			{
				EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;
				if ( entityPlayer.getFoodStats().getFoodLevel() > 0
				  || entityPlayer.isCreative() )
				{
					int toRemove = getTicksTilRemove(itemStackMetabolicGenerator);
					if (!entityPlayer.isCreative() && toRemove <= 0)
					{
						entityPlayer.getFoodStats().addExhaustion(6.0F);
						toRemove = LibConstants.METABOLIC_USES;
					}
					else if (toRemove > 0)
					{
						toRemove--;
					}
					CyberwareAPI.getCyberwareNBT(itemStackMetabolicGenerator).setInteger("toRemove", toRemove);
					
					cyberwareUserData.addPower(getPowerProduction(itemStackMetabolicGenerator), itemStackMetabolicGenerator);
				}
			}
			else
			{
				cyberwareUserData.addPower(getPowerProduction(itemStackMetabolicGenerator) / 10, itemStackMetabolicGenerator);
			}
		}
		
		ItemStack itemStackAdrenalinePump = cyberwareUserData.getCyberware(getCachedStack(META_ADRENALINE_PUMP));
		if (!itemStackAdrenalinePump.isEmpty())
		{
			boolean wasBelow = wasBelow(itemStackAdrenalinePump);
			boolean isBelow = false;
			if (entityLivingBase.getMaxHealth() > 8 && entityLivingBase.getHealth() < 8)
			{
				isBelow = true;

				if ( !wasBelow
				  && cyberwareUserData.usePower(itemStackAdrenalinePump, this.getPowerConsumption(itemStackAdrenalinePump), false) )
				{
					entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600, 0, true, false));
					entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 600, 0, true, false));
				}
			}
			
			CyberwareAPI.getCyberwareNBT(itemStackAdrenalinePump).setBoolean("wasBelow", isBelow);
		}
	}
	
	private int getTicksTilRemove(ItemStack stack)
	{
		NBTTagCompound data = CyberwareAPI.getCyberwareNBT(stack);
		if (!data.hasKey("toRemove"))
		{
			data.setInteger("toRemove", LibConstants.METABOLIC_USES);
		}
		return data.getInteger("toRemove");
	}
	
	private boolean wasBelow(ItemStack stack)
	{
		NBTTagCompound data = CyberwareAPI.getCyberwareNBT(stack);
		if (!data.hasKey("wasBelow"))
		{
			data.setBoolean("wasBelow", false);
		}
		return data.getBoolean("wasBelow");
	}

	@Override
	public int getCapacity(ItemStack wareStack)
	{
		return wareStack.getItemDamage() == META_METABOLIC_GENERATOR ? LibConstants.METABOLIC_PRODUCTION :
		       wareStack.getItemDamage() == META_BATTERY ? LibConstants.BATTERY_CAPACITY * wareStack.getCount() : 0;
	}
	
	@Override
	public int installedStackSize(ItemStack stack)
	{
		return stack.getItemDamage() == META_BATTERY ? 4 : 1;
	}
	
	@Override
	public int getPowerProduction(ItemStack stack)
	{
		return stack.getItemDamage() == META_METABOLIC_GENERATOR ? LibConstants.METABOLIC_PRODUCTION : 0;
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return stack.getItemDamage() == META_ADRENALINE_PUMP ? LibConstants.ADRENALINE_CONSUMPTION : 0;
	}
	
	@Override
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		if (stack.getItemDamage() == META_BATTERY)
		{
			switch (stack.getCount())
			{
				case 1:
					return 5;
				case 2:
					return 7;
				case 3:
					return 9;
				case 4:
					return 11;
			}
		}
		return super.getUnmodifiedEssenceCost(stack);
	}

	@Override
	public boolean hasMenu(ItemStack stack)
	{
		return stack.getItemDamage() == META_METABOLIC_GENERATOR;
	}

	@Override
	public void use(Entity entity, ItemStack stack)
	{
		EnableDisableHelper.toggle(stack);
	}

	@Override
	public String getUnlocalizedLabel(ItemStack stack)
	{
		return EnableDisableHelper.getUnlocalizedLabel(stack);
	}

	private static final float[] f = new float[] { 1.0F, 0.0F, 0.0F };
	
	@Override
	public float[] getColor(ItemStack stack)
	{
		return EnableDisableHelper.isEnabled(stack) ? f : null;
	}
}
