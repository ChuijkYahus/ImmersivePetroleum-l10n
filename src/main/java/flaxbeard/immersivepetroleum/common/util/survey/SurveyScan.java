package flaxbeard.immersivepetroleum.common.util.survey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record SurveyScan(@Nullable UUID uuid, int x, int z, byte[] data) implements ISurveyInfo{
	public static final int SCAN_RADIUS = 32;
	public static final int SCAN_SIZE = SCAN_RADIUS * 2 + 1;
	private static final double sqrt2048 = Math.sqrt((SCAN_RADIUS * SCAN_RADIUS) * 2);
	
	//@formatter:off
	private static final Codec<UUID> UUID_CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.LONG.fieldOf("msb").forGetter(UUID::getMostSignificantBits),
		Codec.LONG.fieldOf("lsb").forGetter(UUID::getLeastSignificantBits)
	).apply(inst, UUID::new));
	
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
	).apply(inst, SurveyScan::create));
	//@formatter:on
	
	public static final StreamCodec<ByteBuf, SurveyScan> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(SurveyScan::fromNBT, ISurveyInfo::writeToTag);
	
	private static SurveyScan fromNBT(CompoundTag nbt){
		UUID uuid = nbt.hasUUID("uuid") ? nbt.getUUID("uuid") : null;
		int x = nbt.getInt("x");
		int z = nbt.getInt("z");
		byte[] data = nbt.getByteArray("map");
		return new SurveyScan(uuid, x, z, data);
	}
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static SurveyScan create(@Nonnull Optional<UUID> opUUID, int x, int z, @Nonnull List<Byte> dataList){
		byte[] data = new byte[dataList.size()];
		for(int i = 0;i < dataList.size();i++){
			data[i] = dataList.get(i);
		}
		return new SurveyScan(opUUID.orElse(null), x, z, data);
	}
	
	public static SurveyScan create(Level world, BlockPos pos){
		UUID uuid = UUID.randomUUID();
		int x = pos.getX();
		int z = pos.getZ();
		byte[] data = scanArea(world, pos);
		return new SurveyScan(uuid, x, z, data);
	}
	
	private static byte[] scanArea(Level world, BlockPos pos){
		final List<Reservoir> islandCache = new ArrayList<>();
		byte[] scanData = new byte[SCAN_SIZE * SCAN_SIZE];
		
		for(int j = -SCAN_RADIUS, a = 0;j <= SCAN_RADIUS;j++, a++){
			for(int i = -SCAN_RADIUS, b = 0;i <= SCAN_RADIUS;i++, b++){
				int x = pos.getX() - i;
				int z = pos.getZ() - j;
				
				int data = 0;
				double current = ReservoirHandler.getValueOf(world, x, z);
				if(current != -1){
					//@formatter:off
					Optional<Reservoir> optional = islandCache.stream()
						.filter(res -> {
							return res.getPolygon().contains(x, z);
						})
						.findFirst();
					//@formatter:on
					
					Reservoir nearbyIsland = optional.orElse(null);
					if(nearbyIsland == null){
						nearbyIsland = ReservoirHandler.getReservoirNoCache(world, new ColumnPos(x, z));
						
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
	
	private static byte[] normalizeScanData(byte[] scanData){
		int max = Integer.MIN_VALUE;
		for(int i = 0;i < scanData.length;i++){
			int data = ((int) scanData[i]) & 0xFF;
			if(data > max)
				max = data;
		}
		for(int i = 0;i < scanData.length;i++){
			int data = ((int) scanData[i]) & 0xFF;
			scanData[i] = (byte) (255 * (data / (float) max));
		}
		return scanData;
	}
	
	@Override
	public int getX(){
		return this.x;
	}
	
	@Override
	public int getZ(){
		return this.z;
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
}
