package flaxbeard.cyberware.api.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.eventhandler.Event;

public class CyberwareHudDataEvent extends Event
{
	private List<IHudElement> elements = new ArrayList<>();
	
	public List<IHudElement> getElements()
	{
		return elements;
	}
	
	public void addElement(IHudElement element)
	{
		elements.add(element);
	}
}
