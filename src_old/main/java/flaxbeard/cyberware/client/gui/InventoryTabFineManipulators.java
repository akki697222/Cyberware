package flaxbeard.cyberware.client.gui;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.item.ItemCyberlimb;
import flaxbeard.cyberware.common.item.ItemHandUpgrade;
import flaxbeard.cyberware.common.network.CyberwarePacketHandler;
import flaxbeard.cyberware.common.network.GuiPacket;
import micdoodle8.mods.galacticraft.api.client.tabs.AbstractTab;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class InventoryTabFineManipulators extends AbstractTab
{
	
	public InventoryTabFineManipulators()
	{
		super(0, 0, 0, new ItemStack(CyberwareContent.handUpgrades, 1, ItemHandUpgrade.META_CRAFT_HANDS));
	}
	
	@Override
	public void onTabClicked()
	{
		Minecraft.getMinecraft().player.openGui(Cyberware.INSTANCE, 1, Minecraft.getMinecraft().player.world, 0, 0, 0);
		CyberwarePacketHandler.INSTANCE.sendToServer(new GuiPacket(1, 0, 0, 0));
	}
	
	@Override
	public boolean shouldAddToList()
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null)
		{
			return false;
		}
		
		boolean hasCyberArm = entityPlayer.getPrimaryHand() == EnumHandSide.RIGHT
		                    ? (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_RIGHT_CYBER_ARM)))
		                    : (cyberwareUserData.isCyberwareInstalled(CyberwareContent.cyberlimbs.getCachedStack(ItemCyberlimb.META_LEFT_CYBER_ARM)));
		
		return hasCyberArm
		    && cyberwareUserData.isCyberwareInstalled(CyberwareContent.handUpgrades.getCachedStack(ItemHandUpgrade.META_CRAFT_HANDS));
	}
}
