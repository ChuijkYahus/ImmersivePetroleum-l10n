package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.client.render.RenderUtils;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.wooden.AutoLubricatorBlock;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashSet;

public class LubricatorGhostRenderer{
	
	private final HashSet<BlockPos> previousMasters = new HashSet<>(256);
	private final RenderType renderType = RenderType.translucent();
	private final Minecraft minecraft;
	public LubricatorGhostRenderer(Minecraft minecraft){
		this.minecraft = minecraft;
	}
	
	@SuppressWarnings("unchecked")
	public void render(PoseStack matrix){
		if(this.minecraft.player == null)
			return;
		
		final ItemStack mainItem = this.minecraft.player.getMainHandItem();
		final ItemStack secondItem = this.minecraft.player.getOffhandItem();
		
		boolean main = (!mainItem.isEmpty()) && mainItem.getItem() == IPContent.Blocks.AUTO_LUBRICATOR.get().asItem();
		boolean off = (!secondItem.isEmpty()) && secondItem.getItem() == IPContent.Blocks.AUTO_LUBRICATOR.get().asItem();
		
		if(main || off){
			matrix.pushPose();
			{
				BlockRenderDispatcher blockDispatcher = this.minecraft.getBlockRenderer();
				MultiBufferSource.BufferSource buffer = RenderUtils.immediate();
				
				// Anti-Jiggle when moving
				Vec3 renderView = MCUtil.getGameRenderer().getMainCamera().getPosition();
				matrix.translate(-renderView.x, -renderView.y, -renderView.z);
				
				final Level level = this.minecraft.player.level();
				final BlockPos base = this.minecraft.player.blockPosition();
				int x, y, z;
				int range = 16;
				for(x = -range;x <= range;x++){
					for(z = -range;z <= range;z++){
						for(y = -range;y <= range;y++){
							BlockPos pos = base.offset(x, y, z);
							BlockEntity te = level.getBlockEntity(pos);
							
							if(te instanceof IMultiblockBE<?> multiblockBE){
								IMultiblockBEHelperMaster<?> masterHelper = Utils.getMultiblockMasterHelper(level, multiblockBE.getHelper());
								
								if(masterHelper == null)
									continue;
								
								BlockPos masterPos = masterHelper.getContext().getLevel().toAbsolute(masterHelper.getMultiblock().masterPosInMB());
								if(this.previousMasters.contains(masterPos))
									continue;
								
								LubricatedHandler.ILubricationHandler handler = LubricatedHandler.getHandlerForTile(masterHelper);
								if(handler != null){
									LubricatedHandler.ILubricationHandler.GhostInfo ghost = handler.getGhostBlockPosition(level, masterHelper);
									
									if(ghost != null){
										BlockState targetState = level.getBlockState(ghost.position());
										
										if(targetState.is(BlockTags.REPLACEABLE) && level.getBlockState(ghost.position().above()).is(BlockTags.REPLACEABLE)){
											this.previousMasters.add(masterPos);
											
											VertexConsumer vBuilder = buffer.getBuffer(this.renderType);
											
											matrix.pushPose();
											{
												matrix.translate(ghost.position().getX(), ghost.position().getY(), ghost.position().getZ());
												
												BlockState state = IPContent.Blocks.AUTO_LUBRICATOR.get().defaultBlockState().setValue(AutoLubricatorBlock.FACING, ghost.facing());
												BakedModel model = blockDispatcher.getBlockModel(state);
												blockDispatcher.getModelRenderer().renderModel(matrix.last(), vBuilder, null, model, 1.0F, 1.0F, 1.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, this.renderType);
												
											}
											matrix.popPose();
											
											buffer.endBatch();
										}
									}
								}
							}
						}
					}
				}
			}
			matrix.popPose();
		}
		
		if(!this.previousMasters.isEmpty())
			this.previousMasters.clear();
	}
}
