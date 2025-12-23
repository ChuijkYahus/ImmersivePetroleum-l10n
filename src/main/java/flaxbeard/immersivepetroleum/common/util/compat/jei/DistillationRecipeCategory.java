package flaxbeard.immersivepetroleum.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class DistillationRecipeCategory extends IPRecipeCategory<DistillationTowerRecipe>{
	public static final ResourceLocation ID = ResourceUtils.ip("distillation");
	
	public DistillationRecipeCategory(IGuiHelper guiHelper){
		super(DistillationTowerRecipe.class, guiHelper, ID, "block.immersivepetroleum.distillation_tower");
		ResourceLocation background = ResourceUtils.ip("textures/gui/jei/distillationtower.png");
		
		setBackground(guiHelper.createDrawable(background, 0, 0, 120, 77));
		setIcon(new ItemStack(IPContent.Multiblock.DISTILLATIONTOWER.block().get()));
	}
	
	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, DistillationTowerRecipe recipe, @Nonnull IFocusGroup focuses){
		int outputTotal = 0;
		List<FluidStack> list = recipe.getFluidOutputs();
		if(!list.isEmpty()){
			for(FluidStack f:list){
				outputTotal += f.getAmount();
			}
			
			// Output Tank
			int tW = 16, tH = 47; // Tank Area Size
			int x0 = 47; // Top-Left-Corner of Tank Area on X
			
			int lastHeight = 52;
			for(int i = list.size() - 1;i >= 0;i--){
				FluidStack f = list.get(i);
				int height = (int) (tH * (f.getAmount() / (float) outputTotal));
				
				IRecipeSlotBuilder slot = builder
						.addSlot(RecipeIngredientRole.OUTPUT, x0, lastHeight - height)
						.setFluidRenderer(f.getAmount(), false, tW, height)
						.addIngredient(NeoForgeTypes.FLUID_STACK, f);
				
				lastHeight -= height;
				
				if(i == 0){
					// Only do this on the "last" fluid
					slot.setOverlay(JEIStuff.tankOverlay, -2, -lastHeight + 3);
				}
			}
		}
		
		if(recipe.getInputFluid() != null){
			builder.addSlot(RecipeIngredientRole.INPUT, 11, 22)
				.setFluidRenderer(outputTotal, false, 16, 47)
				.setOverlay(JEIStuff.tankOverlay, -2, -2)
				.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.getInputFluid().getFluids()));
		}
		
		IRecipeSlotBuilder itemOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 37)
				.addRichTooltipCallback(new TooltipHandler(recipe.getItemOutput()));
		for(StackWithChance s:recipe.getItemOutput()){
			itemOutput.addItemStack(s.stack().get());
		}
	}
	
	@Override
	public void draw(@Nonnull DistillationTowerRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY){
		IDrawable background = getBackground();
		int bWidth = background.getWidth();
		int bHeight = background.getHeight();
		Font font = MCUtil.getFont();
		
		int time = recipe.getTotalProcessTime();
		int energy = recipe.getTotalProcessEnergy() / time;
		
		guiGraphics.pose().pushPose();
		{
			guiGraphics.pose().translate(23, 0, 0);
			
			String text0 = I18n.get("desc.immersiveengineering.info.ift", Utils.fDecimal(energy));
			guiGraphics.drawString(font, text0, bWidth / 2 - font.width(text0) / 2, bHeight - (font.lineHeight * 2), -1, false);
			
			String text1 = I18n.get("desc.immersiveengineering.info.seconds", Utils.fDecimal(time / 20D));
			guiGraphics.drawString(font, text1, bWidth / 2 - font.width(text1) / 2, bHeight - font.lineHeight, -1, false);
		}
		guiGraphics.pose().popPose();
	}
	
}
