package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CokingChamber{
	
	public enum State{
		/** Wait for Input */
		STANDBY,
		
		/** Process materials into the result */
		PROCESSING,
		
		/** Draining residual fluids from processing materials */
		DRAIN_RESIDUE,
		
		/** Filling up the chamber with fluid, with the amount required by the recipe */
		FLOODING,
		
		/** Dumping the result below the chamber output and voiding the flushing fluids */
		DUMPING;
		
		public int id(){
			return ordinal();
		}
	}
	
	@Nullable
	CokerUnitRecipe recipe = null;
	State state = State.STANDBY;
	FluidTank tank;
	
	/** Total capacity. inputAmount + outputAmount, should not go above this */
	int capacity;
	/** This has a ratio of X:1 to the input amount. (X amount of items always adds 1) */
	int inputAmount = 0;
	/** This has a ratio of 1:1 to the output amount. */
	int outputAmount = 0;
	
	int timer = 0;
	
	public CokingChamber(int itemCapacity, int fluidCapacity){
		this.capacity = itemCapacity;
		this.tank = new FluidTank(fluidCapacity);
	}
	
	public CokingChamber readFromNBT(CompoundTag nbt, HolderLookup.Provider provider){
		this.tank.readFromNBT(provider, nbt.getCompound("tank"));
		this.timer = nbt.getInt("timer");
		this.inputAmount = nbt.getInt("input");
		this.outputAmount = nbt.getInt("output");
		this.state = State.values()[nbt.getInt("state")];
		
		if(nbt.contains("recipe", Tag.TAG_STRING)){
			try{
				this.recipe = CokerUnitRecipe.recipes.get(ResourceLocation.parse(nbt.getString("recipe")));
			}catch(ResourceLocationException e){
				ImmersivePetroleum.log.error("Tried to load a coking recipe with an invalid name", e);
			}
		}else{
			this.recipe = null;
		}
		
		return this;
	}
	
	public CompoundTag writeToNBT(CompoundTag nbt, HolderLookup.Provider provider){
		nbt.put("tank", this.tank.writeToNBT(provider, new CompoundTag()));
		nbt.putInt("timer", this.timer);
		nbt.putInt("input", this.inputAmount);
		nbt.putInt("output", this.outputAmount);
		nbt.putInt("state", this.state.id());
		
		if(this.recipe != null){
			nbt.putString("recipe", this.recipe.getType().toString());
		}
		
		return nbt;
	}
	
	/** Returns true when the recipe has been set, false if it already is set and the chamber is working */
	public boolean setRecipe(@Nullable CokerUnitRecipe recipe){
		if(state == State.STANDBY){
			this.recipe = recipe;
			return true;
		}
		
		return false;
	}
	
	/** Always returns 0 if the recipe hasn't been set yet, otherwise it pretty much does what you'd expect it to */
	public int addStack(@Nonnull ItemStack stack, boolean simulate){
		if(this.recipe != null && !stack.isEmpty() && this.recipe.inputItem.test(stack)){
			int capacity = getCapacity() * this.recipe.inputItem.getCount();
			int current = getTotalAmount() * this.recipe.inputItem.getCount();
			
			if(simulate){
				return Math.min(capacity - current, stack.getCount());
			}
			
			int filled = capacity - current;
			if(stack.getCount() < filled){
				filled = stack.getCount();
			}
			this.inputAmount++;
			
			return filled;
		}
		
		return 0;
	}
	
	public State getState(){
		return this.state;
	}
	
	public int getCapacity(){
		return this.capacity;
	}
	
	public int getInputAmount(){
		return this.inputAmount;
	}
	
	public int getOutputAmount(){
		return this.outputAmount;
	}
	
	/** returns the combined I/O Amount */
	public int getTotalAmount(){
		return this.inputAmount + this.outputAmount;
	}
	
	public int getTimer(){
		return this.timer;
	}
	
	private boolean setStage(State state){
		if(this.state != state){
			this.state = state;
			return true;
		}
		return false;
	}
	
	@Nullable
	public CokerUnitRecipe getRecipe(){
		return this.recipe;
	}
	
	/** Expected input. */
	public ItemStack getInputItem(){
		if(this.recipe == null){
			return ItemStack.EMPTY;
		}
		return this.recipe.inputItem.getMatchingStacks()[0];
	}
	
	/** Expected output. */
	public ItemStack getOutputItem(){
		if(this.recipe == null){
			return ItemStack.EMPTY;
		}
		
		return this.recipe.outputItem.copy();
	}
	
	public FluidTank getTank(){
		return this.tank;
	}
	
	/** returns true when the coker should update, false otherwise */
	public boolean tick(IMultiblockContext<CokerUnitLogic.State> context, int chamberId){
		if(this.recipe == null){
			return setStage(State.STANDBY);
		}
		
		CokerUnitLogic.State logicState = context.getState();
		
		switch(this.state){
			case STANDBY -> {
				if(this.recipe != null){
					return setStage(State.PROCESSING);
				}
			}
			case PROCESSING -> {
				if(this.inputAmount > 0 && !getInputItem().isEmpty() && (this.tank.getCapacity() - this.tank.getFluidAmount()) >= this.recipe.outputFluid.getAmount()){
					if(logicState.energy.getEnergyStored() >= this.recipe.getTotalProcessEnergy() / this.recipe.getTotalProcessTime()){
						logicState.energy.extractEnergy(this.recipe.getTotalProcessEnergy() / this.recipe.getTotalProcessTime(), false);
						
						this.timer++;
						if(this.timer >= (this.recipe.getTotalProcessTime() * this.recipe.inputItem.getCount())){
							this.timer = 0;
							
							this.tank.fill(Utils.copyFluidStackWithAmount(this.recipe.outputFluid, this.recipe.outputFluid.getAmount(), false), IFluidHandler.FluidAction.EXECUTE);
							this.inputAmount--;
							this.outputAmount++;
							
							if(this.inputAmount <= 0){
								setStage(State.DRAIN_RESIDUE);
							}
						}
						
						return true;
					}
				}
			}
			case DRAIN_RESIDUE -> {
				if(this.tank.getFluidAmount() > 0){
					FluidTank buffer = logicState.bufferTanks.output();
					FluidStack drained = this.tank.drain(25, IFluidHandler.FluidAction.SIMULATE);
					
					int accepted = buffer.fill(drained, IFluidHandler.FluidAction.SIMULATE);
					if(accepted > 0){
						int amount = Math.min(drained.getAmount(), accepted);
						
						this.tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
						buffer.fill(Utils.copyFluidStackWithAmount(drained, amount, false), IFluidHandler.FluidAction.EXECUTE);
						
						return true;
					}
				}else{
					return setStage(State.FLOODING);
				}
			}
			case FLOODING -> {
				this.timer++;
				if(this.timer >= 2){
					this.timer = 0;
					
					int max = getTotalAmount() * this.recipe.inputFluid.getAmount();
					if(this.tank.getFluidAmount() < max){
						FluidStack accepted = logicState.bufferTanks.input().drain(this.recipe.inputFluid.getAmount(), IFluidHandler.FluidAction.SIMULATE);
						if(accepted.getAmount() >= this.recipe.inputFluid.getAmount()){
							logicState.bufferTanks.input().drain(this.recipe.inputFluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
							this.tank.fill(accepted, IFluidHandler.FluidAction.EXECUTE);
						}
					}else if(this.tank.getFluidAmount() >= max){
						return setStage(State.DUMPING);
					}
				}
			}
			case DUMPING -> {
				boolean update = false;
				
				this.timer++;
				if(this.timer >= 5){ // Output speed will always be fixed
					this.timer = 0;
					
					if(this.outputAmount > 0){
						IMultiblockLevel multiLevel = context.getLevel();
						Level world = multiLevel.getRawLevel();
						int amount = Math.min(this.outputAmount, 1);
						ItemStack copy = this.recipe.outputItem.copy();
						copy.setCount(amount);
						
						// Drop item(s) at the designated chamber output location
						BlockPos itemOutPos = multiLevel.toAbsolute(chamberId == 0 ? CokerUnitLogic.Chamber_A_OUT.posInMultiblock() : CokerUnitLogic.Chamber_B_OUT.posInMultiblock());
						Vec3 center = new Vec3(itemOutPos.getX() + 0.5, itemOutPos.getY() - 0.5, itemOutPos.getZ() + 0.5);
						ItemEntity ent = new ItemEntity(world, center.x, center.y, center.z, copy);
						ent.setDeltaMovement(0.0, 0.0, 0.0); // Any movement has the potential to end with the stack bouncing all over the place
						world.addFreshEntity(ent);
						this.outputAmount -= amount;
						
						update = true;
					}
				}
				
				// Void washing fluid
				if(this.tank.getFluidAmount() > 0){
					this.tank.drain(25, IFluidHandler.FluidAction.EXECUTE);
					
					update = true;
				}
				
				if(this.outputAmount <= 0 && this.tank.isEmpty()){
					this.recipe = null;
					setStage(State.STANDBY);
					
					update = true;
				}
				
				if(update){
					return true;
				}
			}
		}
		
		return false;
	}
}
