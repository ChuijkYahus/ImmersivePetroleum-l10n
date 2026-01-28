package flaxbeard.immersivepetroleum.client.render.debugging;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.Reservoir;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirBoundingBox;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.client.render.RenderUtils;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionData;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.RegionPos;
import flaxbeard.immersivepetroleum.common.datastorage.reservoir.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DebugReservoirRender{
	public static void reservoirDebuggingRender(RenderLevelStageEvent event){
		if(ReservoirHandler.getGenerator() == null){
			return;
		}
		
		Player player = MCUtil.getPlayer();
		
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		
		if((main != ItemStack.EMPTY && main.getItem() == IPContent.DEBUGITEM.get()) || (off != ItemStack.EMPTY && off.getItem() == IPContent.DEBUGITEM.get())){
			DebugItem.Mode mode = null;
			if(main != ItemStack.EMPTY){
				mode = DebugItem.getMode(main);
			}
			if(off != ItemStack.EMPTY){
				mode = DebugItem.getMode(off);
			}
			
			if(mode.render == DebugItem.Render.NONE)
				return;
			
			PoseStack matrix = event.getPoseStack();
			Level world = player.getCommandSenderWorld();
			BlockPos playerPos = player.blockPosition();
			
			matrix.pushPose();
			{
				MultiBufferSource.BufferSource buffer = RenderUtils.immediate();
				
				// Anti-Jiggle when moving
				Vec3 renderView = MCUtil.getGameRenderer().getMainCamera().getPosition();
				matrix.translate(-renderView.x, -renderView.y, -renderView.z);
				
				if(mode.render == DebugItem.Render.RESERVOIR_HEATMAP || mode.render == DebugItem.Render.RESERVOIR_ALL)
					renderHeatMap(matrix, buffer, playerPos, world);
				
				if(mode.render == DebugItem.Render.RESERVOIR_POLYGONS || mode.render == DebugItem.Render.RESERVOIR_ALL)
					renderReservoirPolygons(matrix, buffer, player, playerPos);
				
				buffer.endBatch();
			}
			matrix.popPose();
		}
	}
	
	private static void renderHeatMap(PoseStack matrix, MultiBufferSource.BufferSource buffer, BlockPos playerPos, Level world){
		matrix.pushPose();
		{
			int radius = 16;
			for(int i = -radius;i <= radius;i++){
				for(int j = -radius;j <= radius;j++){
					ChunkPos cPos = new ChunkPos(playerPos.offset(16 * i, 0, 16 * j));
					int chunkX = cPos.getMinBlockX();
					int chunkZ = cPos.getMinBlockZ();
					
					for(int cX = 0;cX < 16;cX++){
						for(int cZ = 0;cZ < 16;cZ++){
							int x = chunkX + cX;
							int z = chunkZ + cZ;
							
							matrix.pushPose();
							{
								double n = ReservoirHandler.getValueOf(world, x, z);
								if(n > -1){
									int c = (int) Math.round(9 * n);
									
									DyeColor color = switch(c){
										case 1 -> DyeColor.BLUE;
										case 2 -> DyeColor.CYAN;
										case 3 -> DyeColor.GREEN;
										case 4 -> DyeColor.LIME;
										case 5 -> DyeColor.YELLOW;
										case 6 -> DyeColor.ORANGE;
										case 7 -> DyeColor.RED;
										default -> c > 7 ? DyeColor.WHITE : DyeColor.BLACK;
									};
									
									int r = (color.getTextColor() & 0xFF0000) >> 16;
									int g = (color.getTextColor() & 0x00FF00) >> 8;
									int b = (color.getTextColor() & 0x0000FF);
									
									int height = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).getY();
									for(;height > 0;height--){
										if(world.getBlockState(new BlockPos(x, height - 1, z)).isSolidRender(world, new BlockPos(x, height - 1, z))){
											break;
										}
									}
									
									matrix.translate(x, Math.max(63, height) + 0.0625, z);
									
									Matrix4f mat = matrix.last().pose();
									
									VertexConsumer builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_POSITION_COLOR);
									builder.addVertex(mat, 0, 0, 0).setColor(r, g, b, 127);
									builder.addVertex(mat, 0, 0, 1).setColor(r, g, b, 127);
									builder.addVertex(mat, 1, 0, 1).setColor(r, g, b, 127);
									builder.addVertex(mat, 1, 0, 0).setColor(r, g, b, 127);
								}
							}
							matrix.popPose();
						}
					}
				}
			}
		}
		matrix.popPose();
	}
	
	private static RegionPos pLocalLast = null;
	private static final Set<Reservoir> reservoirRenderCache = new HashSet<>();
	private static void renderReservoirPolygons(PoseStack matrix, MultiBufferSource.BufferSource buffer, Player player, BlockPos playerPos){
		matrix.pushPose();
		{
			ReservoirRegionDataStorage storage = ReservoirRegionDataStorage.get();
			final ResourceKey<Level> dimKey = player.getCommandSenderWorld().dimension();
			
			RegionPos pLocal = new RegionPos(playerPos);
			if(!pLocal.equals(pLocalLast)){
				pLocalLast = pLocal;
				reservoirRenderCache.clear();
				
				ImmersivePetroleum.log.debug("Refreshing ReservoirCache for Rendering.");
				
				RegionPos p0 = new RegionPos(playerPos, 1, -1);
				RegionPos p1 = new RegionPos(playerPos, 1, 1);
				RegionPos p2 = new RegionPos(playerPos, -1, -1);
				RegionPos p3 = new RegionPos(playerPos, -1, 1);
				
				RegionData[] array = {
					storage.getRegionData(pLocal),
					storage.getRegionData(p0),
					storage.getRegionData(p1),
					storage.getRegionData(p2),
					storage.getRegionData(p3)
				};
				for(RegionData rd: array){
					if(rd != null){
						Multimap<ResourceKey<Level>, Reservoir> m = rd.getReservoirList();
						synchronized(m){
							reservoirRenderCache.addAll(m.get(dimKey));
						}
					}
				}
			}
			
			if(!reservoirRenderCache.isEmpty()){
				float y = 128.0625F;
				int radius = 256;
				radius = radius * radius + radius * radius;
				for(Reservoir reservoir: reservoirRenderCache){
					BlockPos center = reservoir.getBoundingBox().getCenter();
					
					if(center.distSqr(playerPos) <= radius){
						ReservoirBoundingBox bounds = reservoir.getBoundingBox();
						renderReservoirBoundingBox(matrix, buffer, bounds, y);
						
						if(reservoir.getPolygon() != null && !reservoir.getPolygon().isEmpty()){
							List<ColumnPos> poly = reservoir.getPolygon().getPolygonList();
							
							renderPolygon(matrix, buffer, poly, y, center);
						}
					}
				}
			}
		}
		matrix.popPose();
	}
	
	private static void renderReservoirBoundingBox(PoseStack matrix, MultiBufferSource.BufferSource buffer, ReservoirBoundingBox bounds, float y){
		matrix.pushPose();
		{
			float minX = bounds.xMin() + 0.5F;
			float minZ = bounds.zMin() + 0.5F;
			float maxX = bounds.xMax() + 0.5F;
			float maxZ = bounds.zMax() + 0.5F;
			
			VertexConsumer builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINE);
			
			PoseStack.Pose last = matrix.last();
			Matrix4f mat = last.pose();
			
			builder.addVertex(mat, minX, y, minZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, maxX, y, minZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, minX, y, maxZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, maxX, y, maxZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, minX, y, minZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, minX, y, maxZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, maxX, y, minZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
			builder.addVertex(mat, maxX, y, maxZ).setColor(255, 0, 255, 127).setNormal(last, 0, 1, 0);
		}
		matrix.popPose();
	}
	
	private static void renderPolygon(PoseStack matrix, MultiBufferSource.BufferSource buffer, List<ColumnPos> poly, float y, BlockPos center){
		VertexConsumer builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_LINE);
		
		matrix.pushPose();
		{
			PoseStack.Pose last = matrix.last();
			Matrix4f mat = last.pose();
			
			// Draw polygon as line
			int j = poly.size() - 1;
			for(int i = 0;i < poly.size();i++){
				ColumnPos a = poly.get(j);
				ColumnPos b = poly.get(i);
				float f = i / (float) poly.size();
				
				builder.addVertex(mat, a.x() + .5F, y, a.z() + .5F).setColor(f, 0.0F, 1 - f, 0.5F).setNormal(last, 0F, 1F, 0F);
				builder.addVertex(mat, b.x() + .5F, y, b.z() + .5F).setColor(f, 0.0F, 1 - f, 0.5F).setNormal(last, 0F, 1F, 0F);
				
				j = i;
			}
		}
		matrix.popPose();
		
		// Center Marker
		matrix.pushPose();
		{
			PoseStack.Pose last = matrix.last();
			Matrix4f mat = last.pose();
			
			// Y-Axis
			builder.addVertex(mat, center.getX() + .5F, 128F, center.getZ() + .5F).setColor(0.0F, 1.0F, 0.0F, 0.5F).setNormal(last, 0F, 1F, 0F);
			builder.addVertex(mat, center.getX() + .5F, 129F, center.getZ() + .5F).setColor(0.0F, 1.0F, 0.0F, 0.5F).setNormal(last, 0F, 1F, 0F);
			
			// X-Axis
			builder.addVertex(mat, center.getX(), 128.5F, center.getZ() + .5F).setColor(1.0F, 0.0F, 0.0F, 0.5F).setNormal(last, 0F, 1F, 0F);
			builder.addVertex(mat, center.getX() + 1, 128.5F, center.getZ() + .5F).setColor(1.0F, 0.0F, 0.0F, 0.5F).setNormal(last, 0F, 1F, 0F);
			
			// Z-Axis
			builder.addVertex(mat, center.getX() + .5F, 128.5F, center.getZ()).setColor(0.0F, 0.0F, 1.0F, 0.5F).setNormal(last, 0F, 1F, 0F);
			builder.addVertex(mat, center.getX() + .5F, 128.5F, center.getZ() + 1).setColor(0.0F, 0.0F, 1.0F, 0.5F).setNormal(last, 0F, 1F, 0F);
		}
		matrix.popPose();
	}
}
