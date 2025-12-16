package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class OilTankMultiblock extends IPTemplateMultiblock{
	public static final OilTankMultiblock INSTANCE = new OilTankMultiblock();
	
	public OilTankMultiblock(){
		super(ResourceUtils.ip("multiblocks/oiltank"),
			new BlockPos(2, 0, 3), new BlockPos(2, 1, 5), new BlockPos(5, 4, 6),
			IPContent.Multiblock.OILTANK);
	}
	
	@Override
	public float getManualScale(){
		return 12;
	}
	
	@Override
	public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
		consumer.accept(new IPMultiblockProperties(this, 2.5, 0.5, 3.5));
	}
}
