package flaxbeard.cyberware.common.block.tile;

import javax.annotation.Nonnull;
import java.util.List;

import cofh.redstoneflux.api.IEnergyReceiver;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareConfig;

@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = "redstoneflux")
public class TileEntityCharger extends TileEntity implements ITickable, IEnergyReceiver, IEnergyStorage
{
	private PowerContainer container = new PowerContainer();
	
	@CapabilityInject(ITeslaConsumer.class)
	private static Capability TESLA_CONSUMER;
	@CapabilityInject(ITeslaProducer.class)
	private static Capability TESLA_PRODUCER;
	@CapabilityInject(ITeslaHolder.class)
	private static Capability TESLA_HOLDER;
	
	private boolean last = false;

	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		
		container.deserializeNBT(tagCompound.getCompoundTag("power"));
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);
		tagCompound.setTag("power", container.serializeNBT());
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
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == TESLA_CONSUMER || capability == TESLA_PRODUCER || capability == TESLA_HOLDER)
		{
			return (T) this.container;
		}
		
		if(capability == CapabilityEnergy.ENERGY)
			return (T) this;
			
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (capability == TESLA_CONSUMER || capability == TESLA_PRODUCER || capability == TESLA_HOLDER || capability == CapabilityEnergy.ENERGY)
		{
			return true;
		}
			
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public void update()
	{
		List<EntityLivingBase> entitiesInRange = world.getEntitiesWithinAABB(EntityLivingBase.class,
		                                                                       new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
		                                                                                         pos.getX() + 1F, pos.getY() + 2.5F, pos.getZ() + 1F) );
		for (EntityLivingBase entityInRange : entitiesInRange)
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityInRange);
			if ( cyberwareUserData != null
			  && !cyberwareUserData.isAtCapacity(ItemStack.EMPTY, 20)
			  && (container.getStoredPower() >= CyberwareConfig.TESLA_PER_POWER) )
			{
				if (!world.isRemote)
				{
					container.takePower(CyberwareConfig.TESLA_PER_POWER, false);
				}
				cyberwareUserData.addPower(20, ItemStack.EMPTY);
				
				if (entityInRange.ticksExisted % 5 == 0)
				{
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + .5F, pos.getY() + 1F, pos.getZ() + .5F, 0F, .05F, 0F, 255, 150, 255 );
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + .5F, pos.getY() + 1F, pos.getZ() + .5F, .04F, .05F, .04F, 255, 150, 255 );
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + .5F, pos.getY() + 1F, pos.getZ() + .5F, -.04F, .05F, .04F, 255, 150, 255 );
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + .5F, pos.getY() + 1F, pos.getZ() + .5F, .04F, .05F, -.04F, 255, 150, 255 );
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + .5F, pos.getY() + 1F, pos.getZ() + .5F, -.04F, .05F, -.04F, 255, 150, 255 );
				}
			}
		}
		
		boolean hasPower = (container.getStoredPower() >= CyberwareConfig.TESLA_PER_POWER);
		if (hasPower != last && !world.isRemote)
		{
			IBlockState state = world.getBlockState(getPos());
			world.notifyBlockUpdate(pos, state, state, 2);
			last = hasPower;
		}
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return true;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return (int) this.container.getStoredPower();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return (int) this.container.getCapacity();
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		return (int) this.container.givePower(maxReceive, simulate);
	}

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        return (int)container.givePower(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        return (int)container.takePower(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored()
    {
        return (int)container.getStoredPower();
    }

    @Override
    public int getMaxEnergyStored()
    {
        return (int)container.getCapacity();
    }

    @Override
    public boolean canExtract()
    {
        return false;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }
}
