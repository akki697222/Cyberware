package flaxbeard.cyberware.common.network;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ParticlePacket implements IMessage
{
	public ParticlePacket() {}
	
	private int effectId;
	private float x;
	private float y;
	private float z;

	public ParticlePacket(int effectId, float x, float y, float z)
	{
		this.effectId = effectId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(effectId);
		buf.writeFloat(x);
		buf.writeFloat(y);
		buf.writeFloat(z);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		effectId = buf.readInt();
		x = buf.readFloat();
		y = buf.readFloat();
		z = buf.readFloat();
	}
	
	public static class ParticlePacketHandler implements IMessageHandler<ParticlePacket, IMessage>
	{

		@Override
		public IMessage onMessage(ParticlePacket message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(new DoSync(message.effectId, message.x, message.y, message.z));

			return null;
		}
		
	}
	
	private static class DoSync implements Callable<Void>
	{
		private int effectId;
		private float x;
		private float y;
		private float z;
		
		public DoSync(int effectId, float x, float y, float z)
		{
			this.effectId = effectId;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public Void call()
		{
			World world = Minecraft.getMinecraft().world;
			
			if (world != null)
			{
				switch (effectId)
				{
				case 0:
					for (int index = 0; index < 5; index++)
					{
						world.spawnParticle(EnumParticleTypes.HEART,
								x + world.rand.nextFloat() - 0.5F,
								y + world.rand.nextFloat() - 0.5F,
								z + world.rand.nextFloat() - 0.5F,
								2.0F * (world.rand.nextFloat() - 0.5F),
								0.5F,
								2.0F * (world.rand.nextFloat() - 0.5F) );
					}
					break;
					
				case 1:
					for (int index = 0; index < 5; index++)
					{
						world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY,
								x + world.rand.nextFloat() - 0.5F,
								y + world.rand.nextFloat() - 0.5F,
								z + world.rand.nextFloat() - 0.5F,
								2.0F * (world.rand.nextFloat() - 0.5F),
								.5F,
								2.0F * (world.rand.nextFloat() - 0.5F) );
					}
					break;
				}
			}
			
			return null;
		}
	}
}
