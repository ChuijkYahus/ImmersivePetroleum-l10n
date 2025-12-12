package flaxbeard.immersivepetroleum.common.items;

import flaxbeard.immersivepetroleum.client.gui.SeismicSurveyScreen;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.survey.ISurveyInfo;
import flaxbeard.immersivepetroleum.common.util.survey.IslandInfo;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

/**
 * @author TwistedGate
 */
public class SurveyResultItem extends IPItemBase{
	public SurveyResultItem(){
		super(new Item.Properties().stacksTo(1));
	}
	
	@Override
	public boolean addSelfToCreativeTab(){
		// This has no business being in the Tab
		return false;
	}
	
	@Override
	@Nonnull
	public Component getName(@Nonnull ItemStack stack){
		String selfKey = getDescriptionId(stack);
		return Component.translatable(selfKey).withStyle(ChatFormatting.GOLD);
	}
	
	@Override
	public @Nonnull InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, @Nonnull InteractionHand pUsedHand){
		ItemStack held = pPlayer.getItemInHand(pUsedHand);
		
		if(pLevel.isClientSide && ISurveyInfo.from(held) instanceof SurveyScan scan){
			if(scan.uuid() == null){ // Faulty
				pPlayer.displayClientMessage(Component.literal("This survey is faulty. (Destroy me!)").withStyle(ChatFormatting.RED), true);
				return InteractionResultHolder.fail(held);
			}
			
			openGUI(pLevel, scan);
			return InteractionResultHolder.success(held);
		}
		
		return InteractionResultHolder.pass(held);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static void openGUI(Level level, @Nonnull SurveyScan scan){
		MCUtil.setScreen(new SeismicSurveyScreen(level, scan));
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag){
		ISurveyInfo info = ISurveyInfo.from(stack);
		
		if(info instanceof SurveyScan scan){
			if(scan.uuid() == null){
				tooltip.add(Component.literal("SORRY, IM FAULTY!").withStyle(ChatFormatting.RED));
				tooltip.add(Component.literal("YOU'LL HAVE TO TOSS ME!").withStyle(ChatFormatting.RED));
				return;
			}
			
			tooltip.add(Component.translatable("desc.immersivepetroleum.flavour.surveytool.rightclickme"));
			
			if(flag == TooltipFlag.Default.ADVANCED){
				tooltip.add(Component.literal("ID: " + scan.uuid()));
				tooltip.add(Component.literal("dSize: " + (scan.data() != null ? scan.data().length : "Null")));
			}
		}
		
		if(info instanceof IslandInfo islandInfo){
			int expected = islandInfo.expected();
			long amount = islandInfo.amount();
			byte percentage = islandInfo.status();
			FluidStack fs = islandInfo.fluidStack();
			
			if(islandInfo.fluidStack() == FluidStack.EMPTY){
				tooltip.add(Component.literal("SORRY, IM FAULTY!").withStyle(ChatFormatting.RED));
				tooltip.add(Component.literal("YOU'LL HAVE TO TOSS ME!").withStyle(ChatFormatting.RED));
				return;
			}
			
			tooltip.add(Component.translatable(fs.getDescriptionId()).withStyle(ChatFormatting.DARK_GRAY));
			tooltip.add(Component.translatable("desc.immersivepetroleum.info.survey_result.amount", String.format(Locale.ENGLISH, "%,.3f", amount / 1000D), percentage).withStyle(ChatFormatting.DARK_GRAY));
			tooltip.add(Component.translatable("desc.immersivepetroleum.info.survey_result.expected", expected).withStyle(ChatFormatting.DARK_GRAY));
			
		}
		
		if(info != null){
			int x = info.getX();
			int z = info.getZ();
			
			tooltip.add(Component.translatable("desc.immersivepetroleum.flavour.surveytool.location", x, z).withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
