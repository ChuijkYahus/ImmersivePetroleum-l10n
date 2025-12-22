package flaxbeard.immersivepetroleum.api.reservoir;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ReservoirPolygon{
	private static final Set<Location> TEMP = new HashSet<>();
	
	public static ReservoirPolygon make(Level level, ColumnPos start){
		return new ReservoirPolygon(level, start);
	}
	
	public static ReservoirPolygon fromNBT(CompoundTag tag){
		return new ReservoirPolygon(tag);
	}
	
	private final ArrayList<ColumnPos> poly = new ArrayList<>();
	private final ReservoirBoundingBox bounds;
	ReservoirPolygon(Level level, ColumnPos start){
		scan(level, start);
		
		// Only keep outline
		TEMP.removeIf(location -> !location.edge());
		
		makeDirectional();
		cullLines();
		
		this.poly.trimToSize();
		TEMP.clear();
		
		this.bounds = createBoundingBox();
	}
	
	ReservoirBoundingBox createBoundingBox(){
		int xMin = Integer.MAX_VALUE;
		int zMin = Integer.MAX_VALUE;
		
		int xMax = Integer.MIN_VALUE;
		int zMax = Integer.MIN_VALUE;
		
		for(ColumnPos p: this.poly){
			if(p.x() < xMin)
				xMin = p.x();
			if(p.z() < zMin)
				zMin = p.z();
			
			if(p.x() > xMax)
				xMax = p.x();
			if(p.z() > zMax)
				zMax = p.z();
		}
		
		return new ReservoirBoundingBox(xMin, zMin, xMax, zMax);
	}
	
	ReservoirPolygon(CompoundTag nbt){
		int xMin = nbt.getInt("xMin");
		int zMin = nbt.getInt("zMin");
		int xMax = nbt.getInt("xMax");
		int zMax = nbt.getInt("zMax");
		this.bounds = new ReservoirBoundingBox(xMin, zMin, xMax, zMax);
		
		byte[] array = nbt.getByteArray("points");
		for(int i = 0;i < array.length;i += 2){
			int x = this.bounds.xMin() + ((int) array[i] & 0xFF);
			int z = this.bounds.zMin() + ((int) array[i + 1] & 0xFF);
			this.poly.add(new ColumnPos(x, z));
		}
	}
	
	public CompoundTag writeToNBT(){
		CompoundTag tag = new CompoundTag();
		
		ReservoirBoundingBox bounds = getBoundingBox();
		tag.putInt("xMin", bounds.xMin());
		tag.putInt("zMin", bounds.zMin());
		tag.putInt("xMax", bounds.xMax());
		tag.putInt("zMax", bounds.zMax());
		
		byte[] array = new byte[this.poly.size() * 2];
		for(int i = 0, j = 0;i < this.poly.size();i++, j += 2){
			ColumnPos pos = this.poly.get(i);
			byte x = (byte) ((pos.x() - bounds.xMin()) & 0xFF);
			byte z = (byte) ((pos.z() - bounds.zMin()) & 0xFF);
			array[j] = x;
			array[j + 1] = z;
		}
		tag.putByteArray("points", array);
		
		return tag;
	}
	
	/**
	 * Not to be used by other mods!
	 * <p>
	 * If you need to access this for some reason, it better be a good one!
	 */
	@Deprecated
	public List<ColumnPos> getPolygonList(){
		return Collections.unmodifiableList(this.poly);
	}
	
	public boolean isEmpty(){
		return this.poly.isEmpty();
	}
	
	public ReservoirBoundingBox getBoundingBox(){
		return this.bounds;
	}
	
	/**
	 * Convenience method.
	 *
	 * @see #contains(int, int)
	 */
	public boolean contains(ColumnPos pos){
		return contains(pos.x(), pos.z());
	}
	
	/**
	 * Same as {@link #polygonContains(int, int)} but with the Bounds as the first check.
	 *
	 * @param x x-coordinate to query for
	 * @param z z-coordinate to query for
	 * @return whether the reservoir contains this position
	 */
	public boolean contains(int x, int z){
		if(!this.bounds.contains(x, z)){
			return false;
		}
		
		return polygonContains(x, z);
	}
	
	/**
	 * Convenience method.
	 *
	 * @see #polygonContains(int, int)
	 */
	public boolean polygonContains(ColumnPos pos){
		return polygonContains(pos.x(), pos.z());
	}
	
	/**
	 * Test whether the given XZ coordinates are within the islands polygon.
	 *
	 * @param x x-coordinate to test
	 * @param z y-coordinate to test
	 * @return true if the coordinates are inside, false otherwise
	 */
	public boolean polygonContains(int x, int z){
		boolean ret = false;
		int j = this.poly.size() - 1;
		for(int i = 0;i < this.poly.size();i++){
			ColumnPos a = this.poly.get(i);
			ColumnPos b = this.poly.get(j);
			
			// They need to be floats, or it won't work for some reason
			float ax = a.x(), az = a.z();
			float bx = b.x(), bz = b.z();
			
			// Any point directly on the edge is considered "inside"
			if((ax == x && az == z)){
				return true;
			}else if((ax == x && bx == x) && ((z > bz && z < az) || (z > az && z < bz))){
				return true;
			}else if((az == z && bz == z) && ((x > ax && x < bx) || (x > bx && x < ax))){
				return true;
			}
			
			// Voodoo Magic for Point-In-Polygon
			if(((az < z && bz >= z) || (bz < z && az >= z)) && (ax <= x || bx <= x)){
				float f0 = ax + (z - az) / (bz - az) * (bx - ax);
				ret ^= (f0 < x);
			}
			
			j = i;
		}
		
		return ret;
	}
	
	void makeDirectional(){
		Location current = TEMP.stream().findFirst().get();
		TEMP.remove(current);
		final ArrayList<Location> dst = new ArrayList<>();
		dst.add(current);
		while(!TEMP.isEmpty()){
			current = nextDir(current, dst);
			
			if(current == null){
				if(!TEMP.isEmpty())
					ImmersivePetroleum.log.warn("Early-Exit: ReservoirPolygon.TEMP is not Empty! Still containing {}", TEMP.size());
				
				break;
			}
		}
		this.poly.addAll(dst.stream().map(Location::pos).toList());
	}
	
	void cullLines(){
		int endIndex = 0;
		ColumnPos startPos, endPos = null;
		for(int startIndex = 0;startIndex < this.poly.size();startIndex++){
			startPos = this.poly.get(startIndex);
			
			// Find the end of the current line on X
			for(int j = 1;j < 64;j++){
				int index = (startIndex + j) % this.poly.size();
				ColumnPos pos = this.poly.get(index);
				
				if(startPos.z() != pos.z()){
					break;
				}
				
				endIndex = index;
				endPos = pos;
			}
			
			// Find the end the current line on Z
			for(int j = 1;j < 64;j++){
				int index = (startIndex + j) % this.poly.size();
				ColumnPos pos = this.poly.get(index);
				
				if(startPos.x() != pos.x()){
					break;
				}
				
				endIndex = index;
				endPos = pos;
			}
			
			// Diagonal lines
			for(int j = 1;j < 64;j++){
				int index = (startIndex + j) % this.poly.size();
				ColumnPos pos = this.poly.get(index);
				
				int dx = Math.abs(pos.x() - startPos.x());
				int dz = Math.abs(pos.z() - startPos.z());
				
				if(dx != dz){
					break;
				}
				
				endIndex = index;
				endPos = pos;
			}
			
			// Commence culling
			if(endPos != null){
				int len = (endIndex - startIndex);
				if(len > 1){
					int index = startIndex + 1;
					for(int j = index;j < endIndex;j++){
						this.poly.remove(index % this.poly.size());
					}
				}else if(len < 0){
					// Start and End overlap themselves
					len = len + this.poly.size() - 1;
					
					if(len > 1){
						int index = startIndex + 1;
						for(int j = 0;j < len;j++){
							this.poly.remove(index % this.poly.size());
						}
					}
				}
				
				endPos = null;
			}
		}
	}
	
	static Location nextDir(Location current, List<Location> dst){
		Location[] locations = {
			current.offset(1, 0),
			current.offset(-1, 0),
			current.offset(0, 1),
			current.offset(0, -1),
			
			current.offset(-1, -1),
			current.offset(-1, 1),
			current.offset(1, -1),
			current.offset(1, 1)
		};
		
		for(Location location: locations){
			if(TEMP.remove(location)){
				dst.add(location);
				return location;
			}
		}
		
		return null;
	}
	
	void scan(Level level, ColumnPos pos){
		if(TEMP.contains(new Location(pos, false)) || ReservoirHandler.getValueOf(level, pos.x(), pos.z()) == -1)
			return;
		
		final ColumnPos p0 = new ColumnPos(pos.x() + 1, pos.z());
		final ColumnPos p1 = new ColumnPos(pos.x() - 1, pos.z());
		final ColumnPos p2 = new ColumnPos(pos.x(), pos.z() + 1);
		final ColumnPos p3 = new ColumnPos(pos.x(), pos.z() - 1);
		
		boolean b0 = ReservoirHandler.getValueOf(level, p0.x(), p0.z()) == -1;
		boolean b1 = ReservoirHandler.getValueOf(level, p1.x(), p1.z()) == -1;
		boolean b2 = ReservoirHandler.getValueOf(level, p2.x(), p2.z()) == -1;
		boolean b3 = ReservoirHandler.getValueOf(level, p3.x(), p3.z()) == -1;
		
		TEMP.add(new Location(pos, b0 | b1 | b2 | b3));
		
		scan(level, new ColumnPos(p0.x(), p0.z()));
		scan(level, new ColumnPos(p1.x(), p1.z()));
		scan(level, new ColumnPos(p2.x(), p2.z()));
		scan(level, new ColumnPos(p3.x(), p3.z()));
	}
	
	private record Location(@Nonnull ColumnPos pos, boolean edge){
		
		public Location(int x, int z, boolean edge){
			this(new ColumnPos(x, z), edge);
		}
		
		public Location offset(int x, int z){
			return new Location(this.pos.x() + x, this.pos.z() + z, false);
		}
		
		@Override
		public boolean equals(Object o){
			if(this == o)
				return true;
			if(!(o instanceof Location other))
				return false;
			return Objects.equals(this.pos, other.pos);
		}
		
		@Override
		public int hashCode(){
			return this.pos.hashCode();
		}
	}
}
