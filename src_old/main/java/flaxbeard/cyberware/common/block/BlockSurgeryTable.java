
package flaxbeard.cyberware.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;

public class BlockSurgeryTable extends BlockBed
{
	
	public BlockSurgeryTable()
	{
		String name = "surgeryTable";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		
		setTranslationKey(Cyberware.MODID + "." + name);

		GameRegistry.registerTileEntity(TileEntitySurgery.class, new ResourceLocation(Cyberware.MODID, name));
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState,
	                                EntityPlayer entityPlayer, EnumHand hand,
	                                EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
		{
			return true;
		}
		
		if (blockState.getValue(PART) != BlockBed.EnumPartType.HEAD)
		{
			pos = pos.offset(blockState.getValue(FACING));
			blockState = world.getBlockState(pos);

			if (blockState.getBlock() != this)
			{
				return true;
			}
		}

		if ( world.provider.canRespawnHere()
		  && world.getBiome(pos) != Biomes.HELL )
		{
			if (blockState.getValue(OCCUPIED))
			{
				EntityPlayer entityplayer = this.getPlayerInBed(world, pos);

				if (entityplayer != null)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.occupied"));
					return true;
				}
				
				blockState = blockState.withProperty(OCCUPIED, Boolean.FALSE);
				world.setBlockState(pos, blockState, 4);
			}

			EntityPlayer.SleepResult entityplayer$sleepresult = entityPlayer.trySleep(pos);

			if (entityplayer$sleepresult == EntityPlayer.SleepResult.OK)
			{
				blockState = blockState.withProperty(OCCUPIED, Boolean.TRUE);
				world.setBlockState(pos, blockState, 4);
				return true;
			}
			else
			{
				if (entityplayer$sleepresult == EntityPlayer.SleepResult.NOT_POSSIBLE_NOW)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.noSleep"));
				}
				else if (entityplayer$sleepresult == EntityPlayer.SleepResult.NOT_SAFE)
				{
					entityPlayer.sendMessage(new TextComponentTranslation("tile.bed.notSafe"));
				}

				return true;
			}
		}
		else
		{
			world.setBlockToAir(pos);
			BlockPos blockpos = pos.offset(blockState.getValue(FACING).getOpposite());

			if (world.getBlockState(blockpos).getBlock() == this)
			{
				world.setBlockToAir(blockpos);
			}

			world.newExplosion(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 5.0F, true, true);
			return true;
		}
	}
	
	@Nullable
	private EntityPlayer getPlayerInBed(World worldIn, BlockPos pos)
	{
		for (EntityPlayer entityplayer : worldIn.playerEntities)
		{
			if (entityplayer.isPlayerSleeping() && entityplayer.getPosition().equals(pos))
			{
				return entityplayer;
			}
		}

		return null;
	}
	
	@Override
	public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, Entity player)
	{
		return true;
	}
	
	@SubscribeEvent
	public void handleSleep(PlayerSleepInBedEvent event)
	{
		// no operation
	}
}
