package flaxbeard.immersivepetroleum.common.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType.BWList;
import flaxbeard.immersivepetroleum.client.model.IPModel;
import flaxbeard.immersivepetroleum.client.model.IPModels;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPDataComponents;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageDebugSync;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

public class DebugItem extends IPItemBase{
	public enum Mode{
		DISABLED("Disabled"),
		INFO_SPEEDBOAT("Info: Speedboat"),
		
		SEEDBASED_RESERVOIR("Seed-Based Reservoir: Heatmap"),
		SEEDBASED_RESERVOIR_AREA_TEST("Seed-Based Reservoir: Island Testing"),
		
		REFRESH_ALL_IPMODELS("Refresh all IPModels"),
		UPDATE_SHAPES("Does nothing without Debugging Environment"),
		GENERAL_TEST("This one could be dangerous to trigger!")
		;
		
		public static final Codec<Mode> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("mode").forGetter(Mode::id)
		).apply(inst, Mode::fromId));
		
		public static final StreamCodec<ByteBuf, Mode> CODEC_STREAM = ByteBufCodecs.INT.map(Mode::fromId, Mode::id);
		
		public final String display;
		Mode(String display){
			this.display = display;
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
					if(worldIn.isClientSide){
					}else{
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
					BlockPos playerPos = playerIn.blockPosition();
					
					ChunkPos cPos = new ChunkPos(playerPos);
					int chunkX = cPos.getMinBlockX();
					int chunkZ = cPos.getMinBlockZ();
					
					// Does the whole 0-15 local chunk block thing
					int x = playerPos.getX() - cPos.getMinBlockX();
					int z = playerPos.getZ() - cPos.getMinBlockZ();
					
					double noise = ReservoirHandler.getValueOf(worldIn, (chunkX + x), (chunkZ + z));
					
					playerIn.displayClientMessage(Component.literal((chunkX + " " + chunkZ) + ": " + noise), true);
					
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
				}
				case SEEDBASED_RESERVOIR_AREA_TEST -> {
					BlockPos playerPos = playerIn.blockPosition();
					
					ReservoirIsland island;
					if((island = ReservoirHandler.getIsland(worldIn, playerPos)) != null){
						int x = playerPos.getX();
						int z = playerPos.getZ();
						
						float pressure = island.getPressure(worldIn, x, z);
						
						if(playerIn.isShiftKeyDown()){
							island.setAmount(island.getCapacity());
							island.setDirty();
							playerIn.displayClientMessage(Component.literal("Island Refilled."), true);
							return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
						}
						
						String out = String.format(Locale.ENGLISH,
								"Noise: %.3f, Amount: %d/%d, Pressure: %.3f, Flow: %d, Type: %s",
								ReservoirHandler.getValueOf(worldIn, x, z),
								island.getAmount(),
								island.getCapacity(),
								pressure,
								ReservoirIsland.getFlow(pressure),
								new FluidStack(island.getFluid(), 1).getHoverName().getString());
						
						playerIn.displayClientMessage(Component.literal(out), true);
						
					}else{
						/*
						final Multimap<ResourceKey<Level>, ReservoirIsland> islands = ReservoirHandler.getReservoirIslandList();
						
						for(ResourceKey<Level> key:islands.keySet()){
							Collection<ReservoirIsland> list = islands.get(key);
							
							String str = key.location() + " has " + list.size() + " islands.";
							
							playerIn.displayClientMessage(Component.literal(str), false);
						}
						*/
					}
					
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
		Player player = context.getPlayer();
		if(player == null){
			return InteractionResult.PASS;
		}
		
		ItemStack held = player.getItemInHand(context.getHand());
		Mode mode = DebugItem.getMode(held);
		
		BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
		switch(mode){
			case GENERAL_TEST -> {
				Level world = context.getLevel();
				if(world.isClientSide){
					// Client
					
					//player.displayClientMessage(Component.literal(""), false);
					
				}else{
					// Server
					
					BlockPos pos = context.getClickedPos();
					
					ResourceLocation dimensionRL = world.dimension().location();
					ResourceLocation biomeRL = RegistryUtils.getRegistryNameOf(world.getBiome(pos));
					
					player.displayClientMessage(Component.literal(dimensionRL.toString()), false);
					
					for(RecipeHolder<ReservoirType> holder:ReservoirType.map.values()){
						ReservoirType res = holder.value();
						
						BWList dims = res.getDimensions();
						BWList biom = res.getBiomes();
						
						boolean validDimension = dims.valid(dimensionRL);
						boolean validBiome = biom.valid(biomeRL);
						
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
			case UPDATE_SHAPES -> {
				return InteractionResult.PASS;
			}
			default -> {
			}
		}
		
		return InteractionResult.PASS;
	}
	
	public void onSpeedboatClick(MotorboatEntity speedboatEntity, Player player, ItemStack debugStack){
		if(speedboatEntity.level().isClientSide || DebugItem.getMode(debugStack) != Mode.INFO_SPEEDBOAT){
			return;
		}
		
		MutableComponent textOut = Component.literal("-- Speedboat --\n");
		
		FluidStack fluid = speedboatEntity.getContainedFluid();
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
				upgradesText.append("Upgrade " + (i++) + ": ").append(upgrade.getHoverName()).append("\n");
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
