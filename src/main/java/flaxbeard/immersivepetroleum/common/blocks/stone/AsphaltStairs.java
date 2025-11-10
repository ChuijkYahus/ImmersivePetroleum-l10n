package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.common.blocks.IPBlockStairs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import java.util.List;

public class AsphaltStairs extends IPBlockStairs<AsphaltBlock>{
	public AsphaltStairs(AsphaltBlock base){
		super(base);
	}
	
	@Override
	public float getSpeedFactor(){
		return AsphaltBlock.speedFactor();
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag){
		AsphaltBlock.tooltip(tooltip);
		super.appendHoverText(stack, ctx, tooltip, flag);
	}
}
