package flaxbeard.cyberware.common.block.tile;

import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.BlockBeaconLarge;
import flaxbeard.cyberware.common.block.BlockBeaconPost;
import flaxbeard.cyberware.common.lib.LibConstants;

public class TileEntityBeaconLarge extends TileEntityBeacon implements ITickable
{
	private boolean wasWorking = false;
	private int count = 0;
	
	private static int TIER = 3;
	
	@Override
	public void update()
	{
		IBlockState master = world.getBlockState(pos.add(0, -10, 0));
		
		boolean powered = world.isBlockPowered(pos.add(1, -10, 0))
				|| world.isBlockPowered(pos.add(-1, -10, 0))
				|| world.isBlockPowered(pos.add(0, -10, 1))
				|| world.isBlockPowered(pos.add(0, -10, -1));
		boolean working = !powered && master.getBlock() == CyberwareContent.radioPost && master.getValue(BlockBeaconPost.TRANSFORMED) == 2;
		
		if (!wasWorking && working)
		{
			this.enable();
		}
		
		if (wasWorking && !working)
		{
			disable();
		}
		
		wasWorking = working;
		
		if (world.isRemote && working)
		{
			count = (count + 1) % 20;
			if (count == 0)
			{
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() == CyberwareContent.radioLarge)
				{
					boolean ns = state.getValue(BlockBeaconLarge.FACING) == EnumFacing.EAST || state.getValue(BlockBeaconLarge.FACING) == EnumFacing.WEST;
					float dist = .5F;
					float speedMod = .2F;
					int degrees = 45;
					for (int index = 0; index < 18; index++)
					{
						float sin = (float) Math.sin(Math.toRadians(degrees));
						float cos = (float) Math.cos(Math.toRadians(degrees));
						float xOffset = dist * sin;
						float yOffset = .2F + dist * cos;
						float xSpeed = speedMod * sin;
						float ySpeed = speedMod * cos;
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, 
								pos.getX() + .5F + (ns ? xOffset : 0), 
								pos.getY() + .5F + yOffset, 
								pos.getZ() + .5F + (ns ? 0 : xOffset), 
								ns ? xSpeed : 0, 
								ySpeed, 
								ns ? 0 : xSpeed,
								255, 255, 255 );
						
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, 
								pos.getX() + .5F - (ns ? xOffset : 0), 
								pos.getY() + .5F + yOffset, 
								pos.getZ() + .5F - (ns ? 0 : xOffset), 
								ns ? -xSpeed : 0, 
								ySpeed, 
								ns ? 0 : -xSpeed,
								255, 255, 255 );
	
						degrees += 5;
					}
				}
			}
		}
	}
	
	private void disable()
	{
		Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(TIER, world);
		mapBeaconPosition.remove(getPos());
	}

	private void enable()
	{
		Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(TIER, world);
		if (!mapBeaconPosition.containsKey(getPos()))
		{
			mapBeaconPosition.put(getPos(), LibConstants.LARGE_BEACON_RANGE);
		}
	}
	
	@Override
	public void invalidate()
	{
		disable();
		super.invalidate();
	}
}
