package flaxbeard.immersivepetroleum.common.util.survey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
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

import javax.annotation.Nonnull;

public class IslandInfo implements ISurveyInfo{
	public static final String TAG_KEY = "islandscan";
	
	public static final Codec<IslandInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.INT.fieldOf("x").forGetter(s -> s.x),
		Codec.INT.fieldOf("z").forGetter(s -> s.z),
		Codec.BYTE.fieldOf("status").forGetter(s -> s.status),
		Codec.LONG.fieldOf("amount").forGetter(s -> s.amount),
		FluidStack.CODEC.fieldOf("fluidstack").forGetter(s -> s.fluidStack),
		Codec.INT.fieldOf("expected").forGetter(s -> s.expected)
	).apply(inst, IslandInfo::new));
	
	public static final StreamCodec<ByteBuf,IslandInfo> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(IslandInfo::new, ISurveyInfo::writeToTag);
	
	private final int x, z;
	private final byte status;
	private final long amount;
	private FluidStack fluidStack = FluidStack.EMPTY;
	private final int expected;
	
	public IslandInfo(Level world, BlockPos pos, ReservoirIsland island){
		this.x = pos.getX();
		this.z = pos.getZ();
		
		this.status = (byte) (island.getAmount() / (float) island.getCapacity() * 100);
		this.amount = island.getAmount();
		this.fluidStack = new FluidStack(island.getFluid(), 1);
		this.expected = ReservoirIsland.getFlow(island.getPressure(world, pos.getX(), pos.getZ()));
	}
	
	private IslandInfo(CompoundTag tag){
		this.x = tag.getInt("x");
		this.z = tag.getInt("z");
		this.status = tag.getByte("status");
		this.amount = tag.getLong("amount");
		this.expected = tag.getInt("expected");
		
		if(tag.contains("fluid")){
			try{
				ResourceLocation fluidRL = ResourceLocation.parse(tag.getString("fluid"));
				
				Fluid fluid = RegistryUtils.getFluidFromRegistryName(fluidRL);
				if(fluid != null){
					this.fluidStack = new FluidStack(fluid, 1);
				}
			}catch(ResourceLocationException e){
				// Technically don't care, but made it log this just in case.
				ImmersivePetroleum.log.debug("IslandInfo invalid ResourceLocation. Ignoring.");
			}
		}
	}
	
	private IslandInfo(int x, int z, byte status, long amount, FluidStack fs, int expected){
		this.x = x;
		this.z = z;
		this.status = status;
		this.amount = amount;
		this.fluidStack = fs;
		this.expected = expected;
	}
	
	@Override
	public int getX(){
		return this.x;
	}
	
	@Override
	public int getZ(){
		return this.z;
	}
	
	public byte getStatus(){
		return this.status;
	}
	
	public int getExpected(){
		return this.expected;
	}
	
	public long getAmount(){
		return this.amount;
	}
	
	@Nonnull
	public FluidStack getFluidStack(){
		return this.fluidStack;
	}
	
	public Fluid getFluid(){
		return this.fluidStack.getFluid();
	}
	
	@Override
	public void writeToStack(ItemStack stack){
		stack.set(IPDataComponents.ISLAND_INFO, this);
	}
	
	@Override
	public CompoundTag writeToTag(){
		CompoundTag tag = new CompoundTag();
		
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		tag.putByte("status", this.status);
		tag.putLong("amount", this.amount);
		tag.putInt("expected", this.expected);
		
		if(!this.fluidStack.isEmpty()){
			tag.putString("fluid", RegistryUtils.getRegistryNameOf(this.fluidStack.getFluid()).toString());
		}
		
		return tag;
	}
}
