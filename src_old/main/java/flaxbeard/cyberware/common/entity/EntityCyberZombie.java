package flaxbeard.cyberware.common.entity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import flaxbeard.cyberware.api.CyberwareUserDataImpl;
import flaxbeard.cyberware.api.item.ICyberware.EnumSlot;
import flaxbeard.cyberware.common.CyberwareConfig;
import flaxbeard.cyberware.common.CyberwareContent;
import flaxbeard.cyberware.common.handler.CyberwareDataHandler;
import flaxbeard.cyberware.common.lib.LibConstants;

public class EntityCyberZombie extends EntityZombie
{
	private static final DataParameter<Integer> CYBER_VARIANT = EntityDataManager.createKey(EntityCyberZombie.class, DataSerializers.VARINT);

	public boolean hasRandomWare;
	private CyberwareUserDataImpl cyberware;
	
	public EntityCyberZombie(World worldIn)
	{
		super(worldIn);
		cyberware = new CyberwareUserDataImpl();
		hasRandomWare = false;
	}
	
	protected void entityInit()
	{
		super.entityInit();
		dataManager.register(CYBER_VARIANT, 0);
	}
	
	@Override
	public void onLivingUpdate()
	{
		if ( !hasRandomWare
		  && !world.isRemote )
		{
			if ( !isBrute()
			  && world.rand.nextFloat() < (LibConstants.NATURAL_BRUTE_CHANCE / 100F) )
			{
				setBrute();
			}
			CyberwareDataHandler.addRandomCyberware(this, isBrute());
			if (isBrute())
			{
				getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Brute Bonus", 6D, 0));
				getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier("Brute Bonus", 1D, 0));
			}
			setHealth(getMaxHealth());
			hasRandomWare = true;
		}
		if ( isBrute()
		  && height != (1.95F * 1.2F) )
		{
			setSizeNormal(0.6F * 1.2F, 1.95F * 1.2F);
		}
		super.onLivingUpdate();
	}
	
	protected void setSizeNormal(float width, float height)
	{
		if ( width != this.width
		  || height != this.height )
		{
			float widthPrevious = this.width;
			this.width = width;
			this.height = height;
			AxisAlignedBB axisalignedbb = getEntityBoundingBox();
			setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
			                                       axisalignedbb.minX + width, axisalignedbb.minY + height, axisalignedbb.minZ + width ));
			
			if ( this.width > widthPrevious
			  && !firstUpdate
			  && !world.isRemote )
			{
				move(MoverType.SELF, widthPrevious - width, 0.0D, widthPrevious - width);
			}
		}
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		tagCompound = super.writeToNBT(tagCompound);
		
		tagCompound.setBoolean("hasRandomWare", hasRandomWare);
		tagCompound.setBoolean("brute", isBrute());
		
		if (hasRandomWare)
		{
			NBTTagCompound tagCompoundCyberware = cyberware.serializeNBT();
			tagCompound.setTag("ware", tagCompoundCyberware);
		}
		return tagCompound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		
		boolean brute = tagCompound.getBoolean("brute");
		if (brute)
		{
			setBrute();
		}
		hasRandomWare = tagCompound.getBoolean("hasRandomWare");
		if (tagCompound.hasKey("ware"))
		{
			cyberware.deserializeNBT(tagCompound.getCompoundTag("ware"));
		}
	}
	
	@Override
	public <T> T getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing)
	{
		if (capability == CyberwareAPI.CYBERWARE_CAPABILITY)
		{
			return (T) cyberware;
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<?> capability, net.minecraft.util.EnumFacing facing)
	{
		return capability == CyberwareAPI.CYBERWARE_CAPABILITY
		    || super.hasCapability(capability, facing);
	}
	
	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier)
	{
		super.dropEquipment(wasRecentlyHit, lootingModifier);
		
		if ( CyberwareConfig.ENABLE_KATANA
		  && CyberwareConfig.MOBS_ADD_CLOTHES
		  && !getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()
		  && getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() == CyberwareContent.katana )
		{
			ItemStack itemstack = getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).copy();
			if (itemstack.isItemStackDamageable())
			{
				int i = Math.max(itemstack.getMaxDamage() - 25, 1);
				int j = itemstack.getMaxDamage() - rand.nextInt(rand.nextInt(i) + 1);
				
				if (j > i)
				{
					j = i;
				}
				
				if (j < 1)
				{
					j = 1;
				}
				
				itemstack.setItemDamage(j);
			}
			
			entityDropItem(itemstack, 0.0F);
		}
		
		if (hasRandomWare)
		{
			float rarity = Math.min(100.0F, CyberwareConfig.MOBS_CYBER_ZOMBIE_DROP_RARITY + lootingModifier * 5.0F);
			if (world.rand.nextFloat() < (rarity / 100.0F))
			{
				List<ItemStack> allWares = new ArrayList<>();
				for (EnumSlot slot : EnumSlot.values())
				{
					NonNullList<ItemStack> nnlInstalled = cyberware.getInstalledCyberware(slot);
					for (ItemStack stack : nnlInstalled)
					{
						if (!stack.isEmpty())
						{
							allWares.add(stack);
						}
					}
				}
				
				allWares.removeAll(Collections.singleton(ItemStack.EMPTY));
				
				// Sanity check for corrupted NBT
				if (allWares.size() == 0)
				{
					Cyberware.logger.error(String.format("Invalid cyberzombie with hasRandomWare %s with actually no implants: %s",
					                                     hasRandomWare, this ));
					return;
				}
				
				ItemStack drop = ItemStack.EMPTY;
				int count = 0;
				while ( count < 50
				     && ( drop.isEmpty()
				       || drop.getItem() == CyberwareContent.creativeBattery
				       || drop.getItem() == CyberwareContent.bodyPart ) )
				{
					int random = world.rand.nextInt(allWares.size());
					drop = allWares.get(random).copy();
					drop = CyberwareAPI.sanitize(drop);
					drop = CyberwareAPI.getCyberware(drop).setQuality(drop, CyberwareAPI.QUALITY_SCAVENGED);
					drop.setCount(1);
					count++;
				}
				
				if (count < 50)
				{
					entityDropItem(drop, 0.0F);
				}
			}
		}
	}
	
	@Override
	protected void setEquipmentBasedOnDifficulty(@Nonnull DifficultyInstance difficulty)
	{
		super.setEquipmentBasedOnDifficulty(difficulty);
		
		if ( CyberwareConfig.ENABLE_KATANA
		  && CyberwareConfig.MOBS_ADD_CLOTHES
		  && !getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()
		  && getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() == Items.IRON_SWORD )
		{
			setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(CyberwareContent.katana));
			setDropChance(EntityEquipmentSlot.MAINHAND, 0F);
		}
	}
	
	public boolean isBrute()
	{
		return dataManager.get(CYBER_VARIANT) == 1;
	}
	
	public boolean setBrute()
	{
		setChild(false);
		dataManager.set(CYBER_VARIANT, 1);
		
		return !hasRandomWare;
	}
}
