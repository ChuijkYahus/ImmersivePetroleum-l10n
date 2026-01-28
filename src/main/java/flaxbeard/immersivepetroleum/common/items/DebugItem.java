package flaxbeard.immersivepetroleum.common.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirBoundingBox;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirPolygon;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPDataComponents;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageDebugSync;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListBiome;
import flaxbeard.immersivepetroleum.common.reservoir.util.BWListDimension;
import flaxbeard.immersivepetroleum.common.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

public class DebugItem extends IPItemBase{
	
	public enum Render{
		NONE, RESERVOIR_HEATMAP, RESERVOIR_POLYGONS, RESERVOIR_ALL
	}
	
	public enum Mode{
		DISABLED("Disabled"),
		INFO_SPEEDBOAT("Info: Speedboat"),
		
		SEEDBASED_RESERVOIR(Render.RESERVOIR_HEATMAP, "Seed-Based Reservoir: Heatmap."),
		SEEDBASED_RESERVOIR_AREA_TEST(Render.RESERVOIR_ALL, "Seed-Based Reservoir: Testing."),
		SEEDBASED_RESERVOIR_STORAGE(Render.RESERVOIR_POLYGONS, "Seed-Based Reservoir: Storage Tests."),
		SEEDBASED_RESERVOIR_QUICK_WELL(Render.RESERVOIR_HEATMAP, "Seed-Based Reservoir: Quick Well."),
		
		REFRESH_ALL_IPMODELS("Refresh all IPModels"),
		
		GENERAL_TEST("This one could be dangerous to trigger!")
		;
		
