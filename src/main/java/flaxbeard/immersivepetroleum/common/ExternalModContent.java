package flaxbeard.immersivepetroleum.common;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;

public class ExternalModContent{
	
	public static void setup(FMLCommonSetupEvent event){
		IE.forceClassLoad();
	}
	
	/**
	 * ImmersiveEngineering
	 */
	public static class IE{
		private static final Loader loader = new Loader(ResourceUtils::ie);
		
		private static final DeferredHolder<Block, Block> BLOCK_REDSTONE_ENGINEERING = loader.block("rs_engineering");
		private static final DeferredHolder<Item, Item> ITEM_HAMMER = loader.item("hammer");
		private static final DeferredHolder<Item, Item> ITEM_SCREWDRIVER = loader.item("screwdriver");
		private static final DeferredHolder<Item, Item> ITEM_PIPE = loader.item("fluid_pipe");
		private static final DeferredHolder<Item, Item> ITEM_BUCKSHOT = loader.item("bullet_buckshot");
		private static final DeferredHolder<Item, Item> ITEM_EMPTY_SHELL = loader.item("empty_shell");
		private static final DeferredHolder<Fluid, Fluid> FLUID_CONCRETE = loader.fluid("concrete");
		
		public static Fluid fluidConcrete(){
			return FLUID_CONCRETE.get();
		}
		
		public static FluidStack fluidConcrete(int amount){
			return new FluidStack(FLUID_CONCRETE.get(), amount);
		}
		
		public static Block blockRSEngineering(){
			return BLOCK_REDSTONE_ENGINEERING.get();
		}
		
		public static Item itemBuckshot(){
			return ITEM_BUCKSHOT.get();
		}
		
		public static Item itemEmptyShell(){
			return ITEM_EMPTY_SHELL.get();
		}
		
		public static Item itemPipe(){
			return ITEM_PIPE.get();
		}
		
		public static Item itemHammer(){
			return ITEM_HAMMER.get();
		}
		
		public static Item itemScrewdriver(){
			return ITEM_SCREWDRIVER.get();
		}
		
		public static boolean isConcrete(FluidStack fluid){
			return isConcrete(fluid.getFluid());
		}
		
		public static boolean isConcrete(Fluid fluid){
			return fluidConcrete().equals(fluid);
		}
		
		public static boolean isRedstoneEngineering(Block block){
			return blockRSEngineering().equals(block);
		}
		
		public static boolean isBuckshot(ItemStack stack){
			return isBuckshot(stack.getItem());
		}
		
		public static boolean isBuckshot(Item item){
			return itemBuckshot().equals(item);
		}
		
		public static boolean isEmptyShell(ItemStack stack){
			return isEmptyShell(stack.getItem());
		}
		
		public static boolean isEmptyShell(Item item){
			return itemEmptyShell().equals(item);
		}
		
		public static boolean isPipe(ItemStack stack){
			return isPipe(stack.getItem());
		}
		
		public static boolean isPipe(Item item){
			return itemPipe().equals(item);
		}
		
		public static boolean isHammer(ItemStack stack){
			return isHammer(stack.getItem());
		}
		
		public static boolean isHammer(Item item){
			return itemHammer().equals(item);
		}
		
		public static boolean isScrewdriver(ItemStack stack){
			return isScrewdriver(stack.getItem());
		}
		
		public static boolean isScrewdriver(Item item){
			return itemScrewdriver().equals(item);
		}
		
		private static void forceClassLoad(){
		}
	}
	
	/* Not really the best name for this, but better than nothing */
	private record Loader(Function<String, ResourceLocation> modLoc){
		public DeferredHolder<Block, Block> block(String name){
			return DeferredHolder.create(BuiltInRegistries.BLOCK.key(), this.modLoc.apply(name));
		}
		
		public DeferredHolder<Item, Item> item(String name){
			return DeferredHolder.create(BuiltInRegistries.ITEM.key(), this.modLoc.apply(name));
		}
		
		public DeferredHolder<Fluid, Fluid> fluid(String name){
			return DeferredHolder.create(BuiltInRegistries.FLUID.key(), this.modLoc.apply(name));
		}
	}
}
