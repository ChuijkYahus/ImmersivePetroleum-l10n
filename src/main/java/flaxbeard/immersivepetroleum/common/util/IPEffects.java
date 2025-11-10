package flaxbeard.immersivepetroleum.common.util;

import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Consumer;

public class IPEffects{
	
	/**
	 * This is only as a burn prevention for when someone dismounts the {@link MotorboatEntity} while that is in lava<br>
	 */
	public static final DeferredHolder<MobEffect, AntiFireEffect> ANTI_DISMOUNT_FIRE = IPRegisters.registerMobEffect("anti_fire", AntiFireEffect::new);
	
	public static void forceClassLoad(){
	}
	
	private static class AntiFireEffect extends IPEffect{
		public AntiFireEffect(){
			super(MobEffectCategory.BENEFICIAL, 0x7F7F7F);
		}
		
		@Override
		public void initializeClient(Consumer<IClientMobEffectExtensions> consumer){
			consumer.accept(new IClientMobEffectExtensions(){
				
				@Override
				public boolean isVisibleInGui(MobEffectInstance instance){
					return false;
				}
				
				@Override
				public boolean isVisibleInInventory(MobEffectInstance instance){
					return false;
				}
			});
		}
		
		@Override
		public boolean applyEffectTick(LivingEntity living, int amplifier){
			living.clearFire();
			return true;
		}
	}
	
	public static class IPEffect extends MobEffect{
		protected IPEffect(MobEffectCategory type, int color){
			super(type, color);
		}
	}
}
