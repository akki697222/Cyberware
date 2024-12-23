package flaxbeard.cyberware.common;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import flaxbeard.cyberware.common.handler.RecipeHandler;
import flaxbeard.cyberware.common.integration.enderio.EnderIOIntegration;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.oredict.OreDictionary;
import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.block.BlockBeacon;
import flaxbeard.cyberware.common.block.BlockBeaconLarge;
import flaxbeard.cyberware.common.block.BlockBeaconPost;
import flaxbeard.cyberware.common.block.BlockBlueprintArchive;
import flaxbeard.cyberware.common.block.BlockCharger;
import flaxbeard.cyberware.common.block.BlockComponentBox;
import flaxbeard.cyberware.common.block.BlockEngineeringTable;
import flaxbeard.cyberware.common.block.BlockScanner;
import flaxbeard.cyberware.common.block.BlockSurgery;
import flaxbeard.cyberware.common.block.BlockSurgeryChamber;
import flaxbeard.cyberware.common.effect.PotionNeuropozyne;
import flaxbeard.cyberware.common.entity.EntityCyberZombie;
import flaxbeard.cyberware.common.integration.botania.BotaniaIntegration;
import flaxbeard.cyberware.common.integration.tan.ToughAsNailsIntegration;
import flaxbeard.cyberware.common.item.ItemArmUpgrade;
import flaxbeard.cyberware.common.item.ItemArmorCyberware;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.item.ItemBodyPart;
import flaxbeard.cyberware.common.item.ItemBoneUpgrade;
import flaxbeard.cyberware.common.item.ItemBrainUpgrade;
import flaxbeard.cyberware.common.item.ItemCreativeBattery;
import flaxbeard.cyberware.common.item.ItemCybereyeUpgrade;
import flaxbeard.cyberware.common.item.ItemCybereyes;
import flaxbeard.cyberware.common.item.ItemCyberheart;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import flaxbeard.cyberware.common.item.ItemCyberware;
import flaxbeard.cyberware.common.item.ItemCyberwareBase;
import flaxbeard.cyberware.common.item.ItemDenseBattery;
import flaxbeard.cyberware.common.item.ItemExpCapsule;
import flaxbeard.cyberware.common.item.ItemEyeUpgrade;
import flaxbeard.cyberware.common.item.ItemFootUpgrade;
import flaxbeard.cyberware.common.item.ItemHandUpgrade;
import flaxbeard.cyberware.common.item.ItemHeartUpgrade;
import flaxbeard.cyberware.common.item.ItemLegUpgrade;
import flaxbeard.cyberware.common.item.ItemLowerOrgansUpgrade;
import flaxbeard.cyberware.common.item.ItemLungsUpgrade;
import flaxbeard.cyberware.common.item.ItemMuscleUpgrade;
import flaxbeard.cyberware.common.item.ItemNeuropozyne;
import flaxbeard.cyberware.common.item.ItemSkinUpgrade;
import flaxbeard.cyberware.common.item.ItemSwordCyberware;
import flaxbeard.cyberware.common.item.VanillaWares.SpiderEyeWare;
import flaxbeard.cyberware.common.misc.BlueprintCraftingHandler;
import flaxbeard.cyberware.common.misc.CyberwareDyingHandler;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class CyberwareContent
{
    public static final int RARE = 10;
    public static final int UNCOMMON = 25;
    public static final int COMMON = 50;
    public static final int VERY_COMMON = 100;

    public static Block surgeryApparatus;
    public static BlockSurgeryChamber surgeryChamber;
    public static Block charger;
    public static BlockEngineeringTable engineering;
    public static Block scanner;
    public static Block blueprintArchive;
    public static BlockComponentBox componentBox;
    public static BlockBeacon radio;
    public static BlockBeaconLarge radioLarge;
    public static Block radioPost;

    private static ArmorMaterial shadesMat1;
    private static ArmorMaterial shadesMat2;
    public static Item shades;
    public static Item shades2;

    private static ArmorMaterial jacketMat;
    public static Item jacket;

    public static ArmorMaterial trenchMat;
    public static ItemArmorCyberware trenchCoat;

    private static ToolMaterial katanaMat;
    public static Item katana;

    public static Item bodyPart;
    public static ItemCyberware cybereyes;
    public static ItemCyberware cybereyeUpgrades;
    public static ItemCyberware eyeUpgrades;

    public static ItemCyberware brainUpgrades;
    public static Item expCapsule;
    public static ItemCyberware heartUpgrades;
    public static ItemCyberware cyberheart;
    public static ItemCyberware lungsUpgrades;
    public static ItemCyberware lowerOrgansUpgrades;
    public static ItemCyberware denseBattery;
    public static ItemCyberware skinUpgrades;
    public static ItemCyberware muscleUpgrades;
    public static ItemCyberware boneUpgrades;
    public static ItemCyberware armUpgrades;
    public static ItemCyberware handUpgrades;
    public static ItemCyberware legUpgrades;
    public static ItemCyberware footUpgrades;
    public static ItemCyberware cyberlimbs;
    public static ItemCyberware creativeBattery;

    public static Item component;
    public static Item neuropozyne;
    public static Item blueprint;

    public static Potion neuropozyneEffect;
    public static Potion rejectionEffect;

    public static List<Item> items;
    public static List<Block> blocks;

    public static List<NumItems> numItems;
    public static List<ZombieItem> zombieItems;


    public static void preInit()
    {

        items = new ArrayList<>();
        blocks = new ArrayList<>();

        numItems = new ArrayList<>();
        numItems.add(new NumItems(50, 4));
        numItems.add(new NumItems(25, 3));
        numItems.add(new NumItems(25, 5));
        numItems.add(new NumItems(15, 6));
        numItems.add(new NumItems(5, 10));

        zombieItems = new ArrayList<>();

        if (CyberwareConfig.MOBS_ENABLE_CYBER_ZOMBIES)
        {
            EntityRegistry.registerModEntity(new ResourceLocation(Cyberware.MODID+":cyberzombie"), EntityCyberZombie.class, "cyberzombie", 0, Cyberware.INSTANCE, 80, 3, true);
            EntityRegistry.registerEgg(new ResourceLocation(Cyberware.MODID+":cyberzombie"), 0x6B6B6B, 0x799C65);
            if (Loader.isModLoaded(EnderIOIntegration.MOD_ID))
            {
                FMLInterModComms.sendMessage("EnderIO", "poweredSpawner:blacklist:add", "cyberware.cyberzombie");
            }
        }

        neuropozyneEffect = new PotionNeuropozyne("neuropozyne", false, 0x47453d, 0);
        rejectionEffect = new PotionNeuropozyne("rejection", true, 0xFF0000, 1);

        blueprintArchive = new BlockBlueprintArchive();
        componentBox = new BlockComponentBox();

        surgeryApparatus = new BlockSurgery();
        surgeryChamber = new BlockSurgeryChamber();
        charger = new BlockCharger();
        engineering = new BlockEngineeringTable();
        scanner = new BlockScanner();
        radio = new BlockBeacon();
        radioLarge = new BlockBeaconLarge();
        radioPost = new BlockBeaconPost();

        neuropozyne = new ItemNeuropozyne("neuropozyne");
        blueprint = new ItemBlueprint("blueprint");
        component = new ItemCyberwareBase("component", "actuator", "reactor", "titanium", "ssc", "plating", "fiberoptics", "fullerene", "synthnerves", "storage", "microelectric");

        if (CyberwareConfig.ENABLE_CLOTHES)
        {
            shadesMat1 = EnumHelper.addArmorMaterial("SHADES", "cyberware:vanity", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
            shadesMat1.repairMaterial = new ItemStack(Item.getItemFromBlock(Blocks.GLASS),1,OreDictionary.WILDCARD_VALUE);
            shadesMat2 = EnumHelper.addArmorMaterial("SHADES2", "cyberware:vanity_2", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
            shadesMat2.repairMaterial = new ItemStack(Item.getItemFromBlock(Blocks.GLASS),1,OreDictionary.WILDCARD_VALUE);
            shades = new ItemArmorCyberware("shades", shadesMat1, 0, EntityEquipmentSlot.HEAD);
            shades2 = new ItemArmorCyberware("shades2", shadesMat2, 0, EntityEquipmentSlot.HEAD);

            jacketMat = EnumHelper.addArmorMaterial("JACKET", "cyberware:jacket", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
            jacketMat.repairMaterial = new ItemStack(Items.LEATHER,1,OreDictionary.WILDCARD_VALUE);
            jacket = new ItemArmorCyberware("jacket", jacketMat, 0, EntityEquipmentSlot.CHEST);

            trenchMat = EnumHelper.addArmorMaterial("TRENCHCOAT", "cyberware:trenchcoat", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
            trenchMat.repairMaterial = new ItemStack(Items.LEATHER,1,OreDictionary.WILDCARD_VALUE);
            trenchCoat = new ItemArmorCyberware("trenchcoat", trenchMat, 0, EntityEquipmentSlot.CHEST);
        }

        if (CyberwareConfig.ENABLE_KATANA)
        {
            katanaMat = EnumHelper.addToolMaterial("KATANA",
                    ToolMaterial.IRON.getHarvestLevel(),
                    ToolMaterial.DIAMOND.getMaxUses(),
                    ToolMaterial.DIAMOND.getEfficiency(),
                    ToolMaterial.DIAMOND.getAttackDamage(),
                    ToolMaterial.GOLD.getEnchantability());
            katanaMat.setRepairItem(new ItemStack(component, 1, 4));
            
            katana = new ItemSwordCyberware("katana", katanaMat);
        }

        bodyPart = new ItemBodyPart("body_part",
                new EnumSlot[] { EnumSlot.EYES, EnumSlot.CRANIUM, EnumSlot.HEART, EnumSlot.LUNGS, EnumSlot.LOWER_ORGANS, EnumSlot.SKIN, EnumSlot.MUSCLE, EnumSlot.BONE, EnumSlot.ARM, EnumSlot.ARM, EnumSlot.LEG, EnumSlot.LEG },
                new String[] { "eyes", "brain", "heart", "lungs", "stomach", "skin", "muscles", "bones", "arm_left", "arm_right", "leg_left", "leg_right"});


        cybereyes = new ItemCybereyes("cybereyes", EnumSlot.EYES);
        cybereyes.setEssenceCost(8); // 0.2.0 Changed from 10
        cybereyes.setWeights(UNCOMMON);
        cybereyes.setComponents(NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 4),
                                                                    new ItemStack(component, 2, 5),
                                                                    new ItemStack(component, 2, 7) }));

        cybereyeUpgrades = new ItemCybereyeUpgrade("cybereye_upgrades", EnumSlot.EYES,
                new String[] { "night_vision", "underwater_vision", "hudjack", "targeting", "zoom" });
        cybereyeUpgrades.setEssenceCost(2, 2, 1, 1, 1);
        cybereyeUpgrades.setWeights(UNCOMMON, UNCOMMON, UNCOMMON, UNCOMMON, UNCOMMON);
        cybereyeUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 4),
                                                    new ItemStack(component, 2, 5),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 5),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 2, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 5),
                                                    new ItemStack(component, 4, 7) })
        );

        eyeUpgrades = new ItemEyeUpgrade("eye_upgrades", EnumSlot.EYES,
                new String[] { "hudlens" });
        eyeUpgrades.setEssenceCost(1);
        eyeUpgrades.setWeights(VERY_COMMON);
        eyeUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 2, 7) }
                ));

        CyberwareAPI.linkCyberware(Items.SPIDER_EYE, new SpiderEyeWare());

        brainUpgrades = new ItemBrainUpgrade("brain_upgrades", EnumSlot.CRANIUM,
                new String[] { "cortical_stack", "ender_jammer", "consciousness_transmitter", "neural_contextualizer", "matrix", "radio" });
        brainUpgrades.setEssenceCost(3, 10, 2, 2, 8, 2); // TMC 0.2.0 changed from 10
        brainUpgrades.setWeights(RARE, UNCOMMON, UNCOMMON, COMMON, UNCOMMON, UNCOMMON);
        brainUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 1),
                                                    new ItemStack(component, 1, 7),
                                                    new ItemStack(component, 2, 8) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 2, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 3, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 3, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 2, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 9) })
        );
        expCapsule = new ItemExpCapsule("exp_capsule");

        cyberheart = new ItemCyberheart("cyberheart", EnumSlot.HEART);
        cyberheart.setEssenceCost(5);
        cyberheart.setWeights(COMMON);
        cyberheart.setComponents(NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 0),
                                                                     new ItemStack(component, 1, 2),
                                                                     new ItemStack(component, 1, 7) }));

        denseBattery = new ItemDenseBattery("dense_battery", EnumSlot.LOWER_ORGANS);
        denseBattery.setEssenceCost(15);
        denseBattery.setWeights(RARE);
        denseBattery.setComponents(NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 6),
                                                                       new ItemStack(component, 4, 9) }));

        creativeBattery = new ItemCreativeBattery("creative_battery", EnumSlot.LOWER_ORGANS);
        creativeBattery.setEssenceCost(0);

        heartUpgrades = new ItemHeartUpgrade("heart_upgrades", EnumSlot.HEART,
                new String[] { "defibrillator", "platelets", "medkit", "coupler" });
        heartUpgrades.setEssenceCost(10, 5, 15, 10);
        heartUpgrades.setWeights(COMMON, UNCOMMON, UNCOMMON, VERY_COMMON);
        heartUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 0),
                                                    new ItemStack(component, 2, 6),
                                                    new ItemStack(component, 2, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 0),
                                                    new ItemStack(component, 2, 1),
                                                    new ItemStack(component, 1, 8) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 1),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 1),
                                                    new ItemStack(component, 1, 7),
                                                    new ItemStack(component, 2, 9) })
        );

        lungsUpgrades = new ItemLungsUpgrade("lungs_upgrades", EnumSlot.LUNGS,
                new String[] { "oxygen", "hyperoxygenation" });
        lungsUpgrades.setEssenceCost(15, 2);
        lungsUpgrades.setWeights(UNCOMMON, COMMON);
        lungsUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 1),
                                                    new ItemStack(component, 2, 8) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 1),
                                                    new ItemStack(component, 2, 8),
                                                    new ItemStack(component, 1, 9) })
        );

        lowerOrgansUpgrades = new ItemLowerOrgansUpgrade("lower_organs_upgrades", EnumSlot.LOWER_ORGANS,
                new String[] { "liver_filter", "metabolic", "battery", "adrenaline" });
        lowerOrgansUpgrades.setEssenceCost(5, 5, 10, 5);
        lowerOrgansUpgrades.setWeights(UNCOMMON, COMMON, VERY_COMMON, UNCOMMON);
        lowerOrgansUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 1),
                                                    new ItemStack(component, 2, 8) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 1),
                                                    new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 1),
                                                    new ItemStack(component, 2, 8),
                                                    new ItemStack(component, 3, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 0),
                                                    new ItemStack(component, 2, 1) })
        );

        skinUpgrades = new ItemSkinUpgrade("skin_upgrades", EnumSlot.SKIN,
                new String[] { "solar_skin", "subdermal_spikes", "fake_skin", "immuno"});
        skinUpgrades.setEssenceCost(12, 12, 0, -25);
        skinUpgrades.setWeights(VERY_COMMON, UNCOMMON, UNCOMMON, RARE);
        skinUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 1),
                                                    new ItemStack(component, 1, 4),
                                                    new ItemStack(component, 2, 5),
                                                    new ItemStack(component, 1, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 0),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 1, 4),
                                                    new ItemStack(component, 1, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 1),
                                                    new ItemStack(component, 3, 4),
                                                    new ItemStack(component, 2, 5) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 1),
                                                    new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 1, 7),
                                                    new ItemStack(component, 1, 8),
                                                    new ItemStack(component, 1, 9) })
        );

        muscleUpgrades = new ItemMuscleUpgrade("muscle_upgrades", EnumSlot.MUSCLE,
                new String[] { "wired_reflexes", "muscle_replacements" });
        muscleUpgrades.setEssenceCost(5, 15);
        muscleUpgrades.setWeights(UNCOMMON, RARE);
        muscleUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 3, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 0),
                                                    new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 2, 5) })
        );

        boneUpgrades = new ItemBoneUpgrade("bone_upgrades", EnumSlot.BONE,
                new String[] { "bonelacing", "boneflex", "bonebattery" });
        boneUpgrades.setEssenceCost(3, 5, 2);
        boneUpgrades.setWeights(UNCOMMON, RARE, UNCOMMON);
        boneUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 1),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 2, 6) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 1),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 2, 8) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 1),
                                                    new ItemStack(component, 2, 8),
                                                    new ItemStack(component, 1, 9) })
        );

        armUpgrades = new ItemArmUpgrade("arm_upgrades", EnumSlot.ARM,
                new String[] { "bow"  });
        armUpgrades.setEssenceCost(3);
        armUpgrades.setWeights(RARE);
        armUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 4, 0),
                                                    new ItemStack(component, 2, 4) }));

        handUpgrades = new ItemHandUpgrade("hand_upgrades", EnumSlot.HAND,
                new String[] { "craft_hands", "claws", "mining" });
        handUpgrades.setEssenceCost(2, 2, 1);
        handUpgrades.setWeights(RARE, RARE, RARE);
        handUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 4, 0),
                                                    new ItemStack(component, 1, 3),
                                                    new ItemStack(component, 1, 4) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 0),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 1, 4),
                                                    new ItemStack(component, 1, 6),
                                                    new ItemStack(component, 2, 8) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 0),
                                                    new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 1, 4),
                                                    new ItemStack(component, 2, 6) })
        );

        legUpgrades = new ItemLegUpgrade("leg_upgrades", EnumSlot.LEG,
                new String[] { "jump_boost", "fall_damage" });
        legUpgrades.setEssenceCost(3, 2);
        legUpgrades.setWeights(RARE, RARE);
        legUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 0),
                                                    new ItemStack(component, 2, 2) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 3, 2),
                                                    new ItemStack(component, 1, 4),
                                                    new ItemStack(component, 1, 5) }));

        footUpgrades = new ItemFootUpgrade("foot_upgrades", EnumSlot.FOOT,
                new String[] { "spurs", "aqua", "wheels" });
        footUpgrades.setEssenceCost(1, 2, 3);
        footUpgrades.setWeights(UNCOMMON, RARE, UNCOMMON);
        footUpgrades.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 1, 0),
                                                    new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 1, 4) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 0),
                                                    new ItemStack(component, 1, 2),
                                                    new ItemStack(component, 1, 9) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 2, 0),
                                                    new ItemStack(component, 2, 9) }));

        cyberlimbs = new ItemCyberlimb("cyberlimbs",
                new EnumSlot[] { EnumSlot.ARM, EnumSlot.ARM, EnumSlot.LEG, EnumSlot.LEG },
                new String[] { "cyberarm_left", "cyberarm_right", "cyberleg_left", "cyberleg_right" });
        cyberlimbs.setEssenceCost(15, 15, 15, 15);
        cyberlimbs.setComponents(
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 4, 0),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 2, 4),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 4, 0),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 2, 4),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 4, 0),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 2, 4),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 7) }),
                NNLUtil.fromArray(new ItemStack[] { new ItemStack(component, 4, 0),
                                                    new ItemStack(component, 2, 2),
                                                    new ItemStack(component, 2, 4),
                                                    new ItemStack(component, 1, 5),
                                                    new ItemStack(component, 1, 7) })
        );

        ItemStack reinforcement = new ItemStack(component, 1, 2);
        ItemStack circuit = new ItemStack(component, 1, 3);
        ItemStack plating = new ItemStack(component, 1, 4);
        ItemStack fiber = new ItemStack(component, 1, 5);
        
        RecipeHandler.addShapedOreRecipe(new ItemStack(surgeryChamber.itemBlock), 
                                         "III",
                                         "IBI",
                                         "IDI",
                                         'I', "ingotIron",
                                         'B', "blockIron",
                                         'D', new ItemStack(Items.IRON_DOOR)
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(charger), 
                                         "IFI",
                                         "IRI",
                                         "III",
                                         'I', "ingotIron",
                                         'R', "blockRedstone",
                                         'F', new ItemStack(Blocks.IRON_BARS)
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(scanner), 
                                         "IEI",
                                         "IDI",
                                         "III",
                                         'I', "ingotIron",
                                         'D', "gemDiamond",
                                         'E', new ItemStack(cybereyes, 1, OreDictionary.WILDCARD_VALUE)
        );

        ItemStack salvagedEye = new ItemStack(cybereyes);
        salvagedEye = cybereyes.setQuality(salvagedEye, CyberwareAPI.QUALITY_SCAVENGED);
        RecipeHandler.addShapedOreRecipe(new ItemStack(scanner), 
                                         "IEI",
                                         "IDI",
                                         "III",
                                         'I', "ingotIron",
                                         'D', "gemDiamond",
                                         'E', salvagedEye
        );

        //GameRegistry.addRecipe(new BlueprintCraftingHandler());
        RecipeHandler.addRecipe(new BlueprintCraftingHandler()
                                        .setRegistryName(new ResourceLocation(Cyberware.MODID, "blueprintCrafting")) );

        RecipeHandler.addShapelessOreRecipe(new ItemStack(Items.PAPER),
                new ItemStack(blueprint, 1, OreDictionary.WILDCARD_VALUE)
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(engineering.itemBlock), 
                                         " PI",
                                         "III",
                                         "ICI",
                                         'I', "ingotIron",
                                         'P', new ItemStack(Blocks.PISTON),
                                         'C', new ItemStack(Blocks.CRAFTING_TABLE)
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(blueprintArchive), 
                                         "PPP",
                                         "AAA",
                                         "PPP",
                                         'P', "ingotIron", 'A', "paper"
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(componentBox), 
                                         " O ",
                                         "ICI",
                                         " I ",
                                         'I', "ingotIron",
                                         'C', "chestWood",
                                         'O', new ItemStack(component, 1, OreDictionary.WILDCARD_VALUE)
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(radio),
                                         "F  ",
                                         "III",
                                         "ICI",
                                         'I', "ingotIron",
                                         'F', fiber.copy(),
                                         'C', circuit.copy()
        );

        RecipeHandler.addShapedOreRecipe(new ItemStack(radioLarge), 
                                         "IEI",
                                         "PRP",
                                         "ICI",
                                         'I', "ingotIron",
                                         'R', new ItemStack(radio),
                                         'E', new ItemStack(Items.ENDER_EYE),
                                         'P', reinforcement.copy(),
                                         'C', circuit.copy()
        );


        RecipeHandler.addShapedOreRecipe(new ItemStack(radioPost, 6),
                                         "B B",
                                         "BFB",
                                         "BPB",
                                         'B', new ItemStack(Blocks.IRON_BARS),
                                         'P', plating.copy(),
                                         'F', fiber.copy()
        );

        RecipeHandler.addRecipe(new CyberwareDyingHandler());

        if (CyberwareConfig.SURGERY_CRAFTING)
        {
            RecipeHandler.addShapedOreRecipe(new ItemStack(surgeryApparatus),
                                             "III",
                                             "IRI",
                                             "PSA",
                                             'I', "ingotIron",
                                             'R', "blockRedstone",
                                             'P', new ItemStack(Items.DIAMOND_PICKAXE),
                                             'S', new ItemStack(Items.DIAMOND_SWORD),
                                             'A', new ItemStack(Items.DIAMOND_AXE)
            );
        }

        if (CyberwareConfig.INT_BOTANIA && Loader.isModLoaded(BotaniaIntegration.MOD_ID))
        {
            BotaniaIntegration.preInit();
        }

        if (CyberwareConfig.INT_TOUGH_AS_NAILS && Loader.isModLoaded(ToughAsNailsIntegration.MOD_ID))
        {
            ToughAsNailsIntegration.preInit();
        }

        if (CyberwareConfig.INT_ENDER_IO && Loader.isModLoaded(EnderIOIntegration.MOD_ID))
        {
            EnderIOIntegration.preInit();
        }
    }

    public static void postInit()
    {
        if (CyberwareConfig.MOBS_ENABLE_CYBER_ZOMBIES)
        {
            List<Biome> listBiomes = new ArrayList<>();
            StringBuilder strBiomes = new StringBuilder();
            
            for (Biome biome : Biome.REGISTRY)
            {
                for (SpawnListEntry entry : biome.getSpawnableList(EnumCreatureType.MONSTER))
                {
                    if (entry.entityClass == EntityZombie.class)
                    {
                        listBiomes.add(biome);
                        if (strBiomes.length() > 0) strBiomes.append(", ");
                        strBiomes.append(biome.getRegistryName());
                        break;
                    }
                }
            }
            Cyberware.logger.info(String.format("Adding CyberZombies spawning to the following biomes: %s",
                                                strBiomes.toString()));
            EntityRegistry.addSpawn(EntityCyberZombie.class,
                                    CyberwareConfig.MOBS_CYBER_ZOMBIE_WEIGHT,
                                    CyberwareConfig.MOBS_CYBER_ZOMBIE_MIN_PACK,
                                    CyberwareConfig.MOBS_CYBER_ZOMBIE_MAX_PACK,
                                    EnumCreatureType.MONSTER,
                                    listBiomes.toArray(new Biome[0]));
        }

    }

    public static class NumItems extends WeightedRandom.Item
    {
        public int num;
        public NumItems(int weight, int num)
        {
            super(weight);
            this.num = num;
        }

        @Override
        public boolean equals(Object target)
        {
            return target instanceof NumItems && num == ((NumItems)target).num;
        }
    }

    public static class ZombieItem extends WeightedRandom.Item
    {
        public ItemStack stack;
        public ZombieItem(int weight, ItemStack stack)
        {
            super(weight);
            this.stack = stack;
        }

        @Override
        public boolean equals(Object target)
        {
            if (!(target instanceof ZombieItem)) return false;
            ItemStack stack2 = ((ZombieItem)target).stack;
            return ( stack == stack2
                  || ( !stack.isEmpty()
                    && !stack2.isEmpty()
                    && stack.getItem() == stack2.getItem()
                    && stack.getItemDamage() == stack2.getItemDamage()
                    && stack.getCount() == stack2.getCount() ) );
        }
    }
    
    public static ItemStack getItemStackByRegistryName(@Nonnull final String registryName, final int meta) {
        final ResourceLocation resourceLocation = new ResourceLocation(registryName);
        final Item item = Item.REGISTRY.getObject(resourceLocation);
        if (item == null)
        {
            throw new RuntimeException(String.format("Skipping missing mod item %s@%d",
                                                     registryName, meta ));
        }
        final ItemStack itemStack = new ItemStack(item);
        if (meta != -1)
        {
            try
            {
                itemStack.setItemDamage(meta);
            }
            catch (final Exception exception)
            {
                throw new RuntimeException(String.format("Failed to get mod item for %s@%d",
                                                         registryName, meta ));
            }
        }
        return itemStack;
    }
}