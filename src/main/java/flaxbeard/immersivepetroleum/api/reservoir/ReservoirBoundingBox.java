package flaxbeard.immersivepetroleum.api.reservoir;

import net.minecraft.core.BlockPos;

import java.util.Objects;

/**
 * Simple BoundingBox for ReservoirIslands
 *
 * @author TwistedGate
 */
public class ReservoirBoundingBox{
	final int xMin, zMin;
	final int xMax, zMax;
	final BlockPos center;
	
	public ReservoirBoundingBox(int xMin, int zMin, int xMax, int zMax){
		this.xMin = xMin;
		this.zMin = zMin;
		this.xMax = xMax;
		this.zMax = zMax;
		
		this.center = new BlockPos((this.xMin + this.xMax) / 2, 0, (this.zMin + this.zMax) / 2);
	}
	
	public int xMin(){
		return this.xMin;
	}
	
	public int xMax(){
		return this.xMax;
	}
	
	public int zMin(){
		return this.zMin;
	}
	
	public int zMax(){
		return this.zMax;
	}
	
	public BlockPos getCenter(){
		return this.center;
	}
	
	public boolean contains(BlockPos pos){
		return contains(pos.getX(), pos.getZ());
	}
	
	public boolean contains(int x, int z){
		return x >= this.xMin && x <= this.xMax && z >= this.zMin && z <= this.zMax;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(this.xMax, this.zMax, this.xMin, this.zMin);
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		
		if(!(obj instanceof ReservoirBoundingBox other))
			return false;
		
		return this.xMax == other.xMax && this.zMax == other.zMax && this.xMin == other.xMin && this.zMin == other.zMin;
	}
	
	@Override
	public String toString(){
		return String.format("IslandAxisAlignedBB [minX = %d, minZ = %d, maxX = %d, maxZ = %d]", this.xMin, this.zMin, this.xMax, this.zMax);
	}
}
