package flaxbeard.immersivepetroleum.common.data.recipes.builders;

import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.data.recipes.IPGenericBuilder;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWList;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListBiome;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author TwistedGate
 */
public class ReservoirBuilder extends IPGenericBuilder<ReservoirType>{
	
	/**
	 * Creates a new ReservoirType builder instance. This is a shorthand.
	 * <p><code>min</code>, <code>max</code> and <code>trace</code> are Following the Format below.
	 * <pre><code>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </code></pre>
	 * </p>
	 *
	 * @param name   The name of the reservoir
	 * @param fluid  The type of fluid it holds
	 * @param min    The minimum amount of fluid the reservoir can hold
	 * @param max    The capacity of the reservoir
	 * @param trace  Trace amount of the fluid after being depleted
	 * @param weight chance for this reservoir to spawn
	 * @return The completed {@link ReservoirBuilder} once all parameters are added
	 */
	public static ReservoirBuilder builder(String name, Fluid fluid, double min, double max, double trace, int weight){
		return new ReservoirBuilder(name, fluid, min, max, trace, weight);
	}
	
	private final String name;
	private final Fluid fluid;
	private final int minSize;
	private final int maxSize;
	private final int residual;
	private int equilibrium;
	private final int weight;
	
	private BWListBiome bioList;
	private BWListDimension dimList;
	
	private ReservoirBuilder(String name, Fluid fluid, double min, double max, double trace, int weight){
		this.name = name;
		this.fluid = fluid;
		this.minSize = (int) Math.floor(min * 1000D);
		this.maxSize = (int) Math.floor(max * 1000D);
		this.residual = (int) Math.floor(trace * 1000D);
		this.weight = weight;
	}
	
	@Override
	protected ReservoirType makeInstance(){
		ReservoirType type = new ReservoirType(this.name, this.fluid, this.minSize, this.maxSize, this.residual, this.equilibrium, this.weight);
		
		if(this.bioList != null)
			type.setBiomes(this.bioList);
		
		if(this.dimList != null)
			type.setDimensions(this.dimList);
		
		return type;
	}
	
	/**
	 * Sets maximum fluid <code>amount</code> for trace fluid to regenerate.
	 * <p>Following the Format below.
	 * <pre><code>
	 * 1.000 = 1 Bucket
	 * 0.001 = 1 Millibucket
	 * </code></pre>
	 * </p>
	 *
	 * @param amount The amount to set.
	 * @return {@link ReservoirBuilder}
	 */
	public ReservoirBuilder equilibrium(double amount){
		this.equilibrium = (int) Math.floor(amount * 1000D);
		return this;
	}
	
	/**
	 * <i>This may only be called once.</i><br>
	 * <br>
	 * Biome check for this Reservoir.
	 *
	 * @param biomes Biomes to blacklist/whitelist
	 * @return {@link flaxbeard.immersivepetroleum.common.data.recipes.builders.ReservoirBuilder}
	 * @throws IllegalStateException if already set
	 */
	public ReservoirBuilder setBiomes(BWList.Mode mode, @Nonnull ResourceLocation[] biomes){
		if(this.bioList != null){
			throw new IllegalStateException("Biomes list already set.");
		}
		Objects.requireNonNull(biomes);
		
		Set<BWListBiome.Validator> set = Arrays.stream(biomes).map(BWListBiome.Validator::new).collect(Collectors.toSet());
		this.bioList = new BWListBiome(set, mode);
		
		return this;
	}
	
	/**
	 * <i>This may only be called once.</i><br>
	 * <br>
	 * Dimension check for this Reservoir.
	 *
	 * @param dimensions Dimensions to blacklist/whitelist
	 * @return {@link flaxbeard.immersivepetroleum.common.data.recipes.builders.ReservoirBuilder}
	 * @throws IllegalStateException if already set
	 */
	public ReservoirBuilder setDimensions(BWList.Mode mode, @Nonnull ResourceLocation[] dimensions){
		if(this.dimList != null){
			throw new IllegalStateException("Dimensions list already set.");
		}
		Objects.requireNonNull(dimensions);
		
		Set<BWListDimension.Validator> set = Arrays.stream(dimensions).map(BWListDimension.Validator::new).collect(Collectors.toSet());
		this.dimList = new BWListDimension(set, mode);
		
		return this;
	}
}
