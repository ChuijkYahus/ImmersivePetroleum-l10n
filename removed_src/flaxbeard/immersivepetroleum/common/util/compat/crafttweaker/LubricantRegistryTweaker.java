package flaxbeard.immersivepetroleum.common.util.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import net.minecraft.world.level.material.Fluid;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.immersivepetroleum.Lubricant")
public class LubricantRegistryTweaker{
	
	@Method
	public static void register(Many<KnownTag<Fluid>> tag){
		if(tag == null){
			//CraftTweakerAPI.logError("§cLubricantRegistry: Expected fluidtag as input fluid!§r");
			return;
		}
		
		if(tag.getAmount() < 1){
			//CraftTweakerAPI.logError("§cLubricantRegistry: Amount must atleast be 1mB!§r");
			return;
		}
		
		LubricantHandler.register(tag.getData().getTagKey(), tag.getAmount());
	}
}
