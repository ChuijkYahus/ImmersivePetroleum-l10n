package flaxbeard.immersivepetroleum.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class PumpjackMultiblock extends IPTemplateMultiblock{
	public static final PumpjackMultiblock INSTANCE = new PumpjackMultiblock();
	
	private PumpjackMultiblock(){
		super(ResourceUtils.ip("multiblocks/pumpjack"), new BlockPos(1, 0, 0), new BlockPos(1, 1, 4), new BlockPos(3, 4, 6), IPContent.Multiblock.PUMPJACK);
	}
	
	@Override
	public float getManualScale(){
		return 12;
	}
	
	@Override
	public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
		consumer.accept(new IPMultiblockProperties(this, 1.5, 0.5, 0.5));
	}
	
	/*
	public class PumpjackClientData extends IPClientMultiblockProperties{
		private PumpjackTileEntity te;
		private List<BakedQuad> list;
		public PumpjackClientData(){
			super(PumpjackMultiblock.INSTANCE, 0, 0, 0);
		}
		
		@Override
		protected boolean usingCustomRendering(){
			return true;
		}
		
		@Override
		public boolean canRenderFormedStructure(){
			return true;
		}
		
		final Quaternionf rot = new Quaternionf(new AxisAngle4f(90F, new Vector3f(0F, 1F, 0F)));
		@Override
		public void renderCustomFormedStructure(PoseStack matrix, MultiBufferSource buffer){
			if(this.te == null){
				this.te = new PumpjackTileEntity(IPTileTypes.PUMP.master(), BlockPos.ZERO, IPContent.Multiblock.PUMPJACK.get().defaultBlockState());
			}
			
			if(this.list == null){
				BlockState state = this.te.getBlockState();
				BakedModel model = MCUtil.getBlockRenderer().getBlockModel(state);
				this.list = model.getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
			}
			
			if(this.list != null && this.list.size() > 0){
				Level world = MCUtil.getLevel();
				if(world != null){
					matrix.pushPose();
					{
						matrix.translate(1, 0, 0);
						
						RenderUtils.renderModelTESRFast(this.list, buffer.getBuffer(RenderType.solid()), matrix, 0xF000F0, OverlayTexture.NO_OVERLAY);
						
						matrix.pushPose();
						{
							matrix.mulPose(this.rot);
							matrix.translate(-2, -1, -1);
							ImmersivePetroleum.proxy.renderTile(this.te, buffer.getBuffer(RenderType.solid()), matrix, buffer);
						}
						matrix.popPose();
					}
					matrix.popPose();
				}
			}
		}
	}
	*/
}
