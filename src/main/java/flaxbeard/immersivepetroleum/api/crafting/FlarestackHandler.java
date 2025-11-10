package flaxbeard.immersivepetroleum.api.crafting;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple handler for the Flarestack
 *
 * @author TwistedGate
 */
public class FlarestackHandler{
	static final Set<TagKey<Fluid>> burnables = new HashSet<>();
	
	/**
	 * Registers a fluidtag as being burnable in the Flarestack
	 *
	 * @param fluidTag that should be burned
	 */
	public static void register(@Nonnull TagKey<Fluid> fluidTag){
		burnables.add(fluidTag);
	}
	
	/**
	 * Tests whether the given fluid is burnable by the Flarestack or not
	 *
	 * @param fluid to test
	 * @return true if the given fluid is infact burnable, false otherwise
	 */
	public static boolean isBurnable(@Nonnull Fluid fluid){
		return burnables.stream().anyMatch(tag -> FlarestackHandler.match(tag, fluid));
	}
	
	@SuppressWarnings("deprecation")
	private static boolean match(TagKey<Fluid> tag, Fluid fluid){
		return fluid.is(tag);
	}
	
	/**
	 * Tests whether the given fluidstack is burnable by the flarestack or not
	 *
	 * @param fluidstack to test
	 * @return true if the given fluid is infact burnable, false otherwise
	 */
	public static boolean isBurnable(@Nonnull FluidStack fluidstack){
		return !fluidstack.isEmpty() && isBurnable(fluidstack.getFluid());
	}
	
	public static Set<TagKey<Fluid>> getSet(){
		return Collections.unmodifiableSet(burnables);
	}
}