		public static final Codec<Mode> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("mode").forGetter(Mode::id)
		).apply(inst, Mode::fromId));
		
		public static final StreamCodec<ByteBuf, Mode> CODEC_STREAM = ByteBufCodecs.INT.map(Mode::fromId, Mode::id);
		
		public final String display;
		public final Render render;
		Mode(String display){
			this(Render.NONE, display);
		}
		
		Mode(Render render, String display){
			this.display = display;
			this.render = render;
		}
		
		public int id(){
			return ordinal();
		}
		
		public static Mode fromId(int id){
			if(id < 0 || id >= values().length)
				return DISABLED;
			
			return values()[id];
		}
		
		public static Mode fromStack(ItemStack stack){
			Mode mode = stack.get(IPDataComponents.DEBUG_ITEM);
			return mode == null ? Mode.DISABLED : mode;
		}
	}
	
	public DebugItem(){
		super();
	}
	
	@Override
	@Nonnull
	public Component getName(@Nonnull ItemStack stack){
		return Component.literal("IP Debugging Tool").withStyle(ChatFormatting.LIGHT_PURPLE);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext ctx, List<Component> tooltip, @Nonnull TooltipFlag flag){
		tooltip.add(Component.literal("[Shift + Scroll-UP/DOWN] Change mode.").withStyle(ChatFormatting.GRAY));
		Mode mode = getMode(stack);
		if(mode == Mode.DISABLED){
			tooltip.add(Component.literal("  Disabled.").withStyle(ChatFormatting.DARK_GRAY));
		}else{
			tooltip.add(Component.literal("  " + mode.display).withStyle(ChatFormatting.DARK_GRAY));
		}
		
		tooltip.add(Component.literal("You're not supposed to have this.").withStyle(ChatFormatting.DARK_RED));
		super.appendHoverText(stack, ctx, tooltip, flag);
	}
	
	@Override
	public boolean addSelfToCreativeTab(){
		// This has no business being in the Tab
		return false;
	}
	
	@Override
	@Nonnull
	public InteractionResultHolder<ItemStack> use(Level worldIn, @Nonnull Player playerIn, @Nonnull InteractionHand handIn){
		if(!worldIn.isClientSide){
			Mode mode = DebugItem.getMode(playerIn.getItemInHand(handIn));
			
			switch(mode){
				case GENERAL_TEST -> {
					try{
						BlockPos playerPos = playerIn.blockPosition();
						int x = playerPos.getX();
						int z = playerPos.getZ();
						
						if(ReservoirHandler.getValueOf(worldIn, x, z) > -1){
							long t = System.nanoTime();
							ReservoirPolygon reservoirPolygon = ReservoirPolygon.make(worldIn, new ColumnPos(x, z));
							t = System.nanoTime() - t;
							
							ImmersivePetroleum.log.info("Took: {}ns ({}ms)", t, t / 1000000F);
							
							Reservoir reservoir = ReservoirHandler.getReservoir(worldIn, playerPos);
							if(reservoir != null){
								final List<ColumnPos> newPolygon = reservoirPolygon.getPolygonList();
								final List<ColumnPos> ogPolygon = reservoir.getPolygon().getPolygonList();
								
								ImmersivePetroleum.log.info("{} - {} {}", newPolygon.size(), ogPolygon.size(), (newPolygon.size() == ogPolygon.size() ? "Equal" : "Not Equal"));
								
								final BlockState air = Blocks.AIR.defaultBlockState();
								final BlockState blackWool = Blocks.BLACK_WOOL.defaultBlockState();
								final BlockState whiteWool = Blocks.WHITE_WOOL.defaultBlockState();
								final BlockState greenWool = Blocks.GREEN_WOOL.defaultBlockState();
								final BlockState limeWool = Blocks.LIME_WOOL.defaultBlockState();
								final BlockState yellowWool = Blocks.YELLOW_WOOL.defaultBlockState();
								final BlockState redWool = Blocks.RED_WOOL.defaultBlockState();
								
								BlockPos defaultHeight = new BlockPos(0, 127, 0);
								final int range = 3;
								ReservoirBoundingBox bb = reservoir.getBoundingBox();
								for(int k = bb.zMin() - range;k <= bb.zMax() + range;k++){
									for(int i = bb.xMin() - range;i <= bb.xMax() + range;i++){
										for(int j = -range;j <= range;j++){
											BlockPos bPos = defaultHeight.offset(i, j, k);
											worldIn.setBlockAndUpdate(bPos, air);
										}
										
										if(reservoir.getPolygon().contains(i, k))
											worldIn.setBlockAndUpdate(defaultHeight.offset(i, -range, k), limeWool);
										else
											worldIn.setBlockAndUpdate(defaultHeight.offset(i, -range, k), redWool);
									}
								}
								
								for(int i = 0;i < ogPolygon.size();i++){
									ColumnPos cPos = ogPolygon.get(i);
									BlockPos bPos = defaultHeight.offset(cPos.x(), 0, cPos.z());
									
									worldIn.setBlockAndUpdate(bPos, blackWool);
								}
								
								for(int i = 0;i < newPolygon.size();i++){
									ColumnPos cPos = newPolygon.get(i);
									BlockPos bPos = defaultHeight.offset(cPos.x(), 1, cPos.z());
									
									BlockState state = whiteWool;
									if(i == 0)
										state = greenWool;
									if(i == (newPolygon.size() / 2))
										state = yellowWool;
									if(i == (newPolygon.size() - 1))
										state = redWool;
									
									worldIn.setBlockAndUpdate(bPos, state);
								}
							}
							
						}
					}catch(Exception t){
						t.printStackTrace(System.err);
					}
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
				}
				case REFRESH_ALL_IPMODELS -> {
					try{
						IPModels.getModels().forEach(IPModel::init);
						
						playerIn.displayClientMessage(Component.literal("Models refreshed."), true);
					}catch(Exception e){
						e.printStackTrace();
					}
					
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
				}
				case SEEDBASED_RESERVOIR -> {
					BlockPos pos = playerIn.blockPosition();
					
					double noise = ReservoirHandler.getValueOf(worldIn, pos.getX(), pos.getZ());
					
					playerIn.displayClientMessage(Component.literal((pos.getX() + " " + pos.getZ()) + ": " + noise), true);
					
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
				}
				case SEEDBASED_RESERVOIR_AREA_TEST -> {
					BlockPos playerPos = playerIn.blockPosition();
					
					Reservoir reservoir;
					if((reservoir = ReservoirHandler.getReservoir(worldIn, playerPos)) != null){
						int x = playerPos.getX();
						int z = playerPos.getZ();
						
						float pressure = reservoir.getPressure(worldIn, x, z);
						
						if(playerIn.isShiftKeyDown()){
							reservoir.setAmount(reservoir.getCapacity());
							reservoir.setDirty();
							playerIn.displayClientMessage(Component.literal("Reservoir Refilled."), true);
							return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
						}
						
						String out = String.format(Locale.ENGLISH,
								"Noise: %.3f, Amount: %d/%d, Pressure: %.3f, Flow: %d, Type: %s",
								ReservoirHandler.getValueOf(worldIn, x, z),
								reservoir.getAmount(),
								reservoir.getCapacity(),
								pressure,
								Reservoir.getFlow(pressure),
								new FluidStack(reservoir.getFluid(), 1).getHoverName().getString());
						
						playerIn.displayClientMessage(Component.literal(out), true);
						
					}
					
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
				}
				case SEEDBASED_RESERVOIR_STORAGE -> {
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
				}
				default -> {
				}
			}
			return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(handIn));
		}
		
		return super.use(worldIn, playerIn, handIn);
	}
	
	@SuppressWarnings("unused")
	@Override
	@Nonnull
	public InteractionResult useOn(UseOnContext context){
		final Player player = context.getPlayer();
		if(player == null)
			return InteractionResult.PASS;
		
		final ItemStack held = player.getItemInHand(context.getHand());
		final Mode mode = DebugItem.getMode(held);
		
		switch(mode){
			case GENERAL_TEST -> {
				Level world = context.getLevel();
				if(world.isClientSide){
					// Client
					
					//player.displayClientMessage(Component.literal(""), false);
					
				}else{
					// Server
					
					BlockPos pos = context.getClickedPos();
					
					ResourceKey<Level> dimension = world.dimension();
					Holder<Biome> biome = world.getBiome(pos);
					
					player.displayClientMessage(Component.literal(dimension.location().toString()), false);
					
					for(RecipeHolder<ReservoirType> holder:ReservoirType.map.values()){
						ReservoirType res = holder.value();
						
						BWListDimension dims = res.getDimensions();
						BWListBiome biom = res.getBiomes();
						
						boolean validDimension = dims.isValid(dimension);
						boolean validBiome = biom.isValid(biome);
						
						MutableComponent component = Component.literal(res.name)
							.append(Component.literal(" Dimension").withStyle(validDimension ? ChatFormatting.GREEN : ChatFormatting.RED))
							.append(Component.literal(" Biome").withStyle(validBiome ? ChatFormatting.GREEN : ChatFormatting.RED));
						
						if(validDimension && validBiome){
							component = component.append(" (can spawn here)");
						}
						
						player.displayClientMessage(component, false);
					}
				}
				
				return InteractionResult.SUCCESS;
			}
			case SEEDBASED_RESERVOIR_QUICK_WELL -> quickWell(context, player);
			default -> {
			}
		}
		
		return InteractionResult.PASS;
	}
	
	static InteractionResult quickWell(UseOnContext context, Player player){
		if(context.getLevel().isClientSide)
			return InteractionResult.SUCCESS;
		
		final Level level = context.getLevel();
		final BlockPos clickedPos = context.getClickedPos();
		
		Reservoir reservoir = ReservoirRegionDataStorage.get().getReservoir(level, Utils.toColumnPos(clickedPos));
		if(reservoir == null){
			player.displayClientMessage(Component.literal("No Reservoir Here.").withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}
		
		WellTileEntity well = null;
		for(int y = clickedPos.getY();y >= level.getMinBuildHeight() - 1;y--){
			BlockPos current = new BlockPos(clickedPos.getX(), y, clickedPos.getZ());
			Block block = level.getBlockState(current).getBlock();
			
			if(block == IPContent.Blocks.WELL.get() || block == Blocks.BEDROCK){
				level.setBlockAndUpdate(current, IPContent.Blocks.WELL.get().defaultBlockState());
				well = (WellTileEntity) level.getBlockEntity(current);
				break;
			}
		}
		
		if(well == null){
			player.displayClientMessage(Component.literal("Failed to create/get Well.").withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}
		
		final BlockPos wellPos = well.getBlockPos();
		
		int len = Math.abs(clickedPos.getY() - wellPos.getY()) + 1;
		
		for(int i = 1;i < len;i++){
			final BlockPos current = wellPos.offset(0, i, 0);
			
			BlockState blockState = level.getBlockState(current);
			
			if(blockState.getBlock() == Blocks.BEDROCK || blockState.getBlock() == IPContent.Blocks.WELL.get())
				break;
			
			level.setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
			
			well.phyiscalPipesList.add(current.getY());
			well.pipes = 1;
			well.usePipe();
		}
		well.pastPhysicalPart = true;
		well.wellPipeLength = well.phyiscalPipesList.size();
		well.wellPipeLength = well.getMaxPipeLength();
		well.drillingCompleted = true;
		well.tappedReservoirs.add(Utils.toColumnPos(clickedPos));
		
		well.setChanged();
		
		player.displayClientMessage(Component.literal("Created Well.").withStyle(ChatFormatting.GREEN), true);
		
		return InteractionResult.SUCCESS;
	}
	
	public void onSpeedboatClick(MotorboatEntity speedboatEntity, Player player, ItemStack debugStack){
		if(speedboatEntity.level().isClientSide || DebugItem.getMode(debugStack) != Mode.INFO_SPEEDBOAT){
			return;
		}
		
		MutableComponent textOut = Component.literal("-- Speedboat --\n");
		
		FluidStack fluid = speedboatEntity.getTank().getFluid();
		if(fluid == FluidStack.EMPTY){
			textOut.append("Tank: Empty");
		}else{
			textOut.append("Tank: " + fluid.getAmount() + "/" + speedboatEntity.getMaxFuel() + "mB of ").append(fluid.getHoverName());
		}
		
		MutableComponent upgradesText = Component.literal("\n");
		NonNullList<ItemStack> upgrades = speedboatEntity.getUpgrades();
		int i = 0;
		for(ItemStack upgrade:upgrades){
			if(upgrade == null || upgrade == ItemStack.EMPTY){
				upgradesText.append("Upgrade " + (++i) + ": Empty\n");
			}else{
				upgradesText.append("Upgrade " + (++i) + ": ").append(upgrade.getHoverName()).append("\n");
			}
		}
		textOut.append(upgradesText);
		
		player.sendSystemMessage(textOut);
	}
	
	public static void setModeClient(ItemStack stack, Mode mode){
		stack.set(IPDataComponents.DEBUG_ITEM, mode);
		
		IPPacketHandler.sendToServer(new MessageDebugSync(mode));
	}
	
	public static Mode getMode(ItemStack stack){
		return Mode.fromStack(stack);
	}
	
	public static class ClientInputHandler{
		
		public static void onSneakScrolling(InputEvent.MouseScrollingEvent event, Player player, double scrollDelta){
			ItemStack mainItem = player.getMainHandItem();
			ItemStack secondItem = player.getOffhandItem();
			boolean main = !mainItem.isEmpty() && mainItem.getItem() == IPContent.DEBUGITEM.get();
			boolean off = !secondItem.isEmpty() && secondItem.getItem() == IPContent.DEBUGITEM.get();
			
			if(main || off){
				ItemStack target = main ? mainItem : secondItem;
				
				Mode mode = DebugItem.getMode(target);
				int id = mode.ordinal() + (int) scrollDelta;
				if(id < 0){
					id = Mode.values().length - 1;
				}
				if(id >= Mode.values().length){
					id = 0;
				}
				mode = Mode.values()[id];
				
				DebugItem.setModeClient(target, mode);
				player.displayClientMessage(Component.literal(mode.display), true);
				event.setCanceled(true);
			}
		}
	}
}
