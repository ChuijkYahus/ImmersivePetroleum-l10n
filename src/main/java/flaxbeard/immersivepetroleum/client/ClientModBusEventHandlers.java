package flaxbeard.immersivepetroleum.client;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.render.BlockAutoLubricatorRenderer;
import flaxbeard.immersivepetroleum.client.render.BlockSeismicSurveyBarrelRenderer;
import flaxbeard.immersivepetroleum.client.render.EntityMotorboatRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockDerrickRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockHydrotreaterRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockOilTankRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockPumpjackRenderer;
import flaxbeard.immersivepetroleum.common.IPContent.Multiblock;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.entity.IPEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

import java.util.function.Supplier;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientModBusEventHandlers{
	@SubscribeEvent
	public static void registerRenders(RegisterRenderers ev){
		registerBERenderNoContext(ev, Multiblock.DERRICK.masterBE(), MultiblockDerrickRenderer::new);
		registerBERenderNoContext(ev, Multiblock.PUMPJACK.masterBE(), MultiblockPumpjackRenderer::new);
		registerBERenderNoContext(ev, Multiblock.DISTILLATIONTOWER.masterBE(), MultiblockDistillationTowerRenderer::new);
		registerBERenderNoContext(ev, Multiblock.HYDROTREATER.masterBE(), MultiblockHydrotreaterRenderer::new);
		registerBERenderNoContext(ev, Multiblock.OILTANK.masterBE(), MultiblockOilTankRenderer::new);
		
		registerBERender(ev, IPTileTypes.AUTOLUBE.get(), BlockAutoLubricatorRenderer::new);
		registerBERender(ev, IPTileTypes.SEISMIC_SURVEY.get(), BlockSeismicSurveyBarrelRenderer::new);
		
		registerEntityRenderingHandler(ev, IPEntityTypes.MOTORBOAT, EntityMotorboatRenderer::new);
		registerEntityRenderingHandler(ev, IPEntityTypes.MOLOTOV, ThrownItemRenderer::new);
	}
	
	private static <T extends BlockEntity> void registerBERenderNoContext(RegisterRenderers event, Supplier<BlockEntityType<? extends T>> type, Supplier<BlockEntityRenderer<T>> render){
		registerBERenderNoContext(event, type.get(), render);
	}
	
	private static <T extends BlockEntity> void registerBERenderNoContext(RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render){
		event.registerBlockEntityRenderer(type, $ -> render.get());
	}
	
	private static <T extends BlockEntity> void registerBERender(RegisterRenderers ev, BlockEntityType<T> type, Supplier<BlockEntityRenderer<T>> factory){
		ev.registerBlockEntityRenderer(type, ctx -> factory.get());
	}
	
	private static <T extends Entity, T2 extends T> void registerEntityRenderingHandler(RegisterRenderers ev, Supplier<EntityType<T2>> type, EntityRendererProvider<T> renderer){
		ev.registerEntityRenderer(type.get(), renderer);
	}
}
