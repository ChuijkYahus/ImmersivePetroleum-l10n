package flaxbeard.immersivepetroleum.api.reservoir;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionData;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Reservoir implements IReservoir{
	/** Primary mB/t */
	public static final int MIN_MBPT = 15;
	
	/** Pressure related maximum mB/t */
	public static final int MAX_MBPT = 2500;
	
	/** "Unsigned 32-Bit" */
	public static final long MAX_AMOUNT = 0xFFFFFFFFL;
	
	private RegionData regionData;
	
	private RecipeHolder<ReservoirType> type;
	private final ReservoirPolygon polygon;
	private long amount;
	private long capacity;
	
	public Reservoir(@Nonnull ReservoirPolygon polygon, @Nonnull RecipeHolder<ReservoirType> type, long amount){
		this(polygon, type, amount, amount);
	}
	
	/** For use in {@link #readFromNBT(CompoundTag)} */
	Reservoir(@Nonnull ReservoirPolygon polygon, @Nonnull RecipeHolder<ReservoirType> type, long capacity, long amount){
		this.type = Objects.requireNonNull(type);
		this.polygon = Objects.requireNonNull(polygon);
		this.capacity = capacity;
		this.amount = amount;
	}
	
	public void setRegion(RegionData data){
		if(this.regionData == null)
			this.regionData = data;
	}
	
	/**
	 * @param amount   of fluid in this reservoir. (Range: 0 - {@link #MAX_AMOUNT}; Capacity Clamped})
	 * @param capacity of this reservoir. (Range: 0 - {@link #MAX_AMOUNT}; Clamped})
	 * @return {@link Reservoir} self
	 */
	public Reservoir setAmountAndCapacity(long amount, long capacity){
		setCapacity(capacity);
		setAmount(amount);
		return this;
	}
	
	/**
	 * Sets the reservoirs current fluid amount in millibuckets.
	 *
	 * @param amount of fluid in this reservoir. (Range: 0 - {@link #MAX_AMOUNT}; Capacity Clamped})
	 */
	public Reservoir setAmount(long amount){
		this.amount = clamp(amount, 0L, this.capacity);
		return this;
	}
	
	/**
	 * Sets the reservoirs current fluid capacity in millibuckets.
	 *
	 * @param capacity of this reservoir. (Range: 0 - {@link #MAX_AMOUNT}; Clamped})
	 */
	public Reservoir setCapacity(long capacity){
		this.capacity = clamp(capacity, 0L, MAX_AMOUNT);
		return this;
	}
	
	public static long clamp(long num, long min, long max){
		return Math.max(min, Math.min(max, num));
	}
	
	/**
	 * Sets the Reservoir Type
	 */
	public Reservoir setReservoirType(@Nonnull RecipeHolder<ReservoirType> type){
		this.type = Objects.requireNonNull(type);
		return this;
	}
	
	@Override
	public long getAmount(){
		return this.amount;
	}
	
	@Override
	public long getCapacity(){
		return this.capacity;
	}
	
	@Override
	public boolean isEmpty(){
		return this.amount <= 0L;
	}
	
	@Override
	public void setDirty(){
		if(this.regionData != null){
			this.regionData.setDirty();
		}
	}
	
	@Override
	@Nonnull
	public RecipeHolder<ReservoirType> getType(){
		return this.type;
	}
	
	@Override
	public Fluid getFluid(){
		return this.type.value().getFluid();
	}
	
	@Override
	public ReservoirBoundingBox getBoundingBox(){
		return this.polygon.getBoundingBox();
	}
	
	@Override
	public ReservoirPolygon getPolygon(){
		return this.polygon;
	}
	
	private long lastEquilibriumTick = -1L;
	
	/**
	 * Used by WellTileEntity to check to see if reservoir should regenerate residuals or not
	 *
	 * @param level needed to check game time
	 * @return boolean on whether reservoir is below hydrostatic equilibrium
	 */
	public boolean belowHydrostaticEquilibrium(@Nonnull Level level){
		return this.type.value().residual > 0 && this.amount <= this.type.value().equilibrium && this.lastEquilibriumTick != level.getGameTime();
	}
	
	/**
	 * Used by WellTileEntity to handle reservoir residual regeneration
	 *
	 * @param level needed to check game time
	 */
	public void equalizeHydrostaticPressure(@Nonnull Level level){
		if(this.amount <= this.type.value().equilibrium && this.lastEquilibriumTick != level.getGameTime()){
			this.lastEquilibriumTick = level.getGameTime();
			this.amount += this.type.value().residual;
		}
	}
	
	@Override
	public int extract(int amount, FluidAction fluidAction){
		if(isEmpty()){
			return 0;
		}
		
		int extracted = (int) Math.min(amount, this.amount);
		
		if(fluidAction == FluidAction.EXECUTE){
			this.amount -= extracted;
			setDirty();
		}
		
		return extracted;
	}
	
	@Override
	public int extractWithPressure(@Nonnull Level world, int x, int z){
		float pressure = getPressure(world, x, z);
		
		if(pressure > 0.0 && this.amount > 0){
			int flow = (int) Math.min(getFlow(pressure), this.amount);
			
			this.amount -= flow;
			setDirty();
			return flow;
		}
		
		return 0;
	}
	
	/**
	 * <i>Only call on server side!</i>
	 *
	 * @return the Flowrate in mB for the position.
	 */
	public int getFlowFromPressure(@Nonnull Level level, BlockPos pos){
		float pressure = getPressure(level, pos.getX(), pos.getZ());
		return getFlow(pressure);
	}
	
	/**
	 * @param pressure (Clamped: 0.0 - 1.0)
	 * @return the Flowrate in mB for the given Pressure.
	 */
	public static int getFlow(float pressure){
		return MIN_MBPT + (int) Math.floor((MAX_MBPT - MIN_MBPT) * Mth.clamp(pressure, 0.0F, 1.0F));
	}
	
	/**
	 * <i>Only call on server side!</i>
	 *
	 * @param level {@link Level} to query in
	 * @param x     x-coordinate to query
	 * @param z     z-coordinate to query
	 * @return Pressure float
	 */
	public float getPressure(@Nonnull Level level, int x, int z){
		// prevents outside use
		double noise = ReservoirHandler.getValueOf(level, x, z);
		
		if(noise > 0.0D){
			// Pressure should drop from 100% to 0%
			// While the reservoir is between 100% and 50% at max
			
			double half = this.capacity * 0.50;
			double alt = this.amount - half;
			if(alt > 0){
				double pre = alt / half;
				return (float) (pre * noise);
			}
		}
		
		return 0.0F;
	}
	
	@Override
	public CompoundTag writeToNBT(){
		CompoundTag nbt = new CompoundTag();
		
		nbt.putString("reservoir", this.type.id().toString());
		nbt.putInt("amount", (int) (getAmount() & MAX_AMOUNT));
		nbt.putInt("capacity", (int) (getCapacity() & MAX_AMOUNT));
		nbt.put("polygon", this.polygon.writeToNBT());
		
		return nbt;
	}
	
	public static Reservoir readFromNBT(CompoundTag nbt){
		try{
			RecipeHolder<ReservoirType> type = ReservoirType.map.get(ResourceLocation.parse(nbt.getString("reservoir")));
			
			if(type != null){
				long amount = ((long) nbt.getInt("amount")) & MAX_AMOUNT;
				long capacity = ((long) nbt.getInt("capacity")) & MAX_AMOUNT;
				
				ReservoirPolygon polygon = ReservoirPolygon.fromNBT(nbt.getCompound("polygon"));
				return new Reservoir(polygon, type, capacity, amount);
			}
			
		}catch(ResourceLocationException e){
			// Don't care, if it doesn't exist, log it and just move on
			ImmersivePetroleum.log.debug("Failure to load reservoir", e);
		}
		return null;
	}
}
