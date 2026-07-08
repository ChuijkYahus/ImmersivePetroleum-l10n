package flaxbeard.immersivepetroleum.common.util;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import com.mojang.datafixers.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidHelper{
	
	/** Convenience Method */
	public static FluidStack copyFluid(FluidStack fluid, int amount){
		return copyFluid(fluid, amount, false);
	}
	
	/**
	 * Makes a copy of a FluidStack (excluding NBT) and optionally adds the Pressurized tag
	 * 
	 * @param fluid      {@link FluidStack} to use as the fluid type
	 * @param amount     the amount to use in the copied stack
	 * @param pressurize (optionally)
	 * @return {@link FluidStack}, pressurized if above IEs transfer threshold
	 */
	public static FluidStack copyFluid(FluidStack fluid, int amount, boolean pressurize){
		FluidStack fs = new FluidStack(fluid.getFluid(), amount);
		if(pressurize && amount > IFluidPipe.AMOUNT_UNPRESSURIZED){
			fs.set(IEApiDataComponents.FLUID_PRESSURIZED, Unit.INSTANCE);
		}
		return fs;
	}
	
	/**
	 * Creates a pressurized FluidStack instance of the given Fluid.<br>
	 * Only pressurizes the fluid if necessary. (amount goes above {@link IFluidPipe#AMOUNT_UNPRESSURIZED})
	 * 
	 * @param fluid  {@link Fluid} to use as the fluid type
	 * @param amount the amount to use in the created stack
	 * @return {@link FluidStack}, with pressurized tag as needed.
	 */
	public static FluidStack makePressurizedFluid(Fluid fluid, int amount){
		FluidStack fs = new FluidStack(fluid, amount);
		if(amount > IFluidPipe.AMOUNT_UNPRESSURIZED){
			fs.set(IEApiDataComponents.FLUID_PRESSURIZED, Unit.INSTANCE);
		}
		return fs;
	}
	
	/**
	 * Originally in IE as blusunrize.immersiveengineering.common.util.Utils#isFluidContainerFull(ItemStack)
	 */
	public static boolean isFluidContainerFull(ItemStack stack){
		return FluidUtil.getFluidHandler(stack).map(handler -> {
			for(int t = 0;t < handler.getTanks();++t)
				if(handler.getFluidInTank(t).getAmount() < handler.getTankCapacity(t))
					return false;
			return true;
		}).orElse(true);
	}
	
	/**
	 * Originally in IE as
	 * blusunrize.immersiveengineering.common.util.Utils#fillFluidContainer(IFluidHandler, ItemStack, ItemStack, Player)
	 */
	public static ItemStack fillFluidContainer(IFluidHandler handler, ItemStack containerIn, ItemStack containerOut, @Nullable Player player){
		if(containerIn == null || containerIn.isEmpty())
			return ItemStack.EMPTY;
		
		FluidActionResult result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, false);
		if(result.isSuccess()){
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty() || containerOut.isStackable() && containerOut.getCount() < containerOut.getMaxStackSize())){
			//if((containerOut.isEmpty() || ItemHandlerHelper.canItemStacksStack(containerOut, full))){
				if(!containerOut.isEmpty() && containerOut.getCount() + full.getCount() > containerOut.getMaxStackSize())
					return ItemStack.EMPTY;
				result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, true);
				if(result.isSuccess()){
					return result.getResult();
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	/**
	 * FluidStack based version of
	 * blusunrize.immersiveengineering.common.util.Utils#fillFluidContainer(IFluidHandler, ItemStack, ItemStack, Player) minus the
	 * useless bits :D
	 */
	public static ItemStack fillFluidContainer(IFluidTank tank, FluidStack fluid, ItemStack containerIn, ItemStack containerOut){
		if(containerIn == null || containerIn.isEmpty())
			return ItemStack.EMPTY;
		
		FluidActionResult result = tryFillContainer(tank, fluid, containerIn, false);
		if(result.isSuccess()){
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty() || full.isStackable() && full.getCount() < full.getMaxStackSize())){
				if(!containerOut.isEmpty() && containerOut.getCount() + full.getCount() > containerOut.getMaxStackSize()){
					return ItemStack.EMPTY;
				}
				
				result = tryFillContainer(tank, fluid, containerIn, true);
				if(result.isSuccess()){
					return result.getResult();
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	/**
	 * FluidStack based version of net.minecraftforge.fluids.FluidUtil#tryFillContainer(ItemStack, IFluidHandler, int, Player, boolean)
	 * minus the useless bits :D
	 */
	static FluidActionResult tryFillContainer(IFluidTank tank, FluidStack fluidSource, @Nonnull ItemStack container, boolean doFill){
		ItemStack containerCopy = container.copyWithCount(1);
		return FluidUtil.getFluidHandler(containerCopy).map(containerFluidHandler -> {
			
			int fillableAmount = containerFluidHandler.fill(fluidSource, FluidAction.SIMULATE);
			if(fillableAmount > 0){
				if(doFill){
					FluidStack fs = new FluidStack(fluidSource.getFluid(), Math.min(fluidSource.getAmount(), fillableAmount));
					containerFluidHandler.fill(fs, FluidAction.EXECUTE);
					tank.drain(fs, FluidAction.EXECUTE);
				}
				
				ItemStack resultContainer = containerFluidHandler.getContainer();
				return new FluidActionResult(resultContainer);
			}
			
			return FluidActionResult.FAILURE;
		}).orElse(FluidActionResult.FAILURE);
	}
	
	public static ItemStack tryDrainContainer(@Nonnull ItemStack stack, @Nonnull IFluidTank source){
		ItemStack emptyContainer = ItemStack.EMPTY;
		
		IFluidHandlerItem capability = stack.getCapability(Capabilities.FluidHandler.ITEM);
		if(capability != null){
			int amount = Math.min(source.getCapacity() - source.getFluidAmount(), FluidType.BUCKET_VOLUME);
			
			if(amount > 0){
				FluidStack fs = capability.getFluidInTank(0);
				amount = capability.drain(fs.copyWithAmount(Math.min(fs.getAmount(), amount)), FluidAction.SIMULATE).getAmount();
				
				if(amount > 0){
					FluidStack fluidStack = fs.copyWithAmount(amount);
					source.fill(fluidStack, FluidAction.EXECUTE);
					capability.drain(fluidStack, FluidAction.EXECUTE);
					
					if(capability.getFluidInTank(0).isEmpty() && stack.getItem() instanceof BucketItem){
						emptyContainer = new ItemStack(Items.BUCKET, 1);
					}
				}
			}
		}
		
		return emptyContainer;
	}
	
	/**
	 * <b>This is a hack!</b><br>
	 * <br>
	 * Transfer on IE Pipes is limited to 1000mB in a single tick.
	 * So this outputs multiple times with <b>10</b> attempts max or until everything is transferred
	 * 
	 * @return {@link FluidStack} with remainder
	 */
	public static FluidStack iterativeOutput(IFluidHandler out, FluidStack fluid, boolean isIEPipe){
		FluidStack copy = FluidHelper.copyFluid(fluid, fluid.getAmount(), isIEPipe);
		
		for(int attempt = 0;copy.getAmount() > 0 && attempt < 10;attempt++){
			int accepted = out.fill(copy, IFluidHandler.FluidAction.SIMULATE);
			if(accepted == 0)
				break;
			
			int drained = out.fill(FluidHelper.copyFluid(copy, Math.min(copy.getAmount(), accepted), isIEPipe), IFluidHandler.FluidAction.EXECUTE);
			copy = FluidHelper.copyFluid(fluid, copy.getAmount() - drained, isIEPipe);
		}
		
		return copy;
	}
	
	private FluidHelper(){
	}
}
