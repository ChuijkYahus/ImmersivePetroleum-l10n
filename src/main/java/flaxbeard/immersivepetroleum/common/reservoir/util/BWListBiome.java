package flaxbeard.immersivepetroleum.common.reservoir.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BWListBiome extends BWList<Holder<Biome>, BWListBiome.Validator>{
	
	//@formatter:off
	public static final Codec<BWListBiome> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.STRING.listOf().fieldOf("list").xmap(
			strings -> strings.stream().map(Validator::new).collect(Collectors.toSet()),
			set -> set.stream().map(Validator::getString).toList()
		).forGetter(BWListBiome::getSet),
		Codec.BOOL.fieldOf("isBlacklist").forGetter(BWListBiome::isBlacklist)
	).apply(inst, BWListBiome::new));
	//@formatter:on
	
	public static final StreamCodec<RegistryFriendlyByteBuf, BWListBiome> CODEC_STREAM = new StreamCodec<>(){
		@Nonnull
		@Override
		public BWListBiome decode(@Nonnull RegistryFriendlyByteBuf buf){
			int size = buf.readInt();
			Set<Validator> set = new HashSet<>(size);
			if(size > 0){
				for(int i = 0;i < size;i++)
					set.add(Validator.STREAM_CODEC.decode(buf));
			}
			boolean isBlacklist = buf.readBoolean();
			return new BWListBiome(set, isBlacklist);
		}
		
		@Override
		public void encode(@Nonnull RegistryFriendlyByteBuf buf, @Nonnull BWListBiome biomes){
			buf.writeInt(biomes.set.size());
			biomes.set.forEach(v -> Validator.STREAM_CODEC.encode(buf, v));
			buf.writeBoolean(biomes.isBlacklist());
		}
	};
	
	public static final DualCodec<RegistryFriendlyByteBuf, BWListBiome> CODECS = new DualCodec<>(CODEC, CODEC_STREAM);
	
	public BWListBiome(boolean isBlacklist){
		this(new HashSet<>(), isBlacklist);
	}
	
	public BWListBiome(Set<Validator> set, boolean isBlacklist){
		this(set, isBlacklist ? Mode.BLACKLIST : Mode.WHITELIST);
	}
	
	public BWListBiome(Set<Validator> set, Mode mode){
		super(set, mode, Validator::new);
	}
	
	@Override
	public boolean isValid(Holder<Biome> biomeHolder){
		return isValid(validator -> validator.test(biomeHolder));
	}
	
	public static class Validator implements IValidator{
		public static final StreamCodec<ByteBuf, Validator> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Validator::new, Validator::getString);
		
		private final ResourceLocation location;
		private TagKey<Biome> biomeTag = null;
		protected Validator(String rlString){
			boolean isTag = rlString.startsWith("#");
			if(isTag)
				rlString = rlString.substring(1);
			
			final ResourceLocation location = ResourceLocation.parse(rlString);
			if(isTag)
				this.biomeTag = TagKey.create(Registries.BIOME, location);
			
			this.location = location;
		}
		
		public Validator(ResourceLocation location){
			this.location = location;
		}
		
		@Override
		public ResourceLocation location(){
			return this.location;
		}
		
		public boolean test(Holder<Biome> holder){
			return (this.biomeTag != null && holder.is(this.biomeTag)) || test(holder.getKey());
		}
		
		public boolean isTag(){
			return this.biomeTag != null;
		}
		
		public TagKey<Biome> getTag(){
			return this.biomeTag;
		}
		
		@Override
		public String getString(){
			String str = this.location.toString();
			if(isTag())
				str = '#' + str;
			return str;
		}
		
		@Override
		public String toString(){
			return getString();
		}
	}
}
