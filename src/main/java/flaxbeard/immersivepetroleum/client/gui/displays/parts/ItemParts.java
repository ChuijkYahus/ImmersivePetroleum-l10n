package flaxbeard.immersivepetroleum.client.gui.displays.parts;

import flaxbeard.immersivepetroleum.client.gui.displays.DisplayTesting;
import flaxbeard.immersivepetroleum.client.gui.displays.TexCoords;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class ItemParts extends DisplayTesting{
	public static class ItemSlot extends ItemParts{
		public ItemSlot(int x, int y){
			this(x, y, 0);
		}
		
		public ItemSlot(int x, int y, int depth){
			super(x, y, new TexCoords(60, 0, 18, 18), depth);
		}
	}
	
	public static class PipeItemSlot extends ItemParts{
		public PipeItemSlot(int x, int y){
			this(x, y, 0);
		}
		
		public PipeItemSlot(int x, int y, int depth){
			super(x, y, new TexCoords(60, 18, 18, 18), depth);
		}
	}
	
	private ItemParts(int x, int y, @Nonnull TexCoords partPos, int depth){
		super(x, y, partPos, depth);
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		//tooltip.add(Component.literal("ItemParts." + getClass().getSimpleName()).withStyle(ChatFormatting.DARK_GRAY));
	}
}
