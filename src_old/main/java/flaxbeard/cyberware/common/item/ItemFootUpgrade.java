package flaxbeard.cyberware.common.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUpdateEvent;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.EnableDisableHelper;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.lib.LibConstants;
import flaxbeard.cyberware.common.misc.NNLUtil;

public class ItemFootUpgrade extends ItemCyberware implements IMenuItem
{

    public static final int META_SPURS                      = 0;
    public static final int META_AQUA                       = 1;
    public static final int META_WHEELS                     = 2;
    
    public ItemFootUpgrade(String name, EnumSlot slot, String[] subnames)
    {
        super(name, slot, subnames);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public NonNullList<NonNullList<ItemStack>> required(ItemStack stack)
    {
        if (stack.getItemDamage() != META_AQUA) return NonNullList.create();

        return NNLUtil.fromArray(new ItemStack[][] {
                new ItemStack[] { CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG),
                                  CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG) }});
    }

    @SubscribeEvent
    public void handleHorseMove(LivingUpdateEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
        if (entityLivingBase instanceof EntityHorse)
        {
        	ItemStack itemStackSpurs = getCachedStack(META_SPURS);
            EntityHorse entityHorse = (EntityHorse) entityLivingBase;
            for (Entity entityPassenger : entityHorse.getPassengers())
            {
                if (entityPassenger instanceof EntityLivingBase)
                {
	                ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPassenger);
	                if ( cyberwareUserData != null
	                  && cyberwareUserData.isCyberwareInstalled(itemStackSpurs) )
	                {
	                    entityHorse.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1, 5, true, false));
	                    break;
	                }
                }
            }
        }
    }

    private Set<UUID> setIsAquaPowered = new HashSet<>();
    private Map<UUID, Integer> mapCountdownWheelsPowered = new HashMap<>();
    private Map<UUID, Float> mapStepHeight = new HashMap<>();

    @SubscribeEvent(priority=EventPriority.NORMAL)
    public void handleLivingUpdate(CyberwareUpdateEvent event)
    {
        EntityLivingBase entityLivingBase = event.getEntityLiving();
	    ICyberwareUserData cyberwareUserData = event.getCyberwareUserData();
    
        if ( !entityLivingBase.onGround
          && entityLivingBase.isInWater() )
        {
            ItemStack itemStackAqua = cyberwareUserData.getCyberware(getCachedStack(META_AQUA));
            if (!itemStackAqua.isEmpty())
            {
                boolean wasPowered = setIsAquaPowered.contains(entityLivingBase.getUniqueID());
                boolean isPowered = entityLivingBase.ticksExisted % 20 == 0
                                  ? cyberwareUserData.usePower(itemStackAqua, getPowerConsumption(itemStackAqua))
                                  : wasPowered;
                if (isPowered)
                {
                    if ( Math.abs(entityLivingBase.moveStrafing) + Math.abs(entityLivingBase.moveForward) > 0.0F
                      && Math.abs(entityLivingBase.motionX) + Math.abs(entityLivingBase.motionZ) > 0.0F )
                    {
                        int numLegs = 0;
                        if (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_LEG)))
                        {
                            numLegs++;
                        }
                        if (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_LEG)))
                        {
                            numLegs++;
                        }
                        
                        // increase maximum horizontal motion
                        float boost = numLegs * 0.4F;
                        entityLivingBase.moveRelative(entityLivingBase.moveStrafing * boost, 0.26F, entityLivingBase.moveForward * boost, 0.075F);
                    }
                    if (!wasPowered)
                    {
                        setIsAquaPowered.add(entityLivingBase.getUniqueID());
                    }
                }
                else if (entityLivingBase.ticksExisted % 20 == 0)
                {
                    setIsAquaPowered.remove(entityLivingBase.getUniqueID());
                }
            }
        }
        else if (entityLivingBase.ticksExisted % 20 == 0)
        {
            setIsAquaPowered.remove(entityLivingBase.getUniqueID());
        }

        ItemStack itemStackWheels = cyberwareUserData.getCyberware(getCachedStack(META_WHEELS));
        if (!itemStackWheels.isEmpty())
        {
            boolean wasPowered = getCountdownWheelsPowered(entityLivingBase) > 0;

            boolean isPowered = EnableDisableHelper.isEnabled(itemStackWheels)
                             && ( entityLivingBase.ticksExisted % 20 == 0
                                ? cyberwareUserData.usePower(itemStackWheels, getPowerConsumption(itemStackWheels))
                                : wasPowered );
            if (isPowered)
            {
                if (!mapStepHeight.containsKey(entityLivingBase.getUniqueID()))
                {
                    mapStepHeight.put(entityLivingBase.getUniqueID(), Math.max(entityLivingBase.stepHeight, .6F));
                }
                entityLivingBase.stepHeight = 1F;

                mapCountdownWheelsPowered.put(entityLivingBase.getUniqueID(), 10);
            }
            else if (mapStepHeight.containsKey(entityLivingBase.getUniqueID()) && wasPowered)
            {
                entityLivingBase.stepHeight = mapStepHeight.get(entityLivingBase.getUniqueID());

                mapCountdownWheelsPowered.put(entityLivingBase.getUniqueID(), getCountdownWheelsPowered(entityLivingBase) - 1);
            }
            else
            {
                mapCountdownWheelsPowered.put(entityLivingBase.getUniqueID(), 0);
            }
        }
        else if (mapStepHeight.containsKey(entityLivingBase.getUniqueID()))
        {
            entityLivingBase.stepHeight = mapStepHeight.get(entityLivingBase.getUniqueID());

            int countdownWheelsPowered = getCountdownWheelsPowered(entityLivingBase) - 1;
            if (countdownWheelsPowered == 0)
            {
                mapStepHeight.remove(entityLivingBase.getUniqueID());
            }

            mapCountdownWheelsPowered.put(entityLivingBase.getUniqueID(), countdownWheelsPowered);
        }
    }
    
    private int getCountdownWheelsPowered(EntityLivingBase entityLivingBase)
    {
        return mapCountdownWheelsPowered.computeIfAbsent(entityLivingBase.getUniqueID(), k -> 10);
    }

    @Override
    public int getPowerConsumption(ItemStack stack)
    {
        return stack.getItemDamage() == META_AQUA ? LibConstants.AQUA_CONSUMPTION :
               stack.getItemDamage() == META_WHEELS ? LibConstants.WHEEL_CONSUMPTION : 0;
    }

    @Override
    public boolean hasMenu(ItemStack stack)
    {
        return stack.getItemDamage() == META_WHEELS;
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