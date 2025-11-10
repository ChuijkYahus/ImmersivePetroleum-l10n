package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class ParaffinWaxBlock extends IPBlockBase{
	
	public ParaffinWaxBlock(){
		super(Properties.ofFullCopy(Blocks.PACKED_ICE)
			.mapColor(MapColor.COLOR_YELLOW)
			.strength(0.5F, 0.4F)
			.sound(SoundType.HONEY_BLOCK)
			.speedFactor(0.95F)
			.friction(1.05F));
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag){
		tooltip(tooltip);
		super.appendHoverText(stack, ctx, tooltip, flag);
	}
	static void tooltip(List<Component> tooltip){
		tooltip.add(Component.translatable("desc.immersivepetroleum.flavour.paraffin_wax").withStyle(ChatFormatting.GRAY));
	}
	
	@Override
	public int getFlammability(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction face){
		return 100;
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		return () -> new IPBlockItemBase(this, new Item.Properties()){
			@Override
			public int getBurnTime(@Nonnull ItemStack itemStack, RecipeType<?> recipeType){
				return 8000;
			}
		};
	}
}
