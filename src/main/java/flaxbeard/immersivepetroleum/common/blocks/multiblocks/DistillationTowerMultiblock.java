package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class DistillationTowerMultiblock extends IPTemplateMultiblock{
	public static final DistillationTowerMultiblock INSTANCE = new DistillationTowerMultiblock();
	
	private DistillationTowerMultiblock(){
		super(ResourceUtils.ip("multiblocks/distillationtower"),
			new BlockPos(2, 0, 2), new BlockPos(0, 1, 3), new BlockPos(4, 16, 4),
			IPContent.Multiblock.DISTILLATIONTOWER);
	}
	
	@Override
	public float getManualScale(){
		return 6;
	}
	
	@Override
	public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
		consumer.accept(new IPMultiblockProperties(this, 2.5, 0.5, 2.5));
	}
}
