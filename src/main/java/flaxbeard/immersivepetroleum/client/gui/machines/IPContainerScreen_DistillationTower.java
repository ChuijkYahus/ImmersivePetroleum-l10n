package flaxbeard.immersivepetroleum.client.gui.machines;

import flaxbeard.immersivepetroleum.client.gui.IPContainerScreen;
import flaxbeard.immersivepetroleum.client.gui.displays.EnergyDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.FluidDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.MultiFluidDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.parts.FluidParts;
import flaxbeard.immersivepetroleum.common.gui.DistillationTowerContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IPContainerScreen_DistillationTower extends IPContainerScreen<DistillationTowerContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/distillation.png");
	
	public IPContainerScreen_DistillationTower(DistillationTowerContainer container, Inventory inventory, Component title){
		super(container, inventory, title, GUI_TEXTURE);
	}
	
	@Override
	protected void init(){
		super.init();
		
		// Input
		Component inTankName = Component.translatable("gui.immersivepetroleum.tank.input").withStyle(ChatFormatting.AQUA);
		addDisplay(new FluidParts.SlotBucketIn(this.leftPos + 10, this.topPos + 12));
		addDisplay(new FluidParts.SlotBucketOut(this.leftPos + 10, this.topPos + 48));
		addDisplay(FluidDisplay.create(this.leftPos + 50, this.topPos + 20, inTankName, getMenu().input));
		
		// Output
		Component outTankName = Component.translatable("gui.immersivepetroleum.tank.output").withStyle(ChatFormatting.GOLD);
		addDisplay(MultiFluidDisplay.create(this.leftPos + 110, this.topPos + 19, outTankName, getMenu().output));
		addDisplay(new FluidParts.SlotBucketIn(this.leftPos + 132, this.topPos + 12));
		addDisplay(new FluidParts.SlotBucketOut(this.leftPos + 132, this.topPos + 48));
		
		addDisplay(new EnergyDisplay.Normal(this.leftPos + 156, this.topPos + 20, getMenu().energy));
	}
}
