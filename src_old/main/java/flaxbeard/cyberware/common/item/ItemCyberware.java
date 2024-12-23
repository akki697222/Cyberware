package flaxbeard.cyberware.common.item;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mojang.realmsclient.gui.ChatFormatting;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberwareTabItem;
import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.CyberwareContent.ZombieItem;

public class ItemCyberware extends ItemCyberwareBase implements ICyberware, ICyberwareTabItem, IDeconstructable
{
	private EnumSlot[] slots;
	private int[] essence;
	private NonNullList<NonNullList<ItemStack>> components;
	
	public ItemCyberware(String name, EnumSlot[] slots, String[] subnames)
	{		
		super(name, subnames);
		
		this.slots = slots;
		
		this.essence = new int[subnames.length + 1];
		this.components = NonNullList.create();

	}
	
	public ItemCyberware(String name, EnumSlot slot, String[] subnames)
	{		
		this(name, new EnumSlot[] { slot }, subnames);
	}
	
	public ItemCyberware(String name, EnumSlot slot)
	{
		this(name, slot, new String[0]);
	}
	
	public ItemCyberware setWeights(int... weight)
	{
		assert weight.length == Math.max(1, subnames.length);
		for (int meta = 0; meta < weight.length; meta++)
		{
			ItemStack stack = new ItemStack(this, 1, meta);
			int installedStackSize = installedStackSize(stack);
			stack.setCount(installedStackSize);
			this.setQuality(stack, CyberwareAPI.QUALITY_SCAVENGED);
			CyberwareContent.zombieItems.add(new ZombieItem(weight[meta], stack));
		}
		return this;
	}
	
	public ItemCyberware setEssenceCost(int... essence)
	{
		assert essence.length == Math.max(1, subnames.length);
		this.essence = essence;
		return this;
	}
	
	public ItemCyberware setComponents(NonNullList<ItemStack>... components)
	{
		assert components.length == Math.max(1, subnames.length);
		NonNullList<NonNullList<ItemStack>> list = NonNullList.create();
		Collections.addAll(list, components);
		this.components = list;
		return this;
	}
	
	@Override
	public int getEssenceCost(ItemStack stack)
	{
		int cost = getUnmodifiedEssenceCost(stack);
		if (getQuality(stack) == CyberwareAPI.QUALITY_SCAVENGED)
		{
			float half = cost / 2F;
			if (cost > 0)
			{
				cost = cost + (int) Math.ceil(half);
			}
			else
			{
				cost = cost - (int) Math.ceil(half);
			}
		}
		return cost;
	}
	
	protected int getUnmodifiedEssenceCost(ItemStack stack)
	{
		return essence[Math.min(this.subnames.length, stack.getItemDamage())];
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list)
	{
		if (this.isInCreativeTab(tab)) {
			if (subnames.length == 0)
			{
				list.add(new ItemStack(this));
			}
			for (int metadata = 0; metadata < subnames.length; metadata++)
			{
				list.add(new ItemStack(this, 1, metadata));
			}
		}
	}


	@Override
	public EnumSlot getSlot(ItemStack stack)
	{
		return slots[Math.min(slots.length - 1, getDamage(stack))];
	}

	@Override
	public int installedStackSize(ItemStack stack)
	{
		return 1;
	}

	@Override
	public boolean isIncompatible(ItemStack stack, ItemStack other)
	{
		return false;
	}
	
	@Override
	public boolean isEssential(ItemStack stack)
	{
		return false;		
	}

	@Override
	public List<String> getInfo(ItemStack stack)
	{
		List<String> ret = new ArrayList<>();
		List<String> desc = this.getDesciption(stack);
		if (desc != null && desc.size() > 0)
		{

			ret.addAll(desc);
			
		}
		return ret;
	}
	
	public List<String> getStackDesc(ItemStack stack)
	{
		String[] toReturnArray = I18n.format("cyberware.tooltip." + this.getRegistryName().toString().substring(10)
				+ (this.subnames.length > 0 ? "." + stack.getItemDamage() : "")).split("\\\\n");
		List<String> toReturn = new ArrayList<>(Arrays.asList(toReturnArray));
		
		if (toReturn.size() > 0 && toReturn.get(0).length() == 0)
		{
			toReturn.remove(0);
		}
		
		return toReturn;
	}

