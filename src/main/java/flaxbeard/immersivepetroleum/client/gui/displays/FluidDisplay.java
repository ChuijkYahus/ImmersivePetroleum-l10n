package flaxbeard.immersivepetroleum.client.gui.displays;

import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.UnaryOperator;

public class FluidDisplay extends Display{
	
	public static FluidDisplay create(int x, int y, @Nonnull IFluidTank tank){
		return create(x, y, null, tank);
	}
	
	public static FluidDisplay create(int x, int y, @Nullable Component name, @Nonnull IFluidTank tank){
		return new FluidDisplay(x, y, new TexCoords(0, 0, 20, 51), new TexCoords(20, 0, 20, 51), name, tank);
	}
	
	private final IFluidTank tank;
	private final TexCoords tankBackground;
	private final TexCoords tankOverlay;
	private final @Nullable Component name;
	private final DisplayBounds fluidBounds;
	private FluidDisplay(int x, int y, TexCoords tankBackground, TexCoords tankOverlay, @Nullable Component name, @Nonnull IFluidTank tank){
		super(DisplayBounds.of(x, y, tankBackground.w(), tankBackground.h()), 1);
		this.tankBackground = tankBackground;
		this.tankOverlay = tankOverlay;
		this.name = name;
		this.fluidBounds = DisplayBounds.of(x + 2, y + 2, tankBackground.w() - 4, tankBackground.h() - 4);
		this.tank = tank;
		
		// DEBUGGING
		/*
		DebugStorage debugStorage = new DebugStorage(IPContent.Fluids.CRUDEOIL.get(), DistillationTowerLogic.Tanks.CAPACITY, DistillationTowerLogic.Tanks.CAPACITY, true);
		this.tank = debugStorage;
		*/
	}
	
	@Override
	protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
		if(!this.fluidBounds.contains(mouseX, mouseY))
			return;
		
		if(this.name != null)
			tooltip.add(this.name);
		
		fluidTooltip(this.tank.getFluid(), this.tank.getCapacity(), tooltip);
	}
	
	public static void fluidTooltip(FluidStack fluidStack, int capacity, List<Component> tooltip){
		if(fluidStack.isEmpty()){
			tooltip.add(Component.translatable("gui.immersivepetroleum.empty"));
		}else{
			Fluid fluid = fluidStack.getFluid();
			Component ret = fluidStack.getHoverName();
			UnaryOperator<Style> styleModifier = fluid.getFluidType().getRarity(fluidStack).getStyleModifier();
			tooltip.add(ret.copy().withStyle(styleModifier.apply(ret.getStyle())));
		}
		
		if(fluidStack.getFluid() instanceof PotionFluid potion)
			potion.addInformation(fluidStack, tooltip::add);
		
		if(capacity > 0){
			tooltip.add(Component.literal(fluidStack.getAmount() + "/" + capacity + " mB").withStyle(ChatFormatting.GRAY));
		}else if(capacity == 0){
			tooltip.add(Component.literal(fluidStack.getAmount() + " mB").withStyle(ChatFormatting.GRAY));
		}
	}
	
	@Override
	public void draw(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
		blitSprite(guiGraphics, this.tankBackground, this.depth);
		drawFluid(guiGraphics, this.fluidBounds, this.tank, this.depth + 1);
		blitSprite(guiGraphics, this.tankOverlay, this.depth + 2);
	}
	
	private void drawFluid(@Nonnull GuiGraphics guiGraphics, DisplayBounds bounds, IFluidTank tank, int depth){
		FluidStack fluid = tank.getFluid();
		if(fluid.isEmpty())
			return;
		
		IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid.getFluid());
		TextureAtlasSprite spriteAtlas = MCUtil.getBlockSprite(props.getStillTexture(fluid));
		int spriteW = spriteAtlas.contents().width();
		int spriteH = spriteAtlas.contents().height();
		
		if(spriteW > 0 && spriteH > 0){
			int tintColor = props.getTintColor(fluid);
			final int pixelHeight = (int) (bounds.height() * (fluid.getAmount() / (float) tank.getCapacity()));
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
	
	// ############################# DEBUGGING #############################
	
	public static class DebugStorage implements IFluidTank{
		
		private final Fluid fluid;
		private int amount;
		private final int cap;
		
		private final boolean kittMode;
		private int kitt = 0;
		private boolean up = true;
		public DebugStorage(Fluid fluid, int amount, int capacity, boolean kittMode){
			this.fluid = fluid;
			this.amount = amount;
			this.cap = capacity;
			this.kittMode = kittMode;
		}
		
		@Nonnull
		@Override
		public FluidStack getFluid(){
			int amount = getFluidAmount();
			if(amount == 0)
				return FluidStack.EMPTY;
			
			return new FluidStack(this.fluid, Math.min(Math.max(amount, 0), getCapacity()));
		}
		
		@Override
		public int getFluidAmount(){
			if(!this.kittMode)
				return this.amount;
			
			if(this.up){
				this.kitt++;
				if(this.kitt > getCapacity()){
					this.kitt = getCapacity();
					this.up = false;
				}
			}else{
				this.kitt--;
				if(this.kitt < 0){
					this.kitt = 0;
					this.up = true;
				}
			}
			
			this.amount = this.kitt;
			return this.amount;
		}
		
		@Override
		public int getCapacity(){
			return this.cap;
		}
		
		@Override
		public boolean isFluidValid(@Nonnull FluidStack fluidStack){
			return false;
		}
		
		@Override
		public int fill(@Nonnull FluidStack fluidStack, @Nonnull IFluidHandler.FluidAction fluidAction){
			return 0;
		}
		
		@Nonnull
		@Override
		public FluidStack drain(int i, @Nonnull IFluidHandler.FluidAction fluidAction){
			return FluidStack.EMPTY;
		}
		
		@Nonnull
		@Override
		public FluidStack drain(@Nonnull FluidStack fluidStack, @Nonnull IFluidHandler.FluidAction fluidAction){
			return FluidStack.EMPTY;
		}
	}
}
