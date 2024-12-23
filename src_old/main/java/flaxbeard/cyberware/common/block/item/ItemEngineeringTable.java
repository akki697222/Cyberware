package flaxbeard.cyberware.common.block.item;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import flaxbeard.cyberware.common.block.BlockEngineeringTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemEngineeringTable extends Item implements ICyberwareTabItem
{
	private Block block;
	private String[] tt;

	public ItemEngineeringTable(Block block, String... tooltip)
	{
		this.block = block;
		this.tt = tooltip;
	}
	
	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer entityPlayer, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = entityPlayer.getHeldItem(hand);
		if (facing != EnumFacing.UP)
		{
			return EnumActionResult.FAIL;
		}
		else
		{
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if (!block.isReplaceable(worldIn, pos))
			{
				pos = pos.offset(facing);
			}

			if (entityPlayer.canPlayerEdit(pos, facing, stack) && this.block.canPlaceBlockAt(worldIn, pos))
			{
				EnumFacing enumfacing = EnumFacing.fromAngle(entityPlayer.rotationYaw);
				placeDoor(worldIn, pos, enumfacing, this.block);
				SoundType soundtype = this.block.getSoundType();
				worldIn.playSound(entityPlayer, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);
				return EnumActionResult.SUCCESS;
			}
			else
			{
				return EnumActionResult.FAIL;
			}
		}
	}

	public static void placeDoor(World worldIn, BlockPos pos, EnumFacing facing, Block door)
	{
		BlockPos blockpos2 = pos.up();
		
		IBlockState iblockstate = door.getDefaultState().withProperty(BlockEngineeringTable.FACING, facing);
		worldIn.setBlockState(pos, iblockstate.withProperty(BlockEngineeringTable.HALF, BlockEngineeringTable.EnumEngineeringHalf.LOWER), 2);
		worldIn.setBlockState(blockpos2, iblockstate.withProperty(BlockEngineeringTable.HALF, BlockEngineeringTable.EnumEngineeringHalf.UPPER), 2);
		worldIn.notifyNeighborsOfStateChange(pos, door, true);
		worldIn.notifyNeighborsOfStateChange(blockpos2, door, true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced)
	{
		if (this.tt != null)
		{
			for (String str : tt)
			{
				tooltip.add(ChatFormatting.GRAY + I18n.format(str));
			}
		}
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.BLOCKS;
	}
}