	public List<String> getDesciption(ItemStack stack)
	{
		List<String> toReturn = getStackDesc(stack);
		
		if (installedStackSize(stack) > 1)
		{
			toReturn.add(ChatFormatting.BLUE + I18n.format("cyberware.tooltip.max_install", installedStackSize(stack)));
		}
		
		boolean hasPowerConsumption = false;
		String toAddPowerConsumption = "";
		for (int i = 0; i < installedStackSize(stack); i++)
		{
			ItemStack temp = stack.copy();
			temp.setCount(i+1);
			int cost = this.getPowerConsumption(temp);
			if (cost > 0)
			{
				hasPowerConsumption = true;
			}
			
			if (i != 0)
			{
				toAddPowerConsumption += I18n.format("cyberware.tooltip.joiner");
			}
			
			toAddPowerConsumption += " " + cost;
		}
		
		if (hasPowerConsumption)
		{
			String toTranslate = hasCustomPowerMessage(stack) ? 
					"cyberware.tooltip." + this.getRegistryName().toString().substring(10)
					+ (this.subnames.length > 0 ? "." + stack.getItemDamage() : "") + ".power_consumption"
					:
					"cyberware.tooltip.power_consumption";
			toReturn.add(ChatFormatting.GREEN + I18n.format(toTranslate, toAddPowerConsumption));
		}
		
		boolean hasPowerProduction = false;
		String toAddPowerProduction = "";
		for (int i = 0; i < installedStackSize(stack); i++)
		{
			ItemStack temp = stack.copy();
			temp.setCount(i+1);
			int cost = this.getPowerProduction(temp);
			if (cost > 0)
			{
				hasPowerProduction = true;
			}
			
			if (i != 0)
			{
				toAddPowerProduction += I18n.format("cyberware.tooltip.joiner");
			}
			
			toAddPowerProduction += " " + cost;
		}
		
		if (hasPowerProduction)
		{
			String toTranslate = hasCustomPowerMessage(stack) ? 
					"cyberware.tooltip." + this.getRegistryName().toString().substring(10)
					+ (this.subnames.length > 0 ? "." + stack.getItemDamage() : "") + ".power_production"
					:
					"cyberware.tooltip.power_production";
			toReturn.add(ChatFormatting.GREEN + I18n.format(toTranslate, toAddPowerProduction));
		}
		
		if (getCapacity(stack) > 0)
		{
			String toTranslate = hasCustomCapacityMessage(stack) ? 
					"cyberware.tooltip." + this.getRegistryName().toString().substring(10)
					+ (this.subnames.length > 0 ? "." + stack.getItemDamage() : "") + ".capacity"
					:
					"cyberware.tooltip.capacity";
			toReturn.add(ChatFormatting.GREEN + I18n.format(toTranslate, getCapacity(stack)));
		}
		
		
		boolean hasEssenceCost = false;
		boolean essenceCostNegative = true;
		String toAddEssence = "";
		for (int i = 0; i < installedStackSize(stack); i++)
		{
			ItemStack temp = stack.copy();
			temp.setCount(i+1);
			int cost = this.getEssenceCost(temp);
			if (cost != 0)
			{
				hasEssenceCost = true;
			}
			if (cost < 0)
			{
				essenceCostNegative = false;
			}
			
			if (i != 0)
			{
				toAddEssence += I18n.format("cyberware.tooltip.joiner");
			}
			
			toAddEssence += " " + Math.abs(cost);
		}
		
		if (hasEssenceCost)
		{
			toReturn.add(ChatFormatting.DARK_PURPLE + I18n.format(essenceCostNegative ? "cyberware.tooltip.essence" : "cyberware.tooltip.essence_add", toAddEssence));
		}
		

		
		
		return toReturn;
	}
	
	public int getPowerConsumption(ItemStack stack)
	{
		return 0;
	}
	
	public int getPowerProduction(ItemStack stack)
	{
		return 0;
	}
	
	public boolean hasCustomPowerMessage(ItemStack stack)
	{
		return false;
	}
	
	public boolean hasCustomCapacityMessage(ItemStack stack)
	{
		return false;
	}

	@Override
	public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
	{
		return NonNullList.create();
	}

	@Override
	public EnumCategory getCategory(ItemStack stack)
	{
		return EnumCategory.values()[this.getSlot(stack).ordinal()];
	}

	@Override
	public int getCapacity(ItemStack wareStack)
	{
		return 0;
	}

	@Override
	public void onAdded(EntityLivingBase entityLivingBase, ItemStack stack)
	{
		// no operation
	}

	@Override
	public void onRemoved(EntityLivingBase entityLivingBase, ItemStack stack)
	{
		// no operation
	}

	@Override
	public boolean canDestroy(ItemStack stack)
	{
		return stack.getItemDamage() < this.components.size();
	}

	@Override
	public NonNullList<ItemStack> getComponents(ItemStack stack)
	{
		return components.get(Math.min(this.components.size() - 1, stack.getItemDamage()));
	}

	@Override
	public Quality getQuality(ItemStack stack)
	{
		Quality q = CyberwareAPI.getQualityTag(stack);
		
		if (q == null) return CyberwareAPI.QUALITY_MANUFACTURED;
		
		return q;
	}

	@Override
	public ItemStack setQuality(ItemStack stack, Quality quality)
	{
		if (quality == CyberwareAPI.QUALITY_MANUFACTURED)
		{
			if (!stack.isEmpty() && stack.hasTagCompound())
			{
				stack.getTagCompound().removeTag(CyberwareAPI.QUALITY_TAG);
				if (stack.getTagCompound().isEmpty())
				{
					stack.setTagCompound(null);
				}
			}
			return stack;
		}
		return this.canHoldQuality(stack, quality) ? CyberwareAPI.writeQualityTag(stack, quality) : stack;
	}
	
	@SideOnly(Side.CLIENT)
	@Nonnull
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		Quality q = getQuality(stack);
		if (q != null && q.getNameModifier() != null)
		{
			return I18n.format(q.getNameModifier(), ("" + I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim()).trim();
		}
		return ("" + I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
	}

	@Override
	public boolean canHoldQuality(ItemStack stack, Quality quality)
	{
		return true;
	}
}
