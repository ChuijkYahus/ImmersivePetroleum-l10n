package flaxbeard.immersivepetroleum.common.reservoir.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BWListDimension extends BWList<ResourceKey<Level>, BWListDimension.Validator>{
	
	//@formatter:off
	public static final Codec<BWListDimension> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.STRING.listOf().fieldOf("list").xmap(
			strings -> strings.stream().map(Validator::new).collect(Collectors.toSet()),
			set -> set.stream().map(Validator::getString).toList()
		).forGetter(BWListDimension::getSet),
		Codec.BOOL.fieldOf("isBlacklist").forGetter(BWListDimension::isBlacklist)
	).apply(inst, BWListDimension::new));
	//@formatter:on
	
	public static final StreamCodec<RegistryFriendlyByteBuf, BWListDimension> CODEC_STREAM = new StreamCodec<>(){
		@Nonnull
		@Override
		public BWListDimension decode(@Nonnull RegistryFriendlyByteBuf buf){
			int size = buf.readInt();
			Set<Validator> set = new HashSet<>(size);
			if(size > 0){
				for(int i = 0;i < size;i++)
					set.add(Validator.STREAM_CODEC.decode(buf));
			}
			boolean isBlacklist = buf.readBoolean();
			return new BWListDimension(set, isBlacklist);
		}
		
		@Override
		public void encode(@Nonnull RegistryFriendlyByteBuf buf, @Nonnull BWListDimension dimensions){
			buf.writeInt(dimensions.set.size());
			dimensions.set.forEach(v -> Validator.STREAM_CODEC.encode(buf, v));
			buf.writeBoolean(dimensions.isBlacklist());
		}
	};
	public static final DualCodec<RegistryFriendlyByteBuf, BWListDimension> CODECS = new DualCodec<>(CODEC, CODEC_STREAM);
	
	public BWListDimension(boolean isBlacklist){
		this(new HashSet<>(), isBlacklist);
	}
	
	public BWListDimension(Set<Validator> set, boolean isBlacklist){
		this(set, isBlacklist ? Mode.BLACKLIST : Mode.WHITELIST);
	}
	
	public BWListDimension(Set<Validator> set, Mode mode){
		super(set, mode, Validator::new);
	}
	
	@Override
	public boolean isValid(ResourceKey<Level> dimensionKey){
		return isValid(validator -> validator.test(dimensionKey));
	}
	
	public static class Validator implements IValidator{
		public static final StreamCodec<ByteBuf, Validator> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(Validator::new, Validator::location);
		
		private final ResourceLocation location;
		public Validator(String rlString){
			this(ResourceLocation.parse(rlString));
		}
		
		public Validator(ResourceLocation location){
			this.location = location;
		}
		
		@Override
		public ResourceLocation location(){
			return this.location;
		}
		
		@Override
		public String toString(){
			return getString();
		}
	}
}
