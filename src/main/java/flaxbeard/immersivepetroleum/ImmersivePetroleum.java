package flaxbeard.immersivepetroleum;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.cfg.IPClientConfig;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.RecipeReloadListener;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.util.commands.ReservoirCommand;
import flaxbeard.immersivepetroleum.common.util.loot.IPLootFunctions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod(ImmersivePetroleum.MODID)
public class ImmersivePetroleum{
	public static final String MODID = "immersivepetroleum";
	
	public static final Logger log = LogManager.getLogger(MODID);
	
	public static final CommonProxy proxy = proxy(() -> FMLLoader.getDist().isClient() ? new ClientProxy() : new CommonProxy());
	
	private static CommonProxy proxy(Supplier<CommonProxy> proxy){
		return proxy.get();
	}
	
	public ImmersivePetroleum(ModContainer container, Dist dist, IEventBus eBus){
		container.registerConfig(ModConfig.Type.SERVER, IPServerConfig.ALL);
		container.registerConfig(ModConfig.Type.CLIENT, IPClientConfig.ALL);
		
		eBus.addListener(this::setup);
		eBus.addListener(this::loadComplete);
		
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::worldLoad);
		NeoForge.EVENT_BUS.addListener(this::serverStarting);
		NeoForge.EVENT_BUS.addListener(this::registerCommand);
		NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
		
		eBus.addListener(this::networking);
		eBus.addListener(this::registerMenuScreens);
		
		IPRegisters.addRegistersToEventBus(eBus);
		
		IPContent.modConstruction(eBus);
		IPRegisters.runCallbacks(eBus);
		
		IPLootFunctions.modConstruction(eBus);
		IPRecipeTypes.modConstruction(eBus);
		
		//IPToolShaders.modConstruction(); // TODO Perhaps IPToolShaders will make a return in the Future.
	}
	
	private void setup(FMLCommonSetupEvent event){
		NeoForge.EVENT_BUS.register(new CommonEventHandler());
		
		proxy.setup(event);
		
		IPContent.setup(event);
		
		/*
		if(ModList.get().isLoaded("computercraft")){
			IPPeripheralProvider.init();
		}
		*/
		
		ExternalModContent.setup(event);
		ReservoirHandler.recalculateChances();
	}
	
	private void loadComplete(FMLLoadCompleteEvent event){
		proxy.completed(event);
	}
	
	private void registerMenuScreens(RegisterMenuScreensEvent ev){
		proxy.registerContainersAndScreens(ev);
	}
	
	private void registerCommand(RegisterCommandsEvent event){
		LiteralArgumentBuilder<CommandSourceStack> ip = Commands.literal("ip");
		
		ip.then(ReservoirCommand.create());
		
		event.getDispatcher().register(ip);
	}
	
	private void addReloadListeners(AddReloadListenerEvent event){
		event.addListener(new RecipeReloadListener(event.getServerResources()));
	}
	
	private void worldLoad(LevelEvent.Load event){
		if(!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel world && world.dimension() == Level.OVERWORLD){
			final DimensionDataStorage dimData = world.getDataStorage();
			
			ReservoirRegionDataStorage.init(dimData);
			dimData.computeIfAbsent(new SavedData.Factory<>(IPSaveData::new, IPSaveData::new), IPSaveData.dataName);
		}
	}
	
	private void serverStarting(ServerStartingEvent event){
		ReservoirHandler.recalculateChances();
	}
	
	private void networking(RegisterPayloadHandlersEvent event){
		IPPacketHandler.init(event.registrar(MODID));
	}
}
