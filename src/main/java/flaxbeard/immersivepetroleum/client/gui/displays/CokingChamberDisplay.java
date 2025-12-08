package flaxbeard.immersivepetroleum.client.gui.displays;

import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.coker.CokingChamber;
import flaxbeard.immersivepetroleum.common.util.inventory.FluidTankFiltered;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.util.List;

public class CokingChamberDisplay extends Display{
	public static CokingChamberDisplay create(int x, int y, @Nonnull CokingChamber chamber){
		return new CokingChamberDisplay(x, y, new TexCoords(114, 13, 6, 38), new TexCoords(120, 13, 6, 38), chamber);
	}
	
	private final TexCoords raw;
	private final TexCoords processed;
	private final CokingChamber chamber;
	
	public CokingChamberDisplay(int x, int y, TexCoords raw, TexCoords processed, @Nonnull CokingChamber chamber){
		super(DisplayBounds.of(x, y, raw.w(), raw.h()), 1);
		this.raw = raw;
		this.processed = processed;
		this.chamber = chamber;
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		FluidTankFiltered tank = this.chamber.getTank();
		FluidDisplay.fluidTooltip(tank.getFluid(), tank.getCapacity(), tooltip);
	}
	
	@Override
	public void draw(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
		if(this.chamber.getTotalAmount() == 0)
			return;
		
		final float capacity = this.chamber.getCapacity();
		float outputScale = this.chamber.getOutputAmount() / capacity;
		float capacityScale = this.chamber.getTotalAmount() / capacity;
		
		drawBar(guiGraphics, this.raw, this.bounds, capacityScale, 0);
		drawBar(guiGraphics, this.processed, this.bounds, outputScale, 1);
		drawFluid(guiGraphics, this.bounds, this.chamber.getTank(), 2);
	}
	
	private void drawBar(@Nonnull GuiGraphics guiGraphics, TexCoords texCoords, DisplayBounds bounds, float scale, int depth){
		if(scale <= 0.0F)
			return; // No need to draw when empty anyway
		
		TextureAtlasSprite spriteAtlas = MCUtil.getGuiSpriteManager().getSprite(PARTS);
		float spriteW = spriteAtlas.contents().width();
		float spriteH = spriteAtlas.contents().height();
		
		final float u0_ = spriteAtlas.getU0();
		final float v0_ = spriteAtlas.getV0();
		
		float uSize = spriteAtlas.getU1() - u0_;
		float vSize = spriteAtlas.getV1() - v0_;
		
		scale = texCoords.h() * (1F - scale);
		
		float u0 = u0_ + uSize * (texCoords.x() / spriteW);
		float v0 = v0_ + vSize * ((int) (texCoords.y() + scale) / spriteH);
		float u1 = u0_ + uSize * ((texCoords.x() + texCoords.w()) / spriteW);
		float v1 = v0_ + vSize * ((texCoords.y() + texCoords.h()) / spriteH);
		
		int x0 = bounds.x0();
		int x1 = bounds.x1();
		int y0 = bounds.y0() + (int) scale;
		int y1 = bounds.y1();
		
		blitSprite(guiGraphics, spriteAtlas, x0, x1, y0, y1, depth, u0, v0, u1, v1);
	}
	
	private void drawFluid(@Nonnull GuiGraphics guiGraphics, DisplayBounds bounds, IFluidTank tank, int depth){
		FluidStack fluid = tank.getFluid();
		if(fluid.isEmpty())
			return;
		
		IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid.getFluid());
		TextureAtlasSprite spriteAtlas = MCUtil.getBlockSprite(props.getStillTexture(fluid));
		int spriteW = Math.min(spriteAtlas.contents().width(), bounds.width());
		int spriteH = spriteAtlas.contents().height();
		
		if(spriteW > 0 && spriteH > 0){
			int tintColor = props.getTintColor(fluid);
			final int pixelHeight = (int) (bounds.height() * (fluid.getAmount() / (float) tank.getCapacity()));
			final int count = pixelHeight / spriteH;
			final int left = pixelHeight % spriteH;
			
			float u0 = spriteAtlas.getU0();
			float v0 = spriteAtlas.getV0();
			float u1 = spriteAtlas.getU(0.375F);
			float v1 = spriteAtlas.getV1();
			
			int i = 0;
			for(;i < count;i++){
				int h = spriteH * i;
				
				int x0 = bounds.x0();
				int y0 = bounds.y0() + bounds.height() - spriteH - h;
				int x1 = bounds.x0() + spriteW;
				int y1 = bounds.y0() + bounds.height() - h;
				
				drawQuad(guiGraphics, x0, x1, y0, y1, depth, u0, v0, u1, v1, tintColor);
			}
			if(left > 0){
				float scale = 1F - (left / (float) spriteH);
				
				int h = spriteH * i;
				v0 = spriteAtlas.getV(scale);
				
				int x0 = bounds.x0();
				int y0 = bounds.y0() + bounds.height() - left - h;
				int x1 = bounds.x0() + spriteW;
				int y1 = bounds.y0() + bounds.height() - h;
				
				drawQuad(guiGraphics, x0, x1, y0, y1, depth, u0, v0, u1, v1, tintColor);
			}
		}
	}
}
