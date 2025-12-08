package flaxbeard.immersivepetroleum.client.gui;

import flaxbeard.immersivepetroleum.client.gui.displays.Display;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class IPContainerScreen<C extends AbstractContainerMenu> extends AbstractContainerScreen<C>{
	protected final ResourceLocation background;
	private final List<Display> displays = new ArrayList<>();
	public IPContainerScreen(C container, Inventory inventory, Component title, ResourceLocation background){
		super(container, inventory, title);
		this.background = background;
	}
	
	public IPContainerScreen(C container, Inventory inventory, Component title, ResourceLocation background, int backgroundWidth, int backgroundHeight){
		super(container, inventory, title);
		this.background = background;
		this.imageWidth = backgroundWidth;
		this.imageHeight = backgroundHeight;
	}
	
	@Override
	protected void init(){
		super.init();
		this.displays.clear();
	}
	
	protected void addDisplay(Display display){
		this.displays.add(display);
	}
	
	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		List<Component> tooltip = new ArrayList<>();
		if(!this.displays.isEmpty())
			this.displays.forEach(d -> d.fillTooltip(guiGraphics, mouseX, mouseY, tooltip));
		
		if(!tooltip.isEmpty())
			guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY){
		guiGraphics.blit(this.background, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		if(!this.displays.isEmpty())
			this.displays.forEach(d -> d.draw(guiGraphics, mouseX, mouseY));
	}
	
	@Override
	protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
		// Never render these
	}
}
