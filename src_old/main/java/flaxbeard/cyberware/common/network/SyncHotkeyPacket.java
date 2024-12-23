package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import flaxbeard.cyberware.api.item.HotkeyHelper;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncHotkeyPacket implements IMessage
{
	public SyncHotkeyPacket() {}
	
	private int selectedPart;
	private int key;

	public SyncHotkeyPacket(int selectedPart, int key)
	{
		this.selectedPart = selectedPart;
		this.key = key;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(selectedPart);
		buf.writeInt(key);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		selectedPart = buf.readInt();
		key = buf.readInt();
	}
	
	public static class SyncHotkeyPacketHandler implements IMessageHandler<SyncHotkeyPacket, IMessage>
	{
		@Override
		public IMessage onMessage(SyncHotkeyPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(message.selectedPart, message.key, player));

			return null;
		}
	}
	
	private static class DoSync implements Runnable
	{
		private int selectedPart;
		private int key;
		private EntityPlayer entityPlayer;

		public DoSync(int selectedPart, int key, EntityPlayer entityPlayer)
		{
			this.selectedPart = selectedPart;
			this.key = key;
			this.entityPlayer = entityPlayer;
		}
		
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				if (key == Integer.MAX_VALUE)
				{
					HotkeyHelper.removeHotkey(cyberwareUserData, cyberwareUserData.getActiveItems().get(selectedPart));
				}
				else
				{
					HotkeyHelper.removeHotkey(cyberwareUserData, key);
					HotkeyHelper.assignHotkey(cyberwareUserData, cyberwareUserData.getActiveItems().get(selectedPart), key);
				}
			}
		}
	}
}
