package flaxbeard.cyberware.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import flaxbeard.cyberware.common.block.tile.TileEntityScanner;

public class ContainerScanner extends Container
{
	public class SlotScanner extends SlotItemHandler
	{
		public SlotScanner(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}
		
		@Override
		public boolean canTakeStack(EntityPlayer entityPlayer)
		{
			return true;
		}
		
		@Override
		public void onSlotChanged()
		{
			scanner.markDirty();
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack)
		{
			return scanner.slots.isItemValidForSlot(slotNumber, stack);
		}
	}
	
	private final TileEntityScanner scanner;
	
	public ContainerScanner(InventoryPlayer playerInventory, TileEntityScanner scanner)
	{
		this.scanner = scanner;
		
		addSlotToContainer(new SlotScanner(scanner.guiSlots, 0, 35, 53));
		addSlotToContainer(new SlotScanner(scanner.guiSlots, 1, 15, 53));
		
		addSlotToContainer(new SlotScanner(scanner.guiSlots, 2, 141, 57));
		
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new Slot(playerInventory, indexColumn + indexRow * 9 + 9, 8 + indexColumn * 18, 84 + indexRow * 18));
			}
		}

		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new Slot(playerInventory, indexColumn, 8 + indexColumn * 18, 142));
		}
	}
	
	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityPlayer)
	{
		return scanner.isUsableByPlayer(entityPlayer);
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

			if (index == 2)
			{
				if (!mergeItemStack(itemstack1, 3, 39, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (index > 2)
			{
				if (scanner.slots.isItemValidForSlot(1, itemstack1))
				{
					if (!mergeItemStack(itemstack1, 1, 2, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (scanner.slots.isItemValidForSlot(0, itemstack1))
				{
					if (!mergeItemStack(itemstack1, 0, 1, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (index < 30)
				{
					if (!mergeItemStack(itemstack1, 30, 39, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (index < 39)
				{
					if (!mergeItemStack(itemstack1, 3, 30, false) )
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if (!mergeItemStack(itemstack1, 3, 39, false))
			{
				return ItemStack.EMPTY;
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
