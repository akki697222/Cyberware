package flaxbeard.cyberware.common.misc;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SpecificWrapper implements IItemHandlerModifiable
{

	private final IItemHandlerModifiable compose;
	private final int[] slots;

	public SpecificWrapper(IItemHandlerModifiable compose, int... slots)
	{
		this.compose = compose;
		this.slots = slots;
	}

	@Override
	public int getSlots()
	{
		return slots.length;
	}
	
	private int getIndex(int input)
	{
		return slots[input];
	}
	
	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if (checkSlot(slot))
		{
			return compose.getStackInSlot(getIndex(slot));
		}

		return ItemStack.EMPTY;
	}
	
	@Nonnull
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (checkSlot(slot))
		{
			return compose.insertItem(getIndex(slot), stack, simulate);
		}

		return stack;
	}
	
	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (checkSlot(slot))
		{
			return compose.extractItem(getIndex(slot), amount, simulate);
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		if (checkSlot(slot))
		{
			compose.setStackInSlot(getIndex(slot), stack);
		}
	}

	private boolean checkSlot(int localSlot)
	{
		return localSlot < slots.length;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

}