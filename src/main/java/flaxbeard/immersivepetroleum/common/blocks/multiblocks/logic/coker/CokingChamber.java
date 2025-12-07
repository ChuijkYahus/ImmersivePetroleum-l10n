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
import net.minecraft.world.item.crafting.RecipeHolder;
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
	RecipeHolder<CokerUnitRecipe> rHolder = null;
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
				ResourceLocation recipeName = ResourceLocation.parse(nbt.getString("recipe"));
				RecipeHolder<CokerUnitRecipe> recipe = CokerUnitRecipe.recipes.get(recipeName);
				if(recipe == null){
					ImmersivePetroleum.log.warn("Recipe {} is an unknown or removed recipe! Skipping...", recipeName);
				}
			}catch(ResourceLocationException e){
				ImmersivePetroleum.log.error("Tried to load a coking recipe with an invalid name", e);
			}
		}else{
			this.rHolder = null;
		}
		
		return this;
	}
	
	public CompoundTag writeToNBT(CompoundTag nbt, HolderLookup.Provider provider){
		nbt.put("tank", this.tank.writeToNBT(provider, new CompoundTag()));
		nbt.putInt("timer", this.timer);
		nbt.putInt("input", this.inputAmount);
		nbt.putInt("output", this.outputAmount);
		nbt.putInt("state", this.state.id());
		
		if(this.rHolder != null){
			nbt.putString("recipe", this.rHolder.id().toString());
		}
		
		return nbt;
	}
	
	/** Returns true when the recipe has been set, false if it already is set and the chamber is working */
	public boolean setRecipe(@Nullable RecipeHolder<CokerUnitRecipe> recipe){
		if(state == State.STANDBY){
			this.rHolder = recipe;
			return true;
		}
		
		return false;
	}
	
	/** Always returns 0 if the recipe hasn't been set yet, otherwise it pretty much does what you'd expect it to */
	public int addStack(@Nonnull ItemStack stack, boolean simulate){
		if(this.rHolder != null && !stack.isEmpty() && this.rHolder.value().getInputItem().test(stack)){
			int capacity = getCapacity() * this.rHolder.value().getInputItem().getCount();
			int current = getTotalAmount() * this.rHolder.value().getInputItem().getCount();
			
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
	public RecipeHolder<CokerUnitRecipe> getRecipe(){
		return this.rHolder;
	}
	
	/** Expected input. */
	public ItemStack getInputItem(){
		if(this.rHolder == null){
			return ItemStack.EMPTY;
		}
		return this.rHolder.value().getInputItem().getMatchingStacks()[0];
	}
	
	/** Expected output. */
	public ItemStack getOutputItem(){
		if(this.rHolder == null){
			return ItemStack.EMPTY;
		}
		
		return this.rHolder.value().getOutputItem();
	}
	
	public FluidTank getTank(){
		return this.tank;
	}
	
	/** returns true when the coker should update, false otherwise */
	public boolean tick(IMultiblockContext<CokerUnitLogic.State> context, int chamberId){
		if(this.rHolder == null){
			return setStage(State.STANDBY);
		}
		
		CokerUnitLogic.State logicState = context.getState();
		
		switch(this.state){
			case STANDBY -> {
				if(this.rHolder != null){
					return setStage(State.PROCESSING);
				}
			}
			case PROCESSING -> {
				final CokerUnitRecipe recipe = this.rHolder.value();
				
				if(this.inputAmount > 0 && !getInputItem().isEmpty() && (this.tank.getCapacity() - this.tank.getFluidAmount()) >= recipe.getOutputFluid().getAmount()){
					if(logicState.energy.getEnergyStored() >= recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime()){
						logicState.energy.extractEnergy(recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime(), false);
						
						this.timer++;
						if(this.timer >= (recipe.getTotalProcessTime() * recipe.getInputItem().getCount())){
							this.timer = 0;
							
							this.tank.fill(Utils.copyFluidStackWithAmount(recipe.getOutputFluid(), recipe.getOutputFluid().getAmount(), false), IFluidHandler.FluidAction.EXECUTE);
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
					
					final CokerUnitRecipe recipe = this.rHolder.value();
					
					int max = getTotalAmount() * recipe.getInputFluid().amount();
					if(this.tank.getFluidAmount() < max){
						FluidStack accepted = logicState.bufferTanks.input().drain(recipe.getInputFluid().amount(), IFluidHandler.FluidAction.SIMULATE);
						if(accepted.getAmount() >= recipe.getInputFluid().amount()){
							logicState.bufferTanks.input().drain(recipe.getInputFluid().amount(), IFluidHandler.FluidAction.EXECUTE);
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
						ItemStack copy = this.rHolder.value().getOutputItem();
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
					this.rHolder = null;
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
