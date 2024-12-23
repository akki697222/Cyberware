package flaxbeard.cyberware.common.block;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;

public class BlockBlueprintArchive extends BlockContainer
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public BlockBlueprintArchive()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "blueprint_archive";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		
		ItemBlock itemBlock = new ItemBlockCyberware(this, "cyberware.tooltip.blueprint_archive.0","cyberware.tooltip.blueprint_archive.1");
		itemBlock.setRegistryName(name);
		ForgeRegistries.ITEMS.register(itemBlock);
		
		setTranslationKey(Cyberware.MODID + "." + name);
		
		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntityBlueprintArchive.class, new ResourceLocation(Cyberware.MODID, name));
		
		CyberwareContent.blocks.add(this);
		
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Nonnull
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata)
	{
		return new TileEntityBlueprintArchive();
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState blockState, EntityLivingBase placer, ItemStack stack)
	{
		world.setBlockState(pos, blockState.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		if (stack.hasDisplayName())
		{
			TileEntity tileentity = world.getTileEntity(pos);

			if (tileentity instanceof TileEntityBlueprintArchive)
			{
				((TileEntityBlueprintArchive) tileentity).setCustomInventoryName(stack.getDisplayName());
			}
		}
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState,
	                                EntityPlayer entityPlayer, EnumHand hand,
	                                EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileentity = world.getTileEntity(pos);
		
		if (tileentity instanceof TileEntityBlueprintArchive)
		{
			entityPlayer.openGui(Cyberware.INSTANCE, 4, world, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState blockState)
	{ 
		TileEntity tileentity = world.getTileEntity(pos);

		if ( tileentity instanceof TileEntityBlueprintArchive
		  && !world.isRemote )
		{
			TileEntityBlueprintArchive scanner = (TileEntityBlueprintArchive) tileentity;
			
			for (int indexSlot = 0; indexSlot < scanner.slots.getSlots(); indexSlot++)
			{
				ItemStack stack = scanner.slots.getStackInSlot(indexSlot);
				if (!stack.isEmpty())
				{
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
		
		super.breakBlock(world, pos, blockState);
	}

}
