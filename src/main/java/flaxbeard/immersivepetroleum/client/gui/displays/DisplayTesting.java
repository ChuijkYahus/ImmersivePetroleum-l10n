package flaxbeard.immersivepetroleum.client.gui.displays;

import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nonnull;
import java.util.Objects;

/** Probably will end up being destroyed */
public abstract class DisplayTesting extends Display{
	
	protected final @Nonnull TexCoords texCoords;
	
	protected DisplayTesting(int x, int y, @Nonnull TexCoords texCoords, int depth){
		super(DisplayBounds.of(x, y, texCoords.w(), texCoords.h()), depth);
		
		this.texCoords = Objects.requireNonNull(texCoords);
	}
	
	protected DisplayTesting(int x, int y, int width, int height, int depth, @Nonnull TexCoords texCoords){
		super(DisplayBounds.of(x, y, width, height), depth);
		
		this.texCoords = texCoords;
	}
	
	public void draw(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
		blitSprite(guiGraphics, this.depth);
	}
	
	protected void blitSprite(@Nonnull GuiGraphics guiGraphics, int depth){
		TextureAtlasSprite spriteAtlas = MCUtil.getGuiSpriteManager().getSprite(PARTS);
		
		blitSprite(guiGraphics, spriteAtlas, this.bounds.x0(), this.bounds.x1(), this.bounds.y0(), this.bounds.y1(), depth);
	}
	
	protected void blitSprite(@Nonnull GuiGraphics guiGraphics, TextureAtlasSprite spriteAtlas, int x0, int x1, int y0, int y1, int depth){
		final float u0_ = spriteAtlas.getU0();
		final float v0_ = spriteAtlas.getV0();
		
		float uSize = spriteAtlas.getU1() - u0_;
		float vSize = spriteAtlas.getV1() - v0_;
		
		float u0 = u0_ + uSize * this.texCoords.u0();
		float u1 = u0_ + uSize * this.texCoords.u1();
		float v0 = v0_ + vSize * this.texCoords.v0();
		float v1 = v0_ + vSize * this.texCoords.v1();
		
		blitSprite(guiGraphics, spriteAtlas, x0, x1, y0, y1, depth, u0, v0, u1, v1);
	}
}
