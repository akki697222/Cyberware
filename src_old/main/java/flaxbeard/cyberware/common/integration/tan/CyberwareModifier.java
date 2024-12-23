package flaxbeard.cyberware.common.integration.tan;

import flaxbeard.cyberware.Cyberware;
import flaxbeard.cyberware.api.CyberwareAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import flaxbeard.cyberware.api.ICyberwareUserData;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.stat.capability.IThirst;
import toughasnails.api.temperature.IModifierMonitor;
import toughasnails.api.temperature.IModifierMonitor.Context;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale.TemperatureRange;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.temperature.modifier.TemperatureModifier;

import javax.annotation.Nonnull;

public class CyberwareModifier extends TemperatureModifier
{
	private final Type cyberwareType;
	private final String description;
	
	CyberwareModifier(@Nonnull Type type)
	{
		super(Cyberware.MODID + ":" + type.name);
		cyberwareType = type;
		description = Cyberware.MODNAME + ": " + cyberwareType.name;
	}
	
	@Override
	public Temperature applyPlayerModifiers(@Nonnull EntityPlayer entityPlayer, @Nonnull Temperature temperature, @Nonnull IModifierMonitor iModifierMonitor)
	{
		ICyberwareUserData cyberwareUserData = CyberwareAPI.getCapabilityOrNull(entityPlayer);
		if (cyberwareUserData == null) return temperature;
		ItemStack itemStackCyberware = cyberwareUserData.getCyberware(cyberwareType.getCyberware());
		if (itemStackCyberware.isEmpty()) return temperature;
		
		Temperature temperatureToReturn = temperature;
		switch(cyberwareType)
		{
		case SWEAT:
		{
			boolean needCooling = temperature.getRange() == TemperatureRange.WARM
			                   || temperature.getRange() == TemperatureRange.HOT;
			
			if ( needCooling
			  && (ThirstHelper.getThirstData(entityPlayer).getThirst() > 0) )
			{
				if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST))
				{
					IThirst data = ThirstHelper.getThirstData(entityPlayer);
					data.setExhaustion(Math.min(data.getExhaustion() + 0.008F, 40.0F));
				}
				temperatureToReturn = new Temperature(temperature.getRawValue() + cyberwareType.modifier);
			}
			
			iModifierMonitor.addEntry(new Context(getId(), getDescription(), temperature, temperatureToReturn));
			break;
		}
		case BLUBBER:
		{
			temperatureToReturn = new Temperature(temperature.getRawValue() + cyberwareType.modifier);
			iModifierMonitor.addEntry(new Context(getId(), getDescription(), temperature, temperatureToReturn));
			break;
		}
		}
		
		return temperatureToReturn;
	}
	
	@Override
	public boolean isPlayerSpecific()
	{
		return true;
	}
	
	/* done by ancestor
	@Nonnull
	@Override
	public String getId()
	{
		return Cyberware.MODID + cyberwareType.name;
	}
	*/
	
	public String getDescription() {
		return description;
	}
	
	enum Type
	{
		SWEAT("Sweat",-4, 0),
		BLUBBER("Blubber",3, 1);
		
		private final String name;
		private final int modifier;
		private final int meta;
		
		Type(String id, int mod, int meta)
		{
			this.name = id;
			this.modifier = mod;
			this.meta = meta;
		}
		
		public ItemStack getCyberware()
		{
			return new ItemStack(ToughAsNailsIntegration.sweat, 1, meta);
		}
	}
}
