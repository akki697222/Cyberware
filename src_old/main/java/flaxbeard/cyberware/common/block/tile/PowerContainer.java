package flaxbeard.cyberware.common.block.tile;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({
	@Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla"),
	@Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = "tesla"),
	@Optional.Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = "tesla")
})
public class PowerContainer implements ITeslaConsumer, ITeslaHolder, ITeslaProducer, INBTSerializable<NBTTagCompound>
{

	private long stored;
	private long capacity;
	private long inputRate;
	private long outputRate;
	
	public PowerContainer()
	{
		this.stored = 0;
		this.capacity = 5000;
		this.inputRate = 50;
		this.outputRate = 50;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		final NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("power", stored);
		tag.setLong("capacity", capacity);
		tag.setLong("input", inputRate);
		tag.setLong("output", outputRate);
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound tagCompound)
	{
		this.stored = tagCompound.getLong("power");
		this.capacity = tagCompound.getLong("capacity");
		this.inputRate = tagCompound.getLong("input");
		this.outputRate = tagCompound.getLong("output");
			
		if (this.stored > this.getCapacity())
		{
			this.stored = this.getCapacity();
		}
	}

	@Override
	public long getCapacity()
	{
		return capacity;
	}

	@Override
	public long getStoredPower()
	{
		return stored;
	}

	@Override
	public long givePower(long Tesla, boolean simulated)
	{
		final long acceptedTesla = Math.min(this.getCapacity() - this.stored, Math.min(inputRate, Tesla));
		
		if (!simulated)
		{
			this.stored += acceptedTesla;
		}
			
		return acceptedTesla;
	}

	@Override
	public long takePower(long Tesla, boolean simulated)
	{
		final long removedPower = Math.min(this.stored, Math.min(outputRate, Tesla));
		
		if (!simulated)
		{
			this.stored -= removedPower;
		}
			
		return removedPower;
	}

}
