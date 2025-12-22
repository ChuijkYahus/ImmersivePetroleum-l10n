package flaxbeard.immersivepetroleum.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import flaxbeard.immersivepetroleum.common.util.survey.ReservoirInfo;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;

public class IPDataComponents{
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<DebugItem.Mode>> DEBUG_ITEM = IPRegisters.registerDataComponent("debug_item_modes", DebugItem.Mode.CODEC, DebugItem.Mode.CODEC_STREAM);
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Settings.SettingsRecord>> PROJECTOR_SETTINGS = IPRegisters.registerDataComponent("projector_settings", Settings.SettingsRecord.CODEC, Settings.SettingsRecord.CODEC_STREAM);
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SurveyScan>> SURVEY_SCAN = IPRegisters.registerDataComponent("survey_scan", SurveyScan.CODEC, SurveyScan.CODEC_STREAM);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReservoirInfo>> RESERVOIR_INFO = IPRegisters.registerDataComponent("reservoir_info", ReservoirInfo.CODEC, ReservoirInfo.CODEC_STREAM);
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<TankData>> TANK_DATA = IPRegisters.registerDataComponent("tank_data", TankData.CODEC, TankData.CODEC_STREAM);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PowerData>> POWER_DATA = IPRegisters.registerDataComponent("power_data", PowerData.CODEC, PowerData.CODEC_STREAM);
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> CONTAINER_ITEM = IPRegisters.registerDataComponentF("ip_container", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_ITEM = IPRegisters.registerDataComponentF("ip_fluid_item", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
	
	public record TankData(@Nonnull FluidStack fs){
		//@formatter:off
		public static final Codec<TankData> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(FluidStack.CODEC.fieldOf("tank").forGetter(TankData::fs)
		).apply(inst, TankData::new));
		//@formatter:on
		
		public static final StreamCodec<ByteBuf, TankData> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(TankData::new, TankData::toTag);
		
		public TankData(IFluidTank tank){
			this(tank.getFluid());
		}
		
		private TankData(CompoundTag tag){
			this(fromTag(tag));
		}
		
		private static FluidStack fromTag(CompoundTag tag){
			Fluid fluid = RegistryUtils.getFluidFromRegistryName(ResourceLocation.parse(tag.getString("fluid")));
			if(fluid == null)
				return FluidStack.EMPTY;
			
			return new FluidStack(fluid, tag.getInt("amount"));
		}
		
		private CompoundTag toTag(){
			CompoundTag tag = new CompoundTag();
			tag.putString("fluid", RegistryUtils.getRegistryNameOf(this.fs.getFluid()).toString());
			tag.putInt("amount", this.fs.getAmount());
			return tag;
		}
	}
	
	public record PowerData(int energy){
		//@formatter:off
		public static final Codec<PowerData> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(Codec.INT.fieldOf("energy").forGetter(PowerData::energy)
		).apply(inst, PowerData::new));
		//@formatter:on
		
		public static final StreamCodec<ByteBuf, PowerData> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(PowerData::new, PowerData::toTag);
		
		public PowerData(IEnergyStorage storage){
			this(storage.getEnergyStored());
		}
		
		private PowerData(CompoundTag tag){
			this(tag.getInt("energy"));
		}
		
		private CompoundTag toTag(){
			CompoundTag tag = new CompoundTag();
			tag.putInt("energy", this.energy);
			return tag;
		}
	}
	
	public static void forceClassLoad(){
	}
}
