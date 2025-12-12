package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.MultiblockGui;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPMultiblockBase;
import flaxbeard.immersivepetroleum.common.util.IPEffects.IPEffect;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static flaxbeard.immersivepetroleum.ImmersivePetroleum.MODID;

public class IPRegisters{
	private static <T> DeferredRegister<T> make(Registry<T> registry){
		return DeferredRegister.create(registry, MODID);
	}
	
	private static <T> DeferredRegister<T> make(ResourceKey<Registry<T>> registry){
		return DeferredRegister.create(registry, MODID);
	}
	
	private static final DeferredRegister<Block> BLOCK_REGISTER = make(BuiltInRegistries.BLOCK);
	private static final DeferredRegister<Item> ITEM_REGISTER = make(BuiltInRegistries.ITEM);
	private static final DeferredRegister<Fluid> FLUID_REGISTER = make(BuiltInRegistries.FLUID);
	private static final DeferredRegister<BlockEntityType<?>> TE_REGISTER = make(BuiltInRegistries.BLOCK_ENTITY_TYPE);
	private static final DeferredRegister<MenuType<?>> MENU_REGISTER = make(BuiltInRegistries.MENU);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = make(BuiltInRegistries.RECIPE_SERIALIZER);
	private static final DeferredRegister<MobEffect> MOB_EFFECT = make(BuiltInRegistries.MOB_EFFECT);
	private static final DeferredRegister<SoundEvent> SOUND_EVENT = make(BuiltInRegistries.SOUND_EVENT);
	private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = make(BuiltInRegistries.PARTICLE_TYPE);
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPE = make(BuiltInRegistries.ENTITY_TYPE);
	private static final DeferredRegister<FluidType> FLUID_TYPE = make(NeoForgeRegistries.FLUID_TYPES);
	private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = make(Registries.CREATIVE_MODE_TAB);
	private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = make(BuiltInRegistries.FEATURE);
	private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_REGISTER = make(BuiltInRegistries.DATA_COMPONENT_TYPE);
	
	private static final List<Consumer<IEventBus>> MOD_BUS_CALLBACKS = new ArrayList<>();
	
	public static void addRegistersToEventBus(IEventBus eventBus){
		FLUID_REGISTER.register(eventBus);
		BLOCK_REGISTER.register(eventBus);
		ITEM_REGISTER.register(eventBus);
		TE_REGISTER.register(eventBus);
		MENU_REGISTER.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		MOB_EFFECT.register(eventBus);
		SOUND_EVENT.register(eventBus);
		PARTICLE_TYPE.register(eventBus);
		ENTITY_TYPE.register(eventBus);
		FLUID_TYPE.register(eventBus);
		CREATIVE_TABS.register(eventBus);
		FEATURE_REGISTER.register(eventBus);
		DATA_COMPONENT_REGISTER.register(eventBus);
	}
	
	public static void runCallbacks(IEventBus eventBus){
		MOD_BUS_CALLBACKS.forEach(e -> e.accept(eventBus));
	}
	
	public static Iterable<Block> getAllBlocks(){
		return BLOCK_REGISTER.getEntries().stream().map(DeferredHolder::get).filter(block -> !block.getLootTable().equals(BuiltInLootTables.EMPTY)).collect(Collectors.toList());
	}
	
	public static Iterable<Item> getAllItems(){
		return ITEM_REGISTER.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
	}
	
