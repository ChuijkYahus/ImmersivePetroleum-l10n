package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import com.google.common.collect.ImmutableSet;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.OilTankLogic.State;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes.OilTankShape;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public class OilTankLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>, MBOverlayText<State>{
	public static final int EQUALIZING_THRESHOLD = 1;
	
	public enum PortState implements StringRepresentable{
		INPUT, OUTPUT;
		
		@Override
		@Nonnull
		public String getSerializedName(){
			return this.toString().toLowerCase(Locale.ENGLISH);
		}
		
		public Component getText(){
			return Component.translatable("desc.immersivepetroleum.info.oiltank." + getSerializedName());
		}
		
		public PortState next(){
			return this == INPUT ? OUTPUT : INPUT;
		}
	}
	
	public enum Port implements StringRepresentable{
		//@formatter:off
		TOP(new CapabilityPosition(2, 2, 3, RelativeBlockFace.UP)),
		BOTTOM(new CapabilityPosition(2, 0, 3, RelativeBlockFace.DOWN)),
		DYNAMIC_A(new CapabilityPosition(0, 1, 2, RelativeBlockFace.RIGHT)),
		DYNAMIC_B(new CapabilityPosition(4, 1, 2, RelativeBlockFace.LEFT)),
		DYNAMIC_C(new CapabilityPosition(0, 1, 4, RelativeBlockFace.RIGHT)),
		DYNAMIC_D(new CapabilityPosition(4, 1, 4, RelativeBlockFace.LEFT));
		//@formatter:on
		
		public static final Port[] DYNAMIC_PORTS = {DYNAMIC_A, DYNAMIC_B, DYNAMIC_C, DYNAMIC_D};
		
		public final CapabilityPosition posInMultiblock;
		Port(CapabilityPosition posInMultiblock){
			this.posInMultiblock = posInMultiblock;
		}
		
		public boolean matches(BlockPos posInMultiblock){
			return posInMultiblock.equals(this.posInMultiblock.posInMultiblock());
		}
		
		@Override
		@Nonnull
		public String getSerializedName(){
			return this.toString().toLowerCase(Locale.ENGLISH);
		}
		
		static Set<CapabilityPosition> toSet(Port... ports){
			ImmutableSet.Builder<CapabilityPosition> builder = ImmutableSet.builder();
			for(Port port: ports){
				builder.add(port.posInMultiblock);
			}
			return builder.build();
		}
	}
	
	/**
	 * Template-Location of the Redstone Input Port. (2 2 5 & 2 2 2)<br>
	 */
	public static final BlockPos[] Redstone_IN = new BlockPos[]{new BlockPos(2, 2, 5), new BlockPos(2, 2, 2)};
	
	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource){
		return new State(capabilitySource);
	}
	
	@Override
	public void tickClient(IMultiblockContext<State> context){
	}
	
	@Override
	public void tickServer(IMultiblockContext<State> ctx){
		final State state = ctx.getState();
		final IMultiblockLevel level = ctx.getLevel();
		
		final PortState portStateA = state.getPortStateFor(Port.DYNAMIC_A);
		final PortState portStateB = state.getPortStateFor(Port.DYNAMIC_B);
		final PortState portStateC = state.getPortStateFor(Port.DYNAMIC_C);
		final PortState portStateD = state.getPortStateFor(Port.DYNAMIC_D);
		
		boolean wasBalancing = false;
		if((portStateA == PortState.OUTPUT && portStateC == PortState.INPUT) || (portStateA == PortState.INPUT && portStateC == PortState.OUTPUT)){
			wasBalancing |= state.equalize(ctx, Port.DYNAMIC_A, EQUALIZING_THRESHOLD, FluidType.BUCKET_VOLUME * 10);
		}
		
		if((portStateB == PortState.OUTPUT && portStateD == PortState.INPUT) || (portStateB == PortState.INPUT && portStateD == PortState.OUTPUT)){
			wasBalancing |= state.equalize(ctx, Port.DYNAMIC_B, EQUALIZING_THRESHOLD, FluidType.BUCKET_VOLUME * 10);
		}
		
		if(state.rsState.isEnabled(ctx) && state.tank.getFluidAmount() > 0){
			for(Port port: Port.values()){
				// Early exit in case this tank emptied while in this loop
				if(state.tank.getFluidAmount() == 0)
					break;
				
				if((!wasBalancing && state.getPortStateFor(port) == PortState.OUTPUT) || (wasBalancing && port == Port.BOTTOM)){
					Direction facing = state.getPortDirection(level.getOrientation(), port);
					BlockPos pos = level.toAbsolute(port.posInMultiblock.posInMultiblock()).relative(facing);
					
					FluidUtil.getFluidHandler(level.getRawLevel(), pos, facing.getOpposite()).ifPresent(out -> {
						FluidStack fs = FluidHelper.copyFluid(state.tank.getFluid(), Math.min(state.tank.getFluidAmount(), 432), false);
						int accepted = out.fill(fs, IFluidHandler.FluidAction.SIMULATE);
						if(accepted > 0){
							int drained = out.fill(FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), accepted), false), IFluidHandler.FluidAction.EXECUTE);
							state.tank.drain(FluidHelper.copyFluid(state.tank.getFluid(), drained, false), IFluidHandler.FluidAction.EXECUTE);
						}
					});
				}
			}
		}
		
		ctx.markDirtyAndSync();
		state.comparatorHelper.update(ctx, state.tank.getFluidAmount());
	}
	
	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register){
		register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
			for(Port port: Port.values()){
				if(port.matches(position.posInMultiblock())){
					return switch(state.portConfig.get(port)){
						case INPUT -> state.fluidInput;
						case OUTPUT -> state.fluidOutput;
					};
				}
			}
			return null;
		});
	}
	
	@Override
	public ItemInteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient){
		if(ExternalModContent.IE.isHammer(player.getItemInHand(hand))){
			if(hammering(ctx, posInMultiblock, isClient))
				return ItemInteractionResult.SUCCESS;
		}
		
		if(FluidUtils.interactWithFluidHandler(player, hand, ctx.getState().tank)){
			ctx.markDirtyAndSync();
			return ItemInteractionResult.SUCCESS;
		}
		
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
	
	private boolean hammering(IMultiblockContext<State> ctx, BlockPos posInMultiblock, boolean isClient){
		if(!isClient){
			for(Port port: Port.DYNAMIC_PORTS){
				if(port.matches(posInMultiblock)){
					ctx.getState().togglePortState(port);
					ctx.markDirtyAndSync();
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public List<Component> getOverlayText(State state, BlockPos posInMultiblock, BlockHitResult absoluteHit, Player player, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))){
			return List.of(TextUtils.formatFluidStack(state.tank.getFluid()));
		}
		
		return List.of();
	}
	
	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType){
		return OilTankShape.GETTER;
	}
	
	public static class State implements IMultiblockState{
		public final RedstoneControl.RSState rsState = RedstoneControl.RSState.disabledByDefault();
		
		public final FluidTank tank = new FluidTank(1024 * FluidType.BUCKET_VOLUME, f -> !f.getFluid().getFluidType().isLighterThanAir());
		public final EnumMap<Port, PortState> portConfig = new EnumMap<>(Port.class);
		
		private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;
		
		private final IFluidHandler fluidInput;
		private final IFluidHandler fluidOutput;
		public State(IInitialMultiblockContext<State> context){
			final BlockPos masterPos = IPContent.Multiblock.OILTANK.masterPosInMB();
			final Updater update = (ctx, layer, value) -> {
				final IMultiblockLevel level = ctx.getLevel();
				ctx.setComparatorOutputFor(layer, value);
				final BlockPos absPos = level.toAbsolute(masterPos);
				final BlockState stateAt = level.getBlockState(masterPos);
				level.getRawLevel().updateNeighborsAt(absPos, stateAt.getBlock());
			};
			
			//@formatter:off
			this.comparatorHelper = new LayeredComparatorOutput<>(
				this.tank.getCapacity(), 3,
				(ctx, value) -> update.update(ctx, masterPos, value),
				(ctx, layer, value) -> {
					for(int z = -2;z <= 2;z++){
						for(int x = -2;x <= 2;x++){
							if(x == 0 && z == 0)
								continue;
							
							BlockPos pos = masterPos.offset(x, layer, z);
							update.update(ctx, pos, value);
						}
					}
				}
			);
			//@formatter:on
			
			for(Port port: Port.values()){
				if(port == Port.DYNAMIC_B || port == Port.DYNAMIC_C || port == Port.BOTTOM){
					this.portConfig.put(port, PortState.OUTPUT);
				}else{
					this.portConfig.put(port, PortState.INPUT);
				}
			}
			
			this.fluidInput = ArrayFluidHandler.fillOnly(this.tank, context.getMarkDirtyRunnable());
			this.fluidOutput = ArrayFluidHandler.drainOnly(this.tank, context.getMarkDirtyRunnable());
		}
		
		@Override
		public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			nbt.put("tank", this.tank.writeToNBT(provider, new CompoundTag()));
			
			for(Port port: Port.DYNAMIC_PORTS){
				nbt.putInt(port.getSerializedName(), getPortStateFor(port).ordinal());
			}
			
			this.rsState.writeSaveNBT(nbt, provider);
		}
		
		@Override
		public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider){
			this.tank.readFromNBT(provider, nbt.getCompound("tank"));
			
			for(Port port: Port.DYNAMIC_PORTS){
				this.portConfig.put(port, PortState.values()[nbt.getInt(port.getSerializedName())]);
			}
			
			this.rsState.readSaveNBT(nbt, provider);
		}
		
		@Override
		public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			writeSaveNBT(nbt, provider);
		}
		
		@Override
		public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider){
			readSaveNBT(nbt, provider);
		}
		
		public PortState getPortStateFor(Port port){
			return this.portConfig.get(port);
		}
		
		public void togglePortState(Port port){
			this.portConfig.compute(port, (k, next) -> next.next());
		}
		
		private boolean equalize(IMultiblockContext<State> ctx, Port port, int threshold, int maxTransfer){
			IMultiblockLevel level = ctx.getLevel();
			Direction facing = getPortDirection(level.getOrientation(), port);
			BlockPos pos = level.toAbsolute(port.posInMultiblock.posInMultiblock()).relative(facing);
			BlockEntity te = level.getRawLevel().getBlockEntity(pos);
			
			if(te instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper().getContext().getState() instanceof State){
				IMultiblockContext<State> otherState = multiblockBE.getHelper().asType(IPContent.Multiblock.OILTANK).getContext();
				
				int diff = otherState.getState().tank.getFluidAmount() - this.tank.getFluidAmount();
				int amount = Math.min(Math.abs(diff) / 2, maxTransfer);
				
				return (diff <= -threshold && transfer(ctx, otherState, amount)) || (diff >= threshold && transfer(otherState, ctx, amount));
			}
			
			return false;
		}
		
		private boolean transfer(IMultiblockContext<State> src, IMultiblockContext<State> dst, int amount){
			State srcState = src.getState();
			State dstState = dst.getState();
			
			final Holder<Fluid> fluid = srcState.tank.getFluid().getFluidHolder();
			
			FluidStack fs = new FluidStack(fluid, amount);
			int accepted = dstState.tank.fill(fs, IFluidHandler.FluidAction.SIMULATE);
			if(accepted > 0){
				fs = new FluidStack(fluid, accepted);
				dstState.tank.fill(fs, IFluidHandler.FluidAction.EXECUTE);
				srcState.tank.drain(fs, IFluidHandler.FluidAction.EXECUTE);
				
				src.markDirtyAndSync();
				dst.markDirtyAndSync();
				return true;
			}
			
			return false;
		}
		
		private Direction getPortDirection(MultiblockOrientation orientation, Port port){
			boolean isMirrored = orientation.mirrored();
			Direction front = orientation.front();
			switch(port){
				case DYNAMIC_B, DYNAMIC_D -> {
					return isMirrored ? front.getCounterClockWise() : front.getClockWise();
				}
				case DYNAMIC_A, DYNAMIC_C -> {
					return isMirrored ? front.getClockWise() : front.getCounterClockWise();
				}
				case TOP -> {
					return Direction.UP;
				}
				default -> {
					return Direction.DOWN;
				}
			}
		}
		
		interface Updater{
			void update(IMultiblockContext<?> ctx, BlockPos pos, int value);
		}
	}
}
