package flaxbeard.immersivepetroleum.client.gui.displays.parts;

import flaxbeard.immersivepetroleum.client.gui.displays.DisplayTesting;
import flaxbeard.immersivepetroleum.client.gui.displays.TexCoords;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class FluidParts extends DisplayTesting{
	public static class TankBackground extends FluidParts{
		public TankBackground(int x, int y){
			this(x, y, 0);
		}
		
		public TankBackground(int x, int y, int depth){
			super(x, y, new TexCoords(0, 0, 20, 51), depth);
		}
	}
	
	public static class TankOverlay extends FluidParts{
		public TankOverlay(int x, int y){
			this(x, y, 0);
		}
		
		public TankOverlay(int x, int y, int depth){
			super(x, y, new TexCoords(20, 0, 20, 51), depth);
		}
	}
	
	public static class SlotBucketIn extends FluidParts{
		public SlotBucketIn(int x, int y){
			this(x, y, 0);
		}
		
		public SlotBucketIn(int x, int y, int depth){
			super(x, y, new TexCoords(40, 0, 20, 23), depth);
		}
	}
	
	public static class SlotBucketOut extends FluidParts{
		public SlotBucketOut(int x, int y){
			this(x, y, 0);
		}
		
		public SlotBucketOut(int x, int y, int depth){
			super(x, y, new TexCoords(40, 23, 20, 23), depth);
		}
	}
	
	private FluidParts(int x, int y, @Nonnull TexCoords partPos, int depth){
		super(x, y, partPos, depth);
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
	}
}
