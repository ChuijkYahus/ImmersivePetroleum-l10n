package flaxbeard.immersivepetroleum.common.util;

import flaxbeard.immersivepetroleum.common.IPDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.items.ComponentItemHandler;

public class IPItemStackContainerHandler extends ComponentItemHandler{
	public IPItemStackContainerHandler(MutableDataComponentHolder parent, int size){
		super(parent, IPDataComponents.CONTAINER_ITEM.get(), size);
	}
	
	public NonNullList<ItemStack> getContainedItems(){
		ItemContainerContents contents = getContents();
		NonNullList<ItemStack> list = NonNullList.withSize(this.size, ItemStack.EMPTY);
		contents.copyInto(list);
		return list;
	}
}
