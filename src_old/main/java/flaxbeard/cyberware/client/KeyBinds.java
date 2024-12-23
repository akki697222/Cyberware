package flaxbeard.cyberware.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.input.Keyboard;

public class KeyBinds
{
	public static KeyBinding menu;

	public static void init()
	{
		menu = new KeyBinding("cyberware.keybinds.menu", Keyboard.KEY_R, "cyberware.keybinds.category");
		ClientRegistry.registerKeyBinding(menu);
	}


}
