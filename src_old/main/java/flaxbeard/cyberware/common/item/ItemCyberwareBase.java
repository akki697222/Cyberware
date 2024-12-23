package flaxbeard.cyberware.common.item;

import javax.annotation.Nonnull;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemCyberwareBase extends Item
{
	public String[] subnames;
	private ItemStack[] itemStackCache;

	public ItemCyberwareBase(String name, String... subnames)
	{
		super();
		
		setRegistryName(name);
		ForgeRegistries.ITEMS.register(this);
		setTranslationKey(Cyberware.MODID + "." + name);
        
		setCreativeTab(Cyberware.creativeTab);
				
		this.subnames = subnames;
		itemStackCache = new ItemStack[Math.max(subnames.length, 1)];

		setHasSubtypes(this.subnames.length > 0);
		setMaxDamage(0);

        CyberwareContent.items.add(this);
	}
	
	@Nonnull
	@Override
	public String getTranslationKey(ItemStack itemstack)
	{
		int damage = itemstack.getItemDamage();
		if (damage >= subnames.length)
		{
			return super.getTranslationKey();
		}
		return super.getTranslationKey(itemstack) + "." + subnames[damage];
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

	public ItemStack getCachedStack(int damage)
	{
		ItemStack itemStack = itemStackCache[damage];
		if ( itemStack != null
		  && ( itemStack.getItem() != this
		    || itemStack.getCount() != 1
		    || getDamage(itemStack) != damage ) )
		{
			Cyberware.logger.error(String.format("Corrupted item stack cache: found %s as %s:%d, expected %s:%d",
			                                     itemStack, itemStack.getItem(), itemStack.getItemDamage(),
			                                     this, damage ));
			itemStack = null;
		}
		if (itemStack == null)
		{
			itemStack = new ItemStack(this, 1, damage);
			itemStackCache[damage] = itemStack;
		}
		return itemStack;
	}
}
