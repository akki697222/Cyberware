package flaxbeard.cyberware.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import flaxbeard.cyberware.common.CyberwareConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import flaxbeard.cyberware.api.hud.UpdateHudColorPacket;
import flaxbeard.cyberware.api.item.ICyberware;
import flaxbeard.cyberware.api.item.ICyberware.Quality;
import flaxbeard.cyberware.api.item.IDeconstructable;
import flaxbeard.cyberware.api.item.IMenuItem;
import flaxbeard.cyberware.common.misc.NNLUtil;
import flaxbeard.cyberware.common.network.CyberwareSyncPacket;

public final class CyberwareAPI
{
	/**
	 * Store any functional data of your Cyberware in NBT under this tag, which will be cleared when new items are added or removed
	 * to ensure stacking and such works
	 */
	public static final String DATA_TAG = "cyberwareFunctionData";
	
	public static final String QUALITY_TAG = "cyberwareQuality";

	/**
	 * Quality for Cyberware scavenged from mobs
	 */
	public static final Quality QUALITY_SCAVENGED = new Quality("cyberware.quality.scavenged", "cyberware.quality.scavenged.name_modifier", "scavenged");
	
	/**
	 * Quality for Cyberware built at the Engineering Table
	 */
	public static final Quality QUALITY_MANUFACTURED = new Quality("cyberware.quality.manufactured");

	@CapabilityInject(ICyberwareUserData.class)
	public static final Capability<ICyberwareUserData> CYBERWARE_CAPABILITY = null;

	/**
	 * Maximum Tolerance, per-player
	 */
 	public static final IAttribute TOLERANCE_ATTR = new RangedAttribute(null, "cyberware.tolerance", CyberwareConfig.ESSENCE, 0.0F, Double.MAX_VALUE).setDescription("Tolerance").setShouldWatch(true);

	public static Map<ItemStack, ICyberware> linkedWare = new HashMap<>();
	
	public static SimpleNetworkWrapper PACKET_HANDLER;
	
