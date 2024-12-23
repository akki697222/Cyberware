package flaxbeard.cyberware.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class ItemLegUpgrade extends ItemCyberware
{

	private static final int META_JUMP_BOOST            = 0;
	private static final int META_FALL_DAMAGE           = 1;
	
	public ItemLegUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{		
		return NNLUtil.fromArray(new ItemStack[][] { 
				new ItemStack[] { CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG),
				                  CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG) }});
	}
	
	@SubscribeEvent
	public void playerJumps(LivingEvent.LivingJumpEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;
		
		ItemStack itemStackJumpBoost = cyberwareUserData.getCyberware(getCachedStack(META_JUMP_BOOST));
		if (!itemStackJumpBoost.isEmpty())
		{
			int numLegs = 0;
			if (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG)))
			{
				numLegs++;
			}
			if (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)))
			{
				numLegs++;
			}
			if (cyberwareUserData.usePower(itemStackJumpBoost, getPowerConsumption(itemStackJumpBoost)))
			{
				if (entityLivingBase.isSneaking())
				{
					Vec3d vector = entityLivingBase.getLook(0.5F);
					double total = Math.abs(vector.z + vector.x);
					double jump = 0;
					if (jump >= 1)
					{
						jump = (jump + 2D) / 4D;
					}

					double y = vector.y < total ? total : vector.y;

					entityLivingBase.motionY += (numLegs * ((jump + 1) * y)) / 3F;
					entityLivingBase.motionZ += (jump + 1) * vector.z * numLegs;
					entityLivingBase.motionX += (jump + 1) * vector.x * numLegs;
				}
				else
				{
					entityLivingBase.motionY += numLegs * (0.2750000059604645D / 2D);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onFallDamage(LivingAttackEvent event)
	{
		if (event.getSource() != DamageSource.FALL)
		{
			return;
		}
		
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;
		
		if ( cyberwareUserData.isCyberwareInstalled(getCachedStack(META_FALL_DAMAGE))
		  && cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG))
		  && cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)) )
		{
			event.setCanceled(true);
		}
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return stack.getItemDamage() == META_JUMP_BOOST ? LibConstants.JUMPBOOST_CONSUMPTION : 0;
	}
}
