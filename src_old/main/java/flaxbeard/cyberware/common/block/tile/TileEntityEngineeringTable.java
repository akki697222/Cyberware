package flaxbeard.cyberware.common.block.tile;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.oredict.OreDictionary;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.IBlueprint;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.misc.SpecificWrapper;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.ScannerSmashPacket;

public class TileEntityEngineeringTable extends TileEntity implements ITickable
{
	public static class TileEntityEngineeringDummy extends TileEntity
	{
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing)
		{
			TileEntity above = world.getTileEntity(pos.add(0, 1, 0));
			if (above != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				return above.hasCapability(capability, facing);
			}
			return super.hasCapability(capability, facing);
		}
		

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing)
		{
			TileEntity above = world.getTileEntity(pos.add(0, 1, 0));
			if (above != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				return above.getCapability(capability, facing);
			}
			return super.getCapability(capability, facing);
		}
	}
	
	public class ItemStackHandlerEngineering extends ItemStackHandler
	{
		public boolean overrideExtract = false;
		private TileEntityEngineeringTable table;
		
		public ItemStackHandlerEngineering(TileEntityEngineeringTable table, int i)
		{
			super(i);
			this.table = table;
		}
		
		@Override
	    public void setStackInSlot(int slot, ItemStack stack)
	    {
			boolean check = false;
			if (slot == 0 && this.getStackInSlot(0).isEmpty() && !world.isRemote)
			{
				check = true;
			}
			
			super.setStackInSlot(slot, stack);
			
			if (check)
			{
				table.world.setBlockState(getPos(), table.world.getBlockState(getPos()), 2);
				table.world.notifyBlockUpdate(pos, table.world.getBlockState(getPos()), table.world.getBlockState(getPos()), 2);
			}
			
			if (slot >= 2 && slot <= 8)
			{
				table.updateRecipe();
			}
	    }
		
		@Nonnull
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if (!isItemValidForSlot(slot, stack)) return stack;
			
			boolean check = false;
			if (slot == 0 && this.getStackInSlot(0).isEmpty() && !simulate && !world.isRemote)
			{
				check = true;
			}
			
			ItemStack result = super.insertItem(slot, stack, simulate);
			
			if (check)
			{
				table.world.setBlockState(getPos(), table.world.getBlockState(getPos()), 2);
				table.world.notifyBlockUpdate(pos, table.world.getBlockState(getPos()), table.world.getBlockState(getPos()), 2);
			}

			if (slot >= 2 && slot <= 8 && !simulate)
			{
				table.updateRecipe();
			}
			return result;
		}
		
		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if (!canRemoveItem(slot)) return ItemStack.EMPTY;
			

			ItemStack result = super.extractItem(slot, amount, simulate);
			if (slot == 9 && !result.isEmpty() && !simulate)
			{

				table.subtractResources();
				
			}
			if (slot >= 2 && slot <= 7 && !simulate)
			{
				table.updateRecipe();
			}

			return result;
		}

		public boolean canRemoveItem(int slot)
		{
			if (overrideExtract) return true;
			if (!getStackInSlot(8).isEmpty() && (slot >= 2 && slot <= 7)) return false;
			if (slot == 1 || slot == 8) return false;
			return true;
		}

		public boolean isItemValidForSlot(int slot, ItemStack stack)
		{
			switch (slot)
			{
				case 0:
					return CyberwareAPI.canDeconstruct(stack);
				case 1:
					int[] ids = OreDictionary.getOreIDs(stack);
					int paperId = OreDictionary.getOreID("paper");
					for (int id : ids)
					{
						if (id == paperId)
						{
							return true;
						}
					}
					return false;
				case 8:
					return !stack.isEmpty() && stack.getItem() instanceof IBlueprint;
				case 9:
					return false;
				default:
					return overrideExtract || !CyberwareAPI.canDeconstruct(stack);
			}
		}
	}
	
	public class GuiWrapper implements IItemHandlerModifiable
	{
		private ItemStackHandlerEngineering slots;

		public GuiWrapper(ItemStackHandlerEngineering slots)
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
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			slots.overrideExtract = true;
			ItemStack res = slots.insertItem(slot, stack, simulate);
			slots.overrideExtract = false;
			return res;
		}
		
		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			slots.overrideExtract = true;
			ItemStack ret = slots.extractItem(slot, amount, simulate);
			slots.overrideExtract = false;
			return ret;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
			slots.overrideExtract = true;
			slots.setStackInSlot(slot, stack);
			slots.overrideExtract = false;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}
		
	}
	
	public ItemStackHandlerEngineering slots = new ItemStackHandlerEngineering(this, 10);
	private final RangedWrapper slotsTopSides = new RangedWrapper(slots, 0, 7);
	private final SpecificWrapper slotsBottom = new SpecificWrapper(slots, 2, 3, 4, 5, 6, 7, 9);
	public final GuiWrapper guiSlots = new GuiWrapper(slots);
	public String customName = null;
	public float clickedTime = -100F;
	private int time;
	public HashMap<String, BlockPos> lastPlayerArchive = new HashMap<>();
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if (facing == EnumFacing.DOWN)
			{
				return (T) slotsBottom;
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
		
		this.time = tagCompound.getInteger("time");
		
		lastPlayerArchive = new HashMap<>();
		NBTTagList list = (NBTTagList) tagCompound.getTag("playerArchive");
		for (int indexArchive = 0; indexArchive < list.tagCount(); indexArchive++)
		{
			NBTTagCompound tagCompoundAt = list.getCompoundTagAt(indexArchive);
			String name = tagCompoundAt.getString("name");
			int x = tagCompoundAt.getInteger("x");
			int y = tagCompoundAt.getInteger("y");
			int z = tagCompoundAt.getInteger("z");
			BlockPos pos = new BlockPos(x, y, z);
			lastPlayerArchive.put(name, pos);
		}

	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);
		
		tagCompound.setTag("inv", this.slots.serializeNBT());
		
		if (this.hasCustomName())
		{
			tagCompound.setString("CustomName", customName);
		}
		
		tagCompound.setInteger("time", time);
		NBTTagList list = new NBTTagList();
		for (String name : this.lastPlayerArchive.keySet())
		{
			NBTTagCompound entry = new NBTTagCompound();
			entry.setString("name", name);
			BlockPos pos = lastPlayerArchive.get(name);
			entry.setInteger("x", pos.getX());
			entry.setInteger("y", pos.getY());
			entry.setInteger("z", pos.getZ());
			list.appendTag(entry);
		}
		tagCompound.setTag("playerArchive", list);
		return tagCompound;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		NBTTagCompound data = pkt.getNbtCompound();
		this.readFromNBT(data);
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound data = new NBTTagCompound();
		this.writeToNBT(data);
		return new SPacketUpdateTileEntity(pos, 0, data);
	}
	
	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag()
	{
		return writeToNBT(new NBTTagCompound());
	}
	
	public boolean isUsableByPlayer(EntityPlayer entityPlayer)
	{
		return this.world.getTileEntity(pos) == this
		    && entityPlayer.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}
	
	public String getName()
	{
		return this.hasCustomName() ? customName : "cyberware.container.engineering";
	}

	public boolean hasCustomName()
	{
		return this.customName != null && !this.customName.isEmpty();
	}

	public void setCustomInventoryName(String name)
	{
		this.customName = name;
	}
	
	@Override
	public ITextComponent getDisplayName()
	{
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
	}

	public void updateRecipe()
	{
		ItemStack blueprintStack = slots.getStackInSlot(8);
		if (!blueprintStack.isEmpty() && blueprintStack.getItem() instanceof IBlueprint)
		{
			IBlueprint blueprint = (IBlueprint) blueprintStack.getItem();
			NonNullList<ItemStack> toCheck = NonNullList.create();
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				toCheck.add(slots.getStackInSlot(indexSlot + 2).copy());
			}
			ItemStack result = blueprint.getResult(blueprintStack, toCheck).copy();
			if (!result.isEmpty())
			{
				result.setCount(1);
			}
			this.slots.setStackInSlot(9, result);
		}
		else
		{
			this.slots.setStackInSlot(9, ItemStack.EMPTY);
		}
	}
	
	public void subtractResources()
	{
		ItemStack blueprintStack = slots.getStackInSlot(8);
		if (!blueprintStack.isEmpty() && blueprintStack.getItem() instanceof IBlueprint)
		{
			IBlueprint blueprint = (IBlueprint) blueprintStack.getItem();
			NonNullList<ItemStack> toCheck = NonNullList.create();
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				toCheck.add(slots.getStackInSlot(indexSlot + 2).copy());
			}
			NonNullList<ItemStack> result = blueprint.consumeItems(blueprintStack, toCheck);
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				slots.setStackInSlot(indexSlot + 2, result.get(indexSlot));
			}
			this.updateRecipe();
		}
		else
		{
			throw new IllegalStateException("Tried to subtract resources when no blueprint was available!");
		}
	}

	// Runs on the server 
	public void smash(boolean pkt)
	{
		ItemStack toDestroy = slots.getStackInSlot(0);
		
		if (CyberwareAPI.canDeconstruct(toDestroy) && toDestroy.getCount() > 0)
		{
			ItemStack paperSlot = slots.getStackInSlot(1);
			boolean doBlueprint = !paperSlot.isEmpty() && paperSlot.getCount() > 0 && paperSlot.getItem() == Items.PAPER;
			
			NonNullList<ItemStack> components = CyberwareAPI.getComponents(toDestroy);

			List<ItemStack> random = new ArrayList<>();
			for (ItemStack component : components)
			{
				if (!component.isEmpty())
				{
					for (int indexComponent = 0; indexComponent < component.getCount(); indexComponent++)
					{
						ItemStack copy = component.copy();
						copy.setCount(1);
						random.add(copy);
					}
				}
			}
			
			int numToRemove = 1;
			switch (world.getDifficulty())
			{
				case EASY:
					numToRemove = 1;
					break;
				case HARD:
					numToRemove = 2;
					break;
				case NORMAL:
					numToRemove = 2;
					break;
				case PEACEFUL:
					numToRemove = 1;
					break;
				default:
					break;
			}
			
			if (slots.getStackInSlot(0).isItemStackDamageable()) // Damaged items yield less
			{
				float percent = (slots.getStackInSlot(0).getItemDamage() * 1F  / slots.getStackInSlot(0).getMaxDamage());
				int addl = (int) (random.size() * percent);
				addl = Math.max(0, addl - 1);
				numToRemove += addl;
			}
			
			numToRemove = Math.min(numToRemove, random.size() - 1);
			for (int index = 0; index < numToRemove; index++)
			{
				random.remove(world.rand.nextInt(random.size()));
			}

			ItemStackHandler handler = new ItemStackHandler(6);
			for (int indexSlot = 0; indexSlot < 6; indexSlot++)
			{
				handler.setStackInSlot(indexSlot, slots.getStackInSlot(indexSlot + 2).copy());
			}
			boolean canInsert = true;
			
			// Check if drops will fit
			for (ItemStack drop : components)
			{
				ItemStack left = drop.copy();
				boolean wasAble = false;
				for (int slot = 0; slot < 6; slot++)
				{
					left = handler.insertItem(slot, left, false);
					if (left.isEmpty())
					{
						wasAble = true;
						break;
					}
				}
				
				if (!wasAble)
				{
					canInsert = false;
					break;
				}
			}
			
			// Check if blueprint will fit
			if (doBlueprint)
			{
				ItemStack left = ItemBlueprint.getBlueprintForItem(toDestroy);
				boolean wasAble = false;
				for (int slot = 0; slot < 6; slot++)
				{
					left = handler.insertItem(slot, left, false);
					if (left.isEmpty())
					{
						wasAble = true;
						break;
					}
				}
				
				if (!wasAble)
				{
					canInsert = false;
				}
			}

			
			if (canInsert)
			{
				if (pkt)
				{
					CyberwarePacketHandler.INSTANCE.sendToAllAround(new ScannerSmashPacket(pos.getX(), pos.getY(), pos.getZ()), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 25));
				}
				
				if (!world.isRemote)
				{
					if (doBlueprint && getWorld().rand.nextFloat() < (CyberwareConfig.ENGINEERING_CHANCE / 100F))
					{
						ItemStack blue = ItemBlueprint.getBlueprintForItem(toDestroy);
						random.add(blue);
						
						ItemStack current = slots.getStackInSlot(1);
						current.shrink(1);
						if (current.getCount() <= 0)
						{
							current = ItemStack.EMPTY;
						}
						slots.setStackInSlot(1, current);
					}
			
					
					for (ItemStack drop : random)
					{
						ItemStack dropLeft = drop.copy();
						for (int slot = 2; slot < 8; slot++)
						{
							if (!slots.getStackInSlot(slot).isEmpty())
							{
								dropLeft = slots.insertItem(slot, dropLeft, false);
								if (dropLeft.isEmpty())
								{
									break;
								}
							}
						}
						
						for (int slot = 2; slot < 8; slot++)
						{
							dropLeft = slots.insertItem(slot, dropLeft, false);
							if (dropLeft.isEmpty())
							{
								break;
							}
						}
					}
					
					ItemStack current = slots.getStackInSlot(0);
					current.shrink(1);
					if (current.getCount() <= 0 || current.isEmpty())
					{
						world.notifyBlockUpdate(pos, world.getBlockState(getPos()), world.getBlockState(getPos()), 2);

						current = ItemStack.EMPTY;
					}
					slots.setStackInSlot(0, current);
					updateRecipe();
				}
				else
				{
					smashSounds();
				}
			}
		}

	}


	@Override
	public void update()
	{
		if (world.isBlockPowered(getPos()) || world.isBlockPowered(getPos().add(0, -1, 0)))
		{
			if (time == 0)
			{
				this.smash(false);
			}
			time = (time + 1) % 25;
		}
		else
		{
			time = 0;
		}
	}


	public void smashSounds()
	{
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		clickedTime = Minecraft.getMinecraft().player.ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks();
		world.playSound(x, y, z, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1F, 1F, false);
		world.playSound(x, y, z, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1F, .5F, false);
		for (int index = 0; index < 10; index++)
		{
			world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
			                    x + .5F, y, z + .5F,
			                    .25F * (world.rand.nextFloat() - .5F), .1F, .25F * (world.rand.nextFloat() - .5F),
			                    Item.getIdFromItem(slots.getStackInSlot(0).getItem()));
		}
	}

	
}
