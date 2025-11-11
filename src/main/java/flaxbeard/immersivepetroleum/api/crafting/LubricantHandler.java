package flaxbeard.immersivepetroleum.api.crafting;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LubricantHandler{
	static final Set<Pair<TagKey<Fluid>, Integer>> lubricants = new HashSet<>();
	
	/**
	 * Registers a lubricant to be used in the Lubricant Can and Automatic Lubricator
	 *
	 * @param fluid  The {@link TagKey}<{@link Fluid}> to be used as lubricant
	 * @param amount mB of lubricant to spend every 4 ticks
	 */
	public static void register(@Nonnull TagKey<Fluid> fluid, int amount){
		if(lubricants.stream().noneMatch(pair -> pair.getLeft() == fluid)){
			lubricants.add(Pair.of(fluid, amount));
		}
	}
	
	/**
	 * Convenience method.
	 *
	 * @param toCheck Fluid to check
	 * @return mB of this Fluid used to lubricate
	 * @see #getLubeAmount(Fluid)
	 */
	public static int getLubeAmount(@Nonnull FluidStack toCheck){
		return getLubeAmount(toCheck.getFluid());
	}
	
	/**
	 * Gets amount of this Fluid that is used every four ticks for the Automatic Lubricator. 0 if not valid lube. 100 * this result is used for the
	 * Lubricant Can
	 *
	 * @param toCheck Fluid to check
	 * @return mB of this Fluid used to lubricate
	 */
	@SuppressWarnings("deprecation")
	public static int getLubeAmount(@Nonnull Fluid toCheck){
		for(Map.Entry<TagKey<Fluid>, Integer> entry: lubricants){
			if(toCheck.is(entry.getKey())){
				return entry.getValue();
			}
		}
		
		return 0;
	}
	
	/**
	 * Convenience method.
	 *
	 * @param toCheck Fluid to check
	 * @return Whether the Fluid is a lubricant
	 * @see #isValidLube(Fluid)
	 */
	public static boolean isValidLube(@Nonnull FluidStack toCheck){
		return isValidLube(toCheck.getFluid());
	}
	
	/**
	 * Whether the given Fluid is a valid lubricant
	 *
	 * @param toCheck Fluid to check
	 * @return Whether the Fluid is a lubricant
	 */
	@SuppressWarnings("deprecation")
	public static boolean isValidLube(@Nonnull Fluid toCheck){
		return lubricants.stream().anyMatch(pair -> toCheck.is(pair.getKey()));
	}
}
