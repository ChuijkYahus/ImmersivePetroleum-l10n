package flaxbeard.immersivepetroleum.common.gui;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

/**
 * Maybe
 */
public abstract class IPContainerMenu extends AbstractContainerMenu{
	protected IPContainerMenu(@Nullable MenuType<?> type, int id){
		super(type, id);
	}
}
