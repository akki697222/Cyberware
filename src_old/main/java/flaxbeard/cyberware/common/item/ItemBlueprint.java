package flaxbeard.cyberware.common.item;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mojang.realmsclient.gui.ChatFormatting;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.IBlueprint;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.NNLUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBlueprint extends Item implements IBlueprint
{
	public ItemBlueprint(String name)
	{
		super();
		
		setRegistryName(name);
		ForgeRegistries.ITEMS.register(this);
		setTranslationKey(Cyberware.MODID + "." + name);
		
		setCreativeTab(Cyberware.creativeTab);
				
		setHasSubtypes(true);
		setMaxStackSize(1);

		CyberwareContent.items.add(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if ( tagCompound != null
		  && tagCompound.hasKey("blueprintItem") )
		{
			GameSettings settings = Minecraft.getMinecraft().gameSettings;
			if (settings.isKeyDown(settings.keyBindSneak))
			{
				ItemStack blueprintItem = new ItemStack(tagCompound.getCompoundTag("blueprintItem"));
				if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
				{
					NonNullList<ItemStack> items = NNLUtil.copyList(CyberwareAPI.getComponents(blueprintItem));
					tooltip.add(I18n.format("cyberware.tooltip.blueprint", blueprintItem.getDisplayName()));
					for (ItemStack item : items)
					{
						if (!item.isEmpty())
						{
							tooltip.add(item.getCount() + " x " + item.getDisplayName());
						}
					}
					return;
				}
			}
			else
			{
				tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("cyberware.tooltip.shift_prompt"));
				return;
			}
		}
		tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("cyberware.tooltip.craft_blueprint"));
	}
	
	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list)
	{
		if (this.isInCreativeTab(tab)) {
			list.add(new ItemStack(this, 1, 1));
		}
	}
	
	public static ItemStack getBlueprintForItem(ItemStack stack)
	{
		if (!stack.isEmpty() && CyberwareAPI.canDeconstruct(stack))
		{
			ItemStack toBlue = stack.copy();
			

			toBlue.setCount(1);
			if (toBlue.isItemStackDamageable())
			{
				toBlue.setItemDamage(0);
			}
			toBlue.setTagCompound(null);
			
			ItemStack ret = new ItemStack(CyberwareContent.blueprint);
			NBTTagCompound tagCompound = new NBTTagCompound();
			tagCompound.setTag("blueprintItem", toBlue.writeToNBT(new NBTTagCompound()));
			
			ret.setTagCompound(tagCompound);
			return ret;
		}
		else
		{
			return ItemStack.EMPTY;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Nonnull
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if ( tagCompound != null
		  && tagCompound.hasKey("blueprintItem") )
		{
			ItemStack blueprintItem = new ItemStack(tagCompound.getCompoundTag("blueprintItem"));
			if (!blueprintItem.isEmpty())
			{
				return I18n.format("item.cyberware.blueprint.not_blank.name", blueprintItem.getDisplayName()).trim();
			}
		}
		return ("" + I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
	}

	@Override
	public ItemStack getResult(ItemStack stack, NonNullList<ItemStack> craftingItems)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if ( tagCompound != null
		  && tagCompound.hasKey("blueprintItem") )
		{
			ItemStack blueprintItem = new ItemStack(tagCompound.getCompoundTag("blueprintItem"));
			if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
			{
				NonNullList<ItemStack> requiredItems = NNLUtil.copyList(CyberwareAPI.getComponents(blueprintItem));
				for (ItemStack requiredItem : requiredItems) {
					ItemStack required = requiredItem.copy();
					boolean satisfied = false;
					for (ItemStack crafting : craftingItems) {
						if (!crafting.isEmpty() && !required.isEmpty()) {
							if (crafting.getItem() == required.getItem() && crafting.getItemDamage() == required.getItemDamage() && (!required.hasTagCompound() || (ItemStack.areItemStackTagsEqual(required, crafting)))) {
								required.shrink(crafting.getCount());
							}
							if (required.getCount() <= 0) {
								satisfied = true;
								break;
							}
						}
					}
					if (!satisfied) return ItemStack.EMPTY;
				}
				
				return blueprintItem;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> consumeItems(ItemStack stack, NonNullList<ItemStack> craftingItems)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if ( tagCompound != null
		  && tagCompound.hasKey("blueprintItem") )
		{
			ItemStack blueprintItem = new ItemStack(tagCompound.getCompoundTag("blueprintItem"));
			if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
			{
				NonNullList<ItemStack> requiredItems = NNLUtil.copyList(CyberwareAPI.getComponents(blueprintItem));
				NonNullList<ItemStack> newCrafting = NonNullList.create();
				newCrafting.addAll(craftingItems);
				for (ItemStack requiredItem : requiredItems) {
					ItemStack required = requiredItem.copy();
					for (int c = 0; c < newCrafting.size(); c++) {
						ItemStack crafting = newCrafting.get(c);
						if (!crafting.isEmpty() && !required.isEmpty()) {
							if (crafting.getItem() == required.getItem() && crafting.getItemDamage() == required.getItemDamage() && (!required.hasTagCompound() || (ItemStack.areItemStackTagsEqual(required, crafting)))) {
								int toSubtract = Math.min(required.getCount(), crafting.getCount());
								required.shrink(toSubtract);
								crafting.shrink(toSubtract);
								if (crafting.getCount() <= 0) {
									crafting = ItemStack.EMPTY;
								}
								newCrafting.set(c, crafting);
							}
							if (required.getCount() <= 0) {
								break;
							}
						}
					}
				}
				
				return newCrafting;
			}
		}
		throw new IllegalStateException("Consuming items when items shouldn't be consumed!");
	}

	@Override
	public NonNullList<ItemStack> getRequirementsForDisplay(ItemStack stack)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if ( tagCompound != null
		  && tagCompound.hasKey("blueprintItem") )
		{
			ItemStack blueprintItem = new ItemStack(tagCompound.getCompoundTag("blueprintItem"));
			if (!blueprintItem.isEmpty() && CyberwareAPI.canDeconstruct(blueprintItem))
			{
				return CyberwareAPI.getComponents(blueprintItem);
			}
		}
		
		return NonNullList.create();
	}

	@Override
	public ItemStack getIconForDisplay(ItemStack stack)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (  tagCompound != null
		  && tagCompound.hasKey("blueprintItem") )
		{
			return new ItemStack(tagCompound.getCompoundTag("blueprintItem"));
		}
		
		return ItemStack.EMPTY;
	}
}
