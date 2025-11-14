package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import net.minecraft.world.level.material.Fluid;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.immersivepetroleum.Flarestack")
public class FlarestackRegistryTweaker{
	
	/**
	 * Adds a fluid tag to the Flarestacks "burnable fluids" list
	 *
	 * @param tag The fluidtag to be added
	 * @docParam tag <tag:fluids:minecraft:water>
	 */
	@Method
	public static void register(KnownTag<Fluid> tag){
		if(tag == null){
			//CraftTweakerAPI.logError("§cFlarestackHandler: Expected fluidtag as input fluid!§r");
		}
		
		FlarestackHandler.register(tag.getTagKey());
	}
}
