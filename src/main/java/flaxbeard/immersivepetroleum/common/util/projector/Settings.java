package flaxbeard.immersivepetroleum.common.util.projector;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import flaxbeard.immersivepetroleum.common.IPDataComponents;
import flaxbeard.immersivepetroleum.common.network.MessageProjectorSync;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nullable;
import java.util.Optional;

public class Settings{
	public static final String KEY_MODE = "mode";
	public static final String KEY_MULTIBLOCK = "multiblock";
	public static final String KEY_MIRROR = "mirror";
	public static final String KEY_PLACED = "placed";
	public static final String KEY_ROTATION = "rotation";
	public static final String KEY_POSITION = "pos";
	
	private @Nullable IMultiblock multiblock = null;
	private @Nullable BlockPos pos = null;
	private Mode mode = Mode.MULTIBLOCK_SELECTION;
	private Rotation rotation = Rotation.NONE;
	private boolean mirror = false;
	private boolean isPlaced = false;
	
	public Settings(){
		this(new CompoundTag());
	}
	
	public Settings(CompoundTag settingsNbt){
		if(settingsNbt != null && !settingsNbt.isEmpty()){
			this.mode = Mode.values()[Mth.clamp(settingsNbt.getInt(KEY_MODE), 0, Mode.values().length - 1)];
			this.rotation = Rotation.values()[settingsNbt.contains(KEY_ROTATION) ? settingsNbt.getInt(KEY_ROTATION) : 0];
			this.mirror = settingsNbt.getBoolean(KEY_MIRROR);
			this.isPlaced = settingsNbt.getBoolean(KEY_PLACED);
			
			if(settingsNbt.contains(KEY_MULTIBLOCK, Tag.TAG_STRING)){
				String str = settingsNbt.getString("multiblock");
				this.multiblock = MultiblockHandler.getByUniqueName(ResourceLocation.parse(str));
			}
			
			if(settingsNbt.contains(KEY_POSITION, Tag.TAG_COMPOUND)){
				CompoundTag pos = settingsNbt.getCompound("pos");
				int x = pos.getInt("x");
				int y = pos.getInt("y");
				int z = pos.getInt("z");
				this.pos = new BlockPos(x, y, z);
			}
		}
	}
	
	public CompoundTag toNbt(){
		CompoundTag nbt = new CompoundTag();
		nbt.putInt(KEY_MODE, this.mode.ordinal());
		nbt.putInt(KEY_ROTATION, this.rotation.ordinal());
		nbt.putBoolean(KEY_MIRROR, this.mirror);
		nbt.putBoolean(KEY_PLACED, this.isPlaced);
		
		if(this.multiblock != null){
			nbt.putString(KEY_MULTIBLOCK, this.multiblock.getUniqueName().toString());
		}
		
		if(this.pos != null){
			CompoundTag pos = new CompoundTag();
			pos.putInt("x", this.pos.getX());
			pos.putInt("y", this.pos.getY());
			pos.putInt("z", this.pos.getZ());
			nbt.put(KEY_POSITION, pos);
		}
		
		return nbt;
	}
	
	/** Rotate by 90° Clockwise */
	public void rotateCW(){
		this.rotation = this.rotation.getRotated(Rotation.CLOCKWISE_90);
	}
	
	/** Rotate by 90° Counter-Clockwise */
	public void rotateCCW(){
		this.rotation = this.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
	}
	
	public void flip(){
		this.mirror = !this.mirror;
	}
	
	public void switchMode(){
		int id = this.mode.ordinal() + 1;
		this.mode = Mode.values()[id % Mode.values().length];
	}
	
	public void sendPacketToServer(InteractionHand hand){
		MessageProjectorSync.sendToServer(this, hand);
	}
	
	public void sendPacketToClient(Player player, InteractionHand hand){
		MessageProjectorSync.sendToClient(player, this, hand);
	}
	
	public void setRotation(Rotation rotation){
		this.rotation = rotation;
	}
	
	public void setMode(Mode mode){
		this.mode = mode;
	}
	
	public void setMultiblock(@Nullable IMultiblock multiblock){
		this.multiblock = multiblock;
	}
	
	public void setMirror(boolean mirror){
		this.mirror = mirror;
	}
	
	public void setPlaced(boolean isPlaced){
		this.isPlaced = isPlaced;
	}
	
	public void setPos(@Nullable BlockPos pos){
		this.pos = pos;
	}
	
	public Rotation getRotation(){
		return this.rotation;
	}
	
	public boolean isMirrored(){
		return this.mirror;
	}
	
	public boolean isPlaced(){
		return this.isPlaced;
	}
	
	public Settings.Mode getMode(){
		return this.mode;
	}
	
	/**
	 * May return null to indicate that the projection has not been placed yet
	 */
	@Nullable
	public BlockPos getPos(){
		return this.pos;
	}
	
	/** May return null to indicate no multiblock has been selected yet */
	@Nullable
	public IMultiblock getMultiblock(){
		return this.multiblock;
	}
	
	public ItemStack applyTo(ItemStack stack){
		stack.set(IPDataComponents.PROJECTOR_SETTINGS, new SettingsRecord(this));
		return stack;
	}
	
	@Override
	public String toString(){
		return "\"Settings\":[" + toNbt().toString() + "]";
	}
	
