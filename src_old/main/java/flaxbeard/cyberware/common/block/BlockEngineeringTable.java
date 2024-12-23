package flaxbeard.cyberware.common.block;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable.TileEntityEngineeringDummy;

public class BlockEngineeringTable extends BlockContainer
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyEnum<EnumEngineeringHalf> HALF = PropertyEnum.create("half", EnumEngineeringHalf.class);
	public final Item itemBlock;

	public BlockEngineeringTable()
	{
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, EnumEngineeringHalf.LOWER));

		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "engineering_table";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		
		itemBlock = new ItemEngineeringTable(this, "cyberware.tooltip.engineering_table");
		itemBlock.setRegistryName(name);
		ForgeRegistries.ITEMS.register(itemBlock);
		
		setTranslationKey(Cyberware.MODID + "." + name);
		itemBlock.setTranslationKey(Cyberware.MODID + "." + name);
		
		itemBlock.setCreativeTab(Cyberware.creativeTab);
		
		GameRegistry.registerTileEntity(TileEntityEngineeringTable.class, new ResourceLocation(Cyberware.MODID, name));
		GameRegistry.registerTileEntity(TileEntityEngineeringDummy.class, new ResourceLocation(Cyberware.MODID, name + "Dummy"));
		
		CyberwareContent.items.add(itemBlock);
	}
	
	private static final AxisAlignedBB s = new AxisAlignedBB(4F / 16F, 0F, 0F / 16F, 12F / 16F, 1F, 12F / 16F);
	private static final AxisAlignedBB n = new AxisAlignedBB(4F / 16F, 0F, 4F / 16F, 12F / 16F, 1F, 16F / 16F);
	private static final AxisAlignedBB e = new AxisAlignedBB(0F / 16F, 0F, 4F / 16F, 12F / 16F, 1F, 12F / 16F);
	private static final AxisAlignedBB w = new AxisAlignedBB(4F / 16F, 0F, 4F / 16F, 16F / 16F, 1F, 12F / 16F);
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
		if (state.getValue(HALF) == EnumEngineeringHalf.UPPER)
		{
			EnumFacing face = state.getValue(FACING);
			switch (face)
			{
				case NORTH:
					return s;
				case SOUTH:
					return n;
				case EAST:
					return w;
				case WEST:
					return e;
			}
		}

		return super.getBoundingBox(state, source, pos);
		
	}
	
	@Nonnull
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
	{
		return new ItemStack(itemBlock);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		if (state.getValue(HALF) == EnumEngineeringHalf.UPPER)
		{
			BlockPos blockpos = pos.down();
			IBlockState iblockstate = worldIn.getBlockState(blockpos);

			if (iblockstate.getBlock() != this)
			{
				worldIn.setBlockToAir(pos);
			}
			else if (blockIn != this)
			{
				iblockstate.neighborChanged(worldIn, blockpos, blockIn, fromPos);
			}
		}
		else
		{
			BlockPos blockpos1 = pos.up();
			IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);

			if (iblockstate1.getBlock() != this)
			{
				worldIn.setBlockToAir(pos);
				if (!worldIn.isRemote)
				{
					this.dropBlockAsItem(worldIn, pos, state, 0);
				}
			}
		}
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer entityPlayer)
	{
		BlockPos blockpos = pos.down();
		BlockPos blockpos1 = pos.up();

		if (entityPlayer.capabilities.isCreativeMode && state.getValue(HALF) == EnumEngineeringHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this)
		{
			worldIn.setBlockToAir(blockpos);
		}

		if (state.getValue(HALF) == EnumEngineeringHalf.LOWER && worldIn.getBlockState(blockpos1).getBlock() == this)
		{
			if (entityPlayer.capabilities.isCreativeMode)
			{
				worldIn.setBlockToAir(pos);
			}

			worldIn.setBlockToAir(blockpos1);
		}
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if (stack.hasDisplayName())
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if (tileentity instanceof TileEntityEngineeringTable)
			{
				((TileEntityEngineeringTable) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata)
	{
		return (metadata & 1) > 0 ? new TileEntityEngineeringTable() : new TileEntityEngineeringDummy();
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}
	

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState,
	                                EntityPlayer entityPlayer, EnumHand hand,
	                                EnumFacing side, float hitX, float hitY, float hitZ)
	{
		boolean top = blockState.getValue(HALF) == EnumEngineeringHalf.UPPER;
		BlockPos checkPos = top ? pos : pos.add(0, 1, 0);
		TileEntity tileentity = world.getTileEntity(checkPos);
		
		if (tileentity instanceof TileEntityEngineeringTable)
		{
			entityPlayer.openGui(Cyberware.INSTANCE, 2, world, checkPos.getX(), checkPos.getY(), checkPos.getZ());
		}
		
		return true;
		
	}
	
	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState blockState)
	{ 
		boolean top = blockState.getValue(HALF) == EnumEngineeringHalf.UPPER;
		if (top)
		{
			TileEntity tileentity = world.getTileEntity(pos);
			
			if ( tileentity instanceof TileEntityEngineeringTable
			  && !world.isRemote )
			{
				TileEntityEngineeringTable engineering = (TileEntityEngineeringTable) tileentity;
				
				for (int indexSlot = 0; indexSlot < engineering.slots.getSlots(); indexSlot++)
				{
					ItemStack stack = engineering.slots.getStackInSlot(indexSlot);
					if (!stack.isEmpty())
					{
						InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
					}
				}
			}
			super.breakBlock(world, pos, blockState);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int metadata)
	{
		return this.getDefaultState()
				.withProperty(HALF, (metadata & 1) > 0 ? EnumEngineeringHalf.UPPER : EnumEngineeringHalf.LOWER)
				.withProperty(FACING, EnumFacing.byHorizontalIndex(metadata >> 1));
	}
	
	@Override
	public int getMetaFromState(IBlockState blockState)
	{
		return (blockState.getValue(FACING).getHorizontalIndex() << 1)
		     + (blockState.getValue(HALF) == EnumEngineeringHalf.UPPER ? 1 : 0);
	}
	
	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, HALF, FACING);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		return pos.getY() < worldIn.getHeight() - 1
		    && super.canPlaceBlockAt(worldIn, pos)
		    && super.canPlaceBlockAt(worldIn, pos.up());
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getPushReaction(IBlockState blockState)
	{
		return EnumPushReaction.DESTROY;
	}
	
	@Nonnull
	@Override
	public Item getItemDropped(IBlockState blockState, Random rand, int fortune)
	{
		return blockState.getValue(HALF) == EnumEngineeringHalf.UPPER ? Items.AIR : this.itemBlock;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState blockState)
	{
		return blockState.getValue(HALF) == EnumEngineeringHalf.LOWER;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(IBlockState blockState)
	{
		return blockState.getValue(HALF) == EnumEngineeringHalf.LOWER;
	}
	
	public enum EnumEngineeringHalf implements IStringSerializable
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
}
