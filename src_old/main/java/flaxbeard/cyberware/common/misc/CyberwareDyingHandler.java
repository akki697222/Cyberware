package flaxbeard.cyberware.common.misc;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

import com.google.common.collect.Lists;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;

public class CyberwareDyingHandler implements IRecipe
{
	static
	{
		RecipeSorter.register(Cyberware.MODID + ":Cyberware.MODID + \":dying\"", CyberwareDyingHandler.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
	}

	private IRecipe realRecipe;

	public CyberwareDyingHandler()
	{
		this.realRecipe = this;
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(Cyberware.MODID, "dying");
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return true;
	}

	@Override
	public IRecipe setRegistryName(ResourceLocation name)
	{
		return this.realRecipe.setRegistryName(name);
	}

	@Override
	public Class<IRecipe> getRegistryType()
	{
		return this.realRecipe.getRegistryType();
	}

	@Override
	public boolean matches(InventoryCrafting inventoryCrafting, @Nonnull World world)
	{
		ItemStack itemStackArmor = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		
		for (int indexSlot = 0; indexSlot < inventoryCrafting.getSizeInventory(); indexSlot++)
		{
			ItemStack itemStackInSlot = inventoryCrafting.getStackInSlot(indexSlot);
			
			if (!itemStackInSlot.isEmpty())
			{
				if (itemStackInSlot.getItem() instanceof ItemArmor)
				{
					ItemArmor itemarmor = (ItemArmor)itemStackInSlot.getItem();

					if ( itemarmor.getArmorMaterial() != CyberwareContent.trenchMat
					  || !itemStackArmor.isEmpty() )
					{
						return false;
					}
					
					itemStackArmor = itemStackInSlot;
				}
				else
				{
					if (itemStackInSlot.getItem() != Items.DYE)
					{
						return false;
					}
					
					list.add(itemStackInSlot);
				}
			}
		}
		
		return !itemStackArmor.isEmpty() && !list.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		int[] aint = new int[3];
		int i = 0;
		int j = 0;
		ItemArmor itemarmor = null;

		for (int indexSlot = 0; indexSlot < inventoryCrafting.getSizeInventory(); indexSlot++)
		{
			ItemStack itemstack1 = inventoryCrafting.getStackInSlot(indexSlot);

			if (!itemstack1.isEmpty())
			{
				if (itemstack1.getItem() instanceof ItemArmor)
				{
					itemarmor = (ItemArmor)itemstack1.getItem();

					if (itemarmor.getArmorMaterial() != CyberwareContent.trenchMat || !itemstack.isEmpty())
					{
						return ItemStack.EMPTY;
					}

					itemstack = itemstack1.copy();
					itemstack.setCount(1);

					if (itemarmor.hasColor(itemstack1))
					{
						int l = itemarmor.getColor(itemstack);
						float f = (float)(l >> 16 & 255) / 255.0F;
						float f1 = (float)(l >> 8 & 255) / 255.0F;
						float f2 = (float)(l & 255) / 255.0F;
						i = (int)((float)i + Math.max(f, Math.max(f1, f2)) * 255.0F);
						aint[0] = (int)((float)aint[0] + f * 255.0F);
						aint[1] = (int)((float)aint[1] + f1 * 255.0F);
						aint[2] = (int)((float)aint[2] + f2 * 255.0F);
						++j;
					}
				}
				else
				{
					if (itemstack1.getItem() != Items.DYE)
					{
						return ItemStack.EMPTY;
					}

					float[] afloat = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(itemstack1.getMetadata()));
					int l1 = (int)(afloat[0] * 255.0F);
					int i2 = (int)(afloat[1] * 255.0F);
					int j2 = (int)(afloat[2] * 255.0F);
					i += Math.max(l1, Math.max(i2, j2));
					aint[0] += l1;
					aint[1] += i2;
					aint[2] += j2;
					++j;
				}
			}
		}

		if (itemarmor == null)
		{
			return ItemStack.EMPTY;
		}
		else
		{
			int i1 = aint[0] / j;
			int j1 = aint[1] / j;
			int k1 = aint[2] / j;
			float f3 = (float)i / (float)j;
			float f4 = (float)Math.max(i1, Math.max(j1, k1));
			i1 = (int)((float)i1 * f3 / f4);
			j1 = (int)((float)j1 * f3 / f4);
			k1 = (int)((float)k1 * f3 / f4);
			int lvt_12_3_ = (i1 << 8) + j1;
			lvt_12_3_ = (lvt_12_3_ << 8) + k1;
			itemarmor.setColor(itemstack, lvt_12_3_);
			return itemstack;
		}
	}
	
	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}
	
	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inventoryCrafting)
	{
		NonNullList<ItemStack> nnlItemStack = NonNullList.create();

		for (int indexSlot = 0; indexSlot < inventoryCrafting.getSizeInventory(); indexSlot++)
		{
			ItemStack itemstack = inventoryCrafting.getStackInSlot(indexSlot);
			nnlItemStack.add(net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
		}

		return nnlItemStack;
	}
}