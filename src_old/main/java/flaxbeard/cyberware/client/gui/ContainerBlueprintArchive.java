package flaxbeard.cyberware.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;

public class ContainerBlueprintArchive extends Container
{
	private TileEntityBlueprintArchive archive;
	private int numRows;

	public ContainerBlueprintArchive(IInventory playerInventory, TileEntityBlueprintArchive archive)
	{
		this.archive = archive;
		this.numRows = archive.slots.getSlots() / 9;
		int i = (numRows - 4) * 18;

		for (int indexRow = 0; indexRow < numRows; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new SlotItemHandler(archive.slots, indexColumn + indexRow * 9, 8 + indexColumn * 18, 18 + indexRow * 18));
			}
		}

		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new Slot(playerInventory, indexColumn + indexRow * 9 + 9, 8 + indexColumn * 18, 103 + indexRow * 18 + i));
			}
		}

		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new Slot(playerInventory, indexColumn, 8 + indexColumn * 18, 161 + i));
		}
	}
	
	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityPlayer)
	{
		return archive.isUsableByPlayer(entityPlayer);
	}
	
	@Nonnull
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < numRows * 9)
			{
				if (!mergeItemStack(itemstack1, numRows * 9, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!mergeItemStack(itemstack1, 0, numRows * 9, false))
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
		}

		return itemstack;
	}

}