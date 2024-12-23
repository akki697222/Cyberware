package flaxbeard.cyberware.common.block.item;

import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import flaxbeard.cyberware.Cyberware;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemComponentBox extends ItemBlockCyberware
{

	public ItemComponentBox(Block block)
	{
		super(block);
		this.setMaxStackSize(1);
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer entityPlayer, @Nonnull EnumHand hand)
	{
		ItemStack itemStackIn = entityPlayer.getHeldItem(hand);
		entityPlayer.openGui(Cyberware.INSTANCE, 6, worldIn, 0, 0, 0);
		return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
	}
	
	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer entityPlayer, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (entityPlayer.isSneaking())
		{
			EnumActionResult res = super.onItemUse(entityPlayer, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			if (res == EnumActionResult.SUCCESS && entityPlayer.isCreative())
			{
				entityPlayer.inventory.mainInventory.set(entityPlayer.inventory.currentItem, ItemStack.EMPTY);
			}
			return res;
		}
		else
		{
			entityPlayer.openGui(Cyberware.INSTANCE, 6, worldIn, 0, 0, 0);
		}
		return EnumActionResult.SUCCESS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced)
	{
		tooltip.add(ChatFormatting.GRAY + I18n.format("cyberware.tooltip.component_box"));
		tooltip.add(ChatFormatting.GRAY + I18n.format("cyberware.tooltip.component_box2"));
	}
	
}
