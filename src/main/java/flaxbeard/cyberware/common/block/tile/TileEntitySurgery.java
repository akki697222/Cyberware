package flaxbeard.cyberware.common.block.tile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberware;
import flaxbeard.cyberware.api.ICyberware.EnumSlot;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.block.BlockSurgeryChamber;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandler;
import flaxbeard.cyberware.common.lib.LibConstants;

public class TileEntitySurgery extends TileEntity implements ITickable
{
	public ItemStackHandler slotsPlayer = new ItemStackHandler(100);
	public ItemStackHandler slots = new ItemStackHandler(100);
	public boolean[] discardSlots = new boolean[100];
	public boolean[] isEssentialMissing = new boolean[EnumSlot.values().length];
	public int wrongSlot = -1;
	public int ticksWrong = 0;

	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.worldObj.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}
	
	public void updatePlayerSlots(EntityLivingBase entity)
	{
		markDirty();
		if (CyberwareAPI.hasCapability(entity))
		{
			ICyberwareUserData c = CyberwareAPI.getCapability(entity);
			
			// Update slotsPlayer with the items in the player's body
			int i = 0;
			for (EnumSlot slotType : EnumSlot.values())
			{
				for (int n = 0; n < LibConstants.WARE_PER_SLOT; n++)
				{
					ItemStack toPut = ItemStack.copyItemStack(c.getInstalledCyberware(slotType)[n]);
					
					// If there's a new item, don't set it to discard by default unless it conflicts
					if (!ItemStack.areItemStacksEqual(toPut, slotsPlayer.getStackInSlot(i * LibConstants.WARE_PER_SLOT + n)))
					{
						discardSlots[i * LibConstants.WARE_PER_SLOT + n] = false;
						if (doesItemConflict(toPut, slotType, n))
						{
							discardSlots[i * LibConstants.WARE_PER_SLOT + n] = true;
						}
					}
					slotsPlayer.setStackInSlot(i * LibConstants.WARE_PER_SLOT + n, toPut);

				}
				updateEssential(slotType);
				i++;
			}
			
			// Check for items with requirements that are no longer fulfilled
			boolean needToCheck = true;
			while (needToCheck)
			{
				i = 0;
				needToCheck = false;
				for (EnumSlot slotType : EnumSlot.values())
				{
					for (int n = 0; n < LibConstants.WARE_PER_SLOT; n++)
					{
						int index = i * LibConstants.WARE_PER_SLOT + n;
						
						ItemStack stack = slots.getStackInSlot(index);
						if (stack != null && !areRequirementsFulfilled(stack, slotType, n))
						{
							addItemStack(entity, stack);
							slots.setStackInSlot(index, null);
							needToCheck = true;
						}
					}
					i++;
				}
			}
		}
		else
		{
			slotsPlayer = new ItemStackHandler(100);
			for (EnumSlot slotType : EnumSlot.values())
			{
				updateEssential(slotType);
			}
		}
		wrongSlot = -1;
	}
	
	public boolean doesItemConflict(@Nullable ItemStack stack, EnumSlot slot, int n)
	{
		int row = slot.ordinal();
		if (stack != null)
		{
			for (int i = 0; i < LibConstants.WARE_PER_SLOT; i++)
			{
				if (i != n)
				{
					int index = row * LibConstants.WARE_PER_SLOT + i;
					ItemStack slotStack = slots.getStackInSlot(index);
					ItemStack playerStack = slotsPlayer.getStackInSlot(index);
					
					ItemStack otherStack = slotStack != null ? slotStack : (discardSlots[index] ? null : playerStack);
					
					// Automatically incompatible with the same item/damage
					if (otherStack != null && (otherStack.getItem() == stack.getItem() && otherStack.getItemDamage() == stack.getItemDamage()))
					{
						setWrongSlot(index);
						return true;
					}
				
					// Incompatible if either stack doesn't like the other one
					if (otherStack != null && CyberwareAPI.getCyberware(otherStack).isIncompatible(otherStack, stack))
					{
						setWrongSlot(index);
						return true;
					}
					if (otherStack != null && CyberwareAPI.getCyberware(stack).isIncompatible(stack, otherStack))
					{
						setWrongSlot(index);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public void setWrongSlot(int index)
	{
		this.wrongSlot = index;
		if (Minecraft.getMinecraft().thePlayer != null)
		{
			this.ticksWrong = Minecraft.getMinecraft().thePlayer.ticksExisted;
		}
	}
	
	public void disableDependants(ItemStack stack, EnumSlot slot, int n)
	{
		int row = slot.ordinal();
		if (stack != null)
		{
			for (int i = 0; i < LibConstants.WARE_PER_SLOT; i++)
			{
				if (i != n)
				{
					int index = row * LibConstants.WARE_PER_SLOT + i;
					ItemStack playerStack = slotsPlayer.getStackInSlot(index);
					
					
					if (!areRequirementsFulfilled(playerStack, slot, n))
					{
						discardSlots[index] = true;
						System.out.println(":_");
					}
				}
			}
		}
	}
	
	public void enableDependsOn(ItemStack stack, EnumSlot slot, int n)
	{
		int row = slot.ordinal();
		if (stack != null)
		{
			ICyberware ware = CyberwareAPI.getCyberware(stack);
			for (ItemStack needed : ware.required(stack))
			{
				boolean found = false;
				
				for (int i = 0; i < LibConstants.WARE_PER_SLOT; i++)
				{
					if (i != n)
					{
						int index = row * LibConstants.WARE_PER_SLOT + i;
						ItemStack playerStack = slotsPlayer.getStackInSlot(index);
						
						if (playerStack != null && playerStack.getItem() == needed.getItem() && playerStack.getItemDamage() == needed.getItemDamage())
						{
							found = true;
							discardSlots[index] = false;
							break;
						}
					}
				}
				
				if (!found) System.out.println("BADDDD!!!!!!!!!");
			}
		}
	}
	
	public boolean canDisableItem(ItemStack stack, EnumSlot slot, int n)
	{
		int row = slot.ordinal();
		if (stack != null)
		{
			for (int i = 0; i < LibConstants.WARE_PER_SLOT; i++)
			{
				if (i != n)
				{
					int index = row * LibConstants.WARE_PER_SLOT + i;
					ItemStack slotStack = slots.getStackInSlot(index);
					ItemStack playerStack = null;//slotsPlayer.getStackInSlot(index);
					
					ItemStack otherStack = slotStack != null ? slotStack : (discardSlots[index] ? null : playerStack);
					
					if (!areRequirementsFulfilled(otherStack, slot, n))
					{
						setWrongSlot(index);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean areRequirementsFulfilled(ItemStack stack, EnumSlot slot, int n)
	{
		int row = slot.ordinal();
		if (stack != null)
		{
			ICyberware ware = CyberwareAPI.getCyberware(stack);
			for (ItemStack needed : ware.required(stack))
			{
				boolean found = false;
				
				for (int i = 0; i < LibConstants.WARE_PER_SLOT; i++)
				{
					if (i != n)
					{
						int index = row * LibConstants.WARE_PER_SLOT + i;
						ItemStack slotStack = slots.getStackInSlot(index);
						ItemStack playerStack = slotsPlayer.getStackInSlot(index);
						
						ItemStack otherStack = slotStack != null ? slotStack : (discardSlots[index] ? null : playerStack);
						
						if (otherStack != null && otherStack.getItem() == needed.getItem() && otherStack.getItemDamage() == needed.getItemDamage())
						{
							found = true;
							break;
						}
					}
				}
				
				if (!found) return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		
		slots.deserializeNBT(compound.getCompoundTag("inv"));
		slotsPlayer.deserializeNBT(compound.getCompoundTag("inv2"));
		
		NBTTagList list = (NBTTagList) compound.getTag("discard");
		for (int i = 0; i < list.tagCount(); i++)
		{
			this.discardSlots[i] = ((NBTTagByte) list.get(i)).getByte() > 0;
		}
	}
	
	@Override
	public NBTTagCompound getUpdateTag()
	{
		return writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		
		compound = super.writeToNBT(compound);
		
		compound.setTag("inv", this.slots.serializeNBT());
		compound.setTag("inv2", this.slotsPlayer.serializeNBT());
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.discardSlots.length; i++)
		{
			list.appendTag(new NBTTagByte((byte) (this.discardSlots[i] ? 1 : 0)));
		}
		compound.setTag("discard", list);

		return compound;
	}
	
	public void updateEssential(EnumSlot slot)
	{
		this.isEssentialMissing[slot.ordinal()] = !isEssential(slot);
	}

	private boolean isEssential(EnumSlot slot)
	{
		for (int i = 0; i < LibConstants.WARE_PER_SLOT; i++)
		{
			int index = slot.ordinal() * LibConstants.WARE_PER_SLOT + i;
			ItemStack slotStack = slots.getStackInSlot(index);
			ItemStack playerStack = slotsPlayer.getStackInSlot(index);
			
			ItemStack stack = slotStack != null ? slotStack : (discardSlots[index] ? null : playerStack);

			if (stack != null && CyberwareAPI.getCyberware(stack).isEssential(stack))
			{

				return true;
			}
		}
		return false;
	}

	@Override
	public void update()
	{
		if (inProgress && progressTicks < 80)
		{
			EntityPlayer thePlayer = Minecraft.getMinecraft().thePlayer;
			if (targetEntity != null && !targetEntity.isDead && CyberwareAPI.hasCapability(targetEntity))
			{
				BlockPos pos = getPos();
				
				if (progressTicks > 20 && progressTicks < 60)
				{
					targetEntity.posX = pos.getX() + .5F;
					targetEntity.posZ = pos.getZ() + .5F;
				}
				
				if (progressTicks >= 20 && progressTicks <= 60 && progressTicks % 5 == 0)
				{
					targetEntity.attackEntityFrom(EssentialsMissingHandler.surgery, 2F);
				}
				
				if (progressTicks == 60)
				{
					processUpdate();
				}
				
				progressTicks++;
				if (targetEntity == thePlayer)
				{
					workingOnPlayer = true;
					playerProgressTicks = progressTicks;
				}
			}
			else
			{
				inProgress = false;
				progressTicks = 0;
				if (targetEntity == thePlayer)
				{
					workingOnPlayer = false;
				}
				targetEntity = null;
				
				IBlockState state = worldObj.getBlockState(getPos().down());
				if (state.getBlock() instanceof BlockSurgeryChamber)
				{
					((BlockSurgeryChamber) state.getBlock()).toggleDoor(true, state, getPos().down(), worldObj);
				}
			}
		}
		else if (inProgress)
		{
			EntityPlayer thePlayer = Minecraft.getMinecraft().thePlayer;

			if (targetEntity == thePlayer)
			{
				workingOnPlayer = false;
			}
			inProgress = false;
			progressTicks = 0;
			targetEntity = null;
			
			IBlockState state = worldObj.getBlockState(getPos().down());
			if (state.getBlock() instanceof BlockSurgeryChamber)
			{
				((BlockSurgeryChamber) state.getBlock()).toggleDoor(true, state, getPos().down(), worldObj);
			}
		}
	}

	private void processUpdate()
	{
		updatePlayerSlots(targetEntity);
		
		BlockPos p = getPos();

		ICyberwareUserData cyberware = CyberwareAPI.getCapability(targetEntity);
		
		for (int slotIndex = 0; slotIndex < EnumSlot.values().length; slotIndex++)
		{
			EnumSlot slot = EnumSlot.values()[slotIndex];
			ItemStack[] wares = new ItemStack[LibConstants.WARE_PER_SLOT];
			
			int c = 0;
			for (int j = slotIndex * LibConstants.WARE_PER_SLOT; j < (slotIndex + 1) * LibConstants.WARE_PER_SLOT; j++)
			{
				ItemStack newStack = slots.getStackInSlot(j);
				ItemStack targetEntityStack = slotsPlayer.getStackInSlot(j);
				if (newStack != null && newStack.stackSize > 0)
				{
					wares[c] = newStack.copy();
					if (targetEntityStack != null && targetEntityStack.stackSize > 0)
					{
						addItemStack(targetEntity, targetEntityStack);
					}
					c++;
				}
				else if (targetEntityStack != null && targetEntityStack.stackSize > 0)
				{
					if (discardSlots[j])
					{
						addItemStack(targetEntity, targetEntityStack);
						
					}
					else
					{
						wares[c] = slotsPlayer.getStackInSlot(j).copy();
						c++;
					}
				}
			}
			if (!worldObj.isRemote)
			{
				cyberware.setInstalledCyberware(slot, wares);
			}
			cyberware.setHasEssential(slot, !isEssentialMissing[slotIndex]);
		}
		
		if (!worldObj.isRemote)
		{
			CyberwareAPI.updateData(targetEntity);
		}
		slots = new ItemStackHandler(100);
		
	}

	private void addItemStack(EntityLivingBase entity, ItemStack stack)
	{
		
		boolean flag = true;
		
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = ((EntityPlayer) entity);
			flag = !player.inventory.addItemStackToInventory(stack);
		}

		if (flag && !worldObj.isRemote)
		{

			EntityItem item = new EntityItem(worldObj, getPos().getX() + .5F, getPos().getY() - 2F, getPos().getZ() + .5F, stack);
			worldObj.spawnEntityInWorld(item);
		}
	}

	public boolean canOpen()
	{
		return !inProgress;
	}

	public void notifyChange()
	{
		boolean opened = worldObj.getBlockState(getPos().down()).getValue(BlockSurgeryChamber.OPEN);

		if (!opened)
		{
			BlockPos p = getPos();
			List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(p.getX(), p.getY() - 2F, p.getZ(), p.getX() + 1F, p.getY(), p.getZ() + 1F));
			if (entities.size() == 1)
			{
				this.inProgress = true;
				this.progressTicks = 0;
				this.targetEntity = entities.get(0);
			}
		}
	}
	
	public boolean inProgress = false;
	public EntityLivingBase targetEntity = null;
	public int progressTicks = 0;
	public static boolean workingOnPlayer = false;
	public static int playerProgressTicks = 0;
}
