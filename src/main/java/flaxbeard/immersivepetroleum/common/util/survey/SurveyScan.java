package flaxbeard.immersivepetroleum.common.util.survey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.common.IPDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SurveyScan implements ISurveyInfo{
	public static final String TAG_KEY = "surveyscan";
	
	public static final int SCAN_RADIUS = 32;
	public static final int SCAN_SIZE = SCAN_RADIUS * 2 + 1;
	private static final double sqrt2048 = Math.sqrt((SCAN_RADIUS * SCAN_RADIUS) * 2);
	
	private static final Codec<UUID> UUID_CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.LONG.fieldOf("msb").forGetter(UUID::getMostSignificantBits),
		Codec.LONG.fieldOf("lsb").forGetter(UUID::getLeastSignificantBits)
	).apply(inst, UUID::new));
	
	//@formatter:off
	public static final Codec<SurveyScan> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		UUID_CODEC.optionalFieldOf("uuid").forGetter(s -> {
			if(s.uuid == null)
				return Optional.empty();
			return Optional.of(s.uuid);
		}),
		Codec.INT.fieldOf("x").forGetter(s -> s.x),
		Codec.INT.fieldOf("z").forGetter(s -> s.z),
		Codec.BYTE.sizeLimitedListOf(SCAN_SIZE * SCAN_SIZE).fieldOf("data").forGetter(s -> {
			List<Byte> bytes = new ArrayList<>();
			for(byte b: s.data)
				bytes.add(b);
			return bytes;
		})
	).apply(inst, SurveyScan::new));
	//@formatter:on
	
	public static final StreamCodec<ByteBuf, SurveyScan> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(SurveyScan::new, ISurveyInfo::writeToTag);
	
	@Nullable
	private final UUID uuid;
	private final int x, z;
	private final byte[] data;
	
	public SurveyScan(Level world, BlockPos pos){
		this.uuid = UUID.randomUUID();
		this.x = pos.getX();
		this.z = pos.getZ();
		
		this.data = scanArea(world, pos);
	}
	
	private SurveyScan(CompoundTag tag){
		this.uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
		this.x = tag.getInt("x");
		this.z = tag.getInt("z");
		this.data = tag.getByteArray("map");
	}
	
	private SurveyScan(Optional<UUID> uuid, int x, int z, List<Byte> dataList){
		byte[] data = new byte[dataList.size()];
		for(int i = 0;i < dataList.size();i++){
			data[i] = dataList.get(i);
		}
		
		this.uuid = uuid.orElse(null);
		this.x = x;
		this.z = z;
		this.data = data;
	}
	
	@Nullable
	public UUID getUuid(){
		return this.uuid;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getZ(){
		return this.z;
	}
	
	public byte[] getData(){
		return this.data;
	}
	
	@Override
	public void writeToStack(ItemStack stack){
		stack.set(IPDataComponents.SURVEY_SCAN, this);
	}
	
	@Override
	public CompoundTag writeToTag(){
		CompoundTag tag = new CompoundTag();
		
		tag.putUUID("uuid", UUID.randomUUID());
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		tag.putByteArray("map", this.data);
		
		return tag;
	}
	
	private byte[] scanArea(Level world, BlockPos pos){
		final List<ReservoirIsland> islandCache = new ArrayList<>();
		byte[] scanData = new byte[SCAN_SIZE * SCAN_SIZE];
		
		for(int j = -SCAN_RADIUS,a = 0;j <= SCAN_RADIUS;j++,a++){
			for(int i = -SCAN_RADIUS,b = 0;i <= SCAN_RADIUS;i++,b++){
				int x = pos.getX() - i;
				int z = pos.getZ() - j;
				
				int data = 0;
				double current = ReservoirHandler.getValueOf(world, x, z);
				if(current != -1){
					Optional<ReservoirIsland> optional = islandCache.stream().filter(res -> {
						return res.contains(x, z);
					}).findFirst();
					
					ReservoirIsland nearbyIsland = optional.isPresent() ? optional.get() : null;
					if(nearbyIsland == null){
						nearbyIsland = ReservoirHandler.getIslandNoCache(world, new ColumnPos(x, z));
						
						if(nearbyIsland != null){
							islandCache.add(nearbyIsland);
						}
					}
					
					if(nearbyIsland != null){
						data = (int) Mth.clamp(255 * current, 0, 255);
					}
				}
				
				int noise = 31 + (int) (127 * Math.random());
				
				double blend = Math.sqrt(i * i + j * j) / sqrt2048;
				int lerped = (int) (Mth.clampedLerp(data, noise, blend));
				scanData[(a * SCAN_SIZE) + b] = (byte) (lerped & 0xFF);
			}
		}
		
		return normalizeScanData(scanData);
	}
	
	private byte[] normalizeScanData(byte[] scanData){
		int max = Integer.MIN_VALUE;
		for(int i = 0;i < scanData.length;i++){
			int data = ((int) scanData[i]) & 0xFF;
			if(data > max) max = data;
		}
		for(int i = 0;i < scanData.length;i++){
			int data = ((int) scanData[i]) & 0xFF;
			scanData[i] = (byte) (255 * (data / (float) max));
		}
		return scanData;
	}
}
