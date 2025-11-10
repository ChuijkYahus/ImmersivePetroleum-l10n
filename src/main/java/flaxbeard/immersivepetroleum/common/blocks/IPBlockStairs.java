package flaxbeard.immersivepetroleum.common.blocks;

import net.minecraft.world.level.block.StairBlock;

public class IPBlockStairs<B extends IPBlockBase> extends StairBlock{
	public IPBlockStairs(B base){
		super(base.defaultBlockState(), Properties.ofFullCopy(base));
	}
}
