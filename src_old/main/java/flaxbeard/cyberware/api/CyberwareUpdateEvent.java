package flaxbeard.cyberware.api;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent;

public class CyberwareUpdateEvent extends EntityEvent
{
	private final EntityLivingBase entityLivingBase;
	private final ICyberwareUserData cyberwareUserData;
	
	public CyberwareUpdateEvent(@Nonnull EntityLivingBase entityLivingBase, @Nonnull ICyberwareUserData cyberwareUserData)
	{
		super(entityLivingBase);
		this.entityLivingBase = entityLivingBase;
		this.cyberwareUserData = cyberwareUserData;
	}
	
	@Nonnull
	public EntityLivingBase getEntityLiving()
	{
		return entityLivingBase;
	}
	
	@Nonnull
	public ICyberwareUserData getCyberwareUserData()
	{
		return cyberwareUserData;
	}
}