	/**
	 * Sets the HUD color for the Hudjack, radial menu, and other AR HUD elements
	 * 
	 * @param color	A float representation of the desired color
	 */
	@SideOnly(Side.CLIENT)
	public static void setHUDColor(float[] color)
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData != null)
		{
			cyberwareUserData.setHudColor(color);
		}
	}
	
	public static void syncHUDColor()
	{
		PACKET_HANDLER.sendToServer(new UpdateHudColorPacket(getHUDColorHex()));
	}
	
	/**
	 * Sets the HUD color for the Hudjack, radial menu, and other AR HUD elements
	 * 
	 * @param hexVal A hexadecimal representation of the desired color
	 */
	@SideOnly(Side.CLIENT)
	public static void setHUDColor(int hexVal)
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData != null)
		{
			cyberwareUserData.setHudColor(hexVal);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void setHUDColor(float r, float g, float b)
	{
		setHUDColor(new float[] { r, g, b });
	}
	
	@SideOnly(Side.CLIENT)
	public static int getHUDColorHex()
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData != null)
		{
			return cyberwareUserData.getHudColorHex();
		}
		return 0;
	}
	
	@SideOnly(Side.CLIENT)
	public static float[] getHUDColor()
	{
		EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData != null)
		{
			return cyberwareUserData.getHudColor();
		}
		return new float[] { 0F, 0F, 0F };
	}
	
	/**
	 * Can be used by your ICyberware implementation's setQuality function. Helper method that
	 * writes a quality to an easily accessible NBT tag. See the partner function, readQualityTag
	 * 
	 * @param stack	The stack to write to
	 * @return		The modified stack
	 */
	public static ItemStack writeQualityTag(@Nonnull ItemStack stack, @Nonnull Quality quality)
	{
		if (stack.isEmpty()) return stack;
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null)
		{
			tagCompound = new NBTTagCompound();
			stack.setTagCompound(tagCompound);
		}
		tagCompound.setString(QUALITY_TAG, quality.getUnlocalizedName());
		return stack;
	}
	
	@Nullable
	public static Quality getQualityTag(@Nonnull ItemStack stack)
	{
		if (stack.isEmpty()) return null;
		NBTTagCompound tagCompound = stack.getTagCompound();
		if ( tagCompound == null
		  || !tagCompound.hasKey(QUALITY_TAG, NBT.TAG_STRING) )
		{
			return null;
		}
		return Quality.getQualityFromString(stack.getTagCompound().getString(QUALITY_TAG));
	}
	
	/**
	 * Clears all NBT data from Cyberware related to its function, things like power storage or oxygen storage
	 * This ensures that removed Cyberware will stack. This should only be called on Cyberware that is being removed
	 * from the body or otherwise reset - otherwise it may interrupt functionality.
	 * 
	 * @param stack	The ItemStack to sanitize
	 * @return		A sanitized version of the stack
	 */
	public static ItemStack sanitize(@Nonnull ItemStack stack)
	{
		if (!stack.isEmpty())
		{
			NBTTagCompound tagCompound = stack.getTagCompound();
			if (tagCompound != null && tagCompound.hasKey(DATA_TAG))
			{
				tagCompound.removeTag(DATA_TAG);
			}
			if (tagCompound != null && tagCompound.isEmpty())
			{
				stack.setTagCompound(null);
			}
		}
		
		return stack;
	}
	
	/**
	 * Gets the NBT data for Cyberware related to its function. This data is removed when a piece of Cyberware
	 * is removed, and is not counted when determining whether Cyberware stacks are the same for purposes of merging
	 * and such. This function will create a data tag if one does not exist.
	 * 
	 * @param stack	The ItemStack for which you want the data
	 * @return		The data, in the form of an NBTTagCompound
	 */
	@Nonnull
	public static NBTTagCompound getCyberwareNBT(@Nonnull ItemStack stack)
	{
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound == null)
		{
			tagCompound = new NBTTagCompound();
			stack.setTagCompound(tagCompound);
		}
		if (!tagCompound.hasKey(DATA_TAG))
		{
			tagCompound.setTag(DATA_TAG, new NBTTagCompound());
		}
		
		return tagCompound.getCompoundTag(DATA_TAG);
	}
	
	public static boolean areCyberwareStacksEqual(@Nonnull ItemStack stack1,@Nonnull  ItemStack stack2)
	{
		if (stack1.isEmpty() || stack2.isEmpty()) return false;
		
		ItemStack sanitized1 = sanitize(stack1.copy());
		ItemStack sanitized2 = sanitize(stack2.copy());
		return sanitized1.getItem() == sanitized2.getItem()
		    && sanitized1.getItemDamage() == sanitized2.getItemDamage()
		    && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
	
	/**
	 * Links an ItemStack to an instance of ICyberware. This option is generally worse than
	 * implementing ICyberware in your Item, but if you don't have access to the Item it's the
	 * best option. This version of the method links a specific meta value.
	 * 
	 * @param stack	The ItemStack to link
	 * @param link	An instance of ICyberware to link it to
	 */
	public static void linkCyberware(@Nonnull ItemStack stack, ICyberware link)
	{
		if (stack.isEmpty()) return;
		
		ItemStack key = new ItemStack(stack.getItem(), 1, stack.getItemDamage());
		linkedWare.put(key, link);
	}
	
	/**
	 * Links an Item to an instance of ICyberware. This option is generally worse than
	 * implementing ICyberware in your Item, but if you don't have access to the Item it's the
	 * best option. This version of the method links all meta values.
	 * 
	 * @param item	The Item to link
	 * @param link	An instance of ICyberware to link it to
	 */
	public static void linkCyberware(Item item, ICyberware link)
	{
		if (item == null) return;
		
		ItemStack key = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
		linkedWare.put(key, link);
	}
	
	/**
	 * Determines if the inputted item stack is Cyberware. This means its item either
	 * implements ICyberware or is linked to one (in the case of vanilla items)
	 * 
	 * @param stack	The ItemStack to test
	 * @return		If the stack is valid Cyberware
	 */
	public static boolean isCyberware(@Nullable ItemStack stack)
	{
		if (stack != null)
		{
			return !stack.isEmpty()
			    && ( stack.getItem() instanceof ICyberware
			      || getLinkedWare(stack) != null );
		}
		return false;
	}
	
	/**
	 * Returns an instance of ICyberware linked with an itemstack, usually
	 * the item which extends ICyberware, though it may be a standalone
	 * ICyberware-implementing object
	 * 
	 * @param stack	The ItemStack, from which the linked ICyberware is found
	 * @return		The linked instance of ICyberware
	 */
	public static ICyberware getCyberware(@Nonnull ItemStack stack)
	{
		if (!stack.isEmpty())
		{
			if (stack.getItem() instanceof ICyberware)
			{
				return (ICyberware) stack.getItem();
			}
			else if (getLinkedWare(stack) != null)
			{
				return getLinkedWare(stack);
			}
		}
		
		throw new RuntimeException("Cannot call getCyberware on a non-cyberware item!");
	}
	
	/**
	 * Determines if the inputted item stack can be destroyed in the Engineering Table,
	 * meaning it implements IDeconstructable.
	 * 
	 * @param stack	The ItemStack to test
	 * @return		If the stack can be deconstructed.
	 */
	public static boolean canDeconstruct(@Nonnull ItemStack stack)
	{
		return !stack.isEmpty()
		    && (stack.getItem() instanceof IDeconstructable)
		    && ((IDeconstructable) stack.getItem()).canDestroy(stack);
	}

	/**
	 * Returns a list of ItemStacks containing the components of a destructable
	 * item.
	 * 
	 * @param stack	The ItemStack to test
	 * @return		The components of the item
	 */
	public static NonNullList<ItemStack> getComponents(@Nonnull ItemStack stack)
	{
		if ( !stack.isEmpty()
		  && stack.getItem() instanceof IDeconstructable )
		{
			return NNLUtil.copyList(((IDeconstructable)stack.getItem()).getComponents(stack));
		}
		
		throw new RuntimeException("Cannot call getComponents on a non-cyberware or non deconstructable item!");
	}
	
	@Nullable
	private static ICyberware getLinkedWare(@Nonnull ItemStack stack)
	{
		if (stack.isEmpty()) return null;
		
		return getWareFromKey(stack);
	}
	
	@Nullable
	private static ICyberware getWareFromKey(@Nonnull ItemStack key)
	{
		for (Entry<ItemStack, ICyberware> entry : linkedWare.entrySet())
		{
			ItemStack entryKey = entry.getKey();
			if ( key.getItem() == entryKey.getItem()
			  && ( entryKey.getItemDamage() == OreDictionary.WILDCARD_VALUE
				|| entryKey.getItemDamage() == key.getItemDamage() ) ) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * A shortcut method to get you the ICyberwareUserData of a specific entity.
	 * This will return null if the entity is null or has no capability.
	 *
	 * @param targetEntity	The entity whose ICyberwareUserData you want
	 * @return				The ICyberwareUserData associated with the entity
	 */
	@Nullable
	public static ICyberwareUserData getCapabilityOrNull(@Nullable Entity targetEntity)
	{
		if (targetEntity == null) return null;
		return targetEntity.getCapability(CYBERWARE_CAPABILITY, EnumFacing.EAST);
	}
	
	/**
	 * A shortcut method to determine if the entity that is inputted
	 * has ICyberwareUserData. Works with null entites.
	 * This is very CPU intensive, consider using getCapabilityOrNull() instead.
	 * 
	 * @param targetEntity	The entity to test
	 * @return				If the entity has ICyberwareUserData
	 */
	@Deprecated
	public static boolean hasCapability(@Nullable Entity targetEntity)
	{
		return getCapabilityOrNull(targetEntity) != null;
	}
	
	/**
	 * Assistant method to hasCapability. A shortcut to get you the ICyberwareUserData
	 * of a specific entity. Note that you must verify if it has the capability first.
	 * This is very CPU intensive, consider using getCapabilityOrNull() instead.
	 * 
	 * @param targetEntity	The entity whose ICyberwareUserData you want
	 * @return				The ICyberwareUserData associated with the entity
	 */
	@Deprecated
	public static ICyberwareUserData getCapability(@Nonnull Entity targetEntity)
	{
		return getCapabilityOrNull(targetEntity);
	}
	
	/**
	 * A shortcut method for event handlers and the like to quickly tell if an entity
	 * has a piece of Cyberware installed. Can handle null entites and entities without
	 * ICyberwareUserData.
	 * This is very CPU intensive, consider using getCapabilityOrNull() instead.
	 * 
	 * @param targetEntity	The entity you want to check
	 * @param stack			The Cyberware you want to check for
	 * @return				If the entity has the Cyberware
	 */
	@Deprecated
	public static boolean isCyberwareInstalled(@Nullable Entity targetEntity, ItemStack stack)
	{
		ICyberwareUserData cyberwareUserData = getCapabilityOrNull(targetEntity);
		return cyberwareUserData != null && cyberwareUserData.isCyberwareInstalled(stack);
	}
	
	/**
	 * A shortcut method for event handlers and the like to quickly determine what level of
	 * Cyberware is installed. Returns 0 if none. Can handle null entites and entities without
	 * ICyberwareUserData.
	 * This is very CPU intensive, consider using getCapabilityOrNull() instead.
	 * 
	 * @param targetEntity	The entity you want to check
	 * @param stack			The Cyberware you want to check for
	 * @return				If the entity has the Cyberware, the level, or 0 if not
	 */
	@Deprecated
	public static int getCyberwareRank(@Nullable Entity targetEntity, ItemStack stack)
	{
		ICyberwareUserData cyberwareUserData = getCapabilityOrNull(targetEntity);
		return cyberwareUserData == null ? 0 : cyberwareUserData.getCyberwareRank(stack);
	}
	
	/**
	 * A shortcut method for event handlers and the like to get the itemstack for a piece
	 * of cyberware. Useful for NBT data. Can handle null entites and entities without
	 * ICyberwareUserData.
	 * This is very CPU intensive, consider using getCapabilityOrNull() instead.
	 * 
	 * @param targetEntity	The entity you want to check
	 * @param stack			The Cyberware you want to check for
	 * @return				The ItemStack found, or null if none
	 */
	@Deprecated
	public static ItemStack getCyberware(@Nullable Entity targetEntity, ItemStack stack)
	{
		ICyberwareUserData cyberwareUserData = getCapabilityOrNull(targetEntity);
		return cyberwareUserData == null ? ItemStack.EMPTY : cyberwareUserData.getCyberware(stack);
	}
	
	public static void updateData(Entity targetEntity)
	{
		if (!targetEntity.world.isRemote)
		{
			WorldServer world = (WorldServer) targetEntity.world;
			
			ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(targetEntity);
			if (cyberwareUserData == null) return;
			NBTTagCompound tagCompound = cyberwareUserData.serializeNBT();
			
			if (targetEntity instanceof EntityPlayer)
			{
				PACKET_HANDLER.sendTo(new CyberwareSyncPacket(tagCompound, targetEntity.getEntityId()), (EntityPlayerMP) targetEntity);
				// Cyberware.logger.info("Sent data for player " + ((EntityPlayer) targetEntity).getName() + " to that player's client");
			}

			for (EntityPlayer trackingPlayer : world.getEntityTracker().getTrackingPlayers(targetEntity))
			{
				PACKET_HANDLER.sendTo(new CyberwareSyncPacket(tagCompound, targetEntity.getEntityId()), (EntityPlayerMP) trackingPlayer);
				/*
				if (targetEntity instanceof EntityPlayer)
				{
					Cyberware.logger.info("Sent data for player " + ((EntityPlayer) targetEntity).getName() + " to player " + trackingPlayer.getName());
				}
				*/
			}
		}
	}
	
	public static void useActiveItem(Entity entity, ItemStack stack)
	{
		((IMenuItem) stack.getItem()).use(entity, stack);
	}

}
