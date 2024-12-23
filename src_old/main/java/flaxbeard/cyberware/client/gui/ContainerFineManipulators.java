package flaxbeard.cyberware.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

public class ContainerFineManipulators extends ContainerPlayer
{
	public ContainerFineManipulators(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer entityPlayer)
	{
		super(playerInventory, localWorld, entityPlayer);
		
		inventorySlots.clear();
		
		craftMatrix = new InventoryCrafting(this, 3, 3);
		
		addSlotToContainer(new SlotCrafting(playerInventory.player, craftMatrix, craftResult, 0, 124, 35));
		
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 3; indexColumn++)
			{
				addSlotToContainer(new Slot(craftMatrix, indexColumn + indexRow * 3, 30 + indexColumn * 18, 17 + indexRow * 18));
			}
		}
		
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new Slot(playerInventory, indexColumn + (indexRow + 1) * 9, 8 + indexColumn * 18, 84 + indexRow * 18));
			}
		}
		
		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new Slot(playerInventory, indexColumn, 8 + indexColumn * 18, 142));
		}
	}
	
	@Override
	public void onContainerClosed(EntityPlayer entityPlayer)
	{
		super.onContainerClosed(entityPlayer);
		
		for (int indexSlot = 0; indexSlot < 9; indexSlot++)
		{
			ItemStack itemstack = craftMatrix.removeStackFromSlot(indexSlot);
			
			if (!itemstack.isEmpty())
			{
				entityPlayer.dropItem(itemstack, false);
			}
		}
		
		craftResult.setInventorySlotContents(0, ItemStack.EMPTY);
	}
}
