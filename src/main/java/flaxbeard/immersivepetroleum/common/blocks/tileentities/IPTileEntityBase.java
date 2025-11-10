package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class IPTileEntityBase extends BlockEntity{
	public IPTileEntityBase(BlockEntityType<?> blockEntityType, BlockPos pWorldPosition, BlockState pBlockState){
		super(blockEntityType, pWorldPosition, pBlockState);
	}
	
	@Nonnull
	public Level getWorldNonnull(){
		return Objects.requireNonNull(super.getLevel());
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this, (b, p) -> getUpdateTag(p));
	}
	
	@Override
	public void handleUpdateTag(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider){
		loadAdditional(tag, provider);
	}
	
	@Override
	@Nonnull
	public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider provider){
		CompoundTag nbt = new CompoundTag();
		saveAdditional(nbt, provider);
		return nbt;
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider){
		loadAdditional(pkt.getTag(), provider);
	}
	
	@Override
	protected void saveAdditional(@Nonnull CompoundTag nbt, @Nonnull HolderLookup.Provider provider){
		writeCustom(nbt, provider);
	}
	
	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider){
		super.loadAdditional(nbt, provider);
		readCustom(nbt, provider);
	}
	
	protected abstract void writeCustom(CompoundTag compound, HolderLookup.Provider provider);
	
	protected abstract void readCustom(CompoundTag compound, HolderLookup.Provider provider);
}
