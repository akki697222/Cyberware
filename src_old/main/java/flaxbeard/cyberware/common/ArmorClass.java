package flaxbeard.cyberware.common;

import javax.annotation.Nonnull;

import java.util.Map.Entry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

import com.google.common.collect.Multimap;
import flaxbeard.cyberware.Cyberware;

public enum ArmorClass {
	
	NONE(),
	LIGHT(),
	HEAVY;
	
	static boolean enableLogging = false;
	static long timeLastLog_ms;
	static int maxEntityArmor = 10;
	static EntityEquipmentSlot[] armorSlots = { EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD };
	static double[] maxPartArmors = { 1.5D, 3.0D, 4.0D, 1.5D };
	
	public static boolean isWearingLightOrNone(EntityLivingBase entityLivingBase)
	{
		return get(entityLivingBase) != HEAVY;
	}
	
	public static ArmorClass get(@Nonnull EntityLivingBase entityLivingBase) {
		// development support
		// Boosted leather chestplate is heavy
		// /give xxx leather_chestplate 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 6, Slot: "chest", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		// Nerfed diamond chestplate is heavy
		// /give xxx diamond_chestplate 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 1, Slot: "chest", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		// Nerfed leather leggings is light
		// /give xxx leather_leggings 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 1, Slot: "legs", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		// Boosted leather boots is heavy
		// /give xxx leather_boots 1 0 {AttributeModifiers:[{UUIDMost: 436328, UUIDLeast: 436329, Amount: 4, Slot: "feet", AttributeName: "generic.armor", Operation: 0, Name: "generic.armor"}]}
		boolean isLogging = false;
		if (enableLogging) {
			final long timeCurrent_ms = System.currentTimeMillis();
			if (timeCurrent_ms > timeLastLog_ms + 2000) {
				timeLastLog_ms = timeCurrent_ms;
				isLogging = true;
			}
		}
		
		// fast check for heavy armor
		final int entityArmor = entityLivingBase.getTotalArmorValue();
		if (entityArmor > maxEntityArmor) {
			if (isLogging) {
				Cyberware.logger.warn(String.format("Total armor %d is greater than %d => this is HEAVY armor",
				                                    entityArmor, maxEntityArmor ));
			}
			return HEAVY;
		}
		
		// slow check per armor part
		boolean hasNoArmor = true;
		for (final EntityEquipmentSlot entityEquipmentSlot : armorSlots) {
			// skip empty slots
			final ItemStack itemStack = entityLivingBase.getItemStackFromSlot(entityEquipmentSlot);
			if (itemStack.isEmpty()) continue;
			hasNoArmor = false;
			
			// caps on forge absorption
			final double maxPartArmor = maxPartArmors[entityEquipmentSlot.getIndex()];
			if (itemStack.getItem() instanceof ISpecialArmor) {
				final ArmorProperties armorProperties = ((ISpecialArmor) itemStack.getItem()).getProperties(entityLivingBase, itemStack, DamageSource.CACTUS, 1.0D, 1);
				if (armorProperties.AbsorbRatio * 25.0D > maxPartArmor) {
					if (isLogging) {
						Cyberware.logger.warn(String.format("ISpecialArmor absorption %.1f is greater than %.1f (%.1f) => this is HEAVY armor",
						                                    armorProperties.AbsorbRatio, 25.0D / maxPartArmor, maxPartArmor ));
					}
					return HEAVY;
				}
			}
			
			// caps on vanilla armor
			if (itemStack.getItem() instanceof ItemArmor) {
				final ItemArmor itemArmor = (ItemArmor) itemStack.getItem();
				final int damageReductionChestplate = itemArmor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.CHEST);
				final double maxChestplateArmor = maxPartArmors[EntityEquipmentSlot.CHEST.getIndex()];
				if (damageReductionChestplate > maxChestplateArmor) {
					if (isLogging) {
						Cyberware.logger.warn(String.format("ItemArmor material chestplate armor %d is greater then %.1f => this is HEAVY armor",
						                                    damageReductionChestplate, maxChestplateArmor ));
					}
					return HEAVY;
				}
			}
			
			// caps on attributes
			// note: modded armor can change attributes without using NBT, see Vampirism Armor of Swiftness
			// note: the ISpecialArmor check might be already covered by this, but not the other way around. 
			final Multimap<String, AttributeModifier> attributeModifiers = itemStack.getAttributeModifiers(entityEquipmentSlot);
			for (final Entry<String, AttributeModifier> entry : attributeModifiers.entries()) {
				if (!entry.getKey().equals(SharedMonsterAttributes.ARMOR.getName())) {
					continue;
				}
				final double armorValue = entry.getValue().getAmount();
				if (armorValue > maxPartArmor) {
					if (isLogging) {
						Cyberware.logger.warn(String.format("Armor attribute %.1f is greater then %.1f => this is HEAVY armor",
						                                    armorValue, maxPartArmor ));
					}
					return HEAVY;
				}
			}
		}
		if (isLogging) {
			Cyberware.logger.warn(String.format("No heavy armor detected, hasNoArmor is %s",
			                                    hasNoArmor ));
		}
		return hasNoArmor ? NONE : LIGHT;
	}
}