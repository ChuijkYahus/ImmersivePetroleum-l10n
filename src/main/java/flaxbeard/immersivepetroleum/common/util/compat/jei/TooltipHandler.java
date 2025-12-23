package flaxbeard.immersivepetroleum.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class TooltipHandler implements IRecipeSlotRichTooltipCallback{
	private final Map<Item, Float> map = new HashMap<>();
	
	public TooltipHandler(List<StackWithChance> itemOutput){
		if(itemOutput.isEmpty())
			return;
		
		itemOutput.forEach(stack -> this.map.put(stack.stack().get().getItem(), stack.chance()));
	}
	
	public TooltipHandler(@Nonnull StackWithChance stack){
		this.map.put(stack.stack().get().getItem(), stack.chance());
	}
	
	@Override
	public void onRichTooltip(IRecipeSlotView recipeSlotView, @Nonnull ITooltipBuilder tooltip){
		recipeSlotView.getDisplayedIngredient().ifPresent(type -> {
			if(!(type.getIngredient() instanceof ItemStack stack))
				return;
			
			Float t;
			if((t = this.map.get(stack.getItem())) != null){
				double chance = t.doubleValue();
				
				Component text = Component.translatable("desc.immersivepetroleum.compat.jei.distillation.byproduct").withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE);
				
				tooltip.add(text);
				tooltip.add(toTextComponent(chance));
			}
		});
	}
	
	private Component toTextComponent(double chance){
		return Component.literal(String.format(Locale.ENGLISH, "%.2f%%", 100D * chance)).withStyle(ChatFormatting.GRAY);
	}
}
