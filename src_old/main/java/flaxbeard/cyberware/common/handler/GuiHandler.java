package flaxbeard.cyberware.common.handler;

import flaxbeard.cyberware.client.gui.ContainerFineManipulators;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import flaxbeard.cyberware.client.gui.ContainerBlueprintArchive;
import flaxbeard.cyberware.client.gui.ContainerComponentBox;
import flaxbeard.cyberware.client.gui.ContainerEngineeringTable;
import flaxbeard.cyberware.client.gui.ContainerScanner;
import flaxbeard.cyberware.client.gui.ContainerSurgery;
import flaxbeard.cyberware.client.gui.GuiBlueprintArchive;
import flaxbeard.cyberware.client.gui.GuiComponentBox;
import flaxbeard.cyberware.client.gui.GuiEngineeringTable;
import flaxbeard.cyberware.client.gui.GuiFineManipulators;
import flaxbeard.cyberware.client.gui.GuiScanner;
import flaxbeard.cyberware.client.gui.GuiSurgery;
import flaxbeard.cyberware.common.block.tile.TileEntityBlueprintArchive;
import flaxbeard.cyberware.common.block.tile.TileEntityComponentBox;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityScanner;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;

public class GuiHandler implements IGuiHandler
{

	@Override
	public Object getServerGuiElement(int id, EntityPlayer entityPlayer, World world, int x, int y, int z)
	{
		switch (id)
		{
			case 0:
				return new ContainerSurgery(entityPlayer.inventory, (TileEntitySurgery) world.getTileEntity(new BlockPos(x, y, z)));
			case 1:
				return new ContainerFineManipulators(entityPlayer.inventory, true, entityPlayer);
			case 2:
				return new ContainerEngineeringTable(entityPlayer.getCachedUniqueIdString(), entityPlayer.inventory, (TileEntityEngineeringTable) world.getTileEntity(new BlockPos(x, y, z)));
			case 3:
				return new ContainerScanner(entityPlayer.inventory, (TileEntityScanner) world.getTileEntity(new BlockPos(x, y, z)));
			case 4:
				return new ContainerBlueprintArchive(entityPlayer.inventory, (TileEntityBlueprintArchive) world.getTileEntity(new BlockPos(x, y, z)));
			case 5:
				return new ContainerComponentBox(entityPlayer.inventory, (TileEntityComponentBox) world.getTileEntity(new BlockPos(x, y, z)));
			default:
				return new ContainerComponentBox(entityPlayer.inventory, entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem));
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer entityPlayer, World world, int x, int y, int z)
	{
		switch (id)
		{
			case 0:
				return new GuiSurgery(entityPlayer.inventory, (TileEntitySurgery) world.getTileEntity(new BlockPos(x, y, z)));
			case 1:
				return new GuiFineManipulators(entityPlayer, new ContainerFineManipulators(entityPlayer.inventory, false, entityPlayer));
			case 2:
				return new GuiEngineeringTable(entityPlayer.inventory, (TileEntityEngineeringTable) world.getTileEntity(new BlockPos(x, y, z)));
			case 3:
				return new GuiScanner(entityPlayer.inventory, (TileEntityScanner) world.getTileEntity(new BlockPos(x, y, z)));
			case 4:
				return new GuiBlueprintArchive(entityPlayer.inventory, (TileEntityBlueprintArchive) world.getTileEntity(new BlockPos(x, y, z)));
			case 5:
				return new GuiComponentBox(entityPlayer.inventory, (TileEntityComponentBox) world.getTileEntity(new BlockPos(x, y, z)));
			default:
				return new GuiComponentBox(entityPlayer.inventory, entityPlayer.inventory.mainInventory.get(entityPlayer.inventory.currentItem));
		}
	}

}
