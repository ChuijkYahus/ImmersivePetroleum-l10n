package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.IPCreativeTab;
import flaxbeard.immersivepetroleum.common.IPDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class IPBlockItemBase extends BlockItem implements IPCreativeTab.IMightShowUpInCreativeTab{
	public IPBlockItemBase(Block blockIn, Properties builder){
		super(blockIn, builder);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn){
		IPDataComponents.TankData tankData = stack.get(IPDataComponents.TANK_DATA);
		IPDataComponents.PowerData powerData = stack.get(IPDataComponents.POWER_DATA);
		
		if(tankData != null && !tankData.fs().isEmpty()){
			FluidStack fluidstack = tankData.fs();
			if(fluidstack.getAmount() > 0){
				tooltip.add(((MutableComponent) fluidstack.getHoverName()).append(" " + fluidstack.getAmount() + "mB").withStyle(ChatFormatting.GRAY));
			}else{
				tooltip.add(Component.translatable("gui.immersivepetroleum.empty").withStyle(ChatFormatting.GRAY));
			}
		}
		
		if(powerData != null){
			tooltip.add(Component.literal(powerData.energy() + " RF").withStyle(ChatFormatting.GRAY));
		}
		
		super.appendHoverText(stack, ctx, tooltip, flagIn);
	}
	
	@Override
	protected boolean placeBlock(@Nonnull BlockPlaceContext pContext, @Nonnull BlockState pState){
		return super.placeBlock(pContext, pState);
	}
}
