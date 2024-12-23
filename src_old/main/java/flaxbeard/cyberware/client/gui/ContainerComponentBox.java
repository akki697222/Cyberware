package flaxbeard.cyberware.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox.ItemStackHandlerComponent;

public class ContainerComponentBox extends Container
{
	private ItemStackHandler slots;
	private int numRows;
	private final TileEntityComponentBox box;
	private final ItemStack item;

	public ContainerComponentBox(IInventory playerInventory, @Nonnull TileEntityComponentBox box) {
		super();
		
		this.box = box;
		item = ItemStack.EMPTY;
		slots = box.slots;
		numRows = slots.getSlots() / 9;
		
		addSlots(playerInventory);
	}
	
	public ContainerComponentBox(IInventory playerInventory, @Nonnull ItemStack itemStack)
	{
		super();
		
		box = null;
		item = itemStack;
		slots = new ItemStackHandlerComponent(18);

		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if ( tagCompound != null
		  && itemStack.getTagCompound().hasKey("contents") )
		{
			slots.deserializeNBT(tagCompound.getCompoundTag("contents"));
		}
		
		numRows = slots.getSlots() / 9;
		
		addSlots(playerInventory);
	}
	
	private void addSlots(IInventory playerInventory)
	{
		int yOffset = (numRows - 4) * 18;
		
		// component box's inventory
		for (int indexRow = 0; indexRow < numRows; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new SlotItemHandler(slots, indexColumn + indexRow * 9, 8 + indexColumn * 18, 18 + indexRow * 18));
			}
		}
		
		// player's inventory
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new Slot(playerInventory, indexColumn + indexRow * 9 + 9, 8 + indexColumn * 18, 103 + indexRow * 18 + yOffset));
			}
		}
		
		// player's hotbar
		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new Slot(playerInventory, indexColumn, 8 + indexColumn * 18, 161 + yOffset));
		}
	}
	
	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityPlayer)
	{
		return box == null ? entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem) == item
		                   : box.isUsableByPlayer(entityPlayer);
	}
	
	@Override
	public void onContainerClosed(@Nonnull EntityPlayer entityPlayer)
	{
		super.onContainerClosed(entityPlayer);
		
		if (!item.isEmpty())
		{
			NBTTagCompound tagCompoundSlots = slots.serializeNBT();
			NBTTagCompound tagCompoundItem = item.getTagCompound();
			if (tagCompoundItem == null)
			{
				tagCompoundItem = new NBTTagCompound();
				item.setTagCompound(tagCompoundItem);
			}
			tagCompoundItem.setTag("contents", tagCompoundSlots);
		}
	}
	
	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
		
		if ( slot != null
		  && slot.getHasStack() )
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