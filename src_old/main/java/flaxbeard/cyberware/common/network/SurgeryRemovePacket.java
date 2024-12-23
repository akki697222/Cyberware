package flaxbeard.cyberware.common.network;

import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.lib.LibConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SurgeryRemovePacket implements IMessage
{
	public SurgeryRemovePacket() {}
	
	private BlockPos pos;
	private int dimensionId;
	private int slotNumber;
	private boolean isNull;

	public SurgeryRemovePacket(BlockPos pos, int dimensionId, int slotNumber, boolean isNull)
	{
		this.pos = pos;
		this.dimensionId = dimensionId;
		this.slotNumber = slotNumber;
		this.isNull = isNull;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeInt(dimensionId);
		buf.writeInt(slotNumber);
		buf.writeBoolean(isNull);

	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		pos = new BlockPos(x, y, z);
		dimensionId = buf.readInt();
		slotNumber = buf.readInt();
		isNull = buf.readBoolean();
	}
	
	public static class SurgeryRemovePacketHandler implements IMessageHandler<SurgeryRemovePacket, IMessage>
	{

		@Override
		public IMessage onMessage(SurgeryRemovePacket message, MessageContext ctx)
		{
			DimensionManager.getWorld(message.dimensionId).addScheduledTask(new DoSync(message.pos, message.dimensionId, message.slotNumber, message.isNull));

			return null;
		}
		
	}
	
	private static class DoSync implements Runnable
	{
		private BlockPos pos;
		private int dimensionId;
		private int slotNumber;
		private boolean isNull;

		private DoSync(BlockPos pos, int dimensionId, int slotNumber, boolean isNull)
		{
			this.pos = pos;
			this.dimensionId = dimensionId;
			this.slotNumber = slotNumber;
			this.isNull = isNull;
		}

		@Override
		public void run()
		{
			World world = DimensionManager.getWorld(dimensionId);
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntitySurgery)
			{
				TileEntitySurgery surgery = (TileEntitySurgery) te;
	
				surgery.discardSlots[slotNumber] = isNull;
				
				if (isNull)
				{
					surgery.disableDependants(surgery.slotsPlayer.getStackInSlot(slotNumber),
							EnumSlot.values()[slotNumber / 10], slotNumber % LibConstants.WARE_PER_SLOT);
				}
				else
				{
					surgery.enableDependsOn(surgery.slotsPlayer.getStackInSlot(slotNumber),
							EnumSlot.values()[slotNumber / 10], slotNumber % LibConstants.WARE_PER_SLOT);
				}
				surgery.updateEssential(EnumSlot.values()[slotNumber / LibConstants.WARE_PER_SLOT]);
				surgery.updateEssence();
			}
		}
		
	}
}
