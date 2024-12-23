package flaxbeard.cyberware.common;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.UpdateConfigPacket;

public class CyberwareConfig
{
    public static CyberwareConfig INSTANCE = new CyberwareConfig();
    
    public static float ENGINEERING_CHANCE = 15F;
    public static float SCANNER_CHANCE = 10F;
    public static float SCANNER_CHANCE_ADDL = 10F;
    public static int SCANNER_TIME = 24000;
    
    public static int ESSENCE = 100;
    public static int CRITICAL_ESSENCE = 25;
    
    public static boolean MOBS_ENABLE_CYBER_ZOMBIES = true;
    public static int MOBS_CYBER_ZOMBIE_WEIGHT = 15;
    public static int MOBS_CYBER_ZOMBIE_MIN_PACK = 1;
    public static int MOBS_CYBER_ZOMBIE_MAX_PACK = 1;
    public static HashSet<Integer> MOBS_DIMENSION_IDS = new HashSet<>();
    public static boolean MOBS_IS_DIMENSION_BLACKLIST = true;
    public static boolean MOBS_APPLY_DIMENSION_TO_SPAWNING = true;
    public static boolean MOBS_APPLY_DIMENSION_TO_BEACON = true;
    
    public static boolean MOBS_ADD_CLOTHES = true;
    public static float MOBS_CYBER_ZOMBIE_DROP_RARITY = 50.0F;
    public static float MOBS_CLOTH_DROP_RARITY = 50.0F;
    
    public static int HUDR = 76;
    public static int HUDG = 255;
    public static int HUDB = 0;
    
    public static boolean ENABLE_FLOAT = false;
    public static float HUDLENS_FLOAT = 0.1F;
    public static float HUDJACK_FLOAT = 0.05F;
    
    public static boolean SURGERY_CRAFTING = false;
    
    private static String[][] defaultStartingItems;
    private static String[][] startingItems;
    private static NonNullList<NonNullList<ItemStack>> startingStacks;
    
    public static boolean DEFAULT_DROP = false;
    public static boolean DEFAULT_KEEP = false;
    
    public static float DROP_CHANCE = 100F;
    
    public static boolean ENABLE_KATANA = true;
    public static boolean ENABLE_CLOTHES = true;
    public static boolean ENABLE_CUSTOM_PLAYER_MODEL = true;
    
    public static int TESLA_PER_POWER = 1;
    
    public static String FIST_MINING_TOOL_NAME = "minecraft:iron_pickaxe";

    public static boolean INT_ENDER_IO = true;
    public static boolean INT_TOUGH_AS_NAILS = true;
    public static boolean INT_BOTANIA = true;
    public static boolean INT_MATTER_OVERDRIVE = true;

    public static Configuration config;

    public static File configDirectory;

    private static final String C_MOBS = "Mobs";
    private static final String C_OTHER = "Other";
    private static final String C_HUD = "HUD";
    private static final String C_MACHINES = "Machines";
    private static final String C_ESSENCE = "Essence";
    private static final String C_GAMERULES = "Gamerules";
    private static final String C_INTEGRATION = "Integration";


    public static void preInit(@Nonnull FMLPreInitializationEvent event)
    {
        configDirectory = event.getModConfigurationDirectory();
        config = new Configuration(new File(event.getModConfigurationDirectory(), Cyberware.MODID + ".cfg"));
        startingItems = defaultStartingItems = new String[EnumSlot.values().length][0];
        startingStacks = NonNullList.create();
        for (EnumSlot slot : EnumSlot.values())
        {
            NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
            for (int indexSlot = 0; indexSlot < LibConstants.WARE_PER_SLOT; indexSlot++)
            {
                nnlCyberwaresInSlot.add(ItemStack.EMPTY);
            }
            startingStacks.add(nnlCyberwaresInSlot);
        }
        
        int metadata = 0;
        for (int index = 0; index < EnumSlot.values().length; index++)
        {
            if (EnumSlot.values()[index].hasEssential())
            {
                if (EnumSlot.values()[index].isSided())
                {
                    defaultStartingItems[index] = new String[] { "cyberware:body_part 1 " + metadata, "cyberware:body_part 1 " + (metadata + 1)  };
                    metadata += 2;
                }
                else
                {
                    defaultStartingItems[index] = new String[] { "cyberware:body_part 1 " + metadata };
                    metadata++;
                }
            }
            else
            {
                defaultStartingItems[index] = new String[0];
            }
        }
        loadConfig();
        
        config.load();
        for (int index = 0; index < EnumSlot.values().length; index++)
        {
            EnumSlot slot = EnumSlot.values()[index];
            startingItems[index] = config.getStringList("Default augments for " + slot.getName() + " slot",
                    "Defaults", defaultStartingItems[index], "Use format 'id amount metadata'");
        }
        config.save();

    }

