package flaxbeard.cyberware.common.item;

import javax.annotation.Nonnull;
import java.util.*;

import net.minecraft.enchantment.EnchantmentThorns;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ItemSkinUpgrade extends ItemCyberware
{

	public static final int META_SOLARSKIN              = 0;
	public static final int META_SUBDERMAL_SPIKES       = 1;
	public static final int META_SYNTHETIC_SKIN         = 2;
	public static final int META_IMMUNOSUPPRESSANT      = 3;
	
	public ItemSkinUpgrade(String name, EnumSlot slot, String[] subnames)
	{
		super(name, slot, subnames);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void handleLivingUpdate(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
        if (entityLivingBase.ticksExisted % 20 != 0) return;
        
        float lightFactor = getLightFactor(entityLivingBase);
		if (lightFactor <= 0.0F) return;
		
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		
		ItemStack itemStackSolarskin = cyberwareUserData.getCyberware(getCachedStack(META_SOLARSKIN));
		if (!itemStackSolarskin.isEmpty())
		{
			int power = Math.max(0, Math.round(getPowerProduction(itemStackSolarskin) * lightFactor));
			cyberwareUserData.addPower(power, itemStackSolarskin);
		}
	}
	
	private float getLightFactor(EntityLivingBase entityLivingBase)
	{
		World world = entityLivingBase.world;
		// world must have a sun
		if (!entityLivingBase.world.provider.hasSkyLight()) return 0.0F;
		// current position can see the sun
		BlockPos pos = new BlockPos(entityLivingBase.posX, entityLivingBase.posY + entityLivingBase.height, entityLivingBase.posZ);
		if (!entityLivingBase.world.canBlockSeeSky(pos)) return 0.0F;
		
		// sun isn't shaded
		int lightSky = world.getLightFor(EnumSkyBlock.SKY, pos);
		// note: world.getSkylightSubtracted() is server side only
		if (lightSky < 15) return 0.0F;
		
		// it's day time (see Vanilla daylight sensor)
		float celestialAngleRadians = world.getCelestialAngleRadians(1.0F);
		float offsetRadians = celestialAngleRadians < (float) Math.PI ? 0.0F : ((float) Math.PI * 2.0F);
		float celestialAngleRadians2 = celestialAngleRadians + (offsetRadians - celestialAngleRadians) * 0.2F;
		return MathHelper.cos(celestialAngleRadians2);
	}
	
	private static Map<UUID, Collection<LastPotionEffect>> mapEntityLastPotionEffects = new HashMap<>();
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleMissingEssentials(CyberwareUpdateEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
		
		ItemStack itemStackImmunosuppressant = cyberwareUserData.getCyberware(getCachedStack(META_IMMUNOSUPPRESSANT));
		if (!itemStackImmunosuppressant.isEmpty())
		{
			// consume power every 1 s, apply damage every 5 s unless power or Neuropozyne is active
			if ( entityLivingBase instanceof EntityPlayer
			  && entityLivingBase.ticksExisted % 20 == 0 )
			{
				boolean isPowered = cyberwareUserData.usePower(itemStackImmunosuppressant, getPowerConsumption(itemStackImmunosuppressant));
				
				if ( !isPowered
				  && entityLivingBase.ticksExisted % 100 == 0
				  && !entityLivingBase.isPotionActive(CyberwareContent.neuropozyneEffect) )
				{
					entityLivingBase.attackEntityFrom(EssentialsMissingHandler.lowessence, 2.0F);
				}
			}
			
			// increase poison and hunger duration
			if (!entityLivingBase.getEntityWorld().isRemote)
			{
				Collection<LastPotionEffect> lastPotionEffects = mapEntityLastPotionEffects.get(entityLivingBase.getUniqueID());
				if (lastPotionEffects == null)
				{// (this is our first time seeing this player)
					// save all current poison and hunger potions
					lastPotionEffects = new ArrayList<>(2);
					Collection<PotionEffect> currentEffects = entityLivingBase.getActivePotionEffects();
					for (PotionEffect potionEffectCurrent : currentEffects)
					{
						if ( potionEffectCurrent.getPotion() == MobEffects.POISON
						  || potionEffectCurrent.getPotion() == MobEffects.HUNGER )
						{
							lastPotionEffects.add(new LastPotionEffect(potionEffectCurrent));
						}
					}
					mapEntityLastPotionEffects.put(entityLivingBase.getUniqueID(), lastPotionEffects);
				}
				else
				{
					// mark last potion effects for removal
					lastPotionEffects.forEach(lastPotionEffect -> lastPotionEffect.isFound = false);
					
					// check all current poison and hunger potions
					Collection<PotionEffect> currentEffects = entityLivingBase.getActivePotionEffects();
					for (PotionEffect potionEffectCurrent : currentEffects)
					{
						if ( potionEffectCurrent.getPotion() == MobEffects.POISON
						  || potionEffectCurrent.getPotion() == MobEffects.HUNGER )
						{
							// check if it's a new one or changed
							boolean found = false;
							for (LastPotionEffect lastPotionEffect : lastPotionEffects)
							{
								if ( lastPotionEffect.potion    == potionEffectCurrent.getPotion()
								  && lastPotionEffect.amplifier == potionEffectCurrent.getAmplifier()
								  && lastPotionEffect.duration  >= potionEffectCurrent.getDuration() )
								{
									lastPotionEffect.isFound = true;
									found = true;
									break;
								}
							}
							
							if (!found)
							{
								final PotionEffect potionEffectAugmented = new PotionEffect(potionEffectCurrent.getPotion(),
								                                                            (int) (potionEffectCurrent.getDuration() * 1.8F),
								                                                            potionEffectCurrent.getAmplifier(),
								                                                            potionEffectCurrent.getIsAmbient(),
								                                                            potionEffectCurrent.doesShowParticles() );
								entityLivingBase.addPotionEffect(potionEffectAugmented);
								final LastPotionEffect lastPotionEffectAugmented = new LastPotionEffect(potionEffectAugmented);
								lastPotionEffectAugmented.isFound = true;
								lastPotionEffects.add(lastPotionEffectAugmented);
							}
						}
					}
					
					// update and remove last potion effects
					lastPotionEffects.forEach(lastPotionEffect -> lastPotionEffect.duration--);
					lastPotionEffects.removeIf(lastPotionEffect -> lastPotionEffect.duration < 0 || !lastPotionEffect.isFound);
				}
			}
		}
		else if (entityLivingBase.ticksExisted % 20 == 0)
		{
			mapEntityLastPotionEffects.remove(entityLivingBase.getUniqueID());
		}
	}
	
	@Override
	public int getPowerConsumption(ItemStack stack)
	{
		return stack.getItemDamage() == META_IMMUNOSUPPRESSANT ? LibConstants.IMMUNO_CONSUMPTION : 0;
	}
	
	@SubscribeEvent
	public void handleHurt(LivingHurtEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
		if (cyberwareUserData == null) return;
		
		if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_SUBDERMAL_SPIKES)))
		{
			if ( event.getSource() instanceof EntityDamageSource
			  && !(event.getSource() instanceof EntityDamageSourceIndirect) )
			{
				ArmorClass armorClass = ArmorClass.get(entityLivingBase);
				if (armorClass == ArmorClass.HEAVY) return;
				
				Random random = entityLivingBase.getRNG();
				Entity attacker = event.getSource().getTrueSource();
				if ( EnchantmentThorns.shouldHit(3, random)
				  && attacker != null )
				{
					attacker.attackEntityFrom(DamageSource.causeThornsDamage(entityLivingBase), (float) EnchantmentThorns.getDamage(2, random));
				}
			}
		}
	}
	
	@Override
	public int getPowerProduction(ItemStack stack)
	{
		return stack.getItemDamage() == META_SOLARSKIN ? LibConstants.SOLAR_PRODUCTION : 0;
	}
	
	private static class LastPotionEffect
	{
		public final Potion potion;
		public final int amplifier;
		public int duration;
		public boolean isFound;
		
		LastPotionEffect(@Nonnull PotionEffect potionEffect)
		{
			potion = potionEffect.getPotion();
			amplifier = potionEffect.getAmplifier();
			duration = potionEffect.getDuration();
			isFound = false;
		}
		
		@Override
		public String toString() {
			return String.format("%s x %d, Duration:  %d",
			                     potion.getName(), amplifier + 1, duration );
		}
	}
}
