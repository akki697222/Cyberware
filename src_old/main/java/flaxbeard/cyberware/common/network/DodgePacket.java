package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.hud.INotification;
import flaxbeard.cyberware.api.hud.NotificationInstance;
import flaxbeard.cyberware.client.ClientUtils;
import flaxbeard.cyberware.common.handler.HudHandler;
import io.netty.buffer.ByteBuf;

import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DodgePacket implements IMessage
{
	public DodgePacket() {}
	
	private int entityId;

	public DodgePacket(int entityId)
	{
		this.entityId = entityId;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityId);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		entityId = buf.readInt();
	}
	
	public static class DodgePacketHandler implements IMessageHandler<DodgePacket, IMessage>
	{

		@Override
		public IMessage onMessage(DodgePacket message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(new DoSync(message.entityId));

			return null;
		}
		
	}
	
	private static class DoSync implements Callable<Void>
	{
		private int entityId;
		
		public DoSync(int entityId)
		{
			this.entityId = entityId;
		}
		
		@Override
		public Void call()
		{
			Entity targetEntity = Minecraft.getMinecraft().world.getEntityByID(entityId);
			
			if (targetEntity != null)
			{
				for (int index = 0; index < 25; index++)
				{
					Random rand = targetEntity.world.rand;
					targetEntity.world.spawnParticle(EnumParticleTypes.SPELL, targetEntity.posX, targetEntity.posY + rand.nextFloat() * targetEntity.height, targetEntity.posZ, 
							(rand.nextFloat() - .5F) * .2F,
							0,
							(rand.nextFloat() - .5F) * .2F,
							255, 255, 255 );
				
				}
				
				targetEntity.playSound(SoundEvents.ENTITY_FIREWORK_SHOOT, 1F, 1F);
				
				if (targetEntity == Minecraft.getMinecraft().player)
				{
					HudHandler.addNotification(new NotificationInstance(targetEntity.ticksExisted, new DodgeNotification()));
				}
			}
			
			return null;
		}
	}
	
	private static class DodgeNotification implements INotification
	{

		@Override
		public void render(int x, int y)
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(HudHandler.HUD_TEXTURE);
			
			GlStateManager.pushMatrix();
			float[] color = CyberwareAPI.getHUDColor();
			GlStateManager.color(color[0], color[1], color[2]);
			ClientUtils.drawTexturedModalRect(x + 1, y + 1, 0, 39, 15, 14);
			GlStateManager.popMatrix();
		}

		@Override
		public int getDuration()
		{
			return 5;
		}
	}

}
