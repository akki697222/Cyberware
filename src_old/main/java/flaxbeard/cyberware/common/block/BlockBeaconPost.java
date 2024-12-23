package flaxbeard.cyberware.common.block;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityBeaconPost;
import flaxbeard.cyberware.common.block.tile.TileEntityBeaconPost.TileEntityBeaconPostMaster;

public class BlockBeaconPost extends BlockContainer
{
	/** Whether this fence connects in the northern direction */
	public static final PropertyBool NORTH = PropertyBool.create("north");
	/** Whether this fence connects in the eastern direction */
	public static final PropertyBool EAST = PropertyBool.create("east");
	/** Whether this fence connects in the southern direction */
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	/** Whether this fence connects in the western direction */
	public static final PropertyBool WEST = PropertyBool.create("west");
	
	public static final PropertyInteger TRANSFORMED = PropertyInteger.create("transformed", 0, 2);

	
	protected static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[] {new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.625D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
	public static final AxisAlignedBB PILLAR_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1D, 0.625D);
	public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.625D, 0.625D, 1D, 1.0D);
	public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 0.375D, 1D, 0.625D);
	public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1D, 0.375D);
	public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.625D, 0.0D, 0.375D, 1.0D, 1D, 0.625D);

	public BlockBeaconPost()
	{
		super(Material.IRON);
		
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "radio_post";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		

		ItemBlock itemBlock = new ItemBlockCyberware(this,
				"cyberware.tooltip.beacon_post.0",
				"cyberware.tooltip.beacon_post.1",
				"cyberware.tooltip.beacon_post.2");
		itemBlock.setRegistryName(name);
		ForgeRegistries.ITEMS.register(itemBlock);
		
		setTranslationKey(Cyberware.MODID + "." + name);
		
		setCreativeTab(Cyberware.creativeTab);
		
		CyberwareContent.blocks.add(this);
		
		GameRegistry.registerTileEntity(TileEntityBeaconPost.class, new ResourceLocation(Cyberware.MODID, name));
		GameRegistry.registerTileEntity(TileEntityBeaconPostMaster.class, new ResourceLocation(Cyberware.MODID, name + "_master"));

		setDefaultState(blockState.getBaseState()
				.withProperty(TRANSFORMED, 0)
				.withProperty(NORTH, Boolean.FALSE)
				.withProperty(EAST, Boolean.FALSE)
				.withProperty(SOUTH, Boolean.FALSE)
				.withProperty(WEST, Boolean.FALSE));
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase placer, ItemStack itemStack)
	{
		super.onBlockPlacedBy(world, blockPos, blockState, placer, itemStack);
		
		for (int y = -9; y <= 0; y++)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int z = -1; z <= 1; z++)
				{
					BlockPos start = blockPos.add(x, y, z);
					
					boolean isCompleted = complete(world, start);
					if (isCompleted)
					{
						return;
					}
				}
			}
		}
	}
	
	private boolean complete(World world, BlockPos start)
	{
		// validate the structure
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
					
					BlockPos newPos = start.add(x, y, z);
					
					IBlockState state = world.getBlockState(newPos);
					Block block = state.getBlock();
					if (block != this || state.getValue(TRANSFORMED) != 0)
					{
						return false;
					}
				}
			}
		}
		
		// update the block states
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
					
					BlockPos newPos = start.add(x, y, z);
					
					if (newPos.equals(start))
					{
						world.setBlockState(newPos, world.getBlockState(newPos).withProperty(TRANSFORMED, 2), 2);
					}
					else
					{
						world.setBlockState(newPos, world.getBlockState(newPos).withProperty(TRANSFORMED, 1), 2);
						
						TileEntityBeaconPost post = (TileEntityBeaconPost) world.getTileEntity(newPos);
						post.setMasterLoc(start);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
	                                  @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes,
	                                  @Nullable Entity entity, boolean isActualState) {
		state = state.getActualState(world, pos);
		
		if (state.getValue(TRANSFORMED) > 0)
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, getBoundingBox(state, world, pos));
			return;
		}
		
		addCollisionBoxToList(pos, entityBox, collidingBoxes, PILLAR_AABB);

		if (state.getValue(NORTH))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
		}

		if (state.getValue(EAST))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
		}

		if (state.getValue(SOUTH))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
		}

		if (state.getValue(WEST))
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		state = this.getActualState(state, source, pos);
		return BOUNDING_BOXES[getBoundingBoxIdx(state)];
	}

	/**
	 * Returns the correct index into boundingBoxes, based on what the fence is connected to.
	 */
	private static int getBoundingBoxIdx(IBlockState state)
	{
		int i = 0;

		if (state.getValue(NORTH))
		{
			i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
		}

		if (state.getValue(EAST))
		{
			i |= 1 << EnumFacing.EAST.getHorizontalIndex();
		}

		if (state.getValue(SOUTH))
		{
			i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
		}

		if (state.getValue(WEST))
		{
			i |= 1 << EnumFacing.WEST.getHorizontalIndex();
		}

		return i;
	}

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
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

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
	{
		return false;
	}

	public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos)
	{
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();
		return block != Blocks.BARRIER
		    && ( (block instanceof BlockBeaconPost && block.getMaterial(iblockstate) == this.material)
		      || block instanceof BlockFenceGate
		      || ((block.getMaterial(iblockstate).isOpaque() && iblockstate.isFullCube()) && block.getMaterial(iblockstate) != Material.GOURD) );
	}

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side)
	{
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState,
	                                EntityPlayer entityPlayer, EnumHand hand,
	                                EnumFacing facing, float hitX, float hitY, float hitZ) {
		return world.isRemote
		    || ItemLead.attachToFence(entityPlayer, world, pos);
	}

	public int getMetaFromState(IBlockState blockState)
	{
		return blockState.getValue(TRANSFORMED);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata)
	{
		return this.getDefaultState().withProperty(TRANSFORMED, metadata);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		return state.withProperty(NORTH, this.canConnectTo(worldIn, pos.north()))
		            .withProperty(EAST, this.canConnectTo(worldIn, pos.east()))
		            .withProperty(SOUTH, this.canConnectTo(worldIn, pos.south()))
		            .withProperty(WEST, this.canConnectTo(worldIn, pos.west()));
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState withRotation(@Nonnull IBlockState blockState, Rotation rotation)
	{
		switch (rotation)
		{
		case CLOCKWISE_180:
			return blockState.withProperty(NORTH, blockState.getValue(SOUTH)).withProperty(EAST, blockState.getValue(WEST)).withProperty(SOUTH, blockState.getValue(NORTH)).withProperty(WEST, blockState.getValue(EAST));
		case COUNTERCLOCKWISE_90:
			return blockState.withProperty(NORTH, blockState.getValue(EAST)).withProperty(EAST, blockState.getValue(SOUTH)).withProperty(SOUTH, blockState.getValue(WEST)).withProperty(WEST, blockState.getValue(NORTH));
		case CLOCKWISE_90:
			return blockState.withProperty(NORTH, blockState.getValue(WEST)).withProperty(EAST, blockState.getValue(NORTH)).withProperty(SOUTH, blockState.getValue(EAST)).withProperty(WEST, blockState.getValue(SOUTH));
		default:
			return blockState;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState withMirror(@Nonnull IBlockState blockState, Mirror mirrorIn)
	{
		switch (mirrorIn)
		{
			case LEFT_RIGHT:
				return blockState.withProperty(NORTH, blockState.getValue(SOUTH)).withProperty(SOUTH, blockState.getValue(NORTH));
			case FRONT_BACK:
				return blockState.withProperty(EAST, blockState.getValue(WEST)).withProperty(WEST, blockState.getValue(EAST));
			default:
				return super.withMirror(blockState, mirrorIn);
		}
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH, TRANSFORMED);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return state.getValue(TRANSFORMED) > 0 ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata)
	{
		switch (metadata)
		{
		case 2:
			return new TileEntityBeaconPostMaster();
		case 1:
			return new TileEntityBeaconPost();
		default:
			return null;
		}
	}
	
	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState blockState)
	{
		if ( world != null
		  && blockState.getValue(TRANSFORMED) > 0 )
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityBeaconPost)
			{
				TileEntityBeaconPost tileEntityBeaconPost = (TileEntityBeaconPost) tileEntity;
				if (blockState.getValue(TRANSFORMED) == 2)
				{
					tileEntityBeaconPost.destruct();
				}
				else if ( tileEntityBeaconPost.master != null
				       && !tileEntityBeaconPost.master.equals(pos)
				       && !tileEntityBeaconPost.destructing )
				{
					TileEntity masterTe = world.getTileEntity(tileEntityBeaconPost.master);
					
					if (masterTe instanceof TileEntityBeaconPost)
					{
						TileEntityBeaconPost post2 = (TileEntityBeaconPost) masterTe;
						
						if ( post2 instanceof TileEntityBeaconPostMaster
						  && !post2.destructing )
						{
							post2.destruct();
						}
					}
				}
			}
		}
	}
	
    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entityLivingBase)
    {
    	return state.getValue(TRANSFORMED) > 0;
    }
    
}