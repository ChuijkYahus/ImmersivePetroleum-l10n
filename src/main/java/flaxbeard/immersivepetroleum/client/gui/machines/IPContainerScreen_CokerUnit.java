package flaxbeard.immersivepetroleum.client.gui.machines;

import flaxbeard.immersivepetroleum.client.gui.IPContainerScreen;
import flaxbeard.immersivepetroleum.client.gui.displays.CokingChamberDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.EnergyDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.FluidDisplay;
import flaxbeard.immersivepetroleum.client.gui.displays.parts.FluidParts;
import flaxbeard.immersivepetroleum.client.gui.displays.parts.ItemParts;
import flaxbeard.immersivepetroleum.common.gui.CokerUnitContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IPContainerScreen_CokerUnit extends IPContainerScreen<CokerUnitContainer>{
	public static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/coker.png");
	
	public IPContainerScreen_CokerUnit(CokerUnitContainer container, Inventory inventory, Component title){
		super(container, inventory, title, GUI_TEXTURE, 200, 187);
	}
	
	@Override
	protected void init(){
		super.init();
		
		// Input Buffer
		addDisplay(new FluidParts.SlotBucketIn(this.leftPos + 7, this.topPos + 9));
		addDisplay(new FluidParts.SlotBucketOut(this.leftPos + 7, this.topPos + 40));
		Component inTankName = Component.translatable("gui.immersivepetroleum.tank.input").withStyle(ChatFormatting.AQUA);
		addDisplay(FluidDisplay.create(this.leftPos + 30, this.topPos + 12, inTankName, getMenu().input));
		
		// Output Buffer
		Component outTankName = Component.translatable("gui.immersivepetroleum.tank.output").withStyle(ChatFormatting.GOLD);
		addDisplay(FluidDisplay.create(this.leftPos + 150, this.topPos + 12, outTankName, getMenu().output));
		addDisplay(new FluidParts.SlotBucketIn(this.leftPos + 173, this.topPos + 9));
		addDisplay(new FluidParts.SlotBucketOut(this.leftPos + 173, this.topPos + 40));
		
		// Input (Bitumen)
		//addDisplay(new ItemParts.ItemSlot(this.leftPos + 19, this.topPos + 70));
		
		// Coker Progress
		addDisplay(CokingChamberDisplay.create(this.leftPos + 74, this.topPos + 24, getMenu().primary.get())); // Primary
		addDisplay(CokingChamberDisplay.create(this.leftPos + 120, this.topPos + 24, getMenu().secondary.get())); // Secondary
		
		addDisplay(new EnergyDisplay.Small(this.leftPos + 166, this.topPos + 65, getMenu().energy));
	}
}
