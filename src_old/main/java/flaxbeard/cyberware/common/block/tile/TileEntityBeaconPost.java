package flaxbeard.cyberware.common.block.tile;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.BlockBeaconPost;

public class TileEntityBeaconPost extends TileEntity
{
	public static class TileEntityBeaconPostMaster extends TileEntityBeaconPost
	{
		@SideOnly(Side.CLIENT)
		@Nonnull
		@Override
		public AxisAlignedBB getRenderBoundingBox()
		{
			return new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 10, pos.getZ() + 2);
		}
		
		@Override
		public void setMasterLoc(BlockPos start)
		{
			throw new IllegalStateException("NO");
		}
	}
	
	public BlockPos master = null;
	public boolean destructing = false;


	@SideOnly(Side.CLIENT)
	@Override
	public double getMaxRenderDistanceSquared()
	{
		return 16384.0D;
	}
	
	public void setMasterLoc(BlockPos start)
	{
		this.master = start;
		world.notifyBlockUpdate(pos, world.getBlockState(getPos()), world.getBlockState(getPos()), 2);
		this.markDirty();
	}
	
	@Override
	public void invalidate()
	{

		super.invalidate();
	}

	public void destruct()
	{
		if (!destructing)
		{
			destructing = true;
			for (int y = 0; y <= 9; y++)
			{
				for (int x = -1; x <= 1; x++)
				{
					for (int z = -1; z <= 1; z++)
					{
						if (y > 3 && (x != 0 || z != 0))
						{
							continue;
						}
						
						BlockPos newPos = pos.add(x, y, z);
			
						IBlockState state = world.getBlockState(newPos);
						Block block = state.getBlock();
						if (block == CyberwareContent.radioPost && state.getValue(BlockBeaconPost.TRANSFORMED) > 0)
						{
							world.getTileEntity(newPos);
							world.setBlockState(newPos, state.withProperty(BlockBeaconPost.TRANSFORMED, 0), 2);
							
						}
					
					}
				}
			}
		}
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		
		if (!(this instanceof TileEntityBeaconPostMaster))
		{
			int x = tagCompound.getInteger("xx");
			int y = tagCompound.getInteger("yy");
			int z = tagCompound.getInteger("zz");
			this.master = new BlockPos(x, y, z);
		}
		
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
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);
		
		if (!(this instanceof TileEntityBeaconPostMaster))
		{
			tagCompound.setInteger("xx", master.getX());
			tagCompound.setInteger("yy", master.getY());
			tagCompound.setInteger("zz", master.getZ());
		}
				
		return tagCompound;
		
	}
	

}
