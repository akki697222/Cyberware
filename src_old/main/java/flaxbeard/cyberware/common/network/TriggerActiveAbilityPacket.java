package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TriggerActiveAbilityPacket implements IMessage
{
	public TriggerActiveAbilityPacket() {}
	
	private ItemStack stack;

	public TriggerActiveAbilityPacket(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeItemStack(buf, stack);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		stack = ByteBufUtils.readItemStack(buf);
	}
	
	public static class TriggerActiveAbilityPacketHandler implements IMessageHandler<TriggerActiveAbilityPacket, IMessage>
	{
		@Override
		public IMessage onMessage(TriggerActiveAbilityPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(message.stack, player));

			return null;
		}
	}
	
	private static class DoSync implements Runnable
	{
		private ItemStack stack;
		private EntityPlayer entityPlayer;

		public DoSync(ItemStack stack, EntityPlayer entityPlayer)
		{
			this.stack = stack;
			this.entityPlayer = entityPlayer;
		}
		
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				CyberwareAPI.useActiveItem(entityPlayer, cyberwareUserData.getCyberware(stack));
			}
		}
	}
}
