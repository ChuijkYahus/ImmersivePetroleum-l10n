package flaxbeard.immersivepetroleum.client;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.fluids.IPFluid;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class BlockRenderLayers{
	
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event){
		for(IPFluid.IPFluidEntry f: IPFluid.FLUIDS){
			setRenderLayer(f, RenderType.translucent());
		}
	}
	
	private static void setRenderLayer(IPFluid.IPFluidEntry entry, RenderType types){
		ItemBlockRenderTypes.setRenderLayer(entry.source().get(), types);
		ItemBlockRenderTypes.setRenderLayer(entry.flowing().get(), types);
	}
}
