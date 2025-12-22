package flaxbeard.immersivepetroleum.common.datastorage.reservoir;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ColumnPos;

public record RegionPos(int x, int z){
	public RegionPos(BlockPos pos, int xOff, int zOff){
		this((pos.getX() + 256 * xOff) >> 9, (pos.getZ() + 256 * zOff) >> 9);
	}
	
	public RegionPos(ColumnPos pos, int xOff, int zOff){
		this((pos.x() + 256 * xOff) >> 9, (pos.z() + 256 * zOff) >> 9);
	}
	
	public RegionPos(BlockPos pos){
		this(pos.getX() >> 9, pos.getZ() >> 9);
	}
	
	public RegionPos(ColumnPos pos){
		this(pos.x() >> 9, pos.z() >> 9);
	}
}
