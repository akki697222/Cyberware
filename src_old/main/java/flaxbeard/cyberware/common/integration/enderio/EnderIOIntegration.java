package flaxbeard.cyberware.common.integration.enderio;

import flaxbeard.cyberware.common.item.ItemBrainUpgrade;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EnderIOIntegration
{
    public static final String MOD_ID = "enderio";
    
    public static void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new EnderIOIntegration());
    }
    
    @Optional.Method(modid=MOD_ID)
    @SubscribeEvent(priority= EventPriority.HIGHEST)
    public void onTeleportEntity(crazypants.enderio.api.teleport.TeleportEntityEvent event)
    {
        if (!(event.getEntity() instanceof EntityLivingBase)) return;
        EntityLivingBase entityLivingBase = (EntityLivingBase) event.getEntity();
        if (!ItemBrainUpgrade.isTeleportationAllowed(entityLivingBase))
        {
            event.setCanceled(true);
        }
    }
}
