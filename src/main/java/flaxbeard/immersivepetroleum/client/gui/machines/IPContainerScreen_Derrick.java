package flaxbeard.immersivepetroleum.client.gui.machines;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import flaxbeard.immersivepetroleum.client.gui.IPContainerScreen;
import flaxbeard.immersivepetroleum.client.gui.displays.Display;
import flaxbeard.immersivepetroleum.client.gui.displays.DisplayBounds;
import flaxbeard.immersivepetroleum.client.gui.displays.EnergyDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.FluidDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.parts.ItemParts;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.cfg.IPClientConfig;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.gui.DerrickContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic.REQUIRED_CONCRETE_AMOUNT;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic.REQUIRED_WATER_AMOUNT;
import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic.State;

public class IPContainerScreen_Derrick extends IPContainerScreen<DerrickContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/derrick.png");
	
	private Button cfgButton;
	
	public IPContainerScreen_Derrick(DerrickContainer container, Inventory inventory, Component title){
		super(container, inventory, title, GUI_TEXTURE, 200, 172);
	}
	
	@Override
	protected void init(){
		super.init();
		
		// Input (Concrete/Water)
		Component inTankName = Component.translatable("gui.immersivepetroleum.tank.input").withStyle(ChatFormatting.AQUA);
		addDisplay(FluidDisplay.create(this.leftPos + 8, this.topPos + 8, inTankName, getMenu().tank));
		
		// Input (Pipe)
		addDisplay(new ItemParts.PipeItemSlot(this.leftPos + 36, this.topPos + 26));
		
		addDisplay(new EnergyDisplay.Normal(this.leftPos + 181, this.topPos + 31, getMenu().energy));
		
		addDisplay(new StatusConsole(this.leftPos + 60, this.topPos + 8, 0, this));
		
		//@formatter:off
		this.cfgButton = new Button.Builder(Component.translatable("gui.immersivepetroleum.derrick.msg.config"), button -> this.minecraft.setScreen(new IPContainerScreen_DerrickSettingsScreen(this)))
			.bounds(this.leftPos + 8, this.topPos + 61, 50, 20)
			.build();
		addRenderableWidget(this.cfgButton);
		//@formatter:on
		
	}
	
	private static class StatusConsole extends Display{
		protected static final ResourceLocation BOX = ResourceUtils.ip("text_box");
		
		final Font font;
		final IPContainerScreen_Derrick derrickScreen;
		final DerrickContainer container;
		final Console console = new Console(7);
		public StatusConsole(int x, int y, int depth, IPContainerScreen_Derrick derrickScreen){
			super(DisplayBounds.of(x, y, 120, 73), depth);
			this.derrickScreen = derrickScreen;
			this.font = derrickScreen.font;
			this.container = derrickScreen.getMenu();
		}
		
		@Override
		protected void tooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip){
			tooltip.add(Component.translatable("gui.immersivepetroleum.derrick.status_console.desc").withStyle(ChatFormatting.GRAY));
		}
		
		@Override
		public void draw(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
			guiGraphics.blitSprite(BOX, this.bounds.x0(), this.bounds.y0(), 0, this.bounds.width(), this.bounds.height());
			
			if(this.container.pos().getY() <= 62){
				this.console.belowWaterTableText();
				
			}else{
				BlockEntity tile = this.container.level.getBlockEntity(this.container.pos());
				if(tile instanceof IMultiblockBE<?> multiblockBE){
					IMultiblockContext<?> context;
					if((context = multiblockBE.getHelper().getContext()) != null && context.getState() instanceof State state){
						IMultiblockLevel level = context.getLevel();
						
						IMultiblockBEHelper<State> derrickType;
						IMultiblockContext<State> ctx;
						if((derrickType = multiblockBE.getHelper().asType(IPContent.Multiblock.DERRICK)) != null && (ctx = derrickType.getContext()) != null){
							if(!state.rsState.isEnabled(ctx)){ // FIXME This is never true?
								this.console.disabledText();
								
							}else{
								updateStatusConsole(this.console, state, level);
							}
						}
					}
				}
			}
			
			this.console.forEachLine((lineIndex, alignment, text, color) -> {
				drawConsoleText(guiGraphics, lineIndex, alignment, text, color);
			});
		}
		
		private void updateStatusConsole(Console console, State state, IMultiblockLevel mbLevel){
			WellTileEntity well = state.getWell(mbLevel, mbLevel.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()));
			if(well != null){
				if(this.derrickScreen.cfgButton.active && well.wellPipeLength > 0){
					this.derrickScreen.cfgButton.active = false;
					this.derrickScreen.cfgButton.setTooltip(Tooltip.create(Component.translatable("gui.immersivepetroleum.derrick.msg.set_in_stone")));
				}
				
				if(well.wellPipeLength < well.getMaxPipeLength()){
					if(state.drilling){
						String str = String.format(Locale.ROOT, "(%d%%)", (int) (100 * well.wellPipeLength / (float) well.getMaxPipeLength()));
						console.addCenter(Component.translatable("gui.immersivepetroleum.derrick.msg.drilling", str), IPClientConfig.DERRICK_CONSOLE.getTextColorNormal());
						
					}else{
						if(well.pipes <= 0 && !this.container.getSlot(0).hasItem()){
							console.addLeft(Component.translatable("gui.immersivepetroleum.derrick.msg.out_of_pipes"), IPClientConfig.DERRICK_CONSOLE.getTextColorNormal());
						}
						
						if(this.container.energy.getEnergyStored() < IPServerConfig.EXTRACTION.derrick_consumption.get()){
							console.addLeft(Component.translatable("gui.immersivepetroleum.derrick.msg.not_enough_power"), IPClientConfig.DERRICK_CONSOLE.getTextColorNormal());
						}
						
						if(this.container.tank.getFluid().isEmpty()){
							int realPipeLength = (mbLevel.getAbsoluteOrigin().getY() - 1) - well.getBlockPos().getY();
							int concreteNeeded = (REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
							if(concreteNeeded > 0){
								console.missingFluidText(ExternalModContent.IE.fluidConcrete(), concreteNeeded);
							}else{
								int waterNeeded = REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
								if(waterNeeded > 0){
									console.missingFluidText(Fluids.WATER, waterNeeded);
								}
							}
						}
					}
					
				}else{
					if(state.spilling){
						console.safetyValveOpenText();
					}else{
						console.completedText();
					}
				}
			}
		}
		
		private void drawConsoleText(@Nonnull GuiGraphics guiGraphics, int line, Console.Alignment alignment, Component text, int color){
			int strWidth = this.font.width(text.getString());
			
			int x = this.bounds.x0() + 3;
			int y = this.bounds.y0() + 3 + 10 * line;
			
			switch(alignment){
				case CENTER -> {
					float cX = (this.bounds.x0() + this.bounds.x1()) / 2F;
					
					x = (int) (cX - (strWidth / 2F));
				}
				case RIGHT -> {
					x = this.bounds.x1() - 3 - strWidth;
				}
			}
			
			drawString(guiGraphics, text, x, y, color);
		}
		
		private void drawString(@Nonnull GuiGraphics guiGraphics, Component text, int x, int y, int color){
			if(IPClientConfig.DERRICK_CONSOLE.useOldSchool()){
				guiGraphics.drawString(this.font, text, x, y, color);
				
			}else{
				for(int j = -1;j <= 1;j++){
					for(int i = -1;i <= 1;i++){
						if(i == 0 && j == 0)
							continue;
						
						guiGraphics.drawString(this.font, text, x + i, y + j, 0xFF000000, false);
					}
				}
				
				guiGraphics.drawString(this.font, text, x, y, color, false);
			}
		}
		
		static class Console{
			private final Line[] lines;
			Console(int maxLines){
				this.lines = new Line[maxLines];
			}
			
			public void missingFluidText(Fluid fluid, int amount){
				int color = IPClientConfig.DERRICK_CONSOLE.getTextColorNormal();
				
				addLeft(Component.translatable("gui.immersivepetroleum.derrick.msg.missing", Utils.fDecimal(amount) + "mB"), color);
				addLeft(new FluidStack(fluid, 1).getHoverName(), color);
			}
			
			public void belowWaterTableText(){
				int color = IPClientConfig.DERRICK_CONSOLE.getTextColorError();
				
				addMultiline(Console.Alignment.CENTER, I18n.get("gui.immersivepetroleum.derrick.msg.water_table"), color);
				addEmpty();
			}
			
			public void safetyValveOpenText(){
				int color = IPClientConfig.DERRICK_CONSOLE.getTextColorError();
				
				addMultiline(Console.Alignment.CENTER, I18n.get("gui.immersivepetroleum.derrick.msg.safety_valve"), color);
				addEmpty();
			}
			
			public void completedText(){
				int color = IPClientConfig.DERRICK_CONSOLE.getTextColorNormal();
				
				addMultiline(Console.Alignment.CENTER, I18n.get("gui.immersivepetroleum.derrick.msg.completed"), color);
				addEmpty();
			}
			
			public void disabledText(){
				int color = IPClientConfig.DERRICK_CONSOLE.getTextColorError();
				
				addLeft(Component.translatable("gui.immersivepetroleum.derrick.msg.disabled"), color);
			}
			
			public void addMultiline(Alignment alignment, String str, int color){
				String[] lines = str.split("<br>");
				for(int i = Math.min(lines.length, this.lines.length) - 1;i >= 0;i--){
					String line = lines[i];
					add(alignment, Component.literal(line.length() > 25 ? line.substring(0, 25) : line), color);
				}
			}
			
			public void addEmpty(){
				add(null);
			}
			
			public void addLeft(Component text, int color){
				add(Alignment.LEFT, text, color);
			}
			
			public void addCenter(Component text, int color){
				add(Alignment.CENTER, text, color);
			}
			
			public void addRight(Component text, int color){
				add(Alignment.RIGHT, text, color);
			}
			
			private void add(Alignment alignment, Component text, int color){
				add(new Line(alignment, text, color));
			}
			
			private void add(Line line){
				for(int i = 1;i < this.lines.length;i++)
					this.lines[i - 1] = this.lines[i];
				this.lines[this.lines.length - 1] = line;
			}
			
			public int getLineCount(){
				return this.lines.length;
			}
			
			public void forEachLine(Processor consumer){
				int len = this.lines.length;
				for(int i = 0;i < len;i++){
					Line line = this.lines[i];
					if(line == null)
						continue;
					
					consumer.process(len - i - 1, line.alignment, line.text, line.color);
				}
				
				clear();
			}
			
			private void clear(){
				Arrays.fill(this.lines, null);
			}
			
			record Line(Alignment alignment, Component text, int color){
			}
			
			enum Alignment{
				LEFT, CENTER, RIGHT;
			}
			
			@FunctionalInterface
			interface Processor{
				void process(int lineIndex, Alignment alignment, Component text, int color);
			}
		}
	}
}
