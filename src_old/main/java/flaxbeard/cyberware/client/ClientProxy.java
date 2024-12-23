package flaxbeard.cyberware.client;

import java.util.ArrayList;
import java.util.List;

import flaxbeard.cyberware.client.gui.InventoryTabFineManipulators;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import flaxbeard.cyberware.api.item.ICyberware.Quality;
import flaxbeard.cyberware.client.render.CyberwareMeshDefinition;
import flaxbeard.cyberware.client.render.RenderCyberZombie;
import flaxbeard.cyberware.client.render.TileEntityBeaconLargeRenderer;
import flaxbeard.cyberware.client.render.TileEntityEngineeringRenderer;
import flaxbeard.cyberware.client.render.TileEntityScannerRenderer;
import flaxbeard.cyberware.client.render.TileEntitySurgeryChamberRenderer;
import flaxbeard.cyberware.common.CommonProxy;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.block.tile.TileEntityBeaconPost.TileEntityBeaconPostMaster;
import flaxbeard.cyberware.common.block.tile.TileEntityEngineeringTable;
import flaxbeard.cyberware.common.block.tile.TileEntityScanner;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgery;
import flaxbeard.cyberware.common.block.tile.TileEntitySurgeryChamber;
import flaxbeard.cyberware.common.entity.EntityCyberZombie;
import flaxbeard.cyberware.common.handler.CreativeMenuHandler;
import flaxbeard.cyberware.common.handler.CyberwareMenuHandler;
import flaxbeard.cyberware.common.handler.EssentialsMissingHandlerClient;
import flaxbeard.cyberware.common.handler.HudHandler;
import flaxbeard.cyberware.common.item.ItemArmorCyberware;
import flaxbeard.cyberware.common.item.ItemBlueprint;
import flaxbeard.cyberware.common.item.ItemCyberware;
import flaxbeard.cyberware.common.item.ItemCyberwareBase;

public class ClientProxy extends CommonProxy
{
	
	@Override
	public void preInit()
	{
		super.preInit();
		
		for (Block block : CyberwareContent.blocks)
		{
			registerRenders(block);
		}
		
		for (Item item : CyberwareContent.items)
		{
			registerRenders(item);
		}

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySurgeryChamber.class, new TileEntitySurgeryChamberRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityCyberZombie.class, RenderCyberZombie::new);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScanner.class, new TileEntityScannerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEngineeringTable.class, new TileEntityEngineeringRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBeaconPostMaster.class, new TileEntityBeaconLargeRenderer());
	}
	
	@Override
	public void init()
	{
		super.init();
		KeyBinds.init();
		MinecraftForge.EVENT_BUS.register(EssentialsMissingHandlerClient.INSTANCE);
		MinecraftForge.EVENT_BUS.register(CreativeMenuHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(CyberwareMenuHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(HudHandler.INSTANCE);
		ShaderUtil.init();

		if (CyberwareConfig.ENABLE_CLOTHES)
		{
			Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor()
			{
				@Override
				public int colorMultiplier(ItemStack stack, int tintIndex)
				{
					return tintIndex > 0 ? -1 : ((ItemArmorCyberware)stack.getItem()).getColor(stack);
				}
			}, CyberwareContent.trenchCoat);
		}
	}
	
	@Override
	public void postInit()
	{
		super.postInit();
		
		MinecraftForge.EVENT_BUS.register(new TabRegistry());
		
		if (TabRegistry.getTabList().size() == 0)
		{
			TabRegistry.registerTab(new InventoryTabVanilla());
		}
		
		TabRegistry.registerTab(new InventoryTabFineManipulators());
	}
	
	private void registerRenders(Block block)
	{
		Item item = Item.getItemFromBlock(block);
		ModelLoader.setCustomModelResourceLocation(item, 
				0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
	}
	
	private void registerRenders(Item item)
	{
		if (item instanceof ItemCyberware)
		{
			ItemCyberware ware = (ItemCyberware) item;
			List<ModelResourceLocation> models = new ArrayList<>();
			if (ware.subnames.length > 0)
			{
				for (int indexSubname = 0; indexSubname < ware.subnames.length; indexSubname++)
				{
					String name = ware.getRegistryName() + "_" + ware.subnames[indexSubname];
					for (Quality quality : Quality.qualities)
					{
						if ( quality.getSpriteSuffix() != null
						  && ware.canHoldQuality(new ItemStack(ware, 1, indexSubname), quality) )
						{
							models.add(new ModelResourceLocation(name + "_" + quality.getSpriteSuffix(), "inventory"));
						}
					}
					models.add(new ModelResourceLocation(name, "inventory"));
				}
			}
			else
			{
				String name = ware.getRegistryName() + "";

				for (Quality quality : Quality.qualities)
				{
					if ( quality.getSpriteSuffix() != null
					  && ware.canHoldQuality(new ItemStack(ware), quality) )
					{
						models.add(new ModelResourceLocation(name + "_" + quality.getSpriteSuffix(), "inventory"));
					}
				}
				models.add(new ModelResourceLocation(name, "inventory"));

			}
			ModelLoader.registerItemVariants(item, models.toArray(new ModelResourceLocation[0]));
			ModelLoader.setCustomMeshDefinition(item, new CyberwareMeshDefinition());
		}
		else if (item instanceof ItemBlueprint)
		{
			for (int i = 0; i < 2; i++)
			{
				ModelLoader.setCustomModelResourceLocation(item, 
						i, new ModelResourceLocation(item.getRegistryName() + (i == 1 ? "_blank" : ""), "inventory"));
			}
		}
		else if (item instanceof ItemCyberwareBase)
		{
			ItemCyberwareBase base = ((ItemCyberwareBase) item);
			if (base.subnames.length > 0)
			{
				for (int indexSubname = 0; indexSubname < base.subnames.length; indexSubname++)
				{
					ModelLoader.setCustomModelResourceLocation(item, 
							indexSubname, new ModelResourceLocation(item.getRegistryName() + "_" + base.subnames[indexSubname], "inventory"));
				}
			}
			else
			{
				ModelLoader.setCustomModelResourceLocation(item, 
						0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			}
		}
		else
		{
			ModelLoader.setCustomModelResourceLocation(item, 
					0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}

	@Override
	public void wrong(TileEntitySurgery tileEntitySurgery)
	{
		tileEntitySurgery.ticksWrong = Minecraft.getMinecraft().player.ticksExisted;
	}
	
	@Override
	public boolean workingOnPlayer(EntityLivingBase entityLivingBase)
	{
		return entityLivingBase == Minecraft.getMinecraft().player;
	}
}
