package flaxbeard.immersivepetroleum.client.gui.displays;

import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;

public class EnergyDisplay extends Display{
	
	public static class Normal extends EnergyDisplay{
		public Normal(int x, int y, IEnergyStorage storage){
			super(x, y, new TexCoords(78, 1, 11, 50), new TexCoords(89, 3, 7, 46), storage);
		}
	}
	
	public static class Small extends EnergyDisplay{
		public Small(int x, int y, IEnergyStorage storage){
			super(x, y, new TexCoords(96, 26, 11, 25), new TexCoords(107, 28, 7, 21), storage);
		}
	}
	
	private final IEnergyStorage storage;
	private final TexCoords background, bar;
	private final DisplayBounds barBounds;
	private EnergyDisplay(int x, int y, TexCoords background, TexCoords foreground, IEnergyStorage storage){
		super(DisplayBounds.of(x, y, background.w(), background.h()), 0);
		this.storage = storage;
		this.background = background;
		this.bar = foreground;
		this.barBounds = DisplayBounds.of(x + 2, y + 2, foreground.w(), foreground.h());
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		
		tooltip.add(Component.literal(this.storage.getEnergyStored() + "/" + this.storage.getMaxEnergyStored() + " IF"));
	}
	
	@Override
	public void draw(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
		final float scale = (this.storage.getEnergyStored() / (float) this.storage.getMaxEnergyStored());
		
		blitSprite(guiGraphics, this.background, 0);
		drawBar(guiGraphics, this.bar, this.barBounds, scale, this.depth);
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
	
	// ############################# DEBUGGING #############################
	
	private static class DebugStorage implements IEnergyStorage{
		private final int amount, cap;
		
		private final boolean kittMode;
		private int kitt = 0;
		private boolean up = true;
		DebugStorage(boolean kittMode, int amount, int cap){
			this.kittMode = kittMode;
			this.amount = amount;
			this.cap = Math.max(amount, cap);
		}
		
		@Override
		public int receiveEnergy(int i, boolean b){
			return 0;
		}
		
		@Override
		public int extractEnergy(int i, boolean b){
			return 0;
		}
		
		@Override
		public int getEnergyStored(){
			if(!this.kittMode)
				return this.amount;
			
			if(this.up){
				this.kitt++;
				if(this.kitt > getMaxEnergyStored()){
					this.kitt = getMaxEnergyStored();
					this.up = false;
				}
			}else{
				this.kitt--;
				if(this.kitt < 0){
					this.kitt = 0;
					this.up = true;
				}
			}
			
			return this.kitt;
		}
		
		@Override
		public int getMaxEnergyStored(){
			return this.cap;
		}
		
		@Override
		public boolean canExtract(){
			return false;
		}
		
		@Override
		public boolean canReceive(){
			return false;
		}
	}
}