    public static void loadConfig()
    {
        config.load();
        
        MOBS_ENABLE_CYBER_ZOMBIES = config.getBoolean("CyberZombies are enabled", C_MOBS, MOBS_ENABLE_CYBER_ZOMBIES, "");
        MOBS_CYBER_ZOMBIE_WEIGHT = config.getInt("CyberZombies spawning weight", C_MOBS, MOBS_CYBER_ZOMBIE_WEIGHT, 0, Integer.MAX_VALUE, "Vanilla Zombie = 100, Enderman = 10, Witch = 5");
        MOBS_CYBER_ZOMBIE_MIN_PACK = config.getInt("CyberZombies minimum pack size", C_MOBS, MOBS_CYBER_ZOMBIE_MIN_PACK, 0, Integer.MAX_VALUE, "Vanilla Zombie = 4, Enderman = 1, Witch = 1");
        MOBS_CYBER_ZOMBIE_MAX_PACK = config.getInt("CyberZombies maximum pack size", C_MOBS, MOBS_CYBER_ZOMBIE_MAX_PACK, 0, Integer.MAX_VALUE, "Vanilla Zombie = 4, Enderman = 4, Witch = 1");
        int[] dimensionIds = config.get(C_MOBS, "Dimensions ids", new int[] {}).getIntList();
        MOBS_DIMENSION_IDS = new HashSet<>(dimensionIds.length);
        for (int dimensionId : dimensionIds) MOBS_DIMENSION_IDS.add(dimensionId);
        MOBS_IS_DIMENSION_BLACKLIST = config.getBoolean("Dimension ids is a blacklist?", C_MOBS, MOBS_IS_DIMENSION_BLACKLIST, "");
        MOBS_APPLY_DIMENSION_TO_SPAWNING = config.getBoolean("Dimension ids applies to natural spawning?", C_MOBS, MOBS_APPLY_DIMENSION_TO_SPAWNING, "");
        MOBS_APPLY_DIMENSION_TO_BEACON = config.getBoolean("Dimension ids applies to beacon, radio & cranial broadcaster?", C_MOBS, MOBS_APPLY_DIMENSION_TO_BEACON, "");
        
        MOBS_ADD_CLOTHES = config.getBoolean("Add Cyberware clothing to mobs", C_MOBS, MOBS_ADD_CLOTHES, "");
        MOBS_CYBER_ZOMBIE_DROP_RARITY = config.getFloat("Percent chance a CyberZombie drops a cyberware", C_MOBS, MOBS_CYBER_ZOMBIE_DROP_RARITY, 0F, 100F, "");
        MOBS_CLOTH_DROP_RARITY = config.getFloat("Percent chance a Cyberware clothing is dropped", C_MOBS, MOBS_CLOTH_DROP_RARITY, 0F, 100F, "");
        
        SURGERY_CRAFTING = config.getBoolean("Enable crafting recipe for Robosurgeon", C_OTHER, SURGERY_CRAFTING, "Normally only found in Nether fortresses");
        TESLA_PER_POWER = config.getInt("RF/Tesla per internal power unit", C_OTHER, TESLA_PER_POWER, 0, Integer.MAX_VALUE, "");
        
        ESSENCE = config.getInt("Maximum Essence", C_ESSENCE, ESSENCE, 0, Integer.MAX_VALUE, "");
        CRITICAL_ESSENCE = config.getInt("Critical Essence value, where rejection begins", C_ESSENCE, CRITICAL_ESSENCE, 0, Integer.MAX_VALUE, "");
        
        DEFAULT_DROP = config.getBoolean("Default for gamerule cyberware_dropCyberware", C_GAMERULES, DEFAULT_DROP, "Determines if players drop their Cyberware on death. Does not change settings on existing worlds, use /gamerule for that. Overridden if cyberware_keepCyberware is true");
        DEFAULT_KEEP = config.getBoolean("Default for gamerule cyberware_keepCyberware", C_GAMERULES, DEFAULT_KEEP, "Determines if players keep their Cyberware between lives. Does not change settings on existing worlds, use /gamerule for that.");
        
        DROP_CHANCE = config.getFloat("Chance of successful drop", C_GAMERULES, DROP_CHANCE, 0F, 100F, "If dropCyberware enabled, chance for a piece of Cyberware to successfuly drop instead of being destroyed.");
        
        ENGINEERING_CHANCE = config.getFloat("Chance of blueprint from Engineering Table", C_MACHINES, ENGINEERING_CHANCE, 0, 100F, "");
        SCANNER_CHANCE = config.getFloat("Chance of blueprint from Scanner", C_MACHINES, SCANNER_CHANCE, 0, 50F, "");
        SCANNER_CHANCE_ADDL = config.getFloat("Additive chance for Scanner per extra item", C_MACHINES, SCANNER_CHANCE_ADDL, 0, 100F, "");
        SCANNER_TIME = config.getInt("Ticks taken per Scanner operation", C_MACHINES, SCANNER_TIME, 0, Integer.MAX_VALUE, "24000 is one Minecraft day, 1200 is one real-life minute");
        
        ENABLE_KATANA = config.getBoolean("Enable Katana", C_OTHER, ENABLE_KATANA, "");
        ENABLE_CLOTHES = config.getBoolean("Enable Trench Coat, Mirror Shades, and Biker Jacket", C_OTHER, ENABLE_CLOTHES, "");
        ENABLE_CUSTOM_PLAYER_MODEL = config.getBoolean("Enable changes to player model (missing skin, missing limbs, Cybernetic limbs)", C_OTHER, ENABLE_CUSTOM_PLAYER_MODEL, "");
        
        ENABLE_FLOAT = config.getBoolean("Enable hudlens and hudjack float.", C_HUD, ENABLE_FLOAT, "Experimental, defaults to false.");
        
        HUDJACK_FLOAT = config.getFloat("Amount hudjack HUD will 'float' with movement. Set to 0 for no float.", C_HUD, HUDJACK_FLOAT, 0F, 100F, "");
        HUDLENS_FLOAT = config.getFloat("Amount hudlens HUD will 'float' with movement. Set to 0 for no float.", C_HUD, HUDLENS_FLOAT, 0F, 100F, "");
        
        INT_ENDER_IO = config.getBoolean("Enable EnderIO Integration if the mod is Loaded", C_INTEGRATION, INT_ENDER_IO, "Requires EnderIO" );
        INT_TOUGH_AS_NAILS = config.getBoolean("Enable Tough As Nails Integration if the mod is Loaded", C_INTEGRATION, INT_TOUGH_AS_NAILS, "Requires Tough as Nails" );
        INT_BOTANIA = config.getBoolean("Enable Botania Integration if the mod is Loaded", C_INTEGRATION, INT_BOTANIA, "Requires Botania");
        INT_MATTER_OVERDRIVE = config.getBoolean("Enable Matter Overdrive Integration if the mod is Loaded", C_INTEGRATION, INT_MATTER_OVERDRIVE, "Requires Matter Overdrive");
        
        FIST_MINING_TOOL_NAME = config.getString("Registry name of the mining tool equivalent to the reinforced fist", C_OTHER, FIST_MINING_TOOL_NAME, "");
        
        config.save();
    }


