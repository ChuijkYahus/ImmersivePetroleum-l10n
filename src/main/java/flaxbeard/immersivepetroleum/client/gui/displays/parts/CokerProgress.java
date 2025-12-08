package flaxbeard.immersivepetroleum.client.gui.displays.parts;

import flaxbeard.immersivepetroleum.client.gui.displays.DisplayTesting;
import flaxbeard.immersivepetroleum.client.gui.displays.TexCoords;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class CokerProgress extends DisplayTesting{
	public static class Raw extends CokerProgress{
		public Raw(int x, int y){
			this(x, y, 0);
		}
		
		public Raw(int x, int y, int depth){
			super(x, y, new TexCoords(114, 13, 6, 38), depth);
		}
	}
	
	public static class Processed extends CokerProgress{
		public Processed(int x, int y){
			this(x, y, 0);
		}
		
		public Processed(int x, int y, int depth){
			super(x, y, new TexCoords(120, 13, 6, 38), depth);
		}
	}
	
	private CokerProgress(int x, int y, @Nonnull TexCoords partPos, int depth){
		super(x, y, partPos, depth);
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		//tooltip.add(Component.literal("CokerProgress." + getClass().getSimpleName()).withStyle(ChatFormatting.DARK_GRAY));
	}
}
