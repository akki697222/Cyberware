package flaxbeard.cyberware.common.block;

import javax.annotation.Nonnull;

import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareConfig;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.item.ItemBlockCyberware;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;

public class BlockSurgery extends BlockContainer
{

	public BlockSurgery()
	{
		super(Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		
		String name = "surgery";
		
		setRegistryName(name);
		ForgeRegistries.BLOCKS.register(this);
		
		ItemBlock itemBlock = new ItemBlockCyberware(this,"cyberware.tooltip.surgery.0", "cyberware.tooltip.surgery.1");
		itemBlock.setRegistryName(name);
		ForgeRegistries.ITEMS.register(itemBlock);
		
		setTranslationKey(Cyberware.MODID + "." + name);
		
		setCreativeTab(Cyberware.creativeTab);
		GameRegistry.registerTileEntity(TileEntitySurgery.class, new ResourceLocation(Cyberware.MODID, name));
		
		CyberwareContent.blocks.add(this);
	}
	
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata)
	{
		return new TileEntitySurgery();
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState blockState)
	{
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState,
	                                EntityPlayer entityPlayer, EnumHand hand,
	                                EnumFacing side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if (tileEntity instanceof TileEntitySurgery)
		{
			TileEntitySurgery tileEntitySurgery = (TileEntitySurgery) tileEntity;
			
			//Ensure the Base Tolerance Attribute has been updated for any Config Changes
			entityPlayer.getEntityAttribute(CyberwareAPI.TOLERANCE_ATTR).setBaseValue(CyberwareConfig.ESSENCE);
			
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			tileEntitySurgery.updatePlayerSlots(entityPlayer, cyberwareUserData);
			entityPlayer.openGui(Cyberware.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState blockState)
	{ 
		TileEntity tileentity = world.getTileEntity(pos);
		
		if ( tileentity instanceof TileEntitySurgery
		  && !world.isRemote )
		{
			TileEntitySurgery surgery = (TileEntitySurgery) tileentity;
			
			for (int indexSlot = 0; indexSlot < surgery.slots.getSlots(); indexSlot++)
			{
				ItemStack stack = surgery.slots.getStackInSlot(indexSlot);
				if (!stack.isEmpty())
				{
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
		
		super.breakBlock(world, pos, blockState);
	}

}
