package flaxbeard.immersivepetroleum.client.gui.displays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class Display{
	protected static final ResourceLocation PARTS = ResourceUtils.ip("parts");
	
	protected final DisplayBounds bounds;
	protected final int depth;
	public Display(DisplayBounds bounds, int depth){
		this.bounds = bounds;
		this.depth = depth;
	}
	
	public final void fillTooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, @Nonnull List<Component> tooltip){
		if(this.bounds.contains(mouseX, mouseY)){
			tooltip(guiGraphics, mouseX, mouseY, tooltip);
		}
	}
	
	protected abstract void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip);
	
	public abstract void draw(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY);
	
	protected void blitSprite(@Nonnull GuiGraphics guiGraphics, TexCoords texCoords, int depth){
		TextureAtlasSprite spriteAtlas = MCUtil.getGuiSpriteManager().getSprite(PARTS);
		
		final float u0_ = spriteAtlas.getU0();
		final float v0_ = spriteAtlas.getV0();
		
		float uSize = spriteAtlas.getU1() - u0_;
		float vSize = spriteAtlas.getV1() - v0_;
		
		float u0 = u0_ + uSize * texCoords.u0();
		float v0 = v0_ + vSize * texCoords.v0();
		float u1 = u0_ + uSize * texCoords.u1();
		float v1 = v0_ + vSize * texCoords.v1();
		
		int x0 = this.bounds.x0();
		int x1 = this.bounds.x1();
		int y0 = this.bounds.y0();
		int y1 = this.bounds.y1();
		
		blitSprite(guiGraphics, spriteAtlas, x0, x1, y0, y1, depth, u0, v0, u1, v1);
	}
	
	protected final void blitSprite(@Nonnull GuiGraphics guiGraphics, TextureAtlasSprite spriteAtlas, int x0, int x1, int y0, int y1, int depth, float u0, float v0, float u1, float v1){
		RenderSystem.setShaderTexture(0, spriteAtlas.atlasLocation());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.addVertex(matrix4f, x0, y0, depth).setUv(u0, v0);
		bufferbuilder.addVertex(matrix4f, x0, y1, depth).setUv(u0, v1);
		bufferbuilder.addVertex(matrix4f, x1, y1, depth).setUv(u1, v1);
		bufferbuilder.addVertex(matrix4f, x1, y0, depth).setUv(u1, v0);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
	}
	
	protected void drawHighlightQuad(@Nonnull GuiGraphics guiGraphics, int x0, int x1, int y0, int y1, int depth, int color){
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		VertexConsumer buffer = guiGraphics.bufferSource().getBuffer(RenderType.guiOverlay());
		buffer.addVertex(matrix4f, x0, y0, depth).setColor(r, g, b, a);
		buffer.addVertex(matrix4f, x0, y1, depth).setColor(r, g, b, a);
		buffer.addVertex(matrix4f, x1, y1, depth).setColor(r, g, b, a);
		buffer.addVertex(matrix4f, x1, y0, depth).setColor(r, g, b, a);
	}
	
	protected void drawQuad(@Nonnull GuiGraphics guiGraphics, int x0, int x1, int y0, int y1, int depth, float u0, float v0, float u1, float v1, int color){
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		VertexConsumer buffer = guiGraphics.bufferSource().getBuffer(RenderType.TRANSLUCENT);
		buffer.addVertex(matrix4f, x0, y0, depth).setUv(u0, v0).setColor(r, g, b, a).setNormal(1, 1, 1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
		buffer.addVertex(matrix4f, x0, y1, depth).setUv(u0, v1).setColor(r, g, b, a).setNormal(1, 1, 1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
		buffer.addVertex(matrix4f, x1, y1, depth).setUv(u1, v1).setColor(r, g, b, a).setNormal(1, 1, 1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
		buffer.addVertex(matrix4f, x1, y0, depth).setUv(u1, v0).setColor(r, g, b, a).setNormal(1, 1, 1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
	}
}
