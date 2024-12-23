package flaxbeard.cyberware.common.block.tile;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.item.ItemBrainUpgrade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.BlockBeaconLarge;
import flaxbeard.cyberware.common.lib.LibConstants;

public class TileEntityBeacon extends TileEntity implements ITickable
{
	private static List<Integer> tiers = new CopyOnWriteArrayList<>();
	private static Map<Integer, Map<Integer, Map<BlockPos, Integer>>> mapBeaconPositionByTierDimension = new HashMap<>();
	private boolean wasWorking = false;
	private int count = 0;
	
	private static int TIER = 2;
	
	private static Map<Integer, Map<BlockPos, Integer>> getBeaconPositionsForTier(int tier)
	{
		Map<Integer, Map<BlockPos, Integer>> mapBeaconPositionByDimension = mapBeaconPositionByTierDimension.get(tier);
		if (mapBeaconPositionByDimension == null)
		{
			mapBeaconPositionByDimension = new HashMap<>();
			mapBeaconPositionByTierDimension.put(tier, mapBeaconPositionByDimension);
			if (!tiers.contains(tier))
			{
				tiers.add(tier);
				Collections.sort(tiers);
				Collections.reverse(tiers);
			}
		}
		
		return mapBeaconPositionByDimension;
	}
	
	public static Map<BlockPos, Integer> getBeaconPositionsForTierAndDimension(int tier, @Nonnull World world)
	{
		Map<Integer, Map<BlockPos, Integer>> mapBeaconPositionByDimension = getBeaconPositionsForTier(tier);
		int idDimension = world.provider.getDimension();
		return mapBeaconPositionByDimension.computeIfAbsent(idDimension, k -> new HashMap<>());
	}
	
	@Override
	public void update()
	{			
		boolean working = !world.isBlockPowered(pos);
		
		if (!wasWorking && working)
		{
			enable();
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
				if (state.getBlock() == CyberwareContent.radio)
				{
					boolean ns = state.getValue(BlockBeaconLarge.FACING) == EnumFacing.NORTH
					          || state.getValue(BlockBeaconLarge.FACING) == EnumFacing.SOUTH;
					boolean backwards = state.getValue(BlockBeaconLarge.FACING) == EnumFacing.SOUTH
					                 || state.getValue(BlockBeaconLarge.FACING) == EnumFacing.EAST;
					float dist = .2F;
					float speedMod = .08F;
					int degrees = 45;
					for (int index = 0; index < 5; index++)
					{
						float sin = (float) Math.sin(Math.toRadians(degrees));
						float cos = (float) Math.cos(Math.toRadians(degrees));
						float xOffset = dist * sin;
						float yOffset = .2F + dist * cos;
						float xSpeed = speedMod * sin;
						float ySpeed = speedMod * cos;
						float backOffsetX = (backwards ^ ns ? -.3F : .3F);
						float backOffsetZ = (backwards ? .4F : -.4F);

						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, 
								pos.getX() + .5F + (ns ? xOffset + backOffsetX : backOffsetZ), 
								pos.getY() + .5F + yOffset, 
								pos.getZ() + .5F + (ns ? backOffsetZ : xOffset + backOffsetX), 
								ns ? xSpeed : 0, 
								ySpeed, 
								ns ? 0 : xSpeed,
								255, 255, 255 );
						
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, 
								pos.getX() + .5F + (ns ? -xOffset + backOffsetX : backOffsetZ), 
								pos.getY() + .5F + yOffset, 
								pos.getZ() + .5F + (ns ? backOffsetZ : -xOffset + backOffsetX), 
								ns ? -xSpeed : 0, 
								ySpeed, 
								ns ? 0 : -xSpeed,
								255, 255, 255 );
	
						degrees += 18;
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
			mapBeaconPosition.put(getPos(), LibConstants.BEACON_RANGE);
		}
	}
	
	@Override
	public void invalidate()
	{
		disable();
		super.invalidate();
	}
	
	public static int isInRange(World world, double posX, double posY, double posZ)
	{
		for (int tier : tiers)
		{
			Map<BlockPos, Integer> mapBeaconPosition = getBeaconPositionsForTierAndDimension(tier, world);
			for (Entry<BlockPos, Integer> entry : mapBeaconPosition.entrySet())
			{
				double squareDistance = (posX - entry.getKey().getX()) * (posX - entry.getKey().getX())
				                      + (posZ - entry.getKey().getZ()) * (posZ - entry.getKey().getZ());
				if (squareDistance < entry.getValue() * entry.getValue())
				{
					return tier;
				}
			}
		}
		
		List<EntityLivingBase> entitiesInRange = world.getEntitiesWithinAABB(EntityPlayer.class,
				new AxisAlignedBB(posX - LibConstants.BEACON_RANGE_INTERNAL, 0, posZ - LibConstants.BEACON_RANGE_INTERNAL,
				                  posX + LibConstants.BEACON_RANGE_INTERNAL, 255, posZ + LibConstants.BEACON_RANGE_INTERNAL) );
		
		ItemStack itemStackRadioRaw = CyberwareContent.brainUpgrades.getCachedStack(ItemBrainUpgrade.META_RADIO);
		for (EntityLivingBase entityInRange : entitiesInRange)
		{
			if (ItemBrainUpgrade.isRadioWorking(entityInRange))
			{
				ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityInRange);
				if (cyberwareUserData != null)
				{
					ItemStack itemStackRadio = cyberwareUserData.getCyberware(itemStackRadioRaw);
					if ( !itemStackRadio.isEmpty()
					  && EnableDisableHelper.isEnabled(itemStackRadio) )
					{
						return 1;
					}
				}
			}
		}
		
		return -1;
	}
}
