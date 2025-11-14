package flaxbeard.immersivepetroleum.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import flaxbeard.immersivepetroleum.common.util.survey.IslandInfo;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public class IPDataComponents{
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Settings>> PROJECTOR_SETTINGS = IPRegisters.registerDataComponent("projector_settings", Settings.CODEC, Settings.CODEC_STREAM);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SurveyScan>> SURVEY_SCAN = IPRegisters.registerDataComponent("survey_scan", SurveyScan.CODEC, SurveyScan.CODEC_STREAM);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<IslandInfo>> ISLAND_INFO = IPRegisters.registerDataComponent("island_info", IslandInfo.CODEC, IslandInfo.CODEC_STREAM);
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidStack>> BOAT_TANK = IPRegisters.registerDataComponentF("boat_tank", FluidStack.CODEC, FluidStack.STREAM_CODEC);
	
	public record Test(int test){
		public static final Codec<Test> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("int").forGetter(r -> r.test)
		).apply(inst, Test::new));
		
		public static final StreamCodec<ByteBuf, Test> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(Test::new, Test::toTag);
		
		/** Used in {@link AutoLubricatorTileEntity#readOnPlacement} for testing */
		public static final DeferredHolder<DataComponentType<?>, DataComponentType<Test>> DATA_TYPE = IPRegisters.registerDataComponent("test2", CODEC, CODEC_STREAM);
		
		public Test(CompoundTag tag){
			this(tag.getInt("int"));
		}
		
		private CompoundTag toTag(){
			CompoundTag tag = new CompoundTag();
			tag.putInt("int", this.test);
			return tag;
		}
	}
	
	public static void forceClassLoad(){}
}
