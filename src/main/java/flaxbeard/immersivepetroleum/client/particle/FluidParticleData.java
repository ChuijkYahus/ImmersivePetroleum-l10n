package flaxbeard.immersivepetroleum.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class FluidParticleData implements ParticleOptions{
	//@formatter:off
	public static final MapCodec<FluidParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Codec.STRING.fieldOf("fluid").forGetter(FluidParticleData::writeToString))
		.apply(instance, FluidParticleData::new)
	);
	//@formatter:on
	
	private final Fluid fluid;
	public FluidParticleData(String name){
		this(RegistryUtils.getFluidFromRegistryName(ResourceLocation.parse(name)));
	}
	
	public FluidParticleData(Fluid fluid){
		this.fluid = fluid;
	}
	
	@Override
	@Nonnull
	public ParticleType<FluidParticleData> getType(){
		return IPParticleTypes.FLUID_SPILL.get();
	}
	
	public void writeToNetwork(FriendlyByteBuf buffer){
		buffer.writeUtf(RegistryUtils.getRegistryNameOf(this.fluid).toString());
	}
	
	@Nonnull
	public String writeToString(){
		return RegistryUtils.getRegistryNameOf(this.fluid).toString();
	}
	
	@OnlyIn(Dist.CLIENT)
	public Fluid getFluid(){
		return this.fluid;
	}
}
