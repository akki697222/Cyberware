package flaxbeard.cyberware.common.network;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SwitchHeldItemAndRotationPacket implements IMessage
{
	public SwitchHeldItemAndRotationPacket() {}
	
	private int slot;
	private int entityId;
	private int attackerId;

	public SwitchHeldItemAndRotationPacket(int slot, int entityId, int attackerId)
	{
		this.slot = slot;
		this.entityId = entityId;
		this.attackerId = attackerId;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityId);
		buf.writeInt(slot);
		buf.writeInt(attackerId);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		entityId = buf.readInt();
		slot = buf.readInt();
		attackerId = buf.readInt();
	}
	
	public static class SwitchHeldItemAndRotationPacketHandler implements IMessageHandler<SwitchHeldItemAndRotationPacket, IMessage>
	{

		@Override
		public IMessage onMessage(SwitchHeldItemAndRotationPacket message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(new DoSync(message.entityId, message.slot, message.attackerId));

			return null;
		}
		
	}
	
	private static class DoSync implements Callable<Void>
	{
		private int entityId;
		private int slot;
		private int attackerId;

		
		public DoSync(int entityId, int slot, int attackerId)
		{
			this.entityId = entityId;
			this.slot = slot;
			this.attackerId = attackerId;
		}
		
		@Override
		public Void call()
		{
			Entity targetEntity = Minecraft.getMinecraft().world.getEntityByID(entityId);
			
			if (targetEntity != null)
			{
				((EntityPlayer) targetEntity).inventory.currentItem = slot;

				if (attackerId != -1)
				{
					((EntityPlayer) targetEntity).closeScreen();
					Entity facingEntity = Minecraft.getMinecraft().world.getEntityByID(attackerId);
					
					if (facingEntity != null)
					{
						faceEntity(targetEntity, facingEntity);
					}
				}
			}
			
			return null;
		}
		
		public static void faceEntity(Entity player, Entity entity)
		{
			double d0 = entity.posX - player.posX;
			double d2 = entity.posZ - player.posZ;
			double d1;

			if (entity instanceof EntityLivingBase)
			{
				EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
				d1 = entitylivingbase.posY + entitylivingbase.getEyeHeight()
				   - (player.posY + player.getEyeHeight());
			}
			else
			{
				d1 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0D
				   - (player.posY + player.getEyeHeight());
			}

			double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
			player.rotationPitch = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
			player.rotationYaw = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
		}
		
	}

}
