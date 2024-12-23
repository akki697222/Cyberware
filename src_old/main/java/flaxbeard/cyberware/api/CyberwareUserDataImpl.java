package flaxbeard.cyberware.api;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.item.HotkeyHelper;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.api.item.ICyberware.ISidedLimb.EnumSide;
import flaxbeard.cyberware.api.item.IHudjack;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class CyberwareUserDataImpl implements ICyberwareUserData
{
	public static final IStorage<ICyberwareUserData> STORAGE = new CyberwareUserDataStorage();

	private NonNullList<NonNullList<ItemStack>> cyberwaresBySlot = NonNullList.create();
	private boolean[] missingEssentials = new boolean[EnumSlot.values().length * 2];
	
	private int power_stored = 0;
	private int power_production = 0;
 	private int power_lastProduction = 0;
 	private int power_consumption = 0;
 	private int power_lastConsumption = 0;
	private int power_capacity = 0;
	private Map<ItemStack, Integer> power_buffer = new HashMap<>();
	private Map<ItemStack, Integer> power_lastBuffer = new HashMap<>();
	private NonNullList<ItemStack> nnlPowerOutages = NonNullList.create();
	private List<Integer> ticksPowerOutages = new ArrayList<>();
	private int missingEssence = 0;
	private NonNullList<ItemStack> specialBatteries = NonNullList.create();
	private NonNullList<ItemStack> activeItems = NonNullList.create();
	private NonNullList<ItemStack> hudjackItems = NonNullList.create();
	private Map<Integer, ItemStack> hotkeys = new HashMap<>();
	private NBTTagCompound hudData;
	private boolean hasOpenedRadialMenu = false;
	
	private int hudColor = 0x00FFFF;
	private float[] hudColorFloat = new float[] { 0.0F, 1.0F, 1.0F };
	
	public CyberwareUserDataImpl()
	{
		hudData = new NBTTagCompound();
		for (EnumSlot slot : EnumSlot.values())
		{
			NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
			for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
			{
				nnlCyberwaresInSlot.add(ItemStack.EMPTY);
			}
			cyberwaresBySlot.add(nnlCyberwaresInSlot);
		}
		resetWare(null);
	}
	
	@Override
	public void resetWare(EntityLivingBase entityLivingBase)
	{
		for (NonNullList<ItemStack> nnlCyberwaresInSlot : cyberwaresBySlot)
		{
			for (ItemStack item : nnlCyberwaresInSlot)
			{
				if (CyberwareAPI.isCyberware(item))
				{
					CyberwareAPI.getCyberware(item).onRemoved(entityLivingBase, item);
				}
			}
		}
		missingEssence = 0;
		for (EnumSlot slot : EnumSlot.values())
		{
			NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
			NonNullList<ItemStack> startItems = CyberwareConfig.getStartingItems(slot);
			for (ItemStack startItem : startItems)
			{
				nnlCyberwaresInSlot.add(startItem.copy());
			}
			cyberwaresBySlot.set(slot.ordinal(), nnlCyberwaresInSlot);
		}
		missingEssentials = new boolean[EnumSlot.values().length * 2];
		updateCapacity();
	}
	
	@Override
	public List<ItemStack> getPowerOutages()
	{
		return nnlPowerOutages;
	}
	
	@Override
	public List<Integer> getPowerOutageTimes()
	{
		return ticksPowerOutages;
	}
	
	@Override
	public int getCapacity()
	{
		int specialCap = 0;
		for (ItemStack item : specialBatteries)
		{
			ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(item);
			specialCap += battery.getCapacity(item);
		}
		return power_capacity + specialCap;
	}
	
	@Override
	public int getStoredPower()
	{
		int specialStored = 0;
		for (ItemStack item : specialBatteries)
		{
			ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(item);
			specialStored += battery.getStoredEnergy(item);
		}
		return power_stored + specialStored;
	}
	
	@Override
	public float getPercentFull()
	{
		if (getCapacity() == 0) return -1F;
		return getStoredPower() / (float) getCapacity();
	}
	
	@Override
	public boolean isAtCapacity(ItemStack stack)
	{
		return isAtCapacity(stack, 0);
	}
	
	@Override
	public boolean isAtCapacity(ItemStack stack, int buffer)
	{
		// buffer = Math.min(power_capacity - 1, buffer); TODO
		int leftOverSpaceNormal = power_capacity - power_stored;

		if (leftOverSpaceNormal > buffer) return false;
		
		int leftOverSpaceSpecial = 0;

		for (ItemStack batteryStack : specialBatteries)
		{
			ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(batteryStack);
			int spaceInThisSpecial = battery.add(batteryStack, stack, buffer + 1, true);
			leftOverSpaceSpecial += spaceInThisSpecial;
			
			if (leftOverSpaceNormal + leftOverSpaceSpecial > buffer) return false;
		}
		
		return true;
	}
	
	@Override
	public void addPower(int amount, ItemStack inputter)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException("Amount must be positive!");
		}
		
		ItemStack stack = ItemStack.EMPTY;
		if (!inputter.isEmpty())
		{
			if ( inputter.hasTagCompound()
			  || inputter.getCount() != 1 )
			{
				stack = new ItemStack(inputter.getItem(), 1, inputter.getItemDamage());
			}
			else
			{
				stack = inputter;
			}
		}
		
		Integer amountExisting = power_buffer.get(stack);
		power_buffer.put(stack, amount + (amountExisting == null ? 0 : amountExisting));
		
		power_production += amount;
	}
	
	private boolean canGiveOut = true;
	
	@Override
	public boolean usePower(ItemStack stack, int amount)
	{
		return usePower(stack, amount, true);
	}
	
	private int ComputeSum(@Nonnull Map<ItemStack, Integer> map)
	{
		int total = 0;
		for (ItemStack key : map.keySet())
		{
			total += map.get(key);
		}
		return total;
	}
	
	private void subtractFromBufferLast(int amount)
	{
		for (ItemStack key : power_lastBuffer.keySet())
		{
			int get = power_lastBuffer.get(key);
			int amountToSubtract = Math.min(get, amount);
			amount -= amountToSubtract;
			power_lastBuffer.put(key, get - amountToSubtract);
			if (amount <= 0) break;
		}
	}

	@Override
	public boolean usePower(ItemStack stack, int amount, boolean isPassive)
	{
		if (isImmune) return true;
		
		if (!canGiveOut)
		{
			if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
			{
				setOutOfPower(stack);
			}
			return false;
		}
		
		power_consumption += amount;
		
		int sumPowerBufferLast = ComputeSum(power_lastBuffer);
		int amountAvailable = power_stored + sumPowerBufferLast;
		
		int amountAvailableSpecial = 0;
		if (amountAvailable < amount)
		{
			int amountMissing = amount - amountAvailable;

			for (ItemStack batteryStack : specialBatteries)
			{
				ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(batteryStack);
				int extract = battery.extract(batteryStack, amountMissing, true);
				
				amountMissing -= extract;
				amountAvailableSpecial += extract;
				
				if (amountMissing <= 0) break;
			}
			
			if (amountAvailableSpecial + amountAvailable >= amount)
			{
				amountMissing = amount - amountAvailable;
				
				for (ItemStack batteryStack : specialBatteries)
				{
					ISpecialBattery battery = (ISpecialBattery) CyberwareAPI.getCyberware(batteryStack);
					int extract = battery.extract(batteryStack, amountMissing, false);
					
					amountMissing -= extract;
					
					if (amountMissing <= 0) break;
				}
				
				amount -= amountAvailableSpecial;
			}
		}
		
		if (amountAvailable < amount)
		{
			if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
			{
				setOutOfPower(stack);
			}
			if (isPassive)
			{
				canGiveOut = false;
			}
			return false;
		}
		
		int leftAfterBuffer = Math.max(0, amount - sumPowerBufferLast);
		subtractFromBufferLast(amount);
		power_stored -= leftAfterBuffer;
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public void setOutOfPower(ItemStack stack)
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		if ( entityPlayer != null
		  && !stack.isEmpty() )
		{
			int indexFound = -1;
			int indexLoop = 0;
			for (ItemStack stackExisting : nnlPowerOutages)
			{
				if ( !stackExisting.isEmpty()
				  && stackExisting.getItem() == stack.getItem()
				  && stackExisting.getItemDamage() == stack.getItemDamage() )
				{
					indexFound = indexLoop;
					break;
				}
				indexLoop++;
			}
			if (indexFound != -1)
			{
				nnlPowerOutages.remove(indexFound);
				ticksPowerOutages.remove(indexFound);
			}
			nnlPowerOutages.add(stack);
			ticksPowerOutages.add(entityPlayer.ticksExisted);
			if (nnlPowerOutages.size() >= 8)
			{
				nnlPowerOutages.remove(0);
				ticksPowerOutages.remove(0);
			}
		}
	}
	
	@Override
	public NonNullList<ItemStack> getInstalledCyberware(EnumSlot slot)
	{
		return cyberwaresBySlot.get(slot.ordinal());
	}
	
	@Override
	public boolean hasEssential(EnumSlot slot)
	{
		return !missingEssentials[slot.ordinal() * 2];
	}
	
	@Override
	public boolean hasEssential(EnumSlot slot, EnumSide side)
	{
		return !missingEssentials[slot.ordinal() * 2 + (side == EnumSide.LEFT ? 0 : 1)];
	}
	
	@Override
	public void setHasEssential(EnumSlot slot, boolean hasLeft, boolean hasRight)
	{
		missingEssentials[slot.ordinal() * 2    ] = !hasLeft;
		missingEssentials[slot.ordinal() * 2 + 1] = !hasRight;
	}

	@Override
	public void setInstalledCyberware(EntityLivingBase entityLivingBase, EnumSlot slot, @Nonnull List<ItemStack> cyberwaresToInstall)
	{
		while (cyberwaresToInstall.size() > LibConstants.WARE_PER_SLOT)
		{
			cyberwaresToInstall.remove(cyberwaresToInstall.size() - 1);
		}
		while (cyberwaresToInstall.size() < LibConstants.WARE_PER_SLOT)
		{
			cyberwaresToInstall.add(ItemStack.EMPTY);
		}
		setInstalledCyberware(entityLivingBase, slot, NNLUtil.fromArray(cyberwaresToInstall.toArray(new ItemStack[0])));
	}
	
	@Override
	public void updateCapacity()
	{
		power_capacity = 0;
		specialBatteries = NonNullList.create();
		activeItems = NonNullList.create();
		hudjackItems = NonNullList.create();
		hotkeys = new HashMap<>();
		
		for (EnumSlot slot : EnumSlot.values())
		{
			for (ItemStack itemStackCyberware : getInstalledCyberware(slot))
			{
				if (CyberwareAPI.isCyberware(itemStackCyberware))
				{
					ICyberware cyberware = CyberwareAPI.getCyberware(itemStackCyberware);
					
					if ( cyberware instanceof IMenuItem
					  && ((IMenuItem) cyberware).hasMenu(itemStackCyberware) )
					{
						activeItems.add(itemStackCyberware);
						
						int hotkey = HotkeyHelper.getHotkey(itemStackCyberware);
						if (hotkey != -1)
						{
							hotkeys.put(hotkey, itemStackCyberware);
						}
					}
					
					if (cyberware instanceof IHudjack)
					{
						hudjackItems.add(itemStackCyberware);
					}
					
					if (cyberware instanceof ISpecialBattery)
					{
						specialBatteries.add(itemStackCyberware);
					}
					else
					{
						power_capacity += cyberware.getCapacity(itemStackCyberware);
					}
				}
			}
		}
		
		power_stored = Math.min(power_stored, power_capacity);
	}
	
	@Override
	public void setInstalledCyberware(EntityLivingBase entityLivingBase, EnumSlot slot, NonNullList<ItemStack> cyberwaresToInstall)
	{
		if (cyberwaresToInstall.size() != cyberwaresBySlot.get(slot.ordinal()).size())
		{
			Cyberware.logger.error(String.format("Invalid number of cyberware to install: found %d, expecting %d",
			                                     cyberwaresToInstall.size(), cyberwaresBySlot.get(slot.ordinal()).size() ));
		}
		NonNullList<ItemStack> cyberwaresInstalled = cyberwaresBySlot.get(slot.ordinal());
		
		if (entityLivingBase != null)
		{
			for (ItemStack itemStackInstalled : cyberwaresInstalled)
			{
				if (!CyberwareAPI.isCyberware(itemStackInstalled)) continue;
			
				boolean found = false;
				for (ItemStack itemStackToInstall : cyberwaresToInstall)
				{
					if ( CyberwareAPI.areCyberwareStacksEqual(itemStackToInstall, itemStackInstalled)
					  && itemStackToInstall.getCount() == itemStackInstalled.getCount() )
					{
						found = true;
						break;
					}
				}
				
				if (!found)
				{
					CyberwareAPI.getCyberware(itemStackInstalled).onRemoved(entityLivingBase, itemStackInstalled);
				}
			}
			
			for (ItemStack itemStackToInstall : cyberwaresToInstall)
			{
				if (!CyberwareAPI.isCyberware(itemStackToInstall)) continue;
			
				boolean found = false;
				for (ItemStack oldWare : cyberwaresInstalled)
				{
					if ( CyberwareAPI.areCyberwareStacksEqual(itemStackToInstall, oldWare)
					  && itemStackToInstall.getCount() == oldWare.getCount() )
					{
						found = true;
						break;
					}
				}
				
				if (!found)
				{
					CyberwareAPI.getCyberware(itemStackToInstall).onAdded(entityLivingBase, itemStackToInstall);
				}
			}
		}
		
		cyberwaresBySlot.set(slot.ordinal(), cyberwaresToInstall);
	}
	
	@Override
	public boolean isCyberwareInstalled(ItemStack cyberware)
	{
		return getCyberwareRank(cyberware) > 0;
	}
	
	@Override
	public int getCyberwareRank(ItemStack cyberwareTemplate)
	{
		ItemStack cyberwareFound = getCyberware(cyberwareTemplate);
		
		if (!cyberwareFound.isEmpty())
		{
			return cyberwareFound.getCount();
		}
		
		return 0;
	}
	
	@Override
	public ItemStack getCyberware(ItemStack cyberware)
	{
		for (ItemStack itemStack : getInstalledCyberware(CyberwareAPI.getCyberware(cyberware).getSlot(cyberware)))
		{
			if ( !itemStack.isEmpty()
			  && itemStack.getItem() == cyberware.getItem()
			  && itemStack.getItemDamage() == cyberware.getItemDamage() )
			{
				return itemStack;
			}
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		NBTTagList listSlots = new NBTTagList();
		
		for (EnumSlot slot : EnumSlot.values())
		{
			NBTTagList listCyberwares = new NBTTagList();
			for (ItemStack cyberware : getInstalledCyberware(slot))
			{
				NBTTagCompound tagCompoundCyberware = new NBTTagCompound();
				if (!cyberware.isEmpty())
				{
					cyberware.writeToNBT(tagCompoundCyberware);
				}
				listCyberwares.appendTag(tagCompoundCyberware);
			}
			listSlots.appendTag(listCyberwares);
		}
		
		tagCompound.setTag("cyberware", listSlots);
		
		NBTTagList listEssentials = new NBTTagList();
		for (boolean missingEssential : missingEssentials)
		{
			listEssentials.appendTag(new NBTTagByte((byte) (missingEssential ? 1 : 0)));
		}
		tagCompound.setTag("discard", listEssentials);
		tagCompound.setTag("powerBuffer", serializeMap(power_buffer));
		tagCompound.setTag("powerBufferLast", serializeMap(power_lastBuffer));
		tagCompound.setInteger("powerCap", power_capacity);
		tagCompound.setInteger("storedPower", power_stored);
		tagCompound.setInteger("missingEssence", missingEssence);
		tagCompound.setTag("hud", hudData);
		tagCompound.setInteger("color", hudColor);
		tagCompound.setBoolean("hasOpenedRadialMenu", hasOpenedRadialMenu);
		return tagCompound;
	}
	
	private NBTTagList serializeMap(@Nonnull Map<ItemStack, Integer> map)
	{
		NBTTagList listMap = new NBTTagList();
		
		for (ItemStack stack : map.keySet())
		{
			NBTTagCompound tagCompoundEntry = new NBTTagCompound();
			tagCompoundEntry.setBoolean("null", stack.isEmpty());
			if (!stack.isEmpty())
			{
				NBTTagCompound tagCompoundItem = new NBTTagCompound();
				stack.writeToNBT(tagCompoundItem);
				tagCompoundEntry.setTag("item", tagCompoundItem);
			}
			tagCompoundEntry.setInteger("value", map.get(stack));

			listMap.appendTag(tagCompoundEntry);
		}
		
		return listMap;
	}
	
	private Map<ItemStack, Integer> deserializeMap(@Nonnull NBTTagList listMap)
	{
		Map<ItemStack, Integer> map = new HashMap<>();
		for (int index = 0; index < listMap.tagCount(); index++)
		{
			NBTTagCompound tagCompoundEntry = listMap.getCompoundTagAt(index);
			boolean isNull = tagCompoundEntry.getBoolean("null");
			ItemStack stack = ItemStack.EMPTY;
			if (!isNull)
			{
				stack = new ItemStack(tagCompoundEntry.getCompoundTag("item"));
			}
			
			map.put(stack, tagCompoundEntry.getInteger("value"));
		}
		
		return map;
	}

	@Override
	public void deserializeNBT(NBTTagCompound tagCompound)
	{
		power_buffer = deserializeMap(tagCompound.getTagList("powerBuffer", NBT.TAG_COMPOUND));
		power_capacity = tagCompound.getInteger("powerCap");
		power_lastBuffer = deserializeMap((NBTTagList) tagCompound.getTag("powerBufferLast"));
		
		power_stored = tagCompound.getInteger("storedPower");
		if (tagCompound.hasKey("essence"))
		{
			missingEssence = getMaxEssence() - tagCompound.getInteger("essence");
		}
		else
		{
			missingEssence = tagCompound.getInteger("missingEssence");
		}
		hudData = tagCompound.getCompoundTag("hud");
		hasOpenedRadialMenu = tagCompound.getBoolean("hasOpenedRadialMenu");
		NBTTagList listEssentials = (NBTTagList) tagCompound.getTag("discard");
		for (int indexEssential = 0; indexEssential < listEssentials.tagCount(); indexEssential++)
		{
			missingEssentials[indexEssential] = ((NBTTagByte) listEssentials.get(indexEssential)).getByte() > 0;
		}
		
		NBTTagList listSlots = (NBTTagList) tagCompound.getTag("cyberware");
		for (int indexBodySlot = 0; indexBodySlot < listSlots.tagCount(); indexBodySlot++)
		{
			EnumSlot slot = EnumSlot.values()[indexBodySlot];
			
			NBTTagList listCyberwares = (NBTTagList) listSlots.get(indexBodySlot);
			NonNullList<ItemStack> nnlCyberwaresOfType = NonNullList.create();
			for (int indexInventorySlot = 0; indexInventorySlot < LibConstants.WARE_PER_SLOT; indexInventorySlot++){
				nnlCyberwaresOfType.add(ItemStack.EMPTY);
			}

			int countInventorySlots = Math.min(listCyberwares.tagCount(), nnlCyberwaresOfType.size());
			for (int indexInventorySlot = 0; indexInventorySlot < countInventorySlots; indexInventorySlot++)
			{
				nnlCyberwaresOfType.set(indexInventorySlot, new ItemStack(listCyberwares.getCompoundTagAt(indexInventorySlot)));
			}
			
			setInstalledCyberware(null, slot, nnlCyberwaresOfType);
		}
		
		int color = 0x00FFFF;

		if (tagCompound.hasKey("color"))
		{
			color = tagCompound.getInteger("color");
		}
		setHudColor(color);
		
		updateCapacity();
	}
	
	private static class CyberwareUserDataStorage implements IStorage<ICyberwareUserData>
	{
		@Override
		public NBTBase writeNBT(Capability<ICyberwareUserData> capability, ICyberwareUserData cyberwareUserData, EnumFacing side)
		{
			return cyberwareUserData.serializeNBT();
		}

		@Override
		public void readNBT(Capability<ICyberwareUserData> capability, ICyberwareUserData cyberwareUserData, EnumFacing side, NBTBase nbt)
		{
			if (nbt instanceof NBTTagCompound)
			{
				cyberwareUserData.deserializeNBT((NBTTagCompound) nbt);
			}
			else
			{
				throw new IllegalStateException("Cyberware NBT should be a NBTTagCompound!");
			}
		}
	}
	
	public static class Provider implements ICapabilitySerializable<NBTTagCompound>
	{
		public static final ResourceLocation NAME = new ResourceLocation(Cyberware.MODID, "cyberware");
		
		private final ICyberwareUserData cyberwareUserData = new CyberwareUserDataImpl();

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
		{
			return capability == CyberwareAPI.CYBERWARE_CAPABILITY;
		}

		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
		{
			if (capability == CyberwareAPI.CYBERWARE_CAPABILITY)
			{
				return CyberwareAPI.CYBERWARE_CAPABILITY.cast(cyberwareUserData);
			}
			
			return null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			return cyberwareUserData.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound tagCompound)
		{
			cyberwareUserData.deserializeNBT(tagCompound);
		}
	}
	
	private void storePower(Map<ItemStack, Integer> map)
	{
		for (ItemStack itemStackSpecialBattery : specialBatteries)
		{
			ISpecialBattery specialBattery = (ISpecialBattery) CyberwareAPI.getCyberware(itemStackSpecialBattery);
			for (Map.Entry<ItemStack, Integer> entryBuffer : map.entrySet())
			{
				int amountBuffer = entryBuffer.getValue();
				int amountTaken = specialBattery.add(itemStackSpecialBattery, entryBuffer.getKey(), amountBuffer, false);
				entryBuffer.setValue(amountBuffer - amountTaken);
			}
		}
		power_stored = Math.min(power_capacity, power_stored + ComputeSum(map));
	}

	@Override
	public void resetBuffer()
	{
		canGiveOut = true;
		storePower(power_lastBuffer);
		power_lastBuffer = power_buffer;
		power_buffer = new HashMap<>(power_buffer.size());
		isImmune = false;
		
		power_lastConsumption = power_consumption;
		power_lastProduction = power_production;
		power_production = 0;
		power_consumption = 0;
	}

	@Override
	public void setImmune()
	{
		isImmune = true;
	}
	
	private boolean isImmune = false;

	@Override
	@Deprecated
	public int getEssence()
	{
		return getMaxEssence() - missingEssence;
	}
	
	@Override
	@Deprecated
	public int getMaxEssence()
	{
		return CyberwareConfig.ESSENCE;
	}

	@Override
	@Deprecated
	public void setEssence(int essence)
	{
		missingEssence = getMaxEssence() - essence;
	}

	@Override
	public int getMaxTolerance(@Nonnull EntityLivingBase entityLivingBase)
	{
		return (int) entityLivingBase.getAttributeMap().getAttributeInstance(CyberwareAPI.TOLERANCE_ATTR).getAttributeValue();
	}

	@Override
	public int getTolerance(@Nonnull EntityLivingBase entityLivingBase)
	{
		return getMaxTolerance(entityLivingBase) - missingEssence;
	}

	@Override
	public void setTolerance(@Nonnull EntityLivingBase entityLivingBase, int amount)
	{
		missingEssence = getMaxTolerance(entityLivingBase) - amount;
	}



	@Override
	public int getNumActiveItems()
	{
		return activeItems.size();
	}

	@Override
	public List<ItemStack> getActiveItems()
	{
		return activeItems;
	}
	
	@Override
	public List<ItemStack> getHudjackItems()
	{
		return hudjackItems;
	}

	@Override
	public void removeHotkey(int i)
	{
		hotkeys.remove(i);
	}

	@Override
	public void addHotkey(int i, ItemStack stack)
	{
		hotkeys.put(i, stack);
	}
	
	@Override
	public ItemStack getHotkey(int i)
	{
		if (!hotkeys.containsKey(i))
		{
			return ItemStack.EMPTY;
		}
		return hotkeys.get(i);
	}

	@Override
	public Iterable<Integer> getHotkeys()
	{
		return hotkeys.keySet();
	}

	@Override
	public void setHudData(NBTTagCompound tagCompound)
	{
		hudData = tagCompound;
	}

	@Override
	public NBTTagCompound getHudData()
	{
		return hudData;
	}
	
	@Override
	public boolean hasOpenedRadialMenu()
	{
		return hasOpenedRadialMenu;
	}
	
	@Override
	public void setOpenedRadialMenu(boolean hasOpenedRadialMenu)
	{
		this.hasOpenedRadialMenu = hasOpenedRadialMenu;
	}

	@Override
	public void setHudColor(int hexVal)
	{
		float r = ((hexVal >> 16) & 0x0000FF) / 255F;
		float g = ((hexVal >> 8) & 0x0000FF) / 255F;
		float b = ((hexVal) & 0x0000FF) / 255F;
		setHudColor(new float[] { r, g, b });
	}

	@Override
	public int getHudColorHex()
	{
		return hudColor;
	}

	@Override
	public void setHudColor(float[] color)
	{
		hudColorFloat = color;
		int ri = Math.round(color[0] * 255);
		int gi = Math.round(color[1] * 255);
		int bi = Math.round(color[2] * 255);
		
		int rp = (ri << 16) & 0xFF0000;
		int gp = (gi << 8) & 0x00FF00;
		int bp = (bi) & 0x0000FF;
		hudColor = rp | gp | bp;
	}

	@Override
	public float[] getHudColor()
	{
		return hudColorFloat;
	}

	@Override
	public int getProduction()
	{
		return power_lastProduction;
	}

	@Override
 	public int getConsumption()
 	{
		return power_lastConsumption;
	}
}
