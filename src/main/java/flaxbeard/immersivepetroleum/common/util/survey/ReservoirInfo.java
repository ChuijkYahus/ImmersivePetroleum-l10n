package flaxbeard.immersivepetroleum.common.util.survey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.common.IPDataComponents;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public record ReservoirInfo(int x, int z, byte percentage, long amount, FluidStack fluidStack, int expected) implements ISurveyInfo{
	
	//@formatter:off
	public static final Codec<ReservoirInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.INT.fieldOf("x").forGetter(s -> s.x),
		Codec.INT.fieldOf("z").forGetter(s -> s.z),
		Codec.BYTE.fieldOf("percentage").forGetter(s -> s.percentage),
		Codec.LONG.fieldOf("amount").forGetter(s -> s.amount),
		FluidStack.CODEC.fieldOf("fluid").forGetter(s -> s.fluidStack),
		Codec.INT.fieldOf("expected").forGetter(s -> s.expected)
	).apply(inst, ReservoirInfo::new));
	//@formatter:on
	
	public static final StreamCodec<ByteBuf, ReservoirInfo> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(ReservoirInfo::fromNBT, ISurveyInfo::writeToTag);
	
	private static ReservoirInfo fromNBT(CompoundTag nbt){
		int x = nbt.getInt("x");
		int z = nbt.getInt("z");
		byte status = nbt.getByte("percentage");
		long amount = nbt.getLong("amount");
		int expected = nbt.getInt("expected");
		
		FluidStack fluidStack = FluidStack.EMPTY;
		if(nbt.contains("fluid")){
			try{
				ResourceLocation fluidRL = ResourceLocation.parse(nbt.getString("fluid"));
				
				Fluid fluid = RegistryUtils.getFluidFromRegistryName(fluidRL);
				if(fluid != null){
					fluidStack = new FluidStack(fluid, 1);
				}
			}catch(ResourceLocationException e){
				// Technically don't care, but made it log this just in case.
				ImmersivePetroleum.log.debug("ReservoirInfo invalid ResourceLocation. Ignoring.");
			}
		}
		
		return new ReservoirInfo(x, z, status, amount, fluidStack, expected);
	}
	
	public static ReservoirInfo create(Level world, BlockPos pos, Reservoir reservoir){
		int x = pos.getX();
		int z = pos.getZ();
		
		byte status = (byte) (reservoir.getAmount() / (float) reservoir.getCapacity() * 100);
		long amount = reservoir.getAmount();
		FluidStack fluidStack = new FluidStack(reservoir.getFluid(), 1);
		int expected = Reservoir.getFlow(reservoir.getPressure(world, pos.getX(), pos.getZ()));
		
		return new ReservoirInfo(x, z, status, amount, fluidStack, expected);
	}
	
	@Override
	public int getX(){
		return this.x;
	}
	
	@Override
	public int getZ(){
		return this.z;
	}
	
	public Fluid getFluid(){
		return this.fluidStack.getFluid();
	}
	
	@Override
	public void writeToStack(ItemStack stack){
		stack.set(IPDataComponents.RESERVOIR_INFO, this);
	}
	
	@Override
	public CompoundTag writeToTag(){
		CompoundTag tag = new CompoundTag();
		
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		tag.putByte("percentage", this.percentage);
		tag.putLong("amount", this.amount);
		tag.putInt("expected", this.expected);
		
		if(!this.fluidStack.isEmpty()){
			tag.putString("fluid", RegistryUtils.getRegistryNameOf(this.fluidStack.getFluid()).toString());
		}
		
		return tag;
	}
}
