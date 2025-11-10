package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import javax.annotation.Nonnull;

public class IPBlockSlab<B extends IPBlockBase> extends SlabBlock{
	private final B base;
	
	public IPBlockSlab(B base){
		super(Properties.ofFullCopy(base).isSuffocating(causesSuffocation(base)).isRedstoneConductor(isNormalCube(base)));
		this.base = base;
	}
	
	@Override
	public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos){
		return Math.min(base.defaultBlockState().getLightBlock(worldIn, pos), super.getLightBlock(state, worldIn, pos));
	}
	
	@Override
	public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos){
		return super.propagatesSkylightDown(state, reader, pos) || base.defaultBlockState().propagatesSkylightDown(reader, pos);
	}
	
	public static BlockBehaviour.StatePredicate causesSuffocation(Block base){
		return (state, world, pos) -> base.defaultBlockState().isSuffocating(world, pos) && state.getValue(TYPE) == SlabType.DOUBLE;
	}
	
	public static BlockBehaviour.StatePredicate isNormalCube(Block base){
		return (state, world, pos) -> base.defaultBlockState().isRedstoneConductor(world, pos) && state.getValue(TYPE) == SlabType.DOUBLE;
	}
}
