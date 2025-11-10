package flaxbeard.immersivepetroleum.common.blocks.interfaces;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBlockEntityDrop{
	@Nonnull
	List<ItemStack> getBlockEntityDrop(LootContext context);
	
	default ItemStack getFirstBlockEntityDrop(){
		return getBlockEntityDrop(null).getFirst();
	}
}
