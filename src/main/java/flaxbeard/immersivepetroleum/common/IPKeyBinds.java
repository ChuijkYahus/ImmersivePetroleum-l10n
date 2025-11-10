package flaxbeard.immersivepetroleum.common;

import com.mojang.blaze3d.platform.InputConstants;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class IPKeyBinds{
	public static final KeyMapping keybind_preview_flip = new KeyMapping("key.immersivepetroleum.projector.flip", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.immersivepetroleum");
	
	@SubscribeEvent
	public static void registerKeybind(RegisterKeyMappingsEvent event){
		keybind_preview_flip.setKeyConflictContext(KeyConflictContext.IN_GAME);
		
		event.register(keybind_preview_flip);
	}
}
