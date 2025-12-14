package flaxbeard.immersivepetroleum.client.render.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import flaxbeard.immersivepetroleum.client.render.IPBlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class IPMultiblockRenderer<State extends IMultiblockState> extends IPBlockEntityRenderer<MultiblockBlockEntityMaster<State>>{
	
	private BlockPos mbSize = null;
	private final Supplier<TemplateMultiblock> template;
	public IPMultiblockRenderer(Supplier<TemplateMultiblock> template){
		this.template = template;
	}
	
	@Nonnull
	@Override
	public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<State> mb){
		IMultiblockBEHelperMaster<?> helper = mb.getHelper();
		
		if(!helper.getPositionInMB().equals(helper.getMultiblock().masterPosInMB()))
			return super.getRenderBoundingBox(mb);
		
		if(this.mbSize == null)
			this.mbSize = new BlockPos(this.template.get().getSize(helper.getContext().getLevel().getRawLevel()));
		
		IMultiblockLevel mbLevel = helper.getContext().getLevel();
		BlockPos min = mbLevel.toAbsolute(BlockPos.ZERO);
		BlockPos max = mbLevel.toAbsolute(this.mbSize);
		
		return bounds(min, max);
	}
	
	private static AABB bounds(BlockPos min, BlockPos max){
		return new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
	}
}
