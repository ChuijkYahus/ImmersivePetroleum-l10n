package flaxbeard.immersivepetroleum.common.cfg;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.base.Preconditions;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

import java.lang.reflect.Field;
import java.util.List;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPServerConfig{
	public static final Extraction EXTRACTION;
	public static final Refining REFINING;
	public static final Generation GENERATION;
	public static final Miscellaneous MISCELLANEOUS;
	
	public static final ModConfigSpec ALL;
	
	static{
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		
		EXTRACTION = new Extraction(builder);
		REFINING = new Refining(builder);
		GENERATION = new Generation(builder);
		MISCELLANEOUS = new Miscellaneous(builder);
		
		ALL = builder.build();
	}
	
	private static IConfigSpec.ILoadedConfig rawConfig;
	public static CommentedConfig getRawConfig(){
		if(rawConfig == null){
			try{
				Field childConfig = ModConfigSpec.class.getDeclaredField("loadedConfig");
				childConfig.setAccessible(true);
				rawConfig = (IConfigSpec.ILoadedConfig) childConfig.get(ALL);
				Preconditions.checkNotNull(rawConfig);
			}catch(Exception x){
				throw new RuntimeException(x);
			}
		}
		return rawConfig.config();
	}
	
	public static class Extraction{
		public final ConfigValue<Integer> pumpjack_consumption;
		public final ConfigValue<Integer> pumpjack_speed;
		public final ConfigValue<Integer> derrick_consumption;
		Extraction(ModConfigSpec.Builder builder){
			builder.push("Extraction");
			
			pumpjack_consumption = builder
				.comment("The Flux the Pumpjack requires each tick to pump", "Default: 1024")
				.define("pumpjack_consumption", 1024);
			
			pumpjack_speed = builder
				.comment("The amount of mB of oil a Pumpjack extracts per tick", "Default: 15")
				.define("pumpjack_speed", 15);
			
			derrick_consumption = builder
				.comment("The Flux the Derrick requires each tick to operate", "Default: 512")
				.define("derrick_consumption", 512);
			
			builder.pop();
		}
	}
	
	public static class Refining{
		public final ConfigValue<Double> distillationTower_energyModifier;
		public final ConfigValue<Double> distillationTower_timeModifier;
		public final ConfigValue<Double> cokerUnit_energyModifier;
		public final ConfigValue<Double> cokerUnit_timeModifier;
		public final ConfigValue<Double> hydrotreater_energyModifier;
		public final ConfigValue<Double> hydrotreater_timeModifier;
		Refining(ModConfigSpec.Builder builder){
			builder.push("Refining");
			
			distillationTower_energyModifier = builder
				.comment("A modifier to apply to the energy costs of every Distillation Tower recipe", "Default: 1.0")
				.define("distillationTower_energyModifier", 1.0);
			
			distillationTower_timeModifier = builder
				.comment("A modifier to apply to the time of every Distillation recipe. Can't be lower than 1", "Default: 1.0")
				.define("distillationTower_timeModifier", 1.0);
			
			cokerUnit_energyModifier = builder
				.comment("A modifier to apply to the energy costs of every Coker Tower recipe", "Default: 1.0")
				.define("cokerUnit_energyModifier", 1.0);
			
			cokerUnit_timeModifier = builder
				.comment("A modifier to apply to the time of every Coker recipe. Can't be lower than 1", "Default: 1.0")
				.define("cokerUnit_timeModifier", 1.0);
			
			hydrotreater_energyModifier = builder
				.comment("A modifier to apply to the energy costs of every High-Pressure Refinery Unit recipe", "Default: 1.0")
				.define("hydrotreater_energyModifier", 1.0);
			
			hydrotreater_timeModifier = builder
				.comment("A modifier to apply to the time of every High-Pressure Refinery Unit recipe. Can't be lower than 1", "Default: 1.0")
				.define("hydrotreater_timeModifier", 1.0);
			
			builder.pop();
		}
	}
	
	public static class Generation{
		public final ConfigValue<List<? extends String>> fuels;
		Generation(ModConfigSpec.Builder builder){
			builder.push("Generation");
			
			fuels = builder
				.comment("List of Portable Generator fuels. Format: fluid_name, mb_used_per_second, flux_produced_per_tick")
				.defineList("generator_fuels",
				List.of("immersivepetroleum:naphtha, 9, 256",
							"immersivepetroleum:gasoline, 6, 256",
							"immersivepetroleum:benzol, 6, 256"), o -> true);
			
			builder.pop();
		}
	}
	
	public static class Miscellaneous{
		public final ConfigValue<List<? extends String>> boat_fuels;
		public final BooleanValue autounlock_recipes;
		public final BooleanValue asphalt_speed;
		Miscellaneous(ModConfigSpec.Builder builder){
			builder.push("Miscellaneous");
			
			boat_fuels = builder
					.comment("List of Motorboat fuels. Format: fluid_name, mb_used_per_tick")
					.defineList("boat_fuels",
							List.of("immersivepetroleum:gasoline, 1",
							"immersivepetroleum:naphtha, 2",
									"immersivepetroleum:benzol, 2"), o -> true);
			
			autounlock_recipes = builder
					.comment("Automatically unlock IP recipes for new players", "Default: true")
					.define("autounlock_recipes", true);
			
			asphalt_speed = builder
					.comment("Set to false to disable the asphalt block boosting player speed", "Default: true")
					.define("asphalt_speed", true);
			
			builder.pop();
		}
	}
	
	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Loading ev){
		FuelHandler.onConfigReload(ev);
	}
	
	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading ev){
		FuelHandler.onConfigReload(ev);
	}
}
