package flaxbeard.immersivepetroleum.common.items;

import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.entity.MolotovItemEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class MolotovItem extends IPItemBase{
	private static final int SECONDS = 15;
	
	private static Item.Properties makeProperty(boolean isLit){
		Item.Properties prop = new Item.Properties()
				.stacksTo(isLit ? 1 : 64)
				.durability(SECONDS)
				.setNoRepair();
		return prop;
	}
	
	private final boolean isLit;
	public MolotovItem(boolean isLit){
		super(makeProperty(isLit));
		this.isLit = isLit;
	}
	
	@Override
	public boolean addSelfToCreativeTab(){
		return !this.isLit;
	}
	
	@Override
	public void inventoryTick(@Nonnull ItemStack pStack, @Nonnull Level pLevel, @Nonnull Entity pEntity, int pSlotId, boolean pIsSelected){
		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
		
		if(this.isLit && pEntity instanceof Player player){
			CustomData customData = pStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag tagCopy;
			if(customData != null && (tagCopy = customData.copyTag()).contains("lit_time", Tag.TAG_LONG)){
				int duration = (int) (pLevel.getGameTime() - tagCopy.getLong("lit_time")) / 20;
				
				if(player.getAbilities().instabuild){
					if(duration > 0 && pStack.getDamageValue() == 0){
						pStack.setDamageValue(1);
					}
				}else{
					if(duration > SECONDS){
						player.getSlot(pSlotId).set(new ItemStack(Items.GLASS_BOTTLE, 1));
						return;
					}
					
					if(pStack.getDamageValue() != duration){
						pStack.setDamageValue(duration);
					}
				}
			}
			
			/*
			if(pStack.hasTag() && pStack.getTag().contains("lit_time", Tag.TAG_LONG)){
				int duration = (int) (pLevel.getGameTime() - pStack.getTag().getLong("lit_time")) / 20;
				
				if(player.getAbilities().instabuild){
					if(duration > 0 && pStack.getDamageValue() == 0){
						pStack.setDamageValue(1);
					}
				}else{
					if(duration > SECONDS){
						player.getSlot(pSlotId).set(new ItemStack(Items.GLASS_BOTTLE, 1));
						return;
					}
					
					if(pStack.getDamageValue() != duration){
						pStack.setDamageValue(duration);
					}
				}
			}
			*/
		}
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level pLevel, @Nonnull Player pPlayer, @Nonnull InteractionHand pUsedHand){
		if(this.isLit){
			ItemStack stack = pPlayer.getItemInHand(pUsedHand);
			if(stack.isDamaged() && !pLevel.isClientSide){
				pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
				
				MolotovItemEntity entity = new MolotovItemEntity(pLevel, pPlayer);
				entity.setItem(stack);
				entity.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 0.75F, 1.0F);
				pLevel.addFreshEntity(entity);
				
				if(!pPlayer.getAbilities().instabuild){
					stack.shrink(1);
				}
			}
			
			return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
		}else{
			ItemStack mainStack = pPlayer.getItemInHand(pUsedHand);
			ItemStack offStack = pPlayer.getItemInHand(InteractionHand.OFF_HAND);
			
			if(mainStack.getItem() == this && offStack.getItem() == Items.FLINT_AND_STEEL){
				pPlayer.startUsingItem(pUsedHand);
			}
			
			return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
		}
	}
	
	@Nonnull
	@Override
	public ItemStack finishUsingItem(@Nonnull ItemStack pStack, @Nonnull Level pLevel, @Nonnull LivingEntity pLivingEntity){
		if(!this.isLit && pLivingEntity instanceof Player player){
			ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
			ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);
			
			if(mainStack.getItem() == this && offStack.getItem() == Items.FLINT_AND_STEEL){
				pStack.shrink(1);
				if(player instanceof ServerPlayer serverPlayer && !serverPlayer.getAbilities().instabuild){
					offStack.hurtAndBreak(1, serverPlayer.serverLevel(), serverPlayer, (p) -> {
					});
					//offStack.hurtAndBreak(1, serverPlayer, (p) -> p.broadcastBreakEvent(InteractionHand.OFF_HAND));
				}
				
				CompoundTag tag = new CompoundTag();
				tag.putLong("lit_time", pLevel.getGameTime() - 1);
				
				ItemStack lit = new ItemStack(IPContent.Items.MOLOTOV_LIT.get(), 1);
				lit.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				return lit;
			}
		}
		
		return pStack;
	}
	
	@Nonnull
	@Override
	public UseAnim getUseAnimation(@Nonnull ItemStack pStack){
		return UseAnim.BOW;
	}
	
	@Override
	public int getUseDuration(@Nonnull ItemStack stack, @Nonnull LivingEntity living){
		return 20;
	}
}
