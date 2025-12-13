package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.IPShaders;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockDistillationTowerRenderer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class IPRenderTypes extends RenderStateShard{
	static final ResourceLocation activeTexture = ResourceUtils.ip("textures/multiblock/distillation_tower_active_overlay.png");
	static final ResourceLocation oilTankTexture = ResourceUtils.ip("textures/multiblock/oiltank.png");
	
	/**
	 * Intended to only be used by {@link MultiblockDistillationTowerRenderer}
	 */
	public static final RenderType DISTILLATION_TOWER_ACTIVE;
	public static final RenderType OIL_TANK;
	public static final RenderType TRANSLUCENT_LINE;
	public static final RenderType TRANSLUCENT_POSITION_COLOR;
	public static final RenderType ISLAND_DEBUGGING_POSITION_COLOR;
	/**
	 * Used by the Projector
	 */
	public static final RenderType PROJECTION;
	
	/// ** There is no right or wrong here! Just, play around.. NO PRESSURE!!!! You have aaaaall the time in the world! */
	// public static final RenderType EXPERIMENTAL_RENDER_TYPE;
	
	static final RenderStateShard.TextureStateShard TEXTURE_ACTIVE_TOWER = new RenderStateShard.TextureStateShard(activeTexture, false, false);
	static final RenderStateShard.TextureStateShard TEXTURE_OIL_TANK = new RenderStateShard.TextureStateShard(oilTankTexture, false, false);
	static final RenderStateShard.LightmapStateShard LIGHTMAP_ENABLED = new RenderStateShard.LightmapStateShard(true);
	static final RenderStateShard.OverlayStateShard OVERLAY_ENABLED = new RenderStateShard.OverlayStateShard(true);
	static final RenderStateShard.OverlayStateShard OVERLAY_DISABLED = new RenderStateShard.OverlayStateShard(false);
	static final RenderStateShard.DepthTestStateShard DEPTH_ALWAYS = new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS);
	static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, RenderSystem::disableBlend);
	static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("no_transparency", RenderSystem::disableBlend, () -> {
	});
	
	static final RenderStateShard.ShaderStateShard PROJECTION_SHADER = new RenderStateShard.ShaderStateShard(IPShaders::getProjectionStaticShader);
	static final RenderStateShard.ShaderStateShard LINE_SHADER = new RenderStateShard.ShaderStateShard(IPShaders::getTranslucentLineShader);
	static final RenderStateShard.ShaderStateShard TRANSLUCENT_SHADER = new RenderStateShard.ShaderStateShard(IPShaders::getTranslucentShader);
	static final RenderStateShard.ShaderStateShard TRANSLUCENT_POSTION_COLOR_SHADER = new RenderStateShard.ShaderStateShard(IPShaders::getTranslucentPostionColorShader);
	
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
				typeName("rendertype_projection"),
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				RenderType.BIG_BUFFER_SIZE,
				true,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(PROJECTION_SHADER)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.setLightmapState(LIGHTMAP)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setOutputState(TRANSLUCENT_TARGET)
					.setDepthTestState(DEPTH_ALWAYS)
					.createCompositeState(false)
		);
		
		// TODO fix. Lines are weird in 1.17+
		TRANSLUCENT_LINE = RenderType.create(
				typeName("rendertype_line"),
				DefaultVertexFormat.POSITION_COLOR,
				VertexFormat.Mode.LINES,
				RenderType.TRANSIENT_BUFFER_SIZE,
				false,
				false,
				RenderType.CompositeState.builder()
					.setShaderState(LINE_SHADER)
					.setLineState(new LineStateShard(OptionalDouble.of(3.5)))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(DEPTH_ALWAYS)
					.setCullState(NO_CULL)
					.createCompositeState(false)
		);
		
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LINES_SHADER)
			.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.5)))
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false);
		
		DISTILLATION_TOWER_ACTIVE = RenderType.create(
				typeName("distillation_tower_active_overlay"),
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				RenderType.TRANSIENT_BUFFER_SIZE,
				true,
				false,
				RenderType.CompositeState.builder()
					.setShaderState(TRANSLUCENT_SHADER)
					.setTextureState(TEXTURE_ACTIVE_TOWER)
					.setLightmapState(LIGHTMAP_ENABLED)
					.setOverlayState(OVERLAY_DISABLED)
					.setTransparencyState(IPRenderTypes.TRANSLUCENT_TRANSPARENCY)
					.createCompositeState(false)
		);
		
		OIL_TANK = RenderType.create(
				typeName("oil_tank"),
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				RenderType.TRANSIENT_BUFFER_SIZE,
				true,
				false,
				RenderType.CompositeState.builder()
					.setShaderState(TRANSLUCENT_SHADER)
					.setTextureState(TEXTURE_OIL_TANK)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP_ENABLED)
					.setOverlayState(OVERLAY_DISABLED)
					.createCompositeState(false)
		);
		
		TRANSLUCENT_POSITION_COLOR = RenderType.create(
				typeName("rendertype_translucent"),
				DefaultVertexFormat.POSITION_COLOR,
				VertexFormat.Mode.QUADS,
				RenderType.SMALL_BUFFER_SIZE,
				false,
				false,
				RenderType.CompositeState.builder()
					.setShaderState(TRANSLUCENT_POSTION_COLOR_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.createCompositeState(false)
		);
		
		ISLAND_DEBUGGING_POSITION_COLOR = RenderType.create(
				typeName("translucent_pos_color2"),
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
	
	private static String typeName(String str){
		return ImmersivePetroleum.MODID + ":" + str;
	}
	
	private IPRenderTypes(String pName, Runnable pSetupState, Runnable pClearState){
		super(pName, pSetupState, pClearState);
		throw new UnsupportedOperationException();
	}
	
	/** Same as vanilla, just without an overlay */
	public static RenderType getEntitySolid(ResourceLocation locationIn){
		RenderType.CompositeState renderState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("entity_solid", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, renderState);
	}
	
	// TODO this is very very broken in 1.17+
	public static MultiBufferSource disableLighting(MultiBufferSource in){
		return type -> {
			//RenderSystem.disableLighting();
			RenderType rt = new RenderType(
					ImmersivePetroleum.MODID + ":" + type + "_no_lighting",
					type.format(),
					type.mode(),
					type.bufferSize(),
					type.affectsCrumbling(),
					false,
					type::setupRenderState, type::clearRenderState){};
			return in.getBuffer(rt);
		};
	}
}
