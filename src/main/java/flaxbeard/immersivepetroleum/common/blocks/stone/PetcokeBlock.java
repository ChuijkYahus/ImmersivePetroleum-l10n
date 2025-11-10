package flaxbeard.immersivepetroleum.common.blocks.stone;

import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockItemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class PetcokeBlock extends IPBlockBase{
	public PetcokeBlock(){
		super(Block.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.STONE).strength(2, 10).requiresCorrectToolForDrops());
	}
	
	@Override
	public Supplier<BlockItem> blockItemSupplier(){
		return () -> new IPBlockItemBase(this, new Item.Properties()){
			@Override
			public int getBurnTime(@Nonnull ItemStack itemStack, RecipeType<?> recipeType){
				return 32000;
			}
		};
	}
	
	@Override
	public int getFlammability(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction face){
		return 100;
	}
}
