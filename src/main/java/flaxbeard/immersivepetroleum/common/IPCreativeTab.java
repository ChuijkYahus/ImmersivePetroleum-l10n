package flaxbeard.immersivepetroleum.common;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public class IPCreativeTab{
	//@formatter:off
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = IPRegisters.registerCreativeTab(ImmersivePetroleum.MODID, () -> CreativeModeTab.builder()
		.icon(() -> new ItemStack(IPContent.Fluids.CRUDEOIL.bucket().get()))
		.title(Component.translatable("itemGroup." + ImmersivePetroleum.MODID))
		.displayItems(IPCreativeTab::fill).build()
	);
	//@formatter:on
	
	private static void fill(CreativeModeTab.ItemDisplayParameters parms, CreativeModeTab.Output out){
		for(Item item: IPRegisters.getAllItems()){
			if(item instanceof IMightShowUpInCreativeTab i && i.addSelfToCreativeTab()){
				out.accept(item);
			}
		}
	}
	
	/**
	 * I find it amusing doing it this way
	 *
	 * @author TwistedGate
	 */
	public interface IMightShowUpInCreativeTab{
		default boolean addSelfToCreativeTab(){
			return true;
		}
	}
	
	public static void forceClassLoad(){
	}
}