    public static void postInit()
    {
        int index = 0;
        for (String[] items : startingItems)
        {
            EnumSlot slot = EnumSlot.values()[index];
            if (items.length > LibConstants.WARE_PER_SLOT)
            {
                throw new RuntimeException("Cyberware configuration error! Too many items for slot " + slot.getName());
            }
            
            for (int indexItem = 0; indexItem < items.length; indexItem++)
            {
                String itemEncoded = items[indexItem];
                String[] params = itemEncoded.split("\\s+");
                
                String itemName;
                int metadata;
                int quantity;
                
                if (params.length == 1)
                {
                    itemName = params[0];
                    metadata = 0;
                    quantity = 0;
                }
                else if (params.length == 3)
                {
                    itemName = params[0];
                    try
                    {
                        metadata = Integer.parseInt(params[2]);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new RuntimeException("Cyberware configuration error! Item " + (indexItem + 1) + " for "
                                + slot.getName() + " slot has invalid metadata: '" + params[2] + "'");
                    }
                    try
                    {
                        quantity = Integer.parseInt(params[1]);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new RuntimeException("Cyberware configuration error! Item " + (indexItem + 1) + " for "
                                + slot.getName() + " slot has invalid quantity: '" + params[1] + "'");
                    }
                }
                else
                {
                    throw new RuntimeException("Cyberware configuration error! Item " + (indexItem + 1) + " for "
                            + slot.getName() + " slot has too many arguments!");
                }

                Item item;
                try
                {
                    item = CommandBase.getItemByText(null, itemName);
                }
                catch (NumberInvalidException e)
                {
                    throw new RuntimeException("Cyberware configuration error! Item '" + (indexItem + 1) + "' for "
                            + slot.getName() + " slot has a nonexistant item: " + itemName);
                }

                ItemStack stack = new ItemStack(item, quantity, metadata);

                if (!CyberwareAPI.isCyberware(stack))
                {
                    throw new RuntimeException("Cyberware configuration error! " + itemName + " is not a valid piece of cyberware!");
                }
                if ((CyberwareAPI.getCyberware(stack)).getSlot(stack) != slot)
                {
                    throw new RuntimeException("Cyberware configuration error! " + itemEncoded + " will not fit in slot " + slot.getName());
                }

                startingStacks.get(index).set(indexItem, stack);
            }

            index++;
        }
    }
    
    public static NonNullList<ItemStack> getStartingItems(@Nonnull EnumSlot slot)
    {
        return startingStacks.get(slot.ordinal());
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldUnload(@Nonnull WorldEvent.Unload event)
    {
        if ( event.getWorld().isRemote
          && !Minecraft.getMinecraft().getConnection().getNetworkManager().isChannelOpen() )
        {
            loadConfig();
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogin(@Nonnull PlayerLoggedInEvent event)
    {
        EntityPlayer entityPlayer = event.player;
        World world = entityPlayer.world;
        
        if (!world.isRemote)
        {
            CyberwarePacketHandler.INSTANCE.sendTo(new UpdateConfigPacket(), (EntityPlayerMP) entityPlayer);
        }
    }
    
    @SubscribeEvent
    public void onConfigurationChangedEvent(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equalsIgnoreCase(Cyberware.MODID))
        {
            loadConfig();
        }
    }

}