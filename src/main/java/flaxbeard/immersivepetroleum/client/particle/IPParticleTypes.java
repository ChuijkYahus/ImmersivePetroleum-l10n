package flaxbeard.immersivepetroleum.client.particle;

import com.mojang.serialization.MapCodec;
import flaxbeard.immersivepetroleum.common.IPRegisters;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;

public class IPParticleTypes{
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLARE_FIRE = createBasicParticle("flare_fire", false);
	public static final DeferredHolder<ParticleType<?>, ParticleType<FluidParticleData>> FLUID_SPILL = createParticleWithData("fluid_spill", FluidParticleData.CODEC);
	
	public static void forceClassLoad(){
	}
	
	private static DeferredHolder<ParticleType<?>, SimpleParticleType> createBasicParticle(String name, boolean alwaysShow){
		return IPRegisters.registerParticleType(name, () -> new SimpleParticleType(alwaysShow));
	}
	
	private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> createParticleWithData(String name, MapCodec<T> codec){
		ParticleType<T> type = new ParticleType<>(false){
			
			@Nonnull
			@Override
			public MapCodec<T> codec(){
				return codec;
			}
			
			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec(){
				return null;
			}
		};
		
		return IPRegisters.registerParticleType(name, () -> type);
	}
}
