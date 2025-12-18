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
		
		addSingle(blockAtlas, "multiblock/cokerunit");
		addSingle(blockAtlas, "multiblock/derrick");
		addSingle(blockAtlas, "multiblock/distillation_tower");
		addSingle(blockAtlas, "multiblock/hydrotreater");
		addSingle(blockAtlas, "multiblock/oiltank");
		addSingle(blockAtlas, "multiblock/pumpjack_base");
		
		addSingle(blockAtlas, "multiblock/overlay/distillation_tower_active");
		addSingle(blockAtlas, "multiblock/overlay/hydrotreater_active");
		
		addSingle(blockAtlas, "models/lubricator");
		addSingle(blockAtlas, "models/pumpjack_armature");
		
		addSingle(blockAtlas, "projectors/projector");
	}
	
	private void addSingle(SourceList atlas, String path){
		atlas.addSource(new SingleFile(ResourceUtils.ip(path), Optional.empty()));
	}
}
