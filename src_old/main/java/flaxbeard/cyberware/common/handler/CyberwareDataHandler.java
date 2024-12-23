package flaxbeard.cyberware.common.handler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.ValueType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUserDataImpl;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.CyberwareContent.ZombieItem;
import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;
import flaxbeard.cyberware.common.entity.EntityCyberZombie;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.CyberwareSyncPacket;

public class CyberwareDataHandler
{
	public static final CyberwareDataHandler INSTANCE = new CyberwareDataHandler();
	public static final String KEEP_WARE_GAMERULE = "cyberware_keepCyberware";
	public static final String DROP_WARE_GAMERULE = "cyberware_dropCyberware";

	@SubscribeEvent
 	public void onEntityConstructed(EntityEvent.EntityConstructing event)
 	{
		if (event.getEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entityLivingBase = (EntityLivingBase) event.getEntity();
			entityLivingBase.getAttributeMap().registerAttribute(CyberwareAPI.TOLERANCE_ATTR);
		}
	}

	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event)
	{
		GameRules rules = event.getWorld().getGameRules();
		if(!rules.hasRule(KEEP_WARE_GAMERULE))
		{
			rules.addGameRule(KEEP_WARE_GAMERULE, Boolean.toString(CyberwareConfig.DEFAULT_KEEP), ValueType.BOOLEAN_VALUE);
		}
		if(!rules.hasRule(DROP_WARE_GAMERULE))
		{
			rules.addGameRule(DROP_WARE_GAMERULE, Boolean.toString(CyberwareConfig.DEFAULT_DROP), ValueType.BOOLEAN_VALUE);
		}
	}
	
	@SubscribeEvent
	public void attachCyberwareData(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof EntityPlayer)
		{
			event.addCapability(CyberwareUserDataImpl.Provider.NAME, new CyberwareUserDataImpl.Provider());
		}
	}
	
	@SubscribeEvent
	public void playerDeathEvent(PlayerEvent.Clone event)
	{
		EntityPlayer entityPlayerLiving = event.getEntityPlayer();
		EntityPlayer entityPlayerDead = event.getOriginal();
		if (event.isWasDeath())
		{
			if (entityPlayerLiving.world.getWorldInfo().getGameRulesInstance().getBoolean(KEEP_WARE_GAMERULE))
			{
				ICyberwareUserData cyberwareUserDataDead = CyberwareAPI.getCapabilityOrNull(entityPlayerDead);
				ICyberwareUserData cyberwareUserDataLiving = CyberwareAPI.getCapabilityOrNull(entityPlayerLiving);
				if (cyberwareUserDataDead != null && cyberwareUserDataLiving != null)
				{
					cyberwareUserDataLiving.deserializeNBT(cyberwareUserDataDead.serializeNBT());
				}
			}
		}
		else
		{
			ICyberwareUserData cyberwareUserDataDead = CyberwareAPI.getCapabilityOrNull(entityPlayerDead);
			ICyberwareUserData cyberwareUserDataLiving = CyberwareAPI.getCapabilityOrNull(entityPlayerLiving);
			if (cyberwareUserDataDead != null && cyberwareUserDataLiving != null)
			{
				cyberwareUserDataLiving.deserializeNBT(cyberwareUserDataDead.serializeNBT());
			}
		}
	}
	
	@SubscribeEvent
	public void handleCyberzombieDrops(LivingDropsEvent event)
	{
		EntityLivingBase entityLivingBase = event.getEntityLiving();
		if (entityLivingBase instanceof EntityPlayer && !entityLivingBase.world.isRemote)
		{
			EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;
			if ( ( entityPlayer.world.getWorldInfo().getGameRulesInstance().getBoolean(DROP_WARE_GAMERULE)
			    && !entityPlayer.world.getWorldInfo().getGameRulesInstance().getBoolean(KEEP_WARE_GAMERULE) )
			  || ( entityPlayer.world.getWorldInfo().getGameRulesInstance().getBoolean(KEEP_WARE_GAMERULE)
			    && shouldDropWare(event.getSource()) ))
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
				if (cyberwareUserData != null) {
					for (EnumSlot slot : EnumSlot.values())
					{
						NonNullList<ItemStack> nnlInstalled = cyberwareUserData.getInstalledCyberware(slot);
						NonNullList<ItemStack> nnlDefaults = NonNullList.create();
						for (ItemStack itemStackDefault : CyberwareConfig.getStartingItems(EnumSlot.values()[slot.ordinal()]))
						{
							nnlDefaults.add(itemStackDefault.copy());
						}
						for (ItemStack itemStackInstalled : nnlInstalled)
						{
							if (!itemStackInstalled.isEmpty())
							{
								ItemStack itemStackToDrop = itemStackInstalled.copy();
								boolean found = false;
								for (ItemStack itemStackDefault : nnlDefaults)
								{
									if (CyberwareAPI.areCyberwareStacksEqual(itemStackDefault, itemStackToDrop))
									{
										if (itemStackToDrop.getCount() > itemStackDefault.getCount())
										{
											itemStackToDrop.shrink(itemStackDefault.getCount());
										}
										else
										{
											found = true;
										}
									}
								}

								if ( !found
								  && entityPlayer.world.rand.nextFloat() < CyberwareConfig.DROP_CHANCE / 100F )
								{
									EntityItem entityItem = new EntityItem(entityPlayer.world, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, itemStackToDrop);
									event.getDrops().add(entityItem);
								}
							}
						}
					}
					cyberwareUserData.resetWare(entityPlayer);
				}
			}
		}
	}
	
	private boolean shouldDropWare(DamageSource source)
	{
		if (source == EssentialsMissingHandler.noessence) return true;
		if (source == EssentialsMissingHandler.heartless) return true;
		if (source == EssentialsMissingHandler.brainless) return true;
		if (source == EssentialsMissingHandler.nomuscles) return true;
		if (source == EssentialsMissingHandler.spineless) return true;
		
		return false;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void handleCZSpawn(LivingSpawnEvent.SpecialSpawn event)
	{
		if (!(event.getEntityLiving() instanceof EntityLiving)) {
			return;
		}
		
		EntityLiving entityLiving = (EntityLiving) event.getEntityLiving();
		
		if ( entityLiving instanceof EntityPigZombie
		  || !(entityLiving instanceof EntityZombie) )
		{
			final ResourceLocation resourceLocation = EntityList.getKey(entityLiving);
			if ( resourceLocation == null
			  || !resourceLocation.getPath().contains("ombie") )
			{
				return;
			}
		}
		
		if ( CyberwareConfig.MOBS_ENABLE_CYBER_ZOMBIES
		  && !(entityLiving instanceof EntityCyberZombie)
		  && ( !CyberwareConfig.MOBS_APPLY_DIMENSION_TO_BEACON
		    || isValidDimension(event.getWorld()) ) )
		{
			int tier = TileEntityBeacon.isInRange(entityLiving.world, entityLiving.posX, entityLiving.posY, entityLiving.posZ);
			if (tier > 0)
			{
				float chance = tier == 2 ? LibConstants.BEACON_CHANCE
				                         : tier == 1 ? LibConstants.BEACON_CHANCE_INTERNAL
				                                     : LibConstants.LARGE_BEACON_CHANCE;
				if ((event.getWorld().rand.nextFloat() < (chance / 100F))) {
					EntityCyberZombie entityCyberZombie = new EntityCyberZombie(event.getWorld());
					if (event.getWorld().rand.nextFloat() < (LibConstants.BEACON_BRUTE_CHANCE / 100F)) {
						entityCyberZombie.setBrute();
					}
					entityCyberZombie.setLocationAndAngles(entityLiving.posX, entityLiving.posY, entityLiving.posZ, entityLiving.rotationYaw, entityLiving.rotationPitch);
					entityCyberZombie.onInitialSpawn(event.getWorld().getDifficultyForLocation(entityCyberZombie.getPosition()), null);
					
					for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
						if (entityCyberZombie.getItemStackFromSlot(slot).isEmpty())
						{
							entityCyberZombie.setItemStackToSlot(slot, entityLiving.getItemStackFromSlot(slot));
							// @TODO: transfer drop chance, see Halloween in Vanilla
						}
					}
					event.getWorld().spawnEntity(entityCyberZombie);
					entityLiving.deathTime = 19;
					entityLiving.setHealth(0F);
					
					// continue processing to get a chance for clothing
					entityLiving = entityCyberZombie;
				}
			}
		}
		
		if ( CyberwareConfig.ENABLE_CLOTHES
		  && CyberwareConfig.MOBS_ADD_CLOTHES )
		{
			if ( entityLiving.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()
			  && entityLiving.world.rand.nextFloat() < LibConstants.ZOMBIE_SHADES_CHANCE / 100F )
			{
				if (entityLiving.world.rand.nextBoolean())
				{
					entityLiving.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(CyberwareContent.shades));
				}
				else
				{
					entityLiving.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(CyberwareContent.shades2));
				}
				
				entityLiving.setDropChance(EntityEquipmentSlot.HEAD, CyberwareConfig.MOBS_CLOTH_DROP_RARITY / 100F);
			}
			
			float chestRand = entityLiving.world.rand.nextFloat();
			
			if ( entityLiving.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()
			  && chestRand < LibConstants.ZOMBIE_TRENCH_CHANCE / 100F )
			{
				ItemStack stack = new ItemStack(CyberwareContent.trenchCoat);
				int rand = entityLiving.world.rand.nextInt(3);
				if (rand == 0)
				{
					CyberwareContent.trenchCoat.setColor(stack, 0x664028);
				}
				else if (rand == 1)
				{
					CyberwareContent.trenchCoat.setColor(stack, 0xEAEAEA);
				}
				
				entityLiving.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);
				
				entityLiving.setDropChance(EntityEquipmentSlot.CHEST, CyberwareConfig.MOBS_CLOTH_DROP_RARITY / 100F);
			}
			else if ( entityLiving.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()
			       && chestRand - (LibConstants.ZOMBIE_TRENCH_CHANCE / 100F) < LibConstants.ZOMBIE_BIKER_CHANCE / 100F )
			{
				ItemStack stack = new ItemStack(CyberwareContent.jacket);
				
				entityLiving.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);
				
				entityLiving.setDropChance(EntityEquipmentSlot.CHEST, CyberwareConfig.MOBS_CLOTH_DROP_RARITY / 100F);
			}
		}
	}
	
	public static void addRandomCyberware(EntityCyberZombie cyberZombie, boolean brute)
	{	
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(cyberZombie);
		if (cyberwareUserData == null) return;
		
		NonNullList<NonNullList<ItemStack>> wares = NonNullList.create();
		
		for (EnumSlot slot : EnumSlot.values())
		{
			NonNullList<ItemStack> toAdd = cyberwareUserData.getInstalledCyberware(slot);
			toAdd.removeAll(Collections.singleton(ItemStack.EMPTY));
			wares.add(toAdd);
		}
		
		// Cyberzombies get all the power
		ItemStack battery = new ItemStack(CyberwareContent.creativeBattery);
		wares.get(CyberwareContent.creativeBattery.getSlot(battery).ordinal()).add(battery);
		
		int numberOfItemsToInstall = WeightedRandom.getRandomItem(cyberZombie.world.rand, CyberwareContent.numItems).num;
		if (brute)
		{
			numberOfItemsToInstall += LibConstants.MORE_ITEMS_BRUTE;
		}
		
		List<ItemStack> installed = new ArrayList<>();
		
		List<ZombieItem> items = new ArrayList<>(CyberwareContent.zombieItems);
		for (int indexItem = 0; indexItem < numberOfItemsToInstall; indexItem++)
		{
			int tries = 0;
			ItemStack randomItem;
			ICyberware randomWare;
			
			// Ensure we get a unique item
			do
			{
				randomItem = WeightedRandom.getRandomItem(cyberZombie.world.rand, items).stack.copy();
				randomWare = CyberwareAPI.getCyberware(randomItem);
				randomItem.setCount(randomWare.installedStackSize(randomItem));
				tries++;
			}
			while (contains(wares.get(randomWare.getSlot(randomItem).ordinal()), randomItem) && tries < 10);
			
			if (tries < 10)
			{
				// Fulfill requirements
				NonNullList<NonNullList<ItemStack>> required = randomWare.required(randomItem);
				for (NonNullList<ItemStack> requiredCategory : required)
				{
					boolean found = false;
					for (ItemStack option : requiredCategory)
					{
						ICyberware optionWare = CyberwareAPI.getCyberware(option);
						option.setCount(optionWare.installedStackSize(option));
						if (contains(wares.get(optionWare.getSlot(option).ordinal()), option))
						{
							found = true;
							break;
						}
					}
					
					if (!found)
					{
						ItemStack req = requiredCategory.get(cyberZombie.world.rand.nextInt(requiredCategory.size())).copy();
						ICyberware reqWare = CyberwareAPI.getCyberware(req);
						req.setCount(reqWare.installedStackSize(req));
						wares.get(reqWare.getSlot(req).ordinal()).add(req);
						installed.add(req);
						indexItem++;
					}
				}
				wares.get(randomWare.getSlot(randomItem).ordinal()).add(randomItem);
				installed.add(randomItem);
			}
		}
		
		/*
		Cyberware.logger.info(String.format("numberOfItemsToInstall is %s",
		                                    numberOfItemsToInstall));
		for (ItemStack stack : installed)
		{
			numberOfItemsToInstall(String.format("%d x %s",
			                                     stack.getCount(), stack.getTranslationKey() ));
		}
		*/
		
		for (EnumSlot slot : EnumSlot.values())
		{
			cyberwareUserData.setInstalledCyberware(cyberZombie, slot, wares.get(slot.ordinal()));
		}
		cyberwareUserData.updateCapacity();
		
		cyberZombie.setHealth(cyberZombie.getMaxHealth());
		cyberZombie.hasRandomWare = true;
		
		CyberwareAPI.updateData(cyberZombie);
	}
	
	private static boolean contains(NonNullList<ItemStack> nnlHaystack, ItemStack needle)
	{
		for (ItemStack check : nnlHaystack)
		{			
			if ( !check.isEmpty()
			  && !needle.isEmpty()
			  && check.getItem() == needle.getItem()
			  && check.getItemDamage() == needle.getItemDamage() )
			{
				return true;
			}
		}
		return false;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPotentialSpawns(@Nonnull WorldEvent.PotentialSpawns event)
	{
		if (event.getType() != EnumCreatureType.MONSTER) return;
		
		if (!CyberwareConfig.MOBS_APPLY_DIMENSION_TO_SPAWNING) return;
		
		if (isValidDimension(event.getWorld())) return;
		
		List<SpawnListEntry> spawnListEntriesToRemove = new ArrayList<>(4);
		for (SpawnListEntry spawnListEntry : event.getList())
		{
			if (spawnListEntry.entityClass.equals(EntityCyberZombie.class))
			{
				spawnListEntriesToRemove.add(spawnListEntry);
			}
		}
		event.getList().removeAll(spawnListEntriesToRemove);
	}
	
	public boolean isValidDimension(@Nonnull World world)
	{
		boolean isListed = CyberwareConfig.MOBS_DIMENSION_IDS.contains(world.provider.getDimension());
		return (CyberwareConfig.MOBS_IS_DIMENSION_BLACKLIST && !isListed)
		    || (!CyberwareConfig.MOBS_IS_DIMENSION_BLACKLIST && isListed);
	}
	
	@SubscribeEvent
	public void syncCyberwareData(EntityJoinWorldEvent event)
	{
		if (!event.getWorld().isRemote)
		{
			Entity entity = event.getEntity();
			if (entity instanceof EntityPlayer)
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entity);
				if (cyberwareUserData != null)
				{
					NBTTagCompound tagCompound = cyberwareUserData.serializeNBT();
					CyberwarePacketHandler.INSTANCE.sendTo(new CyberwareSyncPacket(tagCompound, entity.getEntityId()), (EntityPlayerMP) entity);
				}
			}
		}
	}

	@SubscribeEvent
	public void startTrackingEvent(StartTracking event)
	{			
		EntityPlayer entityPlayer = event.getEntityPlayer();
		Entity entityTarget = event.getTarget();
		
		if (!entityTarget.world.isRemote)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityTarget);
			if (cyberwareUserData != null)
			{
				NBTTagCompound tagCompound = cyberwareUserData.serializeNBT();
				CyberwarePacketHandler.INSTANCE.sendTo(new CyberwareSyncPacket(tagCompound, entityTarget.getEntityId()), (EntityPlayerMP) entityPlayer);
			}
		}
	}

}
