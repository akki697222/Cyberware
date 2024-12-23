package flaxbeard.cyberware.client.gui;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox.ItemStackHandlerComponent;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;

public class ContainerEngineeringTable extends Container
{
	
	public class SlotEngineering extends SlotItemHandler
	{
		public SlotEngineering(IItemHandler itemHandler, int index, int xPosition, int yPosition)
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
			super.onSlotChanged();
			engineering.markDirty();
			
			if (slotNumber >= 2 && slotNumber <= 8)
			{
				engineering.updateRecipe();
			}
		}
		
		@Override
		public void putStack(@Nonnull ItemStack stack)
		{
			engineering.slots.overrideExtract = true;
			super.putStack(stack);
			engineering.slots.overrideExtract = false;
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack)
		{
			return (slotNumber >= 2 && slotNumber < 8)
			    || engineering.slots.isItemValidForSlot(slotNumber, stack);
		}
	}
	
	public class SlotInv extends Slot
	{
		public SlotInv(IInventory inventoryIn, int index, int xPosition, int yPosition)
		{
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public void onSlotChanged()
		{
			super.onSlotChanged();
			seeIfCheckNewBox();
		}
		
		@Override
		public void putStack(@Nonnull ItemStack stack)
		{
			super.putStack(stack);
			seeIfCheckNewBox();
		}
		
		private void seeIfCheckNewBox()
		{
			if (componentBoxList.size() <= 1)
			{
				checkForNewBoxes();
			}
		}
		
	}
	
	public class SlotComponentBox extends SlotItemHandler
	{

		public SlotComponentBox(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}
		
		@Override
		public void onSlotChanged()
		{
			super.onSlotChanged();
			updateNBT();
		}
		
		private void updateNBT()
		{
			if ( componentBox != null
			  && componentBox instanceof Integer )
			{
				ItemStack item = playerInv.mainInventory.get((Integer) componentBox);
				if (!item.isEmpty() && item.getItem() == CyberwareContent.componentBox.itemBlock)
				{
					NBTTagCompound tagCompoundContents = componentHandler.serializeNBT();
					NBTTagCompound tagCompoundItem = item.getTagCompound();
					if (tagCompoundItem == null)
					{
						tagCompoundItem = new NBTTagCompound();
						item.setTagCompound(tagCompoundItem);
					}
					tagCompoundItem.setTag("contents", tagCompoundContents);
				}
			}
		}
		
	}
	
	private final TileEntityEngineeringTable engineering;
	public TileEntityBlueprintArchive archive;
	public int archiveIndex = 0;
	public ArrayList<TileEntityBlueprintArchive> archiveList = new ArrayList<>();
	
	public Object componentBox;
	public ArrayList<Object> componentBoxList = new ArrayList<>();
	public int componentBoxIndex = 0;
	
	ItemStackHandler componentHandler = null;
	public InventoryPlayer playerInv;
	
	public ContainerEngineeringTable(String uuid, InventoryPlayer playerInventory, @Nonnull TileEntityEngineeringTable engineering)
	{
		this.playerInv = playerInventory;
		this.engineering = engineering;
		archive = null;
		componentBox = null;
		BlockPos target = null;
		if (engineering.lastPlayerArchive.containsKey(uuid))
		{
			target = engineering.lastPlayerArchive.get(uuid);
		}
		for (int y = -2; y < 2; y++)
		{
			for (int x = -2; x < 3; x++)
			{
				for (int z = -2; z < 3; z++)
				{
					BlockPos pos = engineering.getPos().add(x, y, z);
					TileEntity tileEntity = engineering.getWorld().getTileEntity(pos);
					if (tileEntity instanceof TileEntityBlueprintArchive)
					{
						if ( archive == null
						  || tileEntity.getPos().equals(target) )
						{
							archive = (TileEntityBlueprintArchive) tileEntity;
							archiveIndex = archiveList.size();
						}
						
						archiveList.add((TileEntityBlueprintArchive) tileEntity);
					}
				}
			}
		}
		
		for (int indexSlot = 0; indexSlot < playerInventory.mainInventory.size(); indexSlot++)
		{
			ItemStack stack = playerInventory.mainInventory.get(indexSlot);
			if ( !stack.isEmpty()
			  && stack.getItem() == CyberwareContent.componentBox.itemBlock)
			{
				NBTTagCompound tagCompoundStack = stack.getTagCompound();
				if (tagCompoundStack == null)
				{
					tagCompoundStack = new NBTTagCompound();
					stack.setTagCompound(tagCompoundStack);
				}
				if (!tagCompoundStack.hasKey("contents"))
				{
					ItemStackHandler slots = new ItemStackHandlerComponent(18);
					tagCompoundStack.setTag("contents", slots.serializeNBT());
				}
				if (componentBox == null )
				{
					componentBox = indexSlot;
					componentBoxIndex = componentBoxList.size();
				}
				
				componentBoxList.add(indexSlot);
			}
		}
		
		for (int y = -2; y < 2; y++)
		{
			for (int x = -2; x < 3; x++)
			{
				for (int z = -2; z < 3; z++)
				{
					BlockPos pos = engineering.getPos().add(x, y, z);
					TileEntity tileEntity = engineering.getWorld().getTileEntity(pos);
					if (tileEntity instanceof TileEntityComponentBox)
					{
						if (componentBox == null )
						{
							componentBox = tileEntity;
							componentBoxIndex = componentBoxList.size();
						}
						
						componentBoxList.add(tileEntity);
					}
				}
			}
		}
		
		int offset = componentBox == null ? 0 : 65;
		
		addSlotToContainer(new SlotEngineering(engineering.guiSlots, 0, 15 + offset, 20));
		addSlotToContainer(new SlotEngineering(engineering.guiSlots, 1, 15 + offset, 53));
		
		for (int indexColumn = 0; indexColumn < 2; indexColumn++)
		{
			for (int indexRow = 0; indexRow < 3; indexRow++)
			{
				addSlotToContainer(new SlotEngineering(engineering.guiSlots, 2 + indexRow * 2 + indexColumn, 71 + indexColumn * 18 + offset, 17 + indexRow * 18));
			}
		}
		
		addSlotToContainer(new SlotEngineering(engineering.guiSlots, 8, 115 + offset, 53));
		addSlotToContainer(new SlotEngineering(engineering.guiSlots, 9, 145 + offset, 21));
		
		for (int indexRow = 0; indexRow < 3; indexRow++)
		{
			for (int indexColumn = 0; indexColumn < 9; indexColumn++)
			{
				addSlotToContainer(new SlotInv(playerInventory, indexColumn + indexRow * 9 + 9, 8 + indexColumn * 18 + offset, 84 + indexRow * 18));
			}
		}

		for (int indexColumn = 0; indexColumn < 9; indexColumn++)
		{
			addSlotToContainer(new SlotInv(playerInventory, indexColumn, 8 + indexColumn * 18 + offset, 142));
		}
		
		if (archive != null)
		{
			int numRows = archive.slots.getSlots() / 6;
			for (int indexRow = 0; indexRow < 6; indexRow++)
			{
				for (int indexColumn = 0; indexColumn < numRows; indexColumn++)
				{
					addSlotToContainer(new SlotItemHandler(archive.slots, indexColumn + indexRow * numRows, 181 + indexColumn * 18 + offset, 22 + indexRow * 18));
				}
			}
		}
		if (componentBox != null)
		{
			if (componentBox instanceof TileEntityComponentBox)
			{
				componentHandler = ((TileEntityComponentBox) componentBox).slots;
			}
			else
			{
				ItemStack item = playerInv.mainInventory.get((Integer) componentBox);
				ItemStackHandler slots = new ItemStackHandlerComponent(18);
				slots.deserializeNBT(item.getTagCompound().getCompoundTag("contents"));
				componentHandler = slots;
			}
			int numRows = componentHandler.getSlots() / 6;
			for (int indexRow = 0; indexRow < 6; indexRow++)
			{
				for (int indexColumn = 0; indexColumn < numRows; indexColumn++)
				{
					addSlotToContainer(new SlotComponentBox(componentHandler, indexColumn + indexRow * numRows, 8 + indexColumn * 18, 22 + indexRow * 18));
				}
			}
		}
		
		engineering.updateRecipe();
	}
	
	private void validateComponentAndArchive()
	{
		while ( archive != null
		     && archive.getWorld().getTileEntity(archive.getPos()) != archive)
		{
			archiveList.remove(archiveIndex);
			if (archiveList.size() == 0)
			{
				int offset = (componentBox == null ? 0 : 18);
				int numColumns = 18 / 6;
				for (int indexRow = 0; indexRow < 6; indexRow++)
				{
					for (int indexColumn = 0; indexColumn < numColumns; indexColumn++)
					{
						inventorySlots.remove(inventorySlots.size() - 1 - offset);
						inventoryItemStacks.remove(inventoryItemStacks.size() - 1 - offset);
					}
				}
				archive = null;
			}
			else
			{
				archiveIndex = archiveIndex - 1;
				nextArchive();
			}
		}
		
		while ( ( ( componentBox instanceof TileEntityComponentBox
		         && ((TileEntityComponentBox) componentBox).getWorld().getTileEntity(((TileEntityComponentBox) componentBox).getPos()) != componentBox ) )
		       || ( componentBox instanceof Integer
		          && ( playerInv.mainInventory.get((Integer) componentBox).isEmpty()
		            || playerInv.mainInventory.get((Integer) componentBox).getItem() != CyberwareContent.componentBox.itemBlock) ) )
		{
			componentBoxList.remove(componentBoxIndex);
			if (componentBoxList.size() == 0)
			{
				int numColumns = 18 / 6;
				for (int indexRow = 0; indexRow < 6; indexRow++)
				{
					for (int indexColumn = 0; indexColumn < numColumns; indexColumn++)
					{
						inventorySlots.remove(inventorySlots.size() - 1);
						inventoryItemStacks.remove(inventoryItemStacks.size() - 1);
						
					}
				}
				componentBox = null;
				componentHandler = null;
			}
			else
			{
				componentBoxIndex = componentBoxIndex - 1;
				nextComponentBox();
			}
		}
	}
	
	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer entityPlayer)
	{
		validateComponentAndArchive();
		return engineering.isUsableByPlayer(entityPlayer);
	}
	
	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
		boolean doUpdate = false;
		
		int componentLow = (archive == null ? 46 : 64);
		int componentHigh = componentLow + 18;
		int archiveLow = 46;
		int archiveHigh = archiveLow + 18;
		
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			
			if (index == 9)
			{
				if (!mergeItemStack(itemstack1, 10, 46, true))
				{
					return ItemStack.EMPTY;
				}
				
				engineering.subtractResources();
				doUpdate = true;
			}
			else if (index == 8 && archive != null)
			{
				if ( !mergeItemStack(itemstack1, archiveLow, archiveHigh, true)
				  && !mergeItemStack(itemstack1, 10, 46, false) )
				{
					return ItemStack.EMPTY;
				}
			}
			else if (index == 1 && archive != null)
			{
				if ( !mergeItemStack(itemstack1, archiveLow, archiveHigh, true)
				  && !mergeItemStack(itemstack1, 10, 46, false) )
				{
					return ItemStack.EMPTY;
				}
			}
			else if (index > 9)
			{
				if ( engineering.slots.isItemValidForSlot(1, itemstack1)
				  && mergeItemStack(itemstack1, 1, 2, false) )
				{

				}
				else if (engineering.slots.isItemValidForSlot(0, itemstack1))
				{
					if (!mergeItemStack(itemstack1, 0, 1, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (engineering.slots.isItemValidForSlot(8, itemstack1))
				{
					if (!mergeItemStack(itemstack1, 8, 9, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if (index >= 10 && index < 37)
				{
					if ( ( archive == null
					    || !mergeItemStack(itemstack1, archiveLow, archiveHigh, false) )
					    && !mergeItemStack(itemstack1, 2, 8, false)
					    && ( componentBox == null
					      || !mergeItemStack(itemstack1, componentLow, componentHigh, false) )
					    && !mergeItemStack(itemstack1, 37, 46, false) )
					{
						return ItemStack.EMPTY;
					}
				}
				else if (index >= archiveLow && index < archiveHigh)
				{
					if ( !mergeItemStack(itemstack1, 10, 37, false)
					  && !mergeItemStack(itemstack1, 37, 46, false) )
					{
						return ItemStack.EMPTY;
					}
				}
				else if (index >= componentLow && index < componentHigh)
				{
					if ( !mergeItemStack(itemstack1, 2, 8, false)
					  && !mergeItemStack(itemstack1, 10, 37, false)
					  && !mergeItemStack(itemstack1, 37, 46, false) )
					{
						return ItemStack.EMPTY;
					}
				}
				else if (index >= 37 && index < 46)
				{
					if ( !mergeItemStack(itemstack1, 2, 8, false)
					  && !mergeItemStack(itemstack1, 10, 37, false) )
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if ( ( componentBox == null
			         || !mergeItemStack(itemstack1, componentLow, componentHigh, false) )
			       && ( archive == null
			         || !mergeItemStack(itemstack1, archiveLow, archiveHigh, false) )
			       && !mergeItemStack(itemstack1, 10, 46, false) )
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
		
		if (doUpdate)
		{
			engineering.updateRecipe();
		}

		return itemstack;
	}
	
	public void checkForNewBoxes()
	{
		for (int indexSlot = 0; indexSlot < playerInv.mainInventory.size(); indexSlot++)
		{
			if (!componentBoxList.contains(indexSlot))
			{
				ItemStack stack = playerInv.mainInventory.get(indexSlot);
				if ( !stack.isEmpty()
				  && stack.getItem() == CyberwareContent.componentBox.itemBlock)
				{
					NBTTagCompound tagCompoundStack = stack.getTagCompound();
					if (tagCompoundStack == null)
					{
						tagCompoundStack = new NBTTagCompound();
						stack.setTagCompound(tagCompoundStack);
					}
					if (!tagCompoundStack.hasKey("contents"))
					{
						ItemStackHandler slots = new ItemStackHandlerComponent(18);
						tagCompoundStack.setTag("contents", slots.serializeNBT());
					}
					if (componentBox == null )
					{
						componentBox = indexSlot;
						componentBoxIndex = componentBoxList.size();
					}
					
					componentBoxList.add(indexSlot);
				}
			}
		}
	}
	
	public void nextArchive()
	{
		clearSlots();
		archiveIndex = (archiveIndex + 1) % archiveList.size();
		archive = archiveList.get(archiveIndex);
		createSlots();
	}
	
	public void prevArchive()
	{
		clearSlots();
		archiveIndex = (archiveIndex + archiveList.size() - 1) % archiveList.size();
		archive = archiveList.get(archiveIndex);
		createSlots();
	}
	
	public void nextComponentBox()
	{
		checkForNewBoxes();
		clearSlots();
		handleClosingBox();
		componentBoxIndex = (componentBoxIndex + 1) % componentBoxList.size();
		componentBox = componentBoxList.get(componentBoxIndex);
		createSlots();
	}
	
	public void prevComponentBox()
	{
		checkForNewBoxes();
		clearSlots();
		handleClosingBox();
		componentBoxIndex = (componentBoxIndex + componentBoxList.size() - 1) % componentBoxList.size();
		componentBox = componentBoxList.get(componentBoxIndex);
		createSlots();
	}
	
	public void clearSlots()
	{
		int numOccupied = 0;
		if (componentBox != null) numOccupied += 18;
		if (archive != null) numOccupied += 18;
		for (int index = 0; index < numOccupied; index++)
		{
			inventorySlots.remove(inventorySlots.size() - 1);
			inventoryItemStacks.remove(inventoryItemStacks.size() - 1);
		}
	}
	
	public void createSlots()
	{
		validateComponentAndArchive();
		if (archive != null)
		{
			int numColumns = archive.slots.getSlots() / 6;
			for (int indexRow = 0; indexRow < 6; indexRow++)
			{
				for (int indexColumn = 0; indexColumn < numColumns; indexColumn++)
				{
					addSlotToContainer(new SlotItemHandler(archive.slots, indexColumn + indexRow * numColumns, 181 + indexColumn * 18 + (componentBox == null ? 0 : 65), 22 + indexRow * 18));
				}
			}
		}
		if (componentBox != null)
		{
			if (componentBox instanceof TileEntityComponentBox)
			{
				componentHandler = ((TileEntityComponentBox) componentBox).slots;
			}
			else
			{
				ItemStackHandler slots = new ItemStackHandlerComponent(18);
				ItemStack item = playerInv.mainInventory.get((Integer) componentBox);
				slots.deserializeNBT(item.getTagCompound().getCompoundTag("contents"));
				componentHandler = slots;
			}
			
			int numColumns = componentHandler.getSlots() / 6;
			for (int indexRow = 0; indexRow < 6; indexRow++)
			{
				for (int indexColumn = 0; indexColumn < numColumns; indexColumn++)
				{
					addSlotToContainer(new SlotComponentBox(componentHandler, indexColumn + indexRow * numColumns, 8 + indexColumn * 18, 22 + indexRow * 18));
				}
			}
		}
	}
	
	@Override
	public void onContainerClosed(EntityPlayer entityPlayer)
	{
		super.onContainerClosed(entityPlayer);
		handleClosingBox();
	}
	
	public void handleClosingBox()
	{
		// no operation
	}
}
