package flaxbeard.immersivepetroleum.client.render.debugging;

import com.mojang.blaze3d.vertex.PoseStack;
import flaxbeard.immersivepetroleum.client.render.RenderUtils;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DebugText{
	private final List<Component> lines = new ArrayList<>(20);
	protected DebugText(){
	}
	
	public boolean isEmpty(){
		return this.lines.isEmpty();
	}
	
	public int size(){
		return this.lines.size();
	}
	
	public void add(Component component){
		this.lines.add(component);
	}
	
	public void add(int index, Component component){
		this.lines.add(index, component);
	}
	
	public void translated(String key, ChatFormatting... formatting){
		this.lines.add(translatedFormatting(key, formatting));
	}
	
	public void translated(int index, String key, ChatFormatting... formatting){
		this.lines.add(index, translatedFormatting(key, formatting));
	}
	
	private Component translatedFormatting(String key, ChatFormatting... formatting){
		MutableComponent literal = Component.translatable(key);
		
		if(formatting == null || formatting.length == 0)
			return literal;
		
		return literal.withStyle(formatting);
	}
	
	public void literal(String str, ChatFormatting... formatting){
		this.lines.add(literalFormatting(str, formatting));
	}
	
	public void literal(int index, String str, ChatFormatting... formatting){
		this.lines.add(index, literalFormatting(str, formatting));
	}
	
	private Component literalFormatting(String str, ChatFormatting... formatting){
		MutableComponent literal = Component.literal(str);
		
		if(formatting == null || formatting.length == 0)
			return literal;
		
		return literal.withStyle(formatting);
	}
	
	public void addEnergyText(IEnergyStorage energyStorage){
		if(energyStorage == null)
			return;
		
		int stored = energyStorage.getEnergyStored();
		int capacity = energyStorage.getMaxEnergyStored();
		
		MutableComponent literal = Component.literal("Energy: " + stored + "/" + capacity + " RF");
		if(capacity == 0)
			literal.append(Component.literal(" (Dummy-Storage?)").withStyle(ChatFormatting.GRAY));
		
		add(literal);
	}
	
	public void addTankText(IFluidHandler fluidHandler){
		addTankText("Tank", fluidHandler);
	}
	
	public void addTankText(String name, IFluidHandler fluidHandler){
		if(fluidHandler == null)
			return;
		
		boolean named = name != null;
		
		int tanks = fluidHandler.getTanks();
		for(int i = 0;i < tanks;i++){
			int capacity = fluidHandler.getTankCapacity(i);
			FluidStack fluid = fluidHandler.getFluidInTank(i);
			
			String text;
			
			if(fluid.getAmount() == capacity){
				text = fluid.getAmount() + "mB";
			}else{
				text = fluid.getAmount() + "/" + capacity + "mB";
			}
			
			if(tanks == 1){
				text = (named ? name + ": " : "") + text;
			}else{
				if(named){
					text = name + "(" + (i + 1) + ")" + ": " + text;
				}
			}
			
			if(!fluid.isEmpty())
				text += " (" + fluid.getHoverName().getString() + ")";
			
			literal(text);
		}
	}
	
	public void render(GuiGraphics guiGraphics){
		if(this.lines.isEmpty())
			return;
		
		final Font font = MCUtil.getFont();
		final PoseStack transform = guiGraphics.pose();
		
		transform.pushPose();
		{
			MultiBufferSource.BufferSource buffer = RenderUtils.immediate();
			for(int i = 0;i < this.lines.size();i++){
				int w = font.width(this.lines.get(i).getString());
				int yOff = i * (font.lineHeight + 2);
				
				transform.pushPose();
				{
					transform.translate(0, 0, 1);
					RenderUtils.drawColouredRect(guiGraphics, 1, 1 + yOff, w + 1, 10);
					buffer.endBatch();
					// Draw string without shadow
					guiGraphics.drawString(font, this.lines.get(i), 2, 2 + yOff, -1, false);
				}
				transform.popPose();
			}
			
			this.lines.clear();
		}
		transform.popPose();
	}
}
