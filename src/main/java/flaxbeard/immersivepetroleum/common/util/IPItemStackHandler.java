package flaxbeard.immersivepetroleum.common.util;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IPItemStackHandler extends ItemStackHandler implements ICapabilityProvider<ItemStack, Direction, Object>{
	private static final Runnable EMPTY_RUN = () -> {
	};
	
	@Nonnull
	private Runnable onChange = EMPTY_RUN;
	public IPItemStackHandler(int invSize){
		super(invSize);
	}
	
	public void setTile(BlockEntity tile){
		this.onChange = tile != null ? tile::setChanged : EMPTY_RUN;
	}
	
	public void setInventoryForUpdate(Container inv){
		this.onChange = inv != null ? inv::setChanged : EMPTY_RUN;
	}
	
	protected void onContentsChanged(int slot){
		super.onContentsChanged(slot);
		this.onChange.run();
	}
	
	/* // TODO Capability
	LazyOptional<IItemHandler> handler = LazyOptional.of(() -> this);// CapabilityUtils.constantOptional(this);
	
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing){
		if(capability == ForgeCapabilities.ITEM_HANDLER){
			return this.handler.cast();
		}
		
		return LazyOptional.empty();
	}
	*/
	
	public NonNullList<ItemStack> getContainedItems(){
		return this.stacks;
	}
	
	@Nullable
	@Override
	public Object getCapability(@Nonnull ItemStack stack, Direction direction){
		return null;
	}
}
