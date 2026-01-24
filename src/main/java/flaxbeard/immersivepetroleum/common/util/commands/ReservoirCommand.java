package flaxbeard.immersivepetroleum.common.util.commands;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirBoundingBox;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionData;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionPos;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class ReservoirCommand{
	private ReservoirCommand(){
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> create(){
		LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("reservoir").requires(source -> source.hasPermission(4));
		
		main.then(Commands.literal("locate").executes(ReservoirCommand::locate));
		main.then(setters());
		main.then(positional(Commands.literal("get"), ReservoirCommand::get));
		
		return main;
	}
	
	private static int get(CommandContext<CommandSourceStack> context, @Nonnull Reservoir reservoir){
		//@formatter:off
		CommandUtils.sendTranslated(context.getSource(),
				"chat.immersivepetroleum.command.reservoir.get",
				reservoir.getAmount(),
				Utils.fDecimal(reservoir.getAmount() / (double) reservoir.getCapacity() * 100),
				new FluidStack(reservoir.getFluid(), 1).getHoverName()
		);
		//@formatter:on
		return Command.SINGLE_SUCCESS;
	}
	
	private static int locate(CommandContext<CommandSourceStack> command){
		CommandSourceStack source = command.getSource();
		BlockPos srcPos;
		if(source.getEntity() != null){
			srcPos = source.getEntity().blockPosition();
		}else{
			Vec3 position = source.getPosition();
			srcPos = new BlockPos((int) position.x, (int) position.y, (int) position.z);
		}
		
		double dx = srcPos.getX() + 0.5;
		double dz = srcPos.getZ() + 0.5;
		int range = 128;
		int rangeSqr = range * range;
		
		Set<Reservoir> nearby = new HashSet<>();
		
		ReservoirRegionDataStorage storage = ReservoirRegionDataStorage.get();
		
		//@formatter:off
		RegionData[] regions = {
				storage.getRegionData(new RegionPos(srcPos, 1, -1)),
				storage.getRegionData(new RegionPos(srcPos, 1, 1)),
				storage.getRegionData(new RegionPos(srcPos, -1, -1)),
				storage.getRegionData(new RegionPos(srcPos, -1, 1))
		};
		//@formatter:on
		
		final ResourceKey<Level> dimKey = source.getLevel().dimension();
		for(RegionData rd: regions){
			if(rd != null){
				Multimap<ResourceKey<Level>, Reservoir> islands = rd.getReservoirList();
				synchronized(islands){
					islands.get(dimKey).forEach(reservoir -> {
						if(reservoir.getBoundingBox().getCenter().distToCenterSqr(dx, 0, dz) <= rangeSqr){
							nearby.add(reservoir);
						}
					});
				}
			}
		}
		
		if(nearby.isEmpty()){
			CommandUtils.sendTranslated(source, "chat.immersivepetroleum.command.reservoir.notfound");
			return Command.SINGLE_SUCCESS;
		}
		
		// Find the Closest coordinate that can tap into one of them
		Reservoir closestIsland = null;
		double smallestDistance = rangeSqr;
		ColumnPos p = null;
		for(Reservoir reservoir: nearby){
			ReservoirBoundingBox IAABB = reservoir.getBoundingBox();
			for(int z = IAABB.zMin() + 1;z < IAABB.zMax();z++){
				for(int x = IAABB.xMin() + 1;x < IAABB.xMax();x++){
					if(reservoir.getPolygon().contains(x, z)){
						double xa = (x + 0.5) - dx;
						double za = (z + 0.5) - dz;
						double dst = xa * xa + za * za;
						if(dst < smallestDistance){
							p = new ColumnPos(x, z);
							smallestDistance = dst;
							closestIsland = reservoir;
						}
					}
				}
			}
		}
		
		if(closestIsland == null){
			CommandUtils.sendStringError(source, "List should not be empty. Please report this bug. (Immersive Petroleum)");
			return Command.SINGLE_SUCCESS;
		}
		
		// Find the spot with the highest pressure
		double hPressure = 0.0D;
		ReservoirBoundingBox IAABB = closestIsland.getBoundingBox();
		for(int z = IAABB.zMin() + 1;z < IAABB.zMax();z++){
			for(int x = IAABB.xMin() + 1;x < IAABB.xMax();x++){
				double cPressure;
				if(closestIsland.getPolygon().contains(x, z) && (cPressure = ReservoirHandler.getValueOf(source.getLevel(), x, z)) > hPressure){
					hPressure = cPressure;
					p = new ColumnPos(x, z);
				}
			}
		}
		
		final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + p.x() + " ~ " + p.z());
		final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"));
		
		Reservoir finalClosestIsland = closestIsland;
		ColumnPos finalP = p;
		source.sendSuccess(() -> Component.translatable("chat.immersivepetroleum.command.reservoir.locate", finalClosestIsland.getType().value().name, ComponentUtils.wrapInSquareBrackets(Component.literal(finalP.x() + " " + finalP.z())).withStyle((s) -> {
			return s.withColor(ChatFormatting.GREEN).withItalic(true).withClickEvent(clickEvent).withHoverEvent(hoverEvent);
		})), true);
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> setters(){
		LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set").requires(source -> source.hasPermission(4));
		
		set.then(Commands.literal("amount").then(positional(Commands.argument("amount", LongArgumentType.longArg(0, Reservoir.MAX_AMOUNT)), ReservoirCommand::setReservoirAmount)));
		set.then(Commands.literal("capacity").then(positional(Commands.argument("capacity", LongArgumentType.longArg(0, Reservoir.MAX_AMOUNT)), ReservoirCommand::setReservoirCapacity)));
		set.then(Commands.literal("type").then(positional(Commands.argument("name", StringArgumentType.string()).suggests(ReservoirCommand::typeSuggestor), ReservoirCommand::setReservoirType)));
		
		return set;
	}
	
	private static CompletableFuture<Suggestions> typeSuggestor(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
		return SharedSuggestionProvider.suggest(ReservoirType.map.values().stream().map(type -> type.value().name), builder);
	}
	
	private static int setReservoirAmount(CommandContext<CommandSourceStack> context, @Nonnull Reservoir reservoir){
		long amount = context.getArgument("amount", Long.class);
		reservoir.setAmount(amount);
		reservoir.setDirty();
		
		CommandUtils.sendTranslated(context.getSource(), "chat.immersivepetroleum.command.reservoir.set.amount.success", reservoir.getAmount());
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setReservoirCapacity(CommandContext<CommandSourceStack> context, @Nonnull Reservoir reservoir){
		long capacity = context.getArgument("capacity", Long.class);
		reservoir.setAmountAndCapacity(capacity, capacity);
		reservoir.setDirty();
		
		CommandUtils.sendTranslated(context.getSource(), "chat.immersivepetroleum.command.reservoir.set.capacity.success", reservoir.getCapacity());
		return Command.SINGLE_SUCCESS;
	}
	
	private static int setReservoirType(CommandContext<CommandSourceStack> context, @Nonnull Reservoir reservoir){
		String name = context.getArgument("name", String.class);
		RecipeHolder<ReservoirType> type = null;
		for(RecipeHolder<ReservoirType> holder: ReservoirType.map.values()){
			if(holder.value().name.equalsIgnoreCase(name))
				type = holder;
		}
		
		if(type == null){
			CommandUtils.sendTranslatedError(context.getSource(), "chat.immersivepetroleum.command.reservoir.set.type.fail", name);
			return Command.SINGLE_SUCCESS;
		}
		
		reservoir.setReservoirType(type);
		reservoir.setDirty();
		
		CommandUtils.sendTranslated(context.getSource(), "chat.immersivepetroleum.command.reservoir.set.type.success", reservoir.getType().value().name);
		return Command.SINGLE_SUCCESS;
	}
	
	static <T extends ArgumentBuilder<CommandSourceStack, T>> T positional(T builder, BiFunction<CommandContext<CommandSourceStack>, Reservoir, Integer> function){
		builder.executes(command -> {
			ColumnPos pos = Utils.toColumnPos(BlockPos.containing(command.getSource().getPosition()));
			
			Reservoir reservoir = ReservoirHandler.getReservoir(command.getSource().getLevel(), pos);
			if(reservoir == null){
				CommandUtils.sendTranslated(command.getSource(), "chat.immersivepetroleum.command.reservoir.notfound");
				return Command.SINGLE_SUCCESS;
			}
			
			return function.apply(command, reservoir);
		}).then(Commands.argument("location", ColumnPosArgument.columnPos()).executes(command -> {
			ColumnPos pos = ColumnPosArgument.getColumnPos(command, "location");
			
			Reservoir reservoir = ReservoirHandler.getReservoir(command.getSource().getLevel(), pos);
			if(reservoir == null){
				CommandUtils.sendTranslated(command.getSource(), "chat.immersivepetroleum.command.reservoir.notfound");
				return Command.SINGLE_SUCCESS;
			}
			
			return function.apply(command, reservoir);
		}));
		return builder;
	}
}
