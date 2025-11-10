package flaxbeard.immersivepetroleum.common.blocks;

import flaxbeard.immersivepetroleum.common.IPCreativeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.List;

public class IPBlockItemBase extends BlockItem implements IPCreativeTab.IMightShowUpInCreativeTab{
	public IPBlockItemBase(Block blockIn, Properties builder){
		super(blockIn, builder);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn){
		/* // TODO Tank and Energy Display
		if(stack.hasTag()){
			// Display Stored Tank Information
			if(stack.getTag().contains("tank")){
				CompoundTag tank = stack.getTag().getCompound("tank");
				
				FluidStack fluidstack = FluidStack.loadFluidStackFromNBT(tank);
				if(fluidstack.getAmount() > 0){
					tooltip.add(((MutableComponent) fluidstack.getDisplayName()).append(" " + fluidstack.getAmount() + "mB").withStyle(ChatFormatting.GRAY));
				}else{
					tooltip.add(Component.translatable(Lib.GUI + "empty").withStyle(ChatFormatting.GRAY));
				}
			}
			
			// Display Stored Energy Information
			if(stack.getTag().contains("energy")){
				int flux = stack.getTag().getInt("energy");
				tooltip.add(Component.literal(flux + "RF").withStyle(ChatFormatting.GRAY));
			}
		}
		*/
		
		super.appendHoverText(stack, ctx, tooltip, flagIn);
	}
	
	@Override
	protected boolean placeBlock(@Nonnull BlockPlaceContext pContext, @Nonnull BlockState pState){
		return super.placeBlock(pContext, pState);
	}
}
