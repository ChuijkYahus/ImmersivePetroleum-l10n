package flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IReadWriteNBT{
	public CompoundTag writeNBT(HolderLookup.Provider provider);
	public void readNBT(CompoundTag nbt, HolderLookup.Provider provider);
}