	public static <S extends IMultiblockState> MultiblockRegistration<S> registerMetalMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure){
		return registerMetalMultiblock(name, logic, structure, null);
	}
	
	public static <S extends IMultiblockState> MultiblockRegistration<S> registerMetalMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure, @Nullable Consumer<MultiblockBuilder<S>> extras){
		// @formatter:off
		BlockBehaviour.Properties prop = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
			.strength(3, 15)
			.forceSolidOn()
			.requiresCorrectToolForDrops()
			.isViewBlocking((state, blockReader, pos) -> false)
			.noOcclusion()
			.dynamicShape()
			.pushReaction(PushReaction.BLOCK);
		// @formatter:on
		
		return registerMultiblock(name, logic, structure, extras, prop);
	}
	
	public static <S extends IMultiblockState> MultiblockRegistration<S> registerMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure, @Nullable Consumer<MultiblockBuilder<S>> extras, BlockBehaviour.Properties prop){
		//@formatter:off
		MultiblockBuilder<S> builder = new MultiblockBuilder<>(logic, name)
			.structure(structure)
			.defaultBEs(TE_REGISTER)
			.customBlock(BLOCK_REGISTER, ITEM_REGISTER, mb -> new IPMultiblockBase<>(prop, mb), MultiblockItem::new);
		//@formatter:on
		
		if(extras != null){
			extras.accept(builder);
		}
		
		return builder.build(MOD_BUS_CALLBACKS::add);
	}
	
	protected static class MultiblockBuilder<S extends IMultiblockState> extends MultiblockRegistrationBuilder<S, MultiblockBuilder<S>>{
		public MultiblockBuilder(IMultiblockLogic<S> logic, String name){
			super(logic, ResourceUtils.ip(name));
		}
		
		public MultiblockBuilder<S> redstone(IMultiblockComponent.StateWrapper<S, RedstoneControl.RSState> getState, BlockPos... positions){
			redstoneAware();
			return selfWrappingComponent(new RedstoneControl<>(getState, positions));
		}
		
		public MultiblockBuilder<S> comparator(ComparatorManager<S> comparator){
			withComparator();
			return super.selfWrappingComponent(comparator);
		}
		
		public MultiblockBuilder<S> gui(IEMenuTypes.MultiblockContainer<S, ?> menu){
			return component(new MultiblockGui<>(menu));
		}
		
		@Override
		protected MultiblockBuilder<S> self(){
			return this;
		}
	}
	
	public static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> blockConstructor){
		return registerBlock(name, blockConstructor, null);
	}
	
	public static <T extends Block> DeferredHolder<Block, T> registerMultiblockBlock(String name, Supplier<T> blockConstructor){
		return registerBlock(name, blockConstructor, block -> new BlockItem(block, new Item.Properties()));
	}
	
	public static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> blockConstructor, @Nullable Function<T, ? extends BlockItem> blockItem){
		DeferredHolder<Block, T> block = BLOCK_REGISTER.register(name, blockConstructor);
		
		if(blockItem != null){
			registerItem(name, () -> blockItem.apply(block.get()));
		}
		return block;
	}
	
	public static <T extends IPBlockBase> DeferredHolder<Block, T> registerIPBlock(String name, Supplier<T> blockConstructor){
		DeferredHolder<Block, T> block = BLOCK_REGISTER.register(name, blockConstructor);
		
		registerItem(name, () -> block.get().blockItemSupplier().get());
		
		return block;
	}
	
	public static <T extends Item> DeferredHolder<Item, T> registerItem(String name, Supplier<T> itemConstructor){
		return ITEM_REGISTER.register(name, itemConstructor);
	}
	
	public static <T extends Fluid> DeferredHolder<Fluid, T> registerFluid(String name, Supplier<T> fluidConstructor){
		return FLUID_REGISTER.register(name, fluidConstructor);
	}
	
	public static <T extends FluidType> DeferredHolder<FluidType, T> registerFluidType(String name, Supplier<T> supplier){
		return FLUID_TYPE.register(name, supplier);
	}
	
	public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerTE(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> valid){
		// TODO That "nulL" there may come back to bite me later
		return TE_REGISTER.register(name, () -> new BlockEntityType<>(factory, ImmutableSet.of(valid.get()), null));
	}
	
	public static <T extends BlockEntity & IEBlockInterfaces.IGeneralMultiblock> MultiblockBEType<T> registerMultiblockTE(String name, MultiblockBEType.BEWithTypeConstructor<T> factory, Supplier<? extends Block> valid){
		return new MultiblockBEType<>(name, TE_REGISTER, factory, valid, state -> state.hasProperty(IEProperties.MULTIBLOCKSLAVE) && !state.getValue(IEProperties.MULTIBLOCKSLAVE));
	}
	
	public static <T extends RecipeSerializer<?>> DeferredHolder<RecipeSerializer<?>, T> registerSerializer(String name, Supplier<T> serializer){
		return RECIPE_SERIALIZERS.register(name, serializer);
	}
	
	public static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenu(String name, Supplier<MenuType<T>> factory){
		return MENU_REGISTER.register(name, factory);
	}
	
	public static <T extends IPEffect> DeferredHolder<MobEffect, T> registerMobEffect(String name, Supplier<T> constructor){
		return MOB_EFFECT.register(name, constructor);
	}
	
	public static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name){
		return SOUND_EVENT.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceUtils.ip(name)));
	}
	
	public static <T extends ParticleType<?>> DeferredHolder<ParticleType<?>, T> registerParticleType(String name, Supplier<T> particleType){
		return PARTICLE_TYPE.register(name, particleType);
	}
	
	public static <T extends EntityType<?>> DeferredHolder<EntityType<?>, T> registerEntityType(String name, Function<ResourceLocation, T> entityType){
		return ENTITY_TYPE.register(name, () -> entityType.apply(ResourceUtils.ip(name)));
	}
	
	public static <T extends CreativeModeTab> DeferredHolder<CreativeModeTab, T> registerCreativeTab(String name, Supplier<T> tab){
		return CREATIVE_TABS.register(name, tab);
	}
	
	public static <T extends Feature<?>> DeferredHolder<Feature<?>, T> registerFeature(String name, Supplier<T> supplier){
		return FEATURE_REGISTER.register(name, supplier);
	}
	
	/** Using {@link ByteBuf} */
	public static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerDataComponent(String name, Codec<T> codec, StreamCodec<ByteBuf, T> streamCodec){
		return DATA_COMPONENT_REGISTER.register(name, () -> DataComponentType.<T> builder().persistent(codec).networkSynchronized(streamCodec).build());
	}
	
	/** Using {@link RegistryFriendlyByteBuf} */
	public static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerDataComponentF(String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec){
		return DATA_COMPONENT_REGISTER.register(name, () -> DataComponentType.<T> builder().persistent(codec).networkSynchronized(streamCodec).build());
	}
	
	private IPRegisters(){
	}
}
