package flaxbeard.cyberware.common.integration.botania;

import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.ItemCyberware;
import flaxbeard.cyberware.common.misc.NNLUtil;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BotaniaIntegration
{
    public static final String MOD_ID = "botania";
    public static ItemCyberware manaLens;
    
    public static void preInit()
    {
        ItemStack stackManaglass = new ItemStack(Blocks.GLASS); // CyberwareContent.getItemStackByRegistryName("botania:managlass", 0);
        ItemStack stackManasteelIngot = new ItemStack(Items.IRON_INGOT); // CyberwareContent.getItemStackByRegistryName("botania:manaresource", 0);
        
        manaLens = new ItemManaLens("manaseer_lens", ICyberware.EnumSlot.EYES, new String[] { "lens", "link" });
        manaLens.setEssenceCost(1, 1);
        manaLens.setWeights(CyberwareContent.COMMON, CyberwareContent.COMMON);
        manaLens.setComponents(NNLUtil.fromArray(new ItemStack[] { stackManaglass,
                                                                   stackManasteelIngot,
                                                                   new ItemStack(CyberwareContent.component, 1, 6),
                                                                   new ItemStack(CyberwareContent.component, 1, 7) }),
                               NNLUtil.fromArray(new ItemStack[] { stackManaglass,
                                                                   stackManasteelIngot,
                                                                   new ItemStack(CyberwareContent.component, 1, 6),
                                                                   new ItemStack(CyberwareContent.component, 1, 5) }) );
    }
}
