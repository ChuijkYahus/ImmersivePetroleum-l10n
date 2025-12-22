package flaxbeard.immersivepetroleum.client.gui.displays.parts;

import flaxbeard.immersivepetroleum.client.gui.displays.DisplayTesting;
import flaxbeard.immersivepetroleum.client.gui.displays.TexCoords;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class EnergyParts extends DisplayTesting{
	public static class DefaultBackground extends EnergyParts{
		public DefaultBackground(int x, int y){
			this(x, y, 0);
		}
		
		public DefaultBackground(int x, int y, int depth){
			super(x, y, new TexCoords(78, 1, 11, 50), depth);
		}
	}
	
	/** The red bar */
	public static class DefaultForeground extends EnergyParts{
		public DefaultForeground(int x, int y){
			this(x, y, 0);
		}
		
		public DefaultForeground(int x, int y, int depth){
			super(x, y, new TexCoords(89, 3, 7, 46), depth);
		}
	}
	
	public static class SmallBackground extends EnergyParts{
		public SmallBackground(int x, int y){
			this(x, y, 0);
		}
		
		public SmallBackground(int x, int y, int depth){
			super(x, y, new TexCoords(96, 26, 11, 25), depth);
		}
	}
	
	/** The red bar */
	public static class SmallForeground extends EnergyParts{
		public SmallForeground(int x, int y){
			this(x, y, 0);
		}
		
		public SmallForeground(int x, int y, int depth){
			super(x, y, new TexCoords(107, 28, 7, 21), depth);
		}
	}
	
	private EnergyParts(int x, int y, @Nonnull TexCoords partPos, int depth){
		super(x, y, partPos, depth);
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		//tooltip.add(Component.literal("EnergyParts." + getClass().getSimpleName()).withStyle(ChatFormatting.DARK_GRAY));
	}
}
