package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.ICyberwareUserData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncHudDataPacket implements IMessage
{
	public SyncHudDataPacket() {}
	
	private NBTTagCompound tagCompound;

	public SyncHudDataPacket(NBTTagCompound tagCompound)
	{
		this.tagCompound = tagCompound;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, tagCompound);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tagCompound = ByteBufUtils.readTag(buf);
	}
	
	public static class SyncHudDataPacketHandler implements IMessageHandler<SyncHudDataPacket, IMessage>
	{
		@Override
		public IMessage onMessage(SyncHudDataPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(new DoSync(message.tagCompound, player));

			return null;
		}
	}
	
	private static class DoSync implements Runnable
	{
		private NBTTagCompound tagCompound;
		private EntityPlayer entityPlayer;

		public DoSync(NBTTagCompound tagCompound, EntityPlayer entityPlayer)
		{
			this.tagCompound = tagCompound;
			this.entityPlayer = entityPlayer;
		}
		
		@Override
		public void run()
		{
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
			if (cyberwareUserData != null)
			{
				cyberwareUserData.setHudData(tagCompound);
			}
		}
	}
}
