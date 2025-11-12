package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class IPMultiblockTexturesAttach extends SpriteSourceProvider{
	public IPMultiblockTexturesAttach(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper fileHelper){
		super(output, provider, ImmersivePetroleum.MODID, fileHelper);
	}
	
	@Override
	protected void gather(){
		final SourceList blockAtlas = atlas(SpriteSourceProvider.BLOCKS_ATLAS);
		
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/cokerunit"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/derrick"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/distillation_tower"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/distillation_tower_active"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/hydrotreater"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/oiltank"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("multiblock/pumpjack_base"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("models/lubricator"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("models/pumpjack_armature"), Optional.empty()));
		blockAtlas.addSource(new SingleFile(ResourceUtils.ip("projectors/projector"), Optional.empty()));
	}
}
