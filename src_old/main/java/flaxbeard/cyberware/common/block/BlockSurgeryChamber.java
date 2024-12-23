package flaxbeard.cyberware.common.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemSurgeryChamber;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgeryChamber;

public class BlockSurgeryChamber extends BlockContainer
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool OPEN = PropertyBool.create("open");
	public static final PropertyEnum<EnumChamberHalf> HALF = PropertyEnum.create("half", EnumChamberHalf.class);
	public final Item itemBlock;
	
	public BlockSurgeryChamber()
	{
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState()
		                                    .withProperty(FACING, EnumFacing.NORTH)
		                                    .withProperty(OPEN, Boolean.FALSE)
		                                    .withProperty(HALF, EnumChamberHalf.LOWER));
		
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "surgery_chamber";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		
		itemBlock = new ItemSurgeryChamber(this, "cyberware.tooltip.surgery_chamber.0", "cyberware.tooltip.surgery_chamber.1");
		itemBlock.setRegistryName(name);
		ForgeRegistries.ITEMS.register(itemBlock);
		
		setTranslationKey(Cyberware.MODID + "." + name);
		itemBlock.setTranslationKey(Cyberware.MODID + "." + name);
		
		itemBlock.setCreativeTab(Cyberware.creativeTab);
		
		GameRegistry.registerTileEntity(TileEntitySurgeryChamber.class, new ResourceLocation(Cyberware.MODID, name));
		
		CyberwareContent.items.add(itemBlock);
	}
	
	private static final AxisAlignedBB top    = new AxisAlignedBB(       0F, 15F / 16F,        0F,       1F,       1F,       1F);
	private static final AxisAlignedBB south  = new AxisAlignedBB(       0F,        0F,        0F,       1F,       1F, 1F / 16F);
	private static final AxisAlignedBB north  = new AxisAlignedBB(       0F,        0F, 15F / 16F,       1F,       1F,       1F);
	private static final AxisAlignedBB east   = new AxisAlignedBB(       0F,        0F,        0F, 1F / 16F,       1F,       1F);
	private static final AxisAlignedBB west   = new AxisAlignedBB(15F / 16F,        0F,        0F,       1F,       1F,       1F);
	private static final AxisAlignedBB bottom = new AxisAlignedBB(       0F,        0F,        0F,       1F, 1F / 16F,       1F);
	
	@Nonnull
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
	{
		return new ItemStack(itemBlock);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
	                                  @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes,
	                                  @Nullable Entity entity, boolean isActualState)
	{
		EnumFacing face = state.getValue(FACING);
		boolean open = state.getValue(OPEN);
		
		if (state.getValue(HALF) == EnumChamberHalf.UPPER)
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, top);
			if (!open || face != EnumFacing.SOUTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, south);
			}
			if (!open || face != EnumFacing.NORTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, north);
			}
			if (!open || face != EnumFacing.EAST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, east);
			}
			if (!open || face != EnumFacing.WEST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, west);
			}
		}
		else
		{		
			addCollisionBoxToList(pos, entityBox, collidingBoxes, bottom);
			if (!open || face != EnumFacing.SOUTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, south);
			}
			if (!open || face != EnumFacing.NORTH)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, north);
			}
			if (!open || face != EnumFacing.EAST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, east);
			}
			if (!open || face != EnumFacing.WEST)
			{
				addCollisionBoxToList(pos, entityBox, collidingBoxes, west);
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState,
	                                EntityPlayer entityPlayer, EnumHand hand,
	                                EnumFacing side, float hitX, float hitY, float hitZ)
	{
		boolean top = blockState.getValue(HALF) == EnumChamberHalf.UPPER;
		if (canOpen(top ? pos : pos.up(), world))
		{
			toggleDoor(top, blockState, pos, world);
			
			notifySurgeon(top ? pos : pos.up(), world);
		}
		
		return true;
	}
	
	public void toggleDoor(boolean top, IBlockState blockState, BlockPos pos, World worldIn)
	{
		IBlockState blockStateNew = blockState.cycleProperty(OPEN);
		worldIn.setBlockState(pos, blockStateNew, 2);
		
		BlockPos otherPos = pos.up();
		if (top)
		{
			otherPos = pos.down();
		}
		IBlockState otherState = worldIn.getBlockState(otherPos);

		if (otherState.getBlock() == this)
		{
			otherState = otherState.cycleProperty(OPEN);
			worldIn.setBlockState(otherPos, otherState, 2);
		}
	}
	
	private boolean canOpen(BlockPos pos, World worldIn)
	{
		TileEntity above = worldIn.getTileEntity(pos.up());
		
		if (above instanceof TileEntitySurgery)
		{
			return ((TileEntitySurgery) above).canOpen();
		}
		return true;
	}
	
	
	private void notifySurgeon(BlockPos pos, World worldIn)
	{
		TileEntity above = worldIn.getTileEntity(pos.up());
		
		if (above instanceof TileEntitySurgery)
		{
			((TileEntitySurgery) above).notifyChange();
		}
	}
	
	@Override
	public void neighborChanged(IBlockState blockState, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		if (blockState.getValue(HALF) == EnumChamberHalf.UPPER)
		{
			BlockPos blockpos = pos.down();
			IBlockState iblockstate = world.getBlockState(blockpos);

			if (iblockstate.getBlock() != this)
			{
				world.setBlockToAir(pos);
			}
			else if (blockIn != this)
			{
				iblockstate.neighborChanged(world, blockpos, blockIn, fromPos);
			}
		}
		else
		{
			BlockPos blockpos1 = pos.up();
			IBlockState iblockstate1 = world.getBlockState(blockpos1);

			if (iblockstate1.getBlock() != this)
			{
				world.setBlockToAir(pos);
				if (!world.isRemote)
				{
					dropBlockAsItem(world, pos, blockState, 0);
				}
			}
		}
	}
	
	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return state.getValue(HALF) == EnumChamberHalf.UPPER ? Items.AIR : this.itemBlock;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		return pos.getY() < worldIn.getHeight() - 1
		    && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)
		    && super.canPlaceBlockAt(worldIn, pos)
		    && super.canPlaceBlockAt(worldIn, pos.up());
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getPushReaction(IBlockState state)
	{
		return EnumPushReaction.DESTROY;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata)
	{
		return this.getDefaultState()
				.withProperty(HALF, (metadata & 1) > 0 ? EnumChamberHalf.UPPER : EnumChamberHalf.LOWER)
				.withProperty(OPEN, (metadata & 2) > 0)
				.withProperty(FACING, EnumFacing.byHorizontalIndex(metadata >> 2));
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState)
	{
		return (blockState.getValue(FACING).getHorizontalIndex() << 2)
		     + (blockState.getValue(HALF) == EnumChamberHalf.UPPER ? 1 : 0)
		     + (blockState.getValue(OPEN) ? 2 : 0);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, HALF, FACING, OPEN);
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer entityPlayer)
	{
		BlockPos blockpos = pos.down();
		BlockPos blockpos1 = pos.up();

		if (entityPlayer.capabilities.isCreativeMode && state.getValue(HALF) == EnumChamberHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this)
		{
			worldIn.setBlockToAir(blockpos);
		}

		if (state.getValue(HALF) == EnumChamberHalf.LOWER && worldIn.getBlockState(blockpos1).getBlock() == this)
		{
			if (entityPlayer.capabilities.isCreativeMode)
			{
				worldIn.setBlockToAir(pos);
			}

			worldIn.setBlockToAir(blockpos1);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}
	
	
	public enum EnumChamberHalf implements IStringSerializable
	{
		UPPER,
		LOWER;

		public String toString()
		{
			return this.getName();
		}

		public String getName()
		{
			return this == UPPER ? "upper" : "lower";
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}


	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata)
	{
		return (metadata & 1) > 0 ? new TileEntitySurgeryChamber() : null;
	}
}
