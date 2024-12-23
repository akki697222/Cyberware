package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ScannerSmashPacket implements IMessage
{
	public ScannerSmashPacket() {}
	
	private int x;
	private int y;
	private int z;

	public ScannerSmashPacket(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}
	
	public static class ScannerSmashPacketHandler implements IMessageHandler<ScannerSmashPacket, IMessage>
	{

		@Override
		public IMessage onMessage(ScannerSmashPacket message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(new DoSync(message.x, message.y, message.z));

			return null;
		}
		
	}
	
	private static class DoSync implements Callable<Void>
	{
		private int x;
		private int y;
		private int z;
		
		public DoSync(int x, int y, int z)
		{
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
				TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
				if (te != null && te instanceof TileEntityEngineeringTable)
				{
					TileEntityEngineeringTable eng = (TileEntityEngineeringTable) te;
					eng.smashSounds();
				}
			}
			
			return null;
		}
		

	}
	


}
