package flaxbeard.immersivepetroleum.client.gui.displays;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiFluidDisplay extends Display{
	
	public static MultiFluidDisplay create(int x, int y, @Nonnull MultiFluidTankFiltered tank){
		return create(x, y, null, tank);
	}
	
	public static MultiFluidDisplay create(int x, int y, @Nullable Component name, @Nonnull MultiFluidTankFiltered tank){
		return new MultiFluidDisplay(x, y, new TexCoords(0, 0, 20, 51), new TexCoords(20, 0, 20, 51), name, tank);
	}
	
	private final MultiFluidTankFiltered tank;
	private final TexCoords tankBackground;
	private final TexCoords tankOverlay;
	private final Component name;
	private final DisplayBounds fluidBounds;
	private MultiFluidDisplay(int x, int y, TexCoords tankBackground, TexCoords tankOverlay, @Nullable Component name, @Nonnull MultiFluidTankFiltered tank){
		super(DisplayBounds.of(x, y, tankBackground.w(), tankBackground.h()), 1);
		this.tankBackground = tankBackground;
		this.tankOverlay = tankOverlay;
		this.name = name;
		this.fluidBounds = DisplayBounds.of(x + 2, y + 2, tankBackground.w() - 4, tankBackground.h() - 4);
		this.tank = tank;
		
		// DEBUGGING
		/*
		int multi = 2000;
		FluidStack[] fluids = {
			new FluidStack(IPContent.Fluids.NAPHTHA.get(), 1 * multi),
			new FluidStack(IPContent.Fluids.CRUDEOIL.get(), 2 * multi),
			new FluidStack(IPContent.Fluids.DIESEL_SULFUR.get(), 3 * multi),
			new FluidStack(IPContent.Fluids.LUBRICANT.get(), 4 * multi)
		};
		DebugStorage debugStorage = new DebugStorage(fluids, DistillationTowerLogic.Tanks.CAPACITY);
		this.tank = debugStorage;
		*/
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		if(!this.fluidBounds.contains(mouseX, mouseY))
			return;
		
		if(this.name != null)
			tooltip.add(this.name);
		
		if(this.tank.getFluidAmount() == 0)
			tooltip.add(Component.translatable("gui.immersivepetroleum.empty"));
		
		tooltip.add(Component.literal(this.tank.getFluidAmount() + "/" + this.tank.getCapacity() + " mB").withStyle(ChatFormatting.GRAY));
		
		int rel = (this.fluidBounds.y1() - mouseY) - 1;
		if(rel >= 0){
			List<Component> fluidTooltips = new ArrayList<>();
			forEachFluid((fluid, startY, endY) -> {
				if(rel >= startY && rel < endY){
					FluidDisplay.fluidTooltip(fluid, 0, fluidTooltips);
				}
			});
			
			if(!fluidTooltips.isEmpty()){
				tooltip.add(Component.literal("                    ").withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.DARK_GRAY));
				tooltip.addAll(fluidTooltips);
			}
		}
	}
	
	@Override
	public void draw(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY){
		blitSprite(guiGraphics, this.tankBackground, this.depth);
		drawFluids(guiGraphics, this.tank, this.depth + 1, mouseX, mouseY);
		blitSprite(guiGraphics, this.tankOverlay, this.depth + 3);
	}
	
	private void drawFluids(@Nonnull GuiGraphics guiGraphics, MultiFluidTankFiltered tank, int depth, int mouseX, int mouseY){
		if(tank.getFluidAmount() == 0)
			return;
		
		forEachFluid((fluid, startY, endY) -> {
			drawFluid(guiGraphics, this.fluidBounds.move(0, -startY), endY - startY, fluid, depth);
		});
		
		if(this.fluidBounds.contains(mouseX, mouseY)){
			int rel = (this.fluidBounds.y1() - mouseY) - 1;
			if(rel >= 0){
				forEachFluid((fluid, startY, endY) -> {
					if(rel >= startY && rel < endY){
						int x0 = fluidBounds.x0();
						int x1 = fluidBounds.x1();
						int y0 = fluidBounds.y1() - endY;
						int y1 = fluidBounds.y1() - startY;
						drawHighlightQuad(guiGraphics, x0, x1, y0, y1, depth + 1, 0x7FFFFFFF);
					}
				});
			}
		}
	}
	
	private void drawFluid(@Nonnull GuiGraphics guiGraphics, DisplayBounds bounds, int pixelHeight, FluidStack fluid, int depth){
		if(fluid.isEmpty())
			return;
		
		final IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid.getFluid());
		final TextureAtlasSprite spriteAtlas = MCUtil.getBlockSprite(props.getStillTexture(fluid));
		final int spriteW = spriteAtlas.contents().width();
		final int spriteH = spriteAtlas.contents().height();
		
		if(spriteW > 0 && spriteH > 0){
			final int tintColor = props.getTintColor(fluid);
			final int count = pixelHeight / spriteH;
			final int left = pixelHeight % spriteH;
			
			float u0 = spriteAtlas.getU0();
			float v0 = spriteAtlas.getV0();
			float u1 = spriteAtlas.getU1();
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
	
	private void forEachFluid(FluidProcessor processor){
		final float fCap = this.tank.getCapacity();
		int currentAmount = 0;
		int lastY = 0, nextY;
		for(int i = this.tank.getTanks() - 1;i >= 0;i--){
			FluidStack fluid = this.tank.getFluidInTank(i);
			if(fluid.isEmpty())
				continue;
			
			currentAmount += fluid.getAmount();
			nextY = (int) (this.fluidBounds.height() * (currentAmount / fCap));
			processor.process(fluid, lastY, nextY);
			lastY = nextY;
		}
	}
	
	private interface FluidProcessor{
		void process(FluidStack fluid, int startY, int endY);
	}
	
	// ############################# DEBUGGING #############################
	
	public static class DebugStorage extends MultiFluidTankFiltered{
		public DebugStorage(FluidStack[] fluids, int capacity){
			super(capacity);
			Collections.addAll(this.fluids, fluids);
			
			int total = 0;
			for(FluidStack fluid: fluids)
				total += fluid.getAmount();
			if(total > capacity)
				ImmersivePetroleum.log.warn("{} fluids amount exceeds capacity! ({} - {})", getClass().getSimpleName(), total, capacity);
		}
	}
}
