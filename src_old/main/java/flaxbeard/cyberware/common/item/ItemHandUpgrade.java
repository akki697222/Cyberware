package flaxbeard.cyberware.common.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.HashMultimap;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class ItemHandUpgrade extends ItemCyberware implements IMenuItem
{

    public static final int META_CRAFT_HANDS                = 0;
    public static final int META_CLAWS                      = 1;
    public static final int META_MINING                     = 2;
    
    private static Item itemTool;
    
    private static final UUID uuidClawsDamageAttribute = UUID.fromString("63c32801-94fb-40d4-8bd2-89135c1e44b1");
    private static final HashMultimap<String, AttributeModifier> multimapClawsDamageAttribute;
    private static final Map<UUID, Boolean> lastClaws = new HashMap<>();
    public static float clawsTime;
    
    static {
        multimapClawsDamageAttribute = HashMultimap.create();
        multimapClawsDamageAttribute.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                                         new AttributeModifier(uuidClawsDamageAttribute, "Claws damage upgrade", 5.5F, 0));
    }
    
    public ItemHandUpgrade(String name, EnumSlot slot, String[] subnames)
    {
        super(name, slot, subnames);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
    {
        return NNLUtil.fromArray(new ItemStack[][] {
                new ItemStack[] { CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM),
                                  CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM) }});
    }

    @Override
    public boolean isIncompatible(ItemStack stack, ItemStack other)
    {
        return other.getItem() == this;
    }

    private ItemStack getItemStackTool() {
        if (itemTool == null) {
            Item itemConfig = Item.getByNameOrId(CyberwareConfig.FIST_MINING_TOOL_NAME);
            if (itemConfig == null) {
                Cyberware.logger.error(String.format("Unable to find item with id %s, check your configuration. Defaulting fist mining tool to Iron pickaxe.", CyberwareConfig.FIST_MINING_TOOL_NAME));
                itemConfig = Items.IRON_PICKAXE;
            }
            itemTool = itemConfig;
        }
        return new ItemStack(itemTool);
    }
    @SubscribeEvent(priority=EventPriority.NORMAL)
    public void handleLivingUpdate(CyberwareUpdateEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
        ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
        
        ItemStack itemStackClaws = cyberwareUserData.getCyberware(getCachedStack(META_CLAWS));
        if (!itemStackClaws.isEmpty())
        {
            boolean wasEquipped = getLastClaws(entityLivingBase);
            boolean isEquipped = entityLivingBase.getHeldItemMainhand().isEmpty()
                 && ( entityLivingBase.getPrimaryHand() == EnumHandSide.RIGHT
                    ? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
                    : (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM))) )
                 && EnableDisableHelper.isEnabled(itemStackClaws);
            if (isEquipped)
            {
                if ( !wasEquipped
                  || entityLivingBase.ticksExisted % 20 == 0 )
                {
                    addClawsDamage(entityLivingBase);
                    lastClaws.put(entityLivingBase.getUniqueID(), Boolean.TRUE);
                }
                
                if ( !wasEquipped
                  && entityLivingBase.getEntityWorld().isRemote )
                {
                    updateHand(entityLivingBase, true);
                }
            }
            else if ( wasEquipped
                   || entityLivingBase.ticksExisted % 20 == 0 )
            {
                removeClawsDamage(entityLivingBase);
                lastClaws.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
            }
        }
        else if (entityLivingBase.ticksExisted % 20 == 0)
        {
            removeClawsDamage(entityLivingBase);
            lastClaws.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }
    }

    private void updateHand(EntityLivingBase entityLivingBase, boolean delay)
    {
        if ( Minecraft.getMinecraft() != null
          && Minecraft.getMinecraft().player != null
          && entityLivingBase == Minecraft.getMinecraft().player )
        {
            clawsTime = Minecraft.getMinecraft().getRenderPartialTicks() + entityLivingBase.ticksExisted + (delay ? 5 : 0);
        }
    }

    private boolean getLastClaws(EntityLivingBase entityLivingBase)
    {
        if (!lastClaws.containsKey(entityLivingBase.getUniqueID()))
        {
            lastClaws.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }
        return lastClaws.get(entityLivingBase.getUniqueID());
    }
    
    private void addClawsDamage(EntityLivingBase entityLivingBase)
    {
        entityLivingBase.getAttributeMap().applyAttributeModifiers(multimapClawsDamageAttribute);
    }
    
    private void removeClawsDamage(EntityLivingBase entityLivingBase)
    {
        entityLivingBase.getAttributeMap().removeAttributeModifiers(multimapClawsDamageAttribute);
    }
    
    @Override
    public void onRemoved(EntityLivingBase entityLivingBase, ItemStack stack)
    {
        if (stack.getItemDamage() == META_CLAWS)
        {
            removeClawsDamage(entityLivingBase);
        }
    }

    @SubscribeEvent
    public void handleMining(HarvestCheck event)
    {
        EntityPlayer entityPlayer = event.getEntityPlayer();
        ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
        if (cyberwareUserData == null) return;
        
        ItemStack itemStackMining = cyberwareUserData.getCyberware(getCachedStack(META_MINING));
        boolean rightArm = ( entityPlayer.getPrimaryHand() == EnumHandSide.RIGHT
                           ? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
                           : (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM))) );
        if ( rightArm
          && !itemStackMining.isEmpty()
          && entityPlayer.getHeldItemMainhand().isEmpty() )
        {
            ItemStack itemStackTool = getItemStackTool();
            if (itemStackTool.canHarvestBlock(event.getTargetBlock()))
            {
                event.setCanHarvest(true);
            }
        }
    }

    @SubscribeEvent
    public void handleMineSpeed(BreakSpeed event)
    {
        EntityPlayer entityPlayer = event.getEntityPlayer();
        ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
        if (cyberwareUserData == null) return;
    
        ItemStack itemStackMining = cyberwareUserData.getCyberware(getCachedStack(META_MINING));
        boolean rightArm = ( entityPlayer.getPrimaryHand() == EnumHandSide.RIGHT
                           ? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
                           : (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM))) );
        if ( rightArm
          && !itemStackMining.isEmpty()
          && entityPlayer.getHeldItemMainhand().isEmpty() )
        {
            final ItemStack itemStackTool = getItemStackTool();
            event.setNewSpeed(event.getNewSpeed() * itemStackTool.getDestroySpeed(entityPlayer.world.getBlockState(event.getPos())));
        }
    }

    @Override
    public boolean hasMenu(ItemStack stack)
    {
        return stack.getItemDamage() == META_CLAWS;
    }

    @Override
    public void use(Entity entity, ItemStack stack)
    {
        EnableDisableHelper.toggle(stack);
        if (entity instanceof EntityLivingBase && FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            updateHand((EntityLivingBase) entity, false);
        }
    }

    @Override
    public String getUnlocalizedLabel(ItemStack stack)
    {
        return EnableDisableHelper.getUnlocalizedLabel(stack);
    }

    private static final float[] f = new float[] { 1.0F, 0.0F, 0.0F };

    @Override
    public float[] getColor(ItemStack stack)
    {
        return EnableDisableHelper.isEnabled(stack) ? f : null;
    }
}
