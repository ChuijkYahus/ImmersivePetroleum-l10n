package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.IPShaders;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockHydrotreaterRenderer;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockOilTankRenderer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class IPRenderTypes{
	static final TextureStateShard TEXTURE_ACTIVE_TOWER = texture("textures/multiblock/overlay/distillation_tower_active.png");
	static final TextureStateShard TEXTURE_ACTIVE_HYDRO = texture("textures/multiblock/overlay/hydrotreater_active.png");
	static final TextureStateShard TEXTURE_OIL_TANK = texture("textures/multiblock/oiltank.png");
	
	static final LightmapStateShard LIGHTMAP_ENABLED = new LightmapStateShard(true);
	static final OverlayStateShard OVERLAY_ENABLED = new OverlayStateShard(true);
	static final OverlayStateShard OVERLAY_DISABLED = new OverlayStateShard(false);
	static final DepthTestStateShard DEPTH_ALWAYS = new DepthTestStateShard("always", GL11.GL_ALWAYS);
	static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new TransparencyStateShard("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, RenderSystem::disableBlend);
	static final TransparencyStateShard NO_TRANSPARENCY = new TransparencyStateShard("no_transparency", RenderSystem::disableBlend, () -> {
	});
	
	static final ShaderStateShard PROJECTION_SHADER = new ShaderStateShard(IPShaders::getProjectionStaticShader);
	static final ShaderStateShard LINE_SHADER = new ShaderStateShard(IPShaders::getTranslucentLineShader);
	static final ShaderStateShard TRANSLUCENT_SHADER = new ShaderStateShard(IPShaders::getTranslucentShader);
	static final ShaderStateShard TRANSLUCENT_POSTION_COLOR_SHADER = new ShaderStateShard(IPShaders::getTranslucentPostionColorShader);
	
	/// ** There is no right or wrong here! Just, play around.. NO PRESSURE!!!! You have aaaaall the time in the world! */
	//public static final RenderType EXPERIMENTAL_RENDER_TYPE;
	
	/** Used by the Projector */
	public static final RenderType PROJECTION;
	
	/** Only be used by {@link MultiblockDistillationTowerRenderer} */
	public static final RenderType DISTILLATION_TOWER_ACTIVE_OVERLAY = machineExtra("distillation_tower_active_overlay", TEXTURE_ACTIVE_TOWER);
	
	/** Only be used by {@link MultiblockHydrotreaterRenderer} */
	public static final RenderType HYDROTREATER_ACTIVE_OVERLAY = machineExtra("hydrotreater_active_overlay", TEXTURE_ACTIVE_HYDRO);
	
	/** Only be used by {@link MultiblockOilTankRenderer} */
	public static final RenderType OIL_TANK = machineExtra("oil_tank", TEXTURE_OIL_TANK);
	
	public static final RenderType TRANSLUCENT_LINE;
	public static final RenderType TRANSLUCENT_POSITION_COLOR;
	public static final RenderType RESERVOIR_DEBUGGING_POSITION_COLOR;
	
	static{
		/*
		EXPERIMENTAL_RENDER_TYPE = RenderType.create(
				typeName("experimental"),
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				RenderType.BIG_BUFFER_SIZE,
				true,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(PROJECTION_SHADER)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setOutputState(TRANSLUCENT_TARGET)
					.setDepthTestState(DEPTH_ALWAYS)
					.setCullState(CULL)
					.createCompositeState(false)
		);
		*/
		
		PROJECTION = RenderType.create(
				typeName("projection"),
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				RenderType.BIG_BUFFER_SIZE,
				true,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(PROJECTION_SHADER)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.setLightmapState(LIGHTMAP)
					.setTransparencyState(IPRenderTypes.TRANSLUCENT_TRANSPARENCY)
					.setOutputState(TRANSLUCENT_TARGET)
					.setDepthTestState(DEPTH_ALWAYS)
					.createCompositeState(false)
		);
		
		// TODO fix. Lines are weird in 1.17+
		TRANSLUCENT_LINE = RenderType.create(
				typeName("translucent_line"),
				DefaultVertexFormat.POSITION_COLOR,
				VertexFormat.Mode.LINES,
				RenderType.TRANSIENT_BUFFER_SIZE,
				false,
				false,
				RenderType.CompositeState.builder()
					.setShaderState(LINE_SHADER)
					.setLineState(new LineStateShard(OptionalDouble.of(3.5)))
					.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(DEPTH_ALWAYS)
					.setCullState(NO_CULL)
					.createCompositeState(false)
		);
		
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LINES_SHADER)
			.setLineState(new LineStateShard(OptionalDouble.of(3.5)))
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false);
		
		TRANSLUCENT_POSITION_COLOR = RenderType.create(
				typeName("translucent_position_color"),
				DefaultVertexFormat.POSITION_COLOR,
				VertexFormat.Mode.QUADS,
				RenderType.SMALL_BUFFER_SIZE,
				false,
				false,
				RenderType.CompositeState.builder()
					.setShaderState(TRANSLUCENT_POSTION_COLOR_SHADER)
					.setTransparencyState(IPRenderTypes.TRANSLUCENT_TRANSPARENCY)
					.createCompositeState(false)
		);
		
		RESERVOIR_DEBUGGING_POSITION_COLOR = RenderType.create(
				typeName("island_debugging_position_color"),
				DefaultVertexFormat.POSITION_COLOR,
				VertexFormat.Mode.QUADS,
				RenderType.TRANSIENT_BUFFER_SIZE,
				false,
				false,
				RenderType.CompositeState.builder()
					.setCullState(NO_CULL)
					.createCompositeState(false)
		);
	}
	
	private static RenderType machineExtra(String name, TextureStateShard textureStateShard){
		//@formatter:off
		return RenderType.create(
			typeName(name),
			DefaultVertexFormat.BLOCK,
			VertexFormat.Mode.QUADS,
			RenderType.TRANSIENT_BUFFER_SIZE,
			true,
			false,
			RenderType.CompositeState.builder()
				.setShaderState(TRANSLUCENT_SHADER)
				.setTextureState(textureStateShard)
				.setTransparencyState(IPRenderTypes.TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP_ENABLED)
				.setOverlayState(OVERLAY_DISABLED)
				.createCompositeState(false)
		);
		//@formatter:on
	}
	
	/** Same as vanilla, just without an overlay */
	public static RenderType getEntitySolid(ResourceLocation locationIn){
		RenderType.CompositeState renderState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(new TextureStateShard(locationIn, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("entity_solid", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, renderState);
	}
	
	static final Map<String, RenderType> TYPE_CACHE = new HashMap<>();
	// TODO this is very very broken in 1.17+
	public static MultiBufferSource disableLighting(MultiBufferSource in){
		return type -> {
			RenderType rt = TYPE_CACHE.computeIfAbsent(typeName(type + "_no_lighting"), name -> {
				//RenderSystem.disableLighting();
				return new RenderType(
					name,
					type.format(),
					type.mode(),
					type.bufferSize(),
					type.affectsCrumbling(),
					false,
					type::setupRenderState,
					type::clearRenderState
				){};
			});
			
			return in.getBuffer(rt);
		};
	}
	
	private static String typeName(String str){
		return ImmersivePetroleum.MODID + ":" + str;
	}
	
	private static TextureStateShard texture(String path){
		return new TextureStateShard(ResourceUtils.ip(path), false, false);
	}
}
