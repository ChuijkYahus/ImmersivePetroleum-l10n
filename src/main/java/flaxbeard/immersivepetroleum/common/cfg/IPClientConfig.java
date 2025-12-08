package flaxbeard.immersivepetroleum.common.cfg;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPClientConfig{
	public static final Miscellaneous MISCELLANEOUS;
	public static final GridColors GRID_COLORS;
	public static final DerrickConsole DERRICK_CONSOLE;
	
	public static final ModConfigSpec ALL;
	
	static{
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		GRID_COLORS = new GridColors(builder);
		DERRICK_CONSOLE = new DerrickConsole(builder);
		MISCELLANEOUS = new Miscellaneous(builder);
		ALL = builder.build();
	}
	
	public static class GridColors{
		private static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/ClientConfig/GridColors");
		
		final VarCache<Integer> pipe_normal_color;
		final VarCache<Integer> pipe_perforated_color;
		final VarCache<Integer> pipe_perforated_fixed_color;
		GridColors(ModConfigSpec.Builder builder){
			builder.push("GridColors");
			
			ConfigValue<String> pipe_normal_color = builder
					.comment("Normal pipe color. (Hex RGB)", "Default: A5A5A5")
					.define("normal_pipe_color", "A5A5A5", o -> hexValidator(log, o, "normal_pipe_color"));
			this.pipe_normal_color = new VarCache<>(() -> Integer.parseInt(pipe_normal_color.get(), 16));
			
			ConfigValue<String> pipe_perforated_color = builder
					.comment("Perforated pipe color. (Hex RGB)", "Default: 54FF54")
					.define("perforated_pipe_color", "54FF54", o -> hexValidator(log, o, "perforated_pipe_color"));
			this.pipe_perforated_color = new VarCache<>(() -> Integer.parseInt(pipe_perforated_color.get(), 16));
			
			ConfigValue<String> pipe_perforated_fixed_color = builder
					.comment("Perforated pipe color. (Hex RGB)", "Default: FF515A")
					.define("fixed_perforated_pipe_color", "FF515A", o -> hexValidator(log, o, "fixed_perforated_pipe_color"));
			this.pipe_perforated_fixed_color = new VarCache<>(() -> Integer.parseInt(pipe_perforated_fixed_color.get(), 16));
			
			builder.pop();
		}
		
		public int getPipeColorNormal(){
			return this.pipe_normal_color.get();
		}
		
		public int getPipeColorPerforated(){
			return this.pipe_perforated_color.get();
		}
		
		public int getPipeColorPerforatedFixed(){
			return this.pipe_perforated_fixed_color.get();
		}
	}
	
	public static class DerrickConsole{
		private static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/ClientConfig/DerrickConsole");
		
		final VarCache<Boolean> use_old_style;
		final VarCache<Integer> text_color_error;
		final VarCache<Integer> text_color_normal;
		DerrickConsole(ModConfigSpec.Builder builder){
			builder.push("DerrickConsole");
			
			ModConfigSpec.BooleanValue use_old_style = builder
				.comment("Use the old text style.", "Default: false")
				.define("old_school", false);
			this.use_old_style = new VarCache<>(use_old_style::getAsBoolean);
			
			ConfigValue<String> text_color_normal = builder
				.comment("Normal Text Color. (Hex RGB)", "Default: FF9900")
				.define("text_color_normal", "FF9900", o -> hexValidator(log, o, "text_color_normal"));
			this.text_color_normal = new VarCache<>(() -> Integer.parseInt(text_color_normal.get(), 16));
			
			ConfigValue<String> text_color_error = builder
				.comment("Error Text Color. (Hex RGB)", "Default: EF0000")
				.define("text_color_error", "EF0000", o -> hexValidator(log, o, "text_color_normal"));
			this.text_color_error = new VarCache<>(() -> Integer.parseInt(text_color_error.get(), 16));
			
			builder.pop();
		}
		
		public boolean useOldSchool(){
			return use_old_style.get();
		}
		
		public int getTextColorNormal(){
			return text_color_normal.get();
		}
		
		public int getTextColorError(){
			return text_color_error.get();
		}
	}
	
	public static class Miscellaneous{
		private static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/ClientConfig/Miscellaneous");
		
		Miscellaneous(ModConfigSpec.Builder builder){
			builder.push("Miscellaneous");
			
			builder.pop();
		}
	}
	
	private static boolean hexValidator(Logger log, Object obj, String cfgPath){
		if(obj instanceof String str){
			if(str.length() > 6){
				String strNew = str.substring(str.length() - 6);
				log.warn("{}: \"{}\" was cut down to \"{}\".", cfgPath, str, strNew);
				str = strNew;
			}
			if(str.length() == 6){
				try{
					Integer.valueOf(str, 16);
					return true;
				}catch(NumberFormatException ignored){
				}
			}
			log.error("{}: \"{}\" is not a valid RGB Hex color.", cfgPath, str);
		}
		
		return false;
	}
	
	@SubscribeEvent
	public static void onConfigChange(ModConfigEvent.Reloading ev){
		VarCache.CACHES.forEach(VarCache::reset);
	}
	
	private static class VarCache<V>{
		private static final Set<VarCache<?>> CACHES = new HashSet<>();
		
		private final Supplier<V> supplier;
		private V value;
		
		private VarCache(Supplier<V> supplier){
			this.supplier = supplier;
			CACHES.add(this);
		}
		
		public void reset(){
			this.value = null;
		}
		
		public V get(){
			if(this.value == null)
				this.value = this.supplier.get();
			return this.value;
		}
	}
}
