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

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPClientConfig{
	public static final Miscellaneous MISCELLANEOUS;
	public static final GridColors GRID_COLORS;
	
	public static final ModConfigSpec ALL;
	
	static{
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		GRID_COLORS = new GridColors(builder);
		MISCELLANEOUS = new Miscellaneous(builder);
		ALL = builder.build();
	}
	
	public static class GridColors{
		private static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/ClientConfig/GridColors");
		
		public final ConfigValue<String> pipe_normal_color;
		public final ConfigValue<String> pipe_perforated_color;
		public final ConfigValue<String> pipe_perforated_fixed_color;
		GridColors(ModConfigSpec.Builder builder){
			builder.push("GridColors");
			
			pipe_normal_color = builder
					.comment("Normal pipe color. (Hex RGB)")
					.define("normal_pipe_color", "A5A5A5", o -> hexValidator(o, "normal_pipe_color"));
			
			pipe_perforated_color = builder
					.comment("Perforated pipe color. (Hex RGB)")
					.define("perforated_pipe_color", "54FF54", o -> hexValidator(o, "perforated_pipe_color"));
			
			pipe_perforated_fixed_color = builder
					.comment("Perforated pipe color. (Hex RGB)")
					.define("fixed_perforated_pipe_color", "FF515A", o -> hexValidator(o, "fixed_perforated_pipe_color"));
			
			builder.pop();
		}
		
		private boolean hexValidator(Object obj, String cfgPath){
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
	}
	
	public static class Miscellaneous{
		Miscellaneous(ModConfigSpec.Builder builder){
			builder.push("Miscellaneous");
			
			builder.pop();
		}
	}
	
	@SubscribeEvent
	public static void onConfigChange(ModConfigEvent ev){
		
	}
}
