package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenRadialMenuPacket implements IMessage
{
	public OpenRadialMenuPacket() {}

	@Override
	public void toBytes(ByteBuf buf)
	{
		// no operation
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		// no operation
	}
	
	public static class OpenRadialMenuPacketHandler implements IMessageHandler<OpenRadialMenuPacket, IMessage>
	{
		@Override
		public IMessage onMessage(OpenRadialMenuPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(player));

			return null;
		}
	}
	
	private static class DoSync implements Runnable
	{
		private EntityPlayer entityPlayer;

		public DoSync(EntityPlayer entityPlayer)
		{
			this.entityPlayer = entityPlayer;
		}
		
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				cyberwareUserData.setOpenedRadialMenu(true);
			}
		}
	}
}
