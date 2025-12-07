package flaxbeard.immersivepetroleum.common.util.compat.jei;

import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;

public class HighPressureRefineryRecipeCategory extends IPRecipeCategory<HighPressureRefineryRecipe>{
	public static final ResourceLocation ID = ResourceUtils.ip("hydrotreater");
	
	private final IDrawableStatic tankOverlay;
	public HighPressureRefineryRecipeCategory(IGuiHelper guiHelper){
		super(HighPressureRefineryRecipe.class, guiHelper, ID, "block.immersivepetroleum.hydrotreater");
		ResourceLocation background = ResourceUtils.ip("textures/gui/jei/hydrotreater.png");
		setBackground(guiHelper.createDrawable(background, 0, 0, 113, 75));
		setIcon(new ItemStack(IPContent.Multiblock.HYDROTREATER.block().get()));
		
		this.tankOverlay = guiHelper.createDrawable(background, 113, 0, 20, 51);
	}
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, HighPressureRefineryRecipe recipe, @Nonnull IFocusGroup focuses){
		int primaryInputAmount = recipe.getPrimaryInputFluid().amount();
		int secondaryInputAmount = recipe.getSecondaryInputFluid() != null ? recipe.getSecondaryInputFluid().amount() : 0;
		int outputAmount = recipe.getOutputFluid().getAmount();
		int guiTankSize = Math.min(Math.max(Math.max(primaryInputAmount, secondaryInputAmount), outputAmount), 1000);
		
		builder.addSlot(RecipeIngredientRole.INPUT, 25, 3)
			.setFluidRenderer(guiTankSize, false, 20, 51)
			.setOverlay(this.tankOverlay, 0, 0)
			.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.getPrimaryInputFluid().getFluids()));
		
		IRecipeSlotBuilder secondary = builder.addSlot(RecipeIngredientRole.INPUT, 3, 3)
			.setFluidRenderer(guiTankSize, false, 20, 51)
			.setOverlay(this.tankOverlay, 0, 0);
		if(recipe.getSecondaryInputFluid() != null)
			secondary.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.getSecondaryInputFluid().getFluids()));
		
		builder.addSlot(RecipeIngredientRole.OUTPUT, 71, 3)
			.setFluidRenderer(guiTankSize, false, 20, 51)
			.setOverlay(this.tankOverlay, 0, 0)
			.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getOutputFluid());
		
		if(recipe.getSecondaryItem() != null){
			builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 21)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getSecondaryItem().stack().get());
		}
	}
	
	@Override
	public void draw(HighPressureRefineryRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY){
		IDrawable background = getBackground();
		int bWidth = background.getWidth();
		int bHeight = background.getHeight();
		Font font = MCUtil.getFont();
		
		int time = recipe.getTotalProcessTime();
		int energy = recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime();
		
		guiGraphics.pose().pushPose();
		String text0 = I18n.get("desc.immersiveengineering.info.ift", Utils.fDecimal(energy));
		guiGraphics.drawString(font, text0, bWidth / 2 - font.width(text0) / 2, bHeight - (font.lineHeight * 2), -1, false);
		
		String text1 = I18n.get("desc.immersiveengineering.info.seconds", Utils.fDecimal(time / 20D));
		guiGraphics.drawString(font, text1, bWidth / 2 - font.width(text1) / 2, bHeight - font.lineHeight, -1, false);
		
		if(recipe.getSecondaryItem() != null){
			int chance = (int) (100 * recipe.getSecondaryItem().chance());
			
			String text2 = String.format(Locale.US, "%d%%", chance);
			guiGraphics.drawString(font, text2, bWidth + 3 - font.width(text2), bHeight / 2 + 4, -1, false);
		}
		guiGraphics.pose().popPose();
	}
}
