package flaxbeard.immersivepetroleum.common.items;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT)
public class SneakScrollHandler{
	private static boolean sneaking = false;
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event){
		if(event.getEntity() == Minecraft.getInstance().getCameraEntity()){
			sneaking = event.getEntity().isShiftKeyDown();
		}
	}
	
	@SubscribeEvent
	public static void handleScroll(InputEvent.MouseScrollingEvent event){
		double delta = event.getScrollDeltaY();
		
		if(sneaking && delta != 0.0){
			Player player = MCUtil.getPlayer();
			
			DebugItem.ClientInputHandler.onSneakScrolling(event, player, delta);
			ProjectorItem.ClientInputHandler.onSneakScrolling(event, player, delta);
		}
	}
}
