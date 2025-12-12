package flaxbeard.immersivepetroleum.common.util.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class EnumInventory<E extends Enum<E>>{
	
	private final NonNullList<ItemStack> inventory;
	public EnumInventory(Class<E> c){
		this.inventory = NonNullList.withSize(c.getEnumConstants().length, ItemStack.EMPTY);
	}
	
	public void save(CompoundTag nbt, HolderLookup.Provider provider){
		nbt.put("inventory", ContainerHelper.saveAllItems(new CompoundTag(), this.inventory, provider));
	}
	
	public void load(CompoundTag nbt, HolderLookup.Provider provider){
		NonNullList<ItemStack> list = NonNullList.withSize(size(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(nbt.getCompound("inventory"), list, provider);
		
		for(int i = 0;i < this.inventory.size();i++){
			ItemStack stack = ItemStack.EMPTY;
			if(i < list.size()){
				stack = list.get(i);
			}
			
			this.inventory.set(i, stack);
		}
	}
	
	public int size(){
		return this.inventory.size();
	}
	
	public NonNullList<ItemStack> getInternal(){
		return this.inventory;
	}
	
	public ItemStack set(E inv, ItemStack stack){
		return this.inventory.set(inv.ordinal(), stack);
	}
	
	public ItemStack get(E inv){
		return this.inventory.get(inv.ordinal());
	}
}
