package flaxbeard.immersivepetroleum.client.render.debugging;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.OilTankLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokerUnitLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokingChamber;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.AutoLubricatorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.FlarestackTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.GasGeneratorTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.IPTileEntityBase;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellPipeTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionPos;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class DebugRenderHandler{
	public DebugRenderHandler(){
	}
	
	@SubscribeEvent
	public void renderLevelStage(RenderLevelStageEvent event){
		if(event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS){
			DebugReservoirRender.reservoirDebuggingRender(event);
		}
	}
	
	private boolean isHoldingDebugItem(Player player){
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		
		return (main != ItemStack.EMPTY && main.getItem() == IPContent.DEBUGITEM.get()) || (off != ItemStack.EMPTY && off.getItem() == IPContent.DEBUGITEM.get());
	}
	
	static final DebugText debugText = new DebugText();
	
	@SubscribeEvent
	public void renderDebuggingOverlay(RenderGuiLayerEvent.Post event){
		Minecraft mc = Minecraft.getInstance();
		
		if(mc.player != null && event.getName() == VanillaGuiLayers.DEBUG_OVERLAY){
			Player player = mc.player;
			
			if(isHoldingDebugItem(player)){
				HitResult rt = mc.hitResult;
				if(rt != null){
					switch(rt.getType()){
						case BLOCK -> {
							final BlockHitResult result = (BlockHitResult) rt;
							final Level world = player.level();
							final BlockPos hitPos = result.getBlockPos();
							
							BlockState blockState = world.getBlockState(hitPos);
							
							if(blockState.getBlock() instanceof EntityBlock){
								BlockEntity te = world.getBlockEntity(hitPos);
								
								if(te instanceof GasGeneratorTileEntity gas){
									MutableComponent name = Component.translatable(te.getBlockState().getBlock().getDescriptionId()).withStyle(ChatFormatting.GOLD);
									
									boolean isActive = !gas.stopSound(null);
									name.append(Component.literal(isActive ? " (Active)" : " (Inactive)").withStyle(isActive ? ChatFormatting.GREEN : ChatFormatting.RED));
									
									if(world.hasNeighborSignal(gas.getPosition())){
										name.append(Component.literal(" (Redstoned)").withStyle(ChatFormatting.RED));
									}
									
									debugText.add(name);
									
									debugText.addEnergyText(gas.getCapability(Capabilities.EnergyStorage.BLOCK, null));
									debugText.addTankText(gas.getCapability(Capabilities.FluidHandler.BLOCK, null));
									
								}else if(te instanceof IPTileEntityBase){
									debugText.translated(te.getBlockState().getBlock().getDescriptionId(), ChatFormatting.GOLD);
									
									if(te instanceof AutoLubricatorTileEntity autolube){
										debugText.literal("isSlave", autolube.isSlave ? ChatFormatting.GREEN : ChatFormatting.RED);
										
										if(autolube.isSlave)
											autolube = autolube.master();
										
										FluidTank tank = autolube.tank;
										FluidStack fs = tank.getFluid();
										
										debugText.literal("Facing: " + autolube.facing.getName());
										debugText.addTankText(autolube.tank);
										
									}else if(te instanceof FlarestackTileEntity flare){
									}else if(te instanceof WellTileEntity well){
									}else if(te instanceof WellPipeTileEntity wellPipe){
									}
									
								}else if(te instanceof IMultiblockBE<?> generic){
									final IMultiblockBEHelper<?> mbHelper = generic.getHelper();
									final IMultiblockState mbState = mbHelper.getState();
									final MultiblockRegistration<?> multiblock = mbHelper.getMultiblock();
									
									{
										BlockPos tPos = mbHelper.getPositionInMB();
										debugText.literal("Template XYZ: " + tPos.getX() + ", " + tPos.getY() + ", " + tPos.getZ());
										
										Block block = multiblock.block().get();
										MutableComponent name = toTranslation(block.getDescriptionId()).withStyle(ChatFormatting.GOLD);
										
										synchronized(LubricatedHandler.lubricatedTiles){
											for(LubricatedTileInfo info: LubricatedHandler.lubricatedTiles){
												if(info.pos.equals(tPos)){
													name.append(toText(" (Lubricated " + info.ticks + ")").withStyle(ChatFormatting.YELLOW));
												}
											}
										}
										
										boolean rsState = getRedstoneState(generic);
										if(rsState){
											name.append(toText(" (Redstoned)").withStyle(ChatFormatting.RED));
										}
										
										debugText.add(name);
									}
									
									if(mbState instanceof DistillationTowerLogic.State){
										distillationTower(debugText, generic);
										
									}else if(mbState instanceof CokerUnitLogic.State){
										cokerunit(debugText, generic);
										
									}else if(mbState instanceof HydroTreaterLogic.State){
										hydrotreater(debugText, generic);
										
									}else if(mbState instanceof OilTankLogic.State){
										oiltank(debugText, generic);
										
									}else if(mbState instanceof DerrickLogic.State){
										derrick(debugText, generic);
									}
								}
							}else{
								if(blockState.getBlock() instanceof RedStoneWireBlock){
									debugText.literal("Redstone Wire", ChatFormatting.GOLD);
									debugText.literal("Power: " + blockState.getValue(RedStoneWireBlock.POWER));
								}
							}
						}
						case ENTITY -> {
							EntityHitResult result = (EntityHitResult) rt;
							
							if(result.getEntity() instanceof MotorboatEntity boat){
								MutableComponent name = Component.translatable("item.immersivepetroleum.speedboat")
									.withStyle(ChatFormatting.GOLD)
									.append(" (" + boat.getStringUUID() + ")");
								debugText.add(name);
								
								IFluidTank tank = boat.getTank();
								MutableComponent literal = Component.literal(String.format("%d/%d mB", tank.getFluidAmount(), tank.getCapacity()));
								if(!tank.getFluid().isEmpty()){
									literal.append(" (" + tank.getFluid().getHoverName().getString() + ")");
								}
								debugText.add(literal);
								
								NonNullList<ItemStack> upgrades = boat.getUpgrades();
								int i = 0;
								for(ItemStack upgrade: upgrades){
									if(upgrade == ItemStack.EMPTY){
										debugText.literal("Upgrade " + (++i) + ": Empty");
									}else{
										debugText.literal("Upgrade " + (++i) + ": " + upgrade.getHoverName().getString());
									}
								}
							}
						}
						default -> {
							boolean debug = false;
							if(debug){
								final ReservoirRegionDataStorage storage = ReservoirRegionDataStorage.get();
								BlockPos playerPos = MCUtil.getPlayer().blockPosition();
								
								RegionPos rLocal = new RegionPos(playerPos);
								
								RegionPos r0 = new RegionPos(playerPos, 1, -1);
								RegionPos r1 = new RegionPos(playerPos, 1, 1);
								RegionPos r2 = new RegionPos(playerPos, -1, -1);
								RegionPos r3 = new RegionPos(playerPos, -1, 1);
								
								boolean bLocal = storage.getRegionData(rLocal) != null;
								
								boolean b0 = storage.getRegionData(r0) != null;
								boolean b1 = storage.getRegionData(r1) != null;
								boolean b2 = storage.getRegionData(r2) != null;
								boolean b3 = storage.getRegionData(r3) != null;
								
								debugText.literal(String.format("PlayerXYZ: %d %d %d", playerPos.getX(), playerPos.getY(), playerPos.getZ()));
								debugText.literal(String.format("LocalXZ: %d %d", rLocal.x(), rLocal.z()), bLocal ? ChatFormatting.GREEN : ChatFormatting.RED);
								debugText.literal(String.format("XZ: %d %d", r0.x(), r0.z()), b0 ? ChatFormatting.GREEN : ChatFormatting.RED);
								debugText.literal(String.format("XZ: %d %d", r1.x(), r1.z()), b1 ? ChatFormatting.GREEN : ChatFormatting.RED);
								debugText.literal(String.format("XZ: %d %d", r2.x(), r2.z()), b2 ? ChatFormatting.GREEN : ChatFormatting.RED);
								debugText.literal(String.format("XZ: %d %d", r3.x(), r3.z()), b3 ? ChatFormatting.GREEN : ChatFormatting.RED);
							}
						}
					}
					
					if(!debugText.isEmpty()){
						Vec3 location = rt.getLocation();
						BlockPos hit = new BlockPos((int) location.x, (int) location.y, (int) location.z);
						
						debugText.literal(0, "World XYZ: " + hit.getX() + ", " + hit.getY() + ", " + hit.getZ());
						
						debugText.render(event.getGuiGraphics());
					}
				}
			}
		}
	}
	
	/**
	 * Only works when reloading the world, so... broken?<br>
	 * Not much I can do since it goes for All multiblock machines, including IE ones.
	 */
	private <S extends IMultiblockState> boolean getRedstoneState(IMultiblockBE<S> generic){
		final IMultiblockBEHelper<S> mbHelper = generic.getHelper();
		final IMultiblockState mbState = mbHelper.getState();
		final MultiblockRegistration<S> multiblock = mbHelper.getMultiblock();
		
		if(!multiblock.redstoneInputAware())
			return false;
		
		for(MultiblockRegistration.ExtraComponent extraComponent: multiblock.extraComponents()){
			if(extraComponent.component() instanceof RedstoneControl rsCtrl){
				RedstoneControl.RSState rsState = rsCtrl.wrapState(mbState);
				return rsState.isEnabled(mbHelper.getContext());
			}
		}
		
		return false;
	}
	
	private static void distillationTower(DebugText debugText, IMultiblockBE<?> multiblockBE){
		IMultiblockBEHelper<DistillationTowerLogic.State> master = masterOf(multiblockBE.getHelper().asType(IPContent.Multiblock.DISTILLATIONTOWER));
		
		debugText.literal("Input Tank", ChatFormatting.UNDERLINE);
		debugText.addTankText(null, master.getState().tanks.input());
		
		debugText.literal("Output Tank", ChatFormatting.UNDERLINE);
		debugText.addTankText(null, master.getState().tanks.output());
	}
	
	private static void cokerunit(DebugText debugText, IMultiblockBE<?> multiblockBE){
		IMultiblockBEHelper<CokerUnitLogic.State> master = masterOf(multiblockBE.getHelper().asType(IPContent.Multiblock.COKERUNIT));
		
		{
			FluidTankFiltered tank = master.getState().bufferTanks.input();
			FluidStack fs = tank.getFluid();
			debugText.literal("In Buffer: " + (fs.getAmount() + "/" + tank.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getHoverName().getString() + ")")));
		}
		
		{
			FluidTankFiltered tank = master.getState().bufferTanks.output();
			FluidStack fs = tank.getFluid();
			debugText.literal("Out Buffer: " + (fs.getAmount() + "/" + tank.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getHoverName().getString() + ")")));
		}
		
		for(int i = 0;i < master.getState().chambers.array().length;i++){
			CokingChamber chamber = master.getState().chambers.array()[i];
			FluidTankFiltered tank = chamber.getTank();
			FluidStack fs = tank.getFluid();
			
			float completed = chamber.getTotalAmount() > 0 ? 100 * (chamber.getOutputAmount() / (float) chamber.getTotalAmount()) : 0;
			
			debugText.literal("Chamber " + i, ChatFormatting.UNDERLINE, ChatFormatting.AQUA);
			debugText.literal("State: " + chamber.getState().toString());
			debugText.literal("  Tank: " + (fs.getAmount() + "/" + tank.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getHoverName().getString() + ")")));
			debugText.literal("  Content: " + chamber.getTotalAmount() + " / " + chamber.getCapacity() + " (" + chamber.getInputItem().getHoverName().getString() + ")");
			debugText.literal("  Out: " + chamber.getOutputItem().getHoverName().getString());
			debugText.literal("  " + Mth.floor(completed) + "% Completed. (Raw: " + completed + ")");
		}
	}
	
	private static void hydrotreater(DebugText debugText, IMultiblockBE<?> multiblockBE){
		IMultiblockBEHelper<HydroTreaterLogic.State> master = masterOf(multiblockBE.getHelper().asType(IPContent.Multiblock.HYDROTREATER));
		
		IFluidTank[] tanks = master.getState().getInternalTanks();
		if(tanks != null && tanks.length > 0){
			for(int i = 0;i < tanks.length;i++){
				FluidStack fs = tanks[i].getFluid();
				debugText.literal("Tank " + i + ": " + (fs.getAmount() + "/" + tanks[i].getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getHoverName().getString() + ")")));
			}
		}
	}
	
	private static void oiltank(DebugText debugText, IMultiblockBE<?> multiblockBE){
		BlockPos mbpos = multiblockBE.getHelper().getPositionInMB();
		OilTankLogic.Port port = null;
		for(OilTankLogic.Port p: OilTankLogic.Port.values()){
			if(p.matches(mbpos)){
				port = p;
				break;
			}
		}
		
		IMultiblockBEHelper<OilTankLogic.State> tank = masterOf(multiblockBE.getHelper().asType(IPContent.Multiblock.OILTANK));
		
		if(port != null){
			OilTankLogic.PortState portState = tank.getState().portConfig.get(port);
			boolean isInput = portState == OilTankLogic.PortState.INPUT;
			
			MutableComponent component = toText("Port: " + port.getSerializedName())
				.append(toText(" " + portState.getSerializedName()).withStyle(isInput ? ChatFormatting.AQUA : ChatFormatting.GOLD));
			
			debugText.add(component);
		}
		
		debugText.addTankText(tank.getState().tank);
		FluidStack fs = tank.getState().tank.getFluid();
	}
	
	private static void derrick(DebugText debugText, IMultiblockBE<?> multiblockBE){
		IMultiblockBEHelper<DerrickLogic.State> derrick = masterOf(multiblockBE.getHelper().asType(IPContent.Multiblock.DERRICK));
		
		IFluidTank tanks = derrick.getState().tank;
		FluidStack fs = tanks.getFluid();
		debugText.literal("Tank : " + (fs.getAmount() + "/" + tanks.getCapacity() + "mB " + (fs.isEmpty() ? "" : "(" + fs.getHoverName().getString() + ")")));
	}
	
	@Deprecated(forRemoval = true)
	private static <State extends IMultiblockState, H extends IMultiblockBEHelper<State>> H masterOf(H helper){
		if(!(helper instanceof IMultiblockBEHelperMaster<?>) && helper.getContext() != null){
			IMultiblockLevel mbLevel = helper.getContext().getLevel();
			BlockPos masterPos = mbLevel.toAbsolute(helper.getMultiblock().masterPosInMB());
			
			BlockEntity be = mbLevel.getRawLevel().getBlockEntity(masterPos);
			if(be instanceof MultiblockBlockEntityMaster<?> master)
				return (H) master.getHelper();
		}
		
		return helper;
	}
	
	static MutableComponent toText(String string){
		return Component.literal(string);
	}
	
	static MutableComponent toTranslation(String translationKey, Object... args){
		return Component.translatable(translationKey, args);
	}
}
