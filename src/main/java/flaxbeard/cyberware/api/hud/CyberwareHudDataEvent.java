package flaxbeard.cyberware.api.hud;

import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class CyberwareHudDataEvent extends Event {
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
