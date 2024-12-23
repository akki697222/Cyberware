package flaxbeard.cyberware.common.item;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ItemCyberlimb extends ItemCyberware implements ISidedLimb
{

	public static final int META_LEFT_CYBER_ARM         = 0;
	public static final int META_RIGHT_CYBER_ARM        = 1;
	public static final int META_LEFT_CYBER_LEG         = 2;
	public static final int META_RIGHT_CYBER_LEG        = 3;
	
	public ItemCyberlimb(String name, EnumSlot[] slots, String[] subnames)
	{
		super(name, slots, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean isEssential(ItemStack stack)
	{
		return true;		
	}
	
	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		ICyberware ware = CyberwareAPI.getCyberware(other);
		
		if (ware instanceof ISidedLimb)
		{
			return ware.isEssential(other) && ((ISidedLimb) ware).getSide(other) == this.getSide(stack);
		}
		return false;
	}
	
	@Override
	public EnumSide getSide(ItemStack stack)
	{
		return stack.getItemDamage() % 2 == 0 ? EnumSide.LEFT : EnumSide.RIGHT;
	}
	
	public static boolean isPowered(ItemStack stack)
	{
		NBTTagCompound data = CyberwareAPI.getCyberwareNBT(stack);
		if (!data.hasKey("active"))
		{
			data.setBoolean("active", true);
		}
		return data.getBoolean("active");
	}
	
	private Set<Integer> didFall = new HashSet<>();
	
	@SubscribeEvent
	public void handleFallDamage(LivingAttackEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if ( entityLivingBase.world.isRemote
		  && event.getSource() == DamageSource.FALL )
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
			if (cyberwareUserData == null) return;
			if ( cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LEFT_CYBER_LEG))
		      && cyberwareUserData.isCyberwareInstalled(getCachedStack(META_RIGHT_CYBER_LEG)) )
			{
				didFall.add(entityLivingBase.getEntityId());
			}
		}
	}
	
	@SubscribeEvent
	public void handleSound(PlaySoundAtEntityEvent event)
	{
		Entity entity = event.getEntity();
		if ( entity instanceof EntityPlayer
		  && event.getSound() == SoundEvents.ENTITY_PLAYER_HURT
		  && entity.world.isRemote
		  && didFall.contains(entity.getEntityId()) )
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entity);
			if (cyberwareUserData == null) return;
			
			int numLegs = 0;
			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_LEFT_CYBER_LEG)))
			{
				numLegs++;
			}
			if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_RIGHT_CYBER_LEG)))
			{
				numLegs++;
			}
			
			if (numLegs > 0)
			{	
				event.setSound(SoundEvents.ENTITY_IRONGOLEM_HURT);
				event.setPitch(event.getPitch() + 1F);
				didFall.remove(entity.getEntityId());
			}
		}	
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (entityLivingBase.ticksExisted % 20 != 0) return;
		
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		for (int damage = 0; damage < 4; damage++)
		{
			ItemStack itemStackInstalled = cyberwareUserData.getCyberware(getCachedStack(damage));
			if (!itemStackInstalled.isEmpty())
			{
				boolean isPowered = cyberwareUserData.usePower(itemStackInstalled, getPowerConsumption(itemStackInstalled));
				
				CyberwareAPI.getCyberwareNBT(itemStackInstalled).setBoolean("active", isPowered);
			}
		}
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return LibConstants.LIMB_CONSUMPTION;
	}
}
