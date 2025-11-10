package flaxbeard.immersivepetroleum.common.data;

import com.mojang.serialization.Lifecycle;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class IPWorldGen{
	private static final ResourceKey<ConfiguredFeature<?, ?>> RESERVOIR_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceUtils.ip("reservoir"));
	private static final ResourceKey<PlacedFeature> RESERVOIR_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, ResourceUtils.ip("reservoir"));
	private static final ResourceKey<BiomeModifier> RESERVOIR_MODIFIER = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceUtils.ip("reservoir"));
	
	public static List<DataProvider> makeProviders(PackOutput output, CompletableFuture<HolderLookup.Provider> vanillaRegistries){
		final RegistrySetBuilder builder = new RegistrySetBuilder();
		AtomicReference<Holder.Reference<ConfiguredFeature<?, ?>>> configuredFeature = new AtomicReference<>();
		AtomicReference<Holder.Reference<PlacedFeature>> placedFeature = new AtomicReference<>();
		builder.add(Registries.CONFIGURED_FEATURE, ctx -> configuredFeature.set(ctx.register(RESERVOIR_CONFIGURED, new ConfiguredFeature<>(IPContent.WorldGenFeatures.RESERVOIR_FEATURE.get(), new NoneFeatureConfiguration()))));
		builder.add(Registries.PLACED_FEATURE, ctx -> placedFeature.set(ctx.register(RESERVOIR_PLACED, new PlacedFeature(configuredFeature.get(), List.of()))));
		builder.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ctx -> {
			final HolderGetter<Biome> biomeReg = ctx.lookup(Registries.BIOME);
			final HolderSet<Biome> biomes = new AnyHolderSet<>(new DummyRegistryLookup<>(biomeReg, Registries.BIOME));
			final BiomeModifiers.AddFeaturesBiomeModifier modifier = new BiomeModifiers.AddFeaturesBiomeModifier(
				biomes, HolderSet.direct(placedFeature.get()), GenerationStep.Decoration.UNDERGROUND_ORES
			);
			ctx.register(RESERVOIR_MODIFIER, modifier);
		});
		
		return List.of(new DatapackBuiltinEntriesProvider(output, vanillaRegistries, builder, Set.of(ImmersivePetroleum.MODID)));
	}
	
	private record DummyRegistryLookup<T>(HolderGetter<T> getter, ResourceKey<? extends Registry<? extends T>> key) implements HolderLookup.RegistryLookup<T>{
		@Override
		public @NotNull Lifecycle registryLifecycle(){
			return Lifecycle.stable();
		}
		
		@Override
		public @NotNull Stream<Holder.Reference<T>> listElements(){
			return Stream.empty();
		}
		
		@Override
		public @NotNull Stream<HolderSet.Named<T>> listTags(){
			return Stream.empty();
		}
		
		@Override
		public @NotNull Optional<Holder.Reference<T>> get(@NotNull ResourceKey<T> pResourceKey){
			return Optional.empty();
		}
		
		@Override
		public @NotNull Optional<HolderSet.Named<T>> get(@NotNull TagKey<T> pTagKey){
			return Optional.empty();
		}
		
		@Override
		public boolean canSerializeIn(@NotNull HolderOwner<T> pOwner){
			return true;
		}
	}
}
