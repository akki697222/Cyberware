
package flaxbeard.cyberware.common.block;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
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
import flaxbeard.cyberware.common.block.tile.TileEntityBeacon;

public class BlockBeacon extends BlockContainer
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockBeacon()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "beacon";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		
		ItemBlock itemBlock = new ItemBlockCyberware(this, "cyberware.tooltip.beacon");
		itemBlock.setRegistryName(name);
		ForgeRegistries.ITEMS.register(itemBlock);
		
		setTranslationKey(Cyberware.MODID + "." + name);

		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityBeacon.class, new ResourceLocation(Cyberware.MODID, name));
		
		CyberwareContent.blocks.add(this);
	}
	
	//private static final AxisAlignedBB ew = new AxisAlignedBB(5F / 16F, 0F, 3F / 16F, 11F / 16F, 1F, 13F / 16F);
	//private static final AxisAlignedBB ns = new AxisAlignedBB(3F / 16F, 0F, 5F / 16F, 13F / 16F, 1F, 11F / 16F);
	private static final AxisAlignedBB bound = new AxisAlignedBB(1F / 16F, 0F, 1F / 16F, 15F / 16F, 4F / 16F, 15F / 16F);
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return bound;
		/*
		EnumFacing face = state.getValue(FACING);
		if (face == EnumFacing.NORTH || face == EnumFacing.SOUTH)
		{
			return ew;
		}
		else
		{
			return ns;
		}
		*/
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

	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata)
	{
		return new TileEntityBeacon();
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}
	
	@SideOnly(Side.CLIENT)
	@Nonnull
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
	
	@Nonnull
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata)
	{
		EnumFacing enumfacing = EnumFacing.byIndex(metadata);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y)
		{
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState blockState)
	{
		return blockState.getValue(FACING).getIndex();
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState withRotation(@Nonnull IBlockState blockState, Rotation rotation)
	{
		return blockState.withProperty(FACING, rotation.rotate(blockState.getValue(FACING)));
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState withMirror(@Nonnull IBlockState blockState, Mirror mirrorIn)
	{
		return blockState.withRotation(mirrorIn.toRotation(blockState.getValue(FACING)));
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		//return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isFullyOpaque();
		return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isFullBlock();
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		//if (!worldIn.getBlockState(pos.down()).isFullyOpaque())
		if (!worldIn.getBlockState(pos.down()).isFullBlock())
		{
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}

}
