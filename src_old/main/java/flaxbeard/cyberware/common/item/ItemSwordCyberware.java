package flaxbeard.cyberware.common.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class ItemSwordCyberware extends ItemSword implements IDeconstructable
{

	public ItemSwordCyberware(String name, ToolMaterial material)
	{
		super(material);
		
		setRegistryName(name);
		ForgeRegistries.ITEMS.register(this);
		setTranslationKey(Cyberware.MODID + "." + name);
        
		setCreativeTab(Cyberware.creativeTab);
		
        CyberwareContent.items.add(this);
	}

	@Override
	public boolean canDestroy(ItemStack stack)
	{
		return true;
	}

	@Override
	public NonNullList<ItemStack> getComponents(ItemStack stack)
	{
		return NNLUtil.fromArray(new ItemStack[]
				{
					new ItemStack(Items.IRON_INGOT, 2, 0),
					new ItemStack(CyberwareContent.component, 1, 2),
					new ItemStack(CyberwareContent.component, 1, 4)
				});
	}

}
