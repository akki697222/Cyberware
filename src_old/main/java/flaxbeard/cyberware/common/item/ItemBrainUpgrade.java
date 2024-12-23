package flaxbeard.cyberware.common.item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.ArmorClass;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.DodgePacket;

public class ItemBrainUpgrade extends ItemCyberware implements IMenuItem
{
    
    public static final int META_CORTICAL_STACK             = 0;
    public static final int META_ENDER_JAMMER               = 1;
    public static final int META_CONSCIOUSNESS_TRANSMITTER  = 2;
    public static final int META_NEURAL_CONTEXTUALIZER      = 3;
    public static final int META_THREAT_MATRIX              = 4;
    public static final int META_RADIO                      = 5;
    
    public ItemBrainUpgrade(String name, EnumSlot slot, String[] subnames)
    {
        super(name, slot, subnames);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public boolean isIncompatible(ItemStack stack, ItemStack other)
    {
        return other.getItem() == this
            && stack.getItemDamage() == META_CORTICAL_STACK
            && other.getItemDamage() == META_CONSCIOUSNESS_TRANSMITTER;
    }
    
    @SubscribeEvent
    public void handleTeleJam(EnderTeleportEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
        if (!isTeleportationAllowed(entityLivingBase))
        {
            event.setCanceled(true);
        }
    }
    
    public static boolean isTeleportationAllowed(@Nullable EntityLivingBase entityLivingBase) {    
        if (entityLivingBase == null) return true;
        
        ItemStack itemStackJammer = CyberwareContent.brainUpgrades.getCachedStack(ItemBrainUpgrade.META_ENDER_JAMMER);
        
        ICyberwareUserData cyberwareUserDataSelf = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
        if (cyberwareUserDataSelf != null) {
            ItemStack itemStackJammerSelf = cyberwareUserDataSelf.getCyberware(itemStackJammer);
            if ( !itemStackJammerSelf.isEmpty()
              && EnableDisableHelper.isEnabled(itemStackJammerSelf) )
            {
                return false;
            }
        }
        
        float range = 25F;
        List<EntityLivingBase> entitiesInRange = entityLivingBase.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                new AxisAlignedBB(entityLivingBase.posX - range, entityLivingBase.posY - range, entityLivingBase.posZ - range,
                                  entityLivingBase.posX + entityLivingBase.width + range, entityLivingBase.posY + entityLivingBase.height + range, entityLivingBase.posZ + entityLivingBase.width + range));
        for (EntityLivingBase entityInRange : entitiesInRange)
        {
            if (entityLivingBase.getDistanceSq(entityInRange) <= range * range)
            {
                ICyberwareUserData cyberwareUserDataInRange = CyberwareAPI.getCapabilityOrNull(entityInRange);
                if (cyberwareUserDataInRange != null)
                {
                    ItemStack itemStackJammerInRange = cyberwareUserDataInRange.getCyberware(itemStackJammer);
                    if ( !itemStackJammerInRange.isEmpty()
                         && EnableDisableHelper.isEnabled(itemStackJammerInRange) )
                    {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    @SubscribeEvent
    public void handleClone(PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            EntityPlayer entityPlayerOriginal = event.getOriginal();
            
            if (entityPlayerOriginal.world.getGameRules().getBoolean("keepInventory")) {
                return;
            }
            
            ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayerOriginal);
            if (cyberwareUserData == null) return;
            
            if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CORTICAL_STACK)))
            {
                if (!entityPlayerOriginal.world.isRemote)
                {
                    ItemStack stack = new ItemStack(CyberwareContent.expCapsule);
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setInteger("xp", entityPlayerOriginal.experienceTotal);
                    stack.setTagCompound(tagCompound);
                    EntityItem item = new EntityItem(entityPlayerOriginal.world, entityPlayerOriginal.posX, entityPlayerOriginal.posY, entityPlayerOriginal.posZ, stack);
                    entityPlayerOriginal.world.spawnEntity(item);
                }
            }
            else if (cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CONSCIOUSNESS_TRANSMITTER)))
            {
                event.getEntityPlayer().addExperience((int) (Math.min(100, entityPlayerOriginal.experienceLevel * 7) * .9F));
            }
        }
    }

    @SubscribeEvent
    public void handleMining(BreakSpeed event)
    {
        EntityPlayer entityPlayer = event.getEntityPlayer();
        
        ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
        if (cyberwareUserData == null) return;
        
        ItemStack itemStackNeuralContextualizer = cyberwareUserData.getCyberware(getCachedStack(META_NEURAL_CONTEXTUALIZER));
        if ( !itemStackNeuralContextualizer.isEmpty()
          && EnableDisableHelper.isEnabled(itemStackNeuralContextualizer)
          && isContextWorking(entityPlayer)
          && !entityPlayer.isSneaking() )
        {
            IBlockState state = event.getState();
            ItemStack tool = entityPlayer.getHeldItem(EnumHand.MAIN_HAND);

            if ( !tool.isEmpty()
              && ( tool.getItem() instanceof ItemSword
                || tool.getItem().getTranslationKey().contains("sword") ) )
            {
                return;
            }
            
            if (isToolEffective(tool, state)) return;
            
            for (int indexSlot = 0; indexSlot < 10; indexSlot++)
            {
                if (indexSlot != entityPlayer.inventory.currentItem)
                {
                    ItemStack potentialTool = entityPlayer.inventory.mainInventory.get(indexSlot);
                    if (isToolEffective(potentialTool, state))
                    {
                        entityPlayer.inventory.currentItem = indexSlot;
                        return;
                    }
                }
            }
        }
    }

    private static final Map<UUID, Boolean> isContextWorking = new HashMap<>();
    private static final Map<UUID, Boolean> isMatrixWorking = new HashMap<>();
    private static final Map<UUID, Boolean> isRadioWorking = new HashMap<>();

    @SubscribeEvent(priority=EventPriority.NORMAL)
    public void handleLivingUpdate(CyberwareUpdateEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
        if (entityLivingBase.ticksExisted % 20 != 0) return;
        
        ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
        
        ItemStack itemStackNeuralContextualizer = cyberwareUserData.getCyberware(getCachedStack(META_NEURAL_CONTEXTUALIZER));
        if ( !itemStackNeuralContextualizer.isEmpty()
          && EnableDisableHelper.isEnabled(itemStackNeuralContextualizer) )
        {
            isContextWorking.put(entityLivingBase.getUniqueID(), cyberwareUserData.usePower(itemStackNeuralContextualizer, getPowerConsumption(itemStackNeuralContextualizer)));
        }
        else
        {
            isContextWorking.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }
        
        ItemStack itemStackThreatMatrix = cyberwareUserData.getCyberware(getCachedStack(META_THREAT_MATRIX));
        if (!itemStackThreatMatrix.isEmpty())
        {
            isMatrixWorking.put(entityLivingBase.getUniqueID(), cyberwareUserData.usePower(itemStackThreatMatrix, getPowerConsumption(itemStackThreatMatrix)));
        }
        else
        {
            isMatrixWorking.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }
        
        ItemStack itemStackRadio = cyberwareUserData.getCyberware(getCachedStack(META_RADIO));
        if ( !itemStackRadio.isEmpty()
          && EnableDisableHelper.isEnabled(itemStackRadio) )
        {
            isRadioWorking.put(entityLivingBase.getUniqueID(), cyberwareUserData.usePower(itemStackRadio, getPowerConsumption(itemStackRadio)));
        }
        else
        {
            isRadioWorking.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }
    }

    public static boolean isRadioWorking(EntityLivingBase entityLivingBase)
    {
        if (!isRadioWorking.containsKey(entityLivingBase.getUniqueID()))
        {
            isRadioWorking.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }

        return isRadioWorking.get(entityLivingBase.getUniqueID());
    }

    private boolean isContextWorking(EntityLivingBase entityLivingBase)
    {
        if (!isContextWorking.containsKey(entityLivingBase.getUniqueID()))
        {
            isContextWorking.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }

        return isContextWorking.get(entityLivingBase.getUniqueID());
    }

    private boolean isMatrixWorking(EntityLivingBase entityLivingBase)
    {
        if (!isMatrixWorking.containsKey(entityLivingBase.getUniqueID()))
        {
            isMatrixWorking.put(entityLivingBase.getUniqueID(), Boolean.FALSE);
        }

        return isMatrixWorking.get(entityLivingBase.getUniqueID());
    }

    public boolean isToolEffective(ItemStack tool, IBlockState state)
    {
        if (!tool.isEmpty())
        {
            for (String toolType : tool.getItem().getToolClasses(tool))
            {
                if (state.getBlock().isToolEffective(toolType, state))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    @SubscribeEvent
    public void handleXPDrop(LivingExperienceDropEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
        ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
        if (cyberwareUserData == null) return;
        
        if ( cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CORTICAL_STACK))
          || cyberwareUserData.isCyberwareInstalled(getCachedStack(META_CONSCIOUSNESS_TRANSMITTER)) )
        {
            event.setCanceled(true);
        }
    }

    private static final ArrayList<String> lastHits = new ArrayList<>();

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void handleHurt(LivingAttackEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
        if (!isMatrixWorking(entityLivingBase)) return;
        ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityLivingBase);
        if (cyberwareUserData == null) return;

        if ( cyberwareUserData.isCyberwareInstalled(getCachedStack(META_THREAT_MATRIX))
          && !entityLivingBase.world.isRemote
          && event.getSource() instanceof EntityDamageSource )
        {
            Entity attacker = event.getSource().getTrueSource();
            if (entityLivingBase instanceof EntityPlayer)
            {
                String str = entityLivingBase.getEntityId() + " " + entityLivingBase.ticksExisted + " " + (attacker == null ? -1 : attacker.getEntityId());
                if (lastHits.contains(str))
                {
                    return;
                }
                else
                {
                    lastHits.add(str);
                }
            }

            ArmorClass armorClass = ArmorClass.get(entityLivingBase);
            if (armorClass == ArmorClass.HEAVY) return;
            
            if ( (float) entityLivingBase.hurtResistantTime <= (float) entityLivingBase.maxHurtResistantTime / 2.0F )
            {
                Random random = entityLivingBase.getRNG();
                if (random.nextFloat() < (armorClass == ArmorClass.LIGHT ? LibConstants.DODGE_ARMOR : LibConstants.DODGE_NO_ARMOR))
                {
                    event.setCanceled(true);
                    entityLivingBase.hurtResistantTime = entityLivingBase.maxHurtResistantTime;
                    entityLivingBase.hurtTime = entityLivingBase.maxHurtTime = 10;
                    entityLivingBase.lastDamage = 9999F;
                    CyberwarePacketHandler.INSTANCE.sendToAllAround(new DodgePacket(entityLivingBase.getEntityId()),
                                                                    new TargetPoint(entityLivingBase.world.provider.getDimension(), entityLivingBase.posX, entityLivingBase.posY, entityLivingBase.posZ, 50));
                }
            }
        }
    }

    @Override
    public int getPowerConsumption(ItemStack stack)
    {
        return stack.getItemDamage() == META_NEURAL_CONTEXTUALIZER ? LibConstants.CONTEXTUALIZER_CONSUMPTION
             : stack.getItemDamage() == META_THREAT_MATRIX ? LibConstants.MATRIX_CONSUMPTION
             : stack.getItemDamage() == META_RADIO ? LibConstants.RADIO_CONSUMPTION
             : 0;
    }
    
    @Override
    public boolean hasMenu(ItemStack stack)
    {
        return stack.getItemDamage() == META_ENDER_JAMMER
            || stack.getItemDamage() == META_NEURAL_CONTEXTUALIZER
            || stack.getItemDamage() == META_RADIO;
    }
    
    @Override
    public void use(Entity entity, ItemStack stack)
    {
        EnableDisableHelper.toggle(stack);
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
