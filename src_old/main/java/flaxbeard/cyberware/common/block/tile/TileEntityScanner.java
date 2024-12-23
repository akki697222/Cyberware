package flaxbeard.cyberware.common.block.tile;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.oredict.OreDictionary;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemBlueprint;

public class TileEntityScanner extends TileEntity implements ITickable
{
	public class ItemStackHandlerScanner extends ItemStackHandler
	{
		public ItemStackHandlerScanner(int size)
		{
			super(size);
		}
		
		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
		{
			if (!isItemValidForSlot(slot, stack)) return stack;
			
			return super.insertItem(slot, stack, simulate);
		}
		
		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return super.extractItem(slot, amount, simulate);
		}
		
		public boolean isItemValidForSlot(int slot, ItemStack stack)
		{
			validateSlotIndex(slot);
			
			switch (slot)
			{
			case 0:
				return CyberwareAPI.canDeconstruct(stack);
				
			case 1:
				int[] idsOreDictionary = OreDictionary.getOreIDs(stack);
				int idPaper = OreDictionary.getOreID("paper");
				for (int idOreDictionary : idsOreDictionary)
				{
					if (idOreDictionary == idPaper)
					{
						return true;
					}
				}
				return false;
				
			case 2:
				return false;
			}
			return true;
		}
	}
	
	public class GuiWrapper implements IItemHandlerModifiable
	{
		private ItemStackHandlerScanner slots;
		
		public GuiWrapper(ItemStackHandlerScanner slots)
		{
			this.slots = slots;
		}
		
		@Override
		public int getSlots()
		{
			return slots.getSlots();
		}
		
		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return slots.getStackInSlot(slot);
		}
		
		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
		{
			return slots.insertItem(slot, stack, simulate);
		}
		
		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return slots.extractItem(slot, amount, simulate);
		}
		
		@Override
		public void setStackInSlot(int slot, @Nonnull ItemStack stack)
		{
			slots.setStackInSlot(slot, stack);
		}
		
		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}
		
	}
	
	public ItemStackHandlerScanner slots = new ItemStackHandlerScanner(3);
	private final RangedWrapper slotsTopSides = new RangedWrapper(slots, 0, 2);
	private final RangedWrapper slotsBottom = new RangedWrapper(slots, 2, 3);
	private final RangedWrapper slotsBottom2 = new RangedWrapper(slots, 0, 1);
	public final GuiWrapper guiSlots = new GuiWrapper(slots);
	public String customName = null;
	public int ticks = 0;
	public int ticksMove = 0;
	public int lastX = 0;
	public int x = 0;
	public int lastZ = 0;
	public int z = 0;
	
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if (facing == EnumFacing.DOWN)
			{
				if ( !slots.getStackInSlot(2).isEmpty()
				  && !slots.getStackInSlot(0).isEmpty() )
				{
					return (T) slotsBottom2;
				}
				else
				{
					return (T) slotsBottom;
				}
			}
			else
			{
				return (T) slotsTopSides;
			}
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		
		slots.deserializeNBT(tagCompound.getCompoundTag("inv"));
		
		if (tagCompound.hasKey("CustomName", 8))
		{
			customName = tagCompound.getString("CustomName");
		}
		
		ticks = tagCompound.getInteger("ticks");
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);
		
		tagCompound.setTag("inv", slots.serializeNBT());
		
		if (hasCustomName())
		{
			tagCompound.setString("CustomName", customName);
		}
		
		tagCompound.setInteger("ticks", ticks);
		
		return tagCompound;
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, @Nonnull SPacketUpdateTileEntity packetUpdateTileEntity)
	{
		NBTTagCompound tagCompound = packetUpdateTileEntity.getNbtCompound();
		readFromNBT(tagCompound);
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(pos, 0, tagCompound);
	}
	
	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag()
	{
		return writeToNBT(new NBTTagCompound());
	}
	
	public boolean isUsableByPlayer(EntityPlayer entityPlayer)
	{
		return world.getTileEntity(pos) == this
		    && entityPlayer.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}
	
	public String getName()
	{
		return hasCustomName() ? customName : "cyberware.container.scanner";
	}
	
	public boolean hasCustomName()
	{
		return customName != null && !customName.isEmpty();
	}
	
	public void setCustomInventoryName(String name)
	{
		customName = name;
	}
	
	@Override
	public ITextComponent getDisplayName()
	{
		return hasCustomName() ? new TextComponentString(getName()) : new TextComponentTranslation(getName());
	}
	
	@Override
	public void update()
	{
		ItemStack toDestroy = slots.getStackInSlot(0);
		if ( CyberwareAPI.canDeconstruct(toDestroy)
		  && toDestroy.getCount() > 0
		  && slots.getStackInSlot(2).isEmpty() )
		{
			ticks++;
			
			if ( ticksMove > ticks
			  || (ticks - ticksMove > Math.max(Math.abs(lastX - x) * 3, Math.abs(lastZ - z) * 3) + 10) )
			{
				ticksMove = ticks;
				lastX = x;
				lastZ = z;
				while (x == lastX)
				{
					x = world.rand.nextInt(11);
				}
				while (z == lastZ)
				{
					z = world.rand.nextInt(11);
				}
			}
			if (ticks > CyberwareConfig.SCANNER_TIME)
			{
				ticks = 0;
				ticksMove = 0;

				if ( !world.isRemote
				  && !slots.getStackInSlot(1).isEmpty() )
				{
					float chance = CyberwareConfig.SCANNER_CHANCE
					             + CyberwareConfig.SCANNER_CHANCE_ADDL * (slots.getStackInSlot(0).getCount() - 1);
					if (slots.getStackInSlot(0).isItemStackDamageable())
					{
						chance = 50F * (1F - (slots.getStackInSlot(0).getItemDamage() / (float) slots.getStackInSlot(0).getMaxDamage()));
					}
					chance = Math.min(chance, 50F);
					
					if (world.rand.nextFloat() < (chance / 100F))
					{
						ItemStack stackBlueprint = ItemBlueprint.getBlueprintForItem(toDestroy);
						slots.setStackInSlot(2, stackBlueprint);
						ItemStack current = slots.getStackInSlot(1);
						current.shrink(1);
						if (current.getCount() <= 0)
						{
							current = ItemStack.EMPTY;
						}
						slots.setStackInSlot(1, current);
						world.notifyBlockUpdate(pos, world.getBlockState(getPos()), world.getBlockState(getPos()), 2);
					}
				}
			}
			markDirty();
		}
		else
		{
			x = lastX = z = lastZ = 0;
			if (ticks != 0)
			{
				ticks = 0;
				markDirty();
			}
		}
	}
	
	public float getProgress()
	{
		return ticks * 1F / CyberwareConfig.SCANNER_TIME;
	}
	
}
