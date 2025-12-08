package flaxbeard.immersivepetroleum.client.gui.machines;

import flaxbeard.immersivepetroleum.client.gui.IPContainerScreen;
import flaxbeard.immersivepetroleum.client.gui.displays.EnergyDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.FluidDisplay;
import flaxbeard.immersivepetroleum.common.gui.HydrotreaterContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IPContainerScreen_Hydrotreater extends IPContainerScreen<HydrotreaterContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/hydrotreater.png");
	
	public IPContainerScreen_Hydrotreater(HydrotreaterContainer container, Inventory inventory, Component title){
		super(container, inventory, title, GUI_TEXTURE, 140, 69);
	}
	
	@Override
	protected void init(){
		super.init();
		
		Component primaryTankName = Component.translatable("gui.immersivepetroleum.hydrotreater.tank.primary").withStyle(ChatFormatting.AQUA);
		addDisplay(FluidDisplay.create(this.leftPos + 9, this.topPos + 9, primaryTankName, getMenu().primary));
		
		Component secondaryTankName = Component.translatable("gui.immersivepetroleum.hydrotreater.tank.secondary").withStyle(ChatFormatting.AQUA);
		addDisplay(FluidDisplay.create(this.leftPos + 32, this.topPos + 9, secondaryTankName, getMenu().secondary));
		
		Component outputTankName = Component.translatable("gui.immersivepetroleum.tank.output").withStyle(ChatFormatting.GOLD);
		addDisplay(FluidDisplay.create(this.leftPos + 90, this.topPos + 9, outputTankName, getMenu().output));
		
		addDisplay(new EnergyDisplay.Normal(this.leftPos + 120, this.topPos + 10, getMenu().energy));
	}
}