	public enum Mode{
		MULTIBLOCK_SELECTION, PROJECTION;
		
		final String translation;
		Mode(){
			this.translation = "desc.immersivepetroleum.info.projector.mode_" + ordinal();
		}
		
		public Component getTranslated(){
			return Component.translatable(this.translation);
		}
	}
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public record SettingsRecord(@Nullable IMultiblock multiblock, @Nullable BlockPos pos, Mode mode, Rotation rotation, boolean isMirrored, boolean isPlaced){
		public static final String KEY_MODE = "mode";
		public static final String KEY_MULTIBLOCK = "multiblock";
		public static final String KEY_MIRROR = "mirror";
		public static final String KEY_PLACED = "placed";
		public static final String KEY_ROTATION = "rotation";
		public static final String KEY_POSITION = "pos";
		
		public static final Codec<SettingsRecord> CODEC = RecordCodecBuilder.create(inst -> inst.group(Codec.STRING.optionalFieldOf("multiblock").forGetter(s -> {
			if(s.multiblock == null)
				return Optional.empty();
			return Optional.of(s.multiblock.getUniqueName().toString());
		}), BlockPos.CODEC.optionalFieldOf("pos").forGetter(s -> Optional.ofNullable(s.pos)), Codec.INT.fieldOf("mode").forGetter(s -> s.mode.ordinal()), Rotation.CODEC.fieldOf("rotation").forGetter(SettingsRecord::rotation), Codec.BOOL.fieldOf("isMirrored").forGetter(SettingsRecord::isMirrored), Codec.BOOL.fieldOf("isPlaced").forGetter(SettingsRecord::isPlaced)).apply(inst, SettingsRecord::new));
		
		public static final StreamCodec<ByteBuf, SettingsRecord> CODEC_STREAM = ByteBufCodecs.COMPOUND_TAG.map(SettingsRecord::fromNbt, SettingsRecord::toNbt);
		
		public static SettingsRecord fromNbt(CompoundTag settingsNbt){
			@Nullable
			IMultiblock multiblock = null;
			@Nullable
			BlockPos pos = null;
			Mode mode = Mode.MULTIBLOCK_SELECTION;
			Rotation rotation = Rotation.NONE;
			boolean isMirrored = false;
			boolean isPlaced = false;
			
			if(settingsNbt != null && !settingsNbt.isEmpty()){
				mode = Mode.values()[Mth.clamp(settingsNbt.getInt(KEY_MODE), 0, Mode.values().length - 1)];
				rotation = Rotation.values()[settingsNbt.contains(KEY_ROTATION) ? settingsNbt.getInt(KEY_ROTATION) : 0];
				isMirrored = settingsNbt.getBoolean(KEY_MIRROR);
				isPlaced = settingsNbt.getBoolean(KEY_PLACED);
				
				if(settingsNbt.contains(KEY_MULTIBLOCK, Tag.TAG_STRING)){
					String str = settingsNbt.getString("multiblock");
					multiblock = MultiblockHandler.getByUniqueName(ResourceLocation.parse(str));
				}
				
				if(settingsNbt.contains(KEY_POSITION, Tag.TAG_COMPOUND)){
					CompoundTag posNbt = settingsNbt.getCompound("pos");
					int x = posNbt.getInt("x");
					int y = posNbt.getInt("y");
					int z = posNbt.getInt("z");
					pos = new BlockPos(x, y, z);
				}
			}
			
			return new SettingsRecord(multiblock, pos, mode, rotation, isMirrored, isPlaced);
		}
		
		public CompoundTag toNbt(){
			CompoundTag nbt = new CompoundTag();
			nbt.putInt(KEY_MODE, this.mode.ordinal());
			nbt.putInt(KEY_ROTATION, this.rotation.ordinal());
			nbt.putBoolean(KEY_MIRROR, this.isMirrored);
			nbt.putBoolean(KEY_PLACED, this.isPlaced);
			
			if(this.multiblock != null){
				nbt.putString(KEY_MULTIBLOCK, this.multiblock.getUniqueName().toString());
			}
			
			if(this.pos != null){
				CompoundTag pos = new CompoundTag();
				pos.putInt("x", this.pos.getX());
				pos.putInt("y", this.pos.getY());
				pos.putInt("z", this.pos.getZ());
				nbt.put(KEY_POSITION, pos);
			}
			
			return nbt;
		}
		
		private SettingsRecord(Optional<String> mbString, Optional<BlockPos> optPos, int mode, Rotation rotation, boolean isMirrored, boolean isPlaced){
			this(mbString.map(s -> MultiblockHandler.getByUniqueName(ResourceLocation.parse(s))).orElse(null), optPos.orElse(null), Mode.values()[mode], rotation, isMirrored, isPlaced);
		}
		
		public SettingsRecord(Settings settings){
			this(settings.multiblock, settings.pos, settings.mode, settings.rotation, settings.mirror, settings.isPlaced);
		}
		
		public Settings convert(){
			Settings settings = new Settings();
			settings.multiblock = this.multiblock;
			settings.pos = this.pos;
			settings.mode = this.mode;
			settings.rotation = this.rotation;
			settings.mirror = this.isMirrored;
			settings.isPlaced = this.isPlaced;
			return settings;
		}
	}
}
