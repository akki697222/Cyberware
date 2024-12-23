package flaxbeard.cyberware.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.lib.LibConstants;

public class ContainerSurgery extends Container
{
	public class SlotSurgery extends SlotItemHandler
	{
		public final int savedXPosition;
		public final int savedYPosition;
		public final EnumSlot slot;
		private final int index;
		private IItemHandler playerItems;

		public SlotSurgery(IItemHandler itemHandler, IItemHandler playerItems, int index, int xPosition, int yPosition, EnumSlot slot)
		{
			super(itemHandler, index, xPosition, yPosition);
			
			savedXPosition = xPosition;
			savedYPosition = yPosition;
			this.slot = slot;
			this.index = index;
			this.playerItems = playerItems;
		}
		
		public ItemStack getPlayerStack()
		{
			return playerItems.getStackInSlot(slotNumber);
		}
		
		public boolean slotDiscarded()
		{
			return surgery.discardSlots[slotNumber];
		}
		
		public void setDiscarded(boolean dis)
		{
			surgery.discardSlots[slotNumber] = dis;
			surgery.updateEssential(slot);
			surgery.updateEssence();
		}
		
		@Override
		public boolean canTakeStack(EntityPlayer entityPlayer)
		{
			return surgery.canDisableItem(getStack(), slot, index % LibConstants.WARE_PER_SLOT);
		}
		
		@Override
		public void onSlotChanged()
		{
			super.onSlotChanged();
			
			surgery.updateEssence();
			surgery.markDirty();
		}
		
		@Override
		public void putStack(@Nonnull ItemStack stack)
		{
			if (isItemValid(stack))
			{
				surgery.disableDependants(getPlayerStack(), slot, index % LibConstants.WARE_PER_SLOT);
				super.putStack(stack);
			}
			surgery.markDirty();
			surgery.updateEssential(slot);
			surgery.updateEssence();
		}
		
		/*
		@Override
		public void onPickupFromSlot(EntityPlayer entityPlayer, ItemStack stack)
	    {
			super.onPickupFromSlot(entityPlayer, stack);
			surgery.markDirty();
			surgery.updateEssential(slot);
			surgery.updateEssence();
	    }
	    */
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack)
		{
			ItemStack playerStack = getPlayerStack();
			if ( !getPlayerStack().isEmpty()
			  && !surgery.canDisableItem(playerStack, slot, index % LibConstants.WARE_PER_SLOT) )
			{
				return false;
			}
			if ( !( !stack.isEmpty()
			     && CyberwareAPI.isCyberware(stack)
			     && CyberwareAPI.getCyberware(stack).getSlot(stack) == slot ) )
			{
				return false;
			}
			
			if (CyberwareAPI.areCyberwareStacksEqual(stack, playerStack))
			{
				int stackSize = CyberwareAPI.getCyberware(stack).installedStackSize(stack);
				if (playerStack.getCount() == stackSize) return false;
			}
			
			
			return !doesItemConflict(stack)
			    && areRequirementsFulfilled(stack);
		}
		
		public boolean doesItemConflict(@Nonnull ItemStack stack)
		{
			return surgery.doesItemConflict(stack, slot, index % LibConstants.WARE_PER_SLOT);
		}
		
		public boolean areRequirementsFulfilled(@Nonnull ItemStack stack)
		{
			return surgery.areRequirementsFulfilled(stack, slot, index % LibConstants.WARE_PER_SLOT);
		}
		
		@Override
		public int getItemStackLimit(@Nonnull ItemStack stack)
		{
			if ( stack.isEmpty()
			  || !CyberwareAPI.isCyberware(stack) )
			{
				return 1;
			}
			ItemStack playerStack = getPlayerStack();
			int stackSize = CyberwareAPI.getCyberware(stack).installedStackSize(stack);
			if (CyberwareAPI.areCyberwareStacksEqual(playerStack, stack))
			{
				return stackSize - playerStack.getCount();
			}
			return stackSize;
		}
	}
	
	private final TileEntitySurgery surgery;
	
	public ContainerSurgery(InventoryPlayer playerInventory, TileEntitySurgery surgery)
	{
		this.surgery = surgery;
		
		int indexContainerSlot = 0;
		for (EnumSlot slot : EnumSlot.values())
		{
			for (int indexCyberwareSlot = 0; indexCyberwareSlot < 8; indexCyberwareSlot++)
			{
				addSlotToContainer(new SlotSurgery(surgery.slots, surgery.slotsPlayer, indexContainerSlot, 9 + 20 * indexCyberwareSlot, 109, slot));
				indexContainerSlot++;
			}
			for (int indexCyberwareSlot = 0; indexCyberwareSlot < LibConstants.WARE_PER_SLOT - 8; indexCyberwareSlot++)
			{
				addSlotToContainer(new SlotSurgery(surgery.slots, surgery.slotsPlayer, indexContainerSlot, Integer.MIN_VALUE, Integer.MIN_VALUE, slot));
				indexContainerSlot++;
			}
		}
		
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new Slot(playerInventory, indexColumn + indexRow * 9 + 9, 8 + indexColumn * 18, 103 + indexRow * 18 + 37));
			}
		}
		
		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new Slot(playerInventory, indexColumn, 8 + indexColumn * 18, 161 + 37));
		}
	}
	
	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityPlayer)
	{
		return surgery.isUsableByPlayer(entityPlayer);
	}
	
	@Nonnull
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
	
		if ( slot != null
		  && slot.getHasStack() )
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
	
			if (!(slot instanceof SlotSurgery))
			{
				if ( index >= 3
				  && index < 30 )
				{
					if (!mergeItemStack(itemstack1, 30, 39, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if ( index >= 30
				       && index < 39
				       && !mergeItemStack(itemstack1, 3, 30, false) )
				{
					return ItemStack.EMPTY;
				}
			}
			
			if (itemstack1.getCount() == 0)
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
	
			if (itemstack1.getCount() == itemstack.getCount())
			{
				return ItemStack.EMPTY;
			}
	
			slot.onTake(entityPlayer, itemstack1);
		}
	
		return itemstack;
	}
}
