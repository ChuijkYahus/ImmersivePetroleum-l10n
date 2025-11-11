package flaxbeard.immersivepetroleum.common.entity;

import flaxbeard.immersivepetroleum.common.IPRegisters;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public class IPEntityTypes{
	public static final DeferredHolder<EntityType<?>, EntityType<MotorboatEntity>> MOTORBOAT = IPRegisters.registerEntityType("speedboat", s -> {
		EntityType<MotorboatEntity> ret = EntityType.Builder.<MotorboatEntity> of(MotorboatEntity::new, MobCategory.MISC)
			.sized(1.375F, 0.5625F)
			.clientTrackingRange(10)
			.build(s.toString());
		return ret;
	});
	
	
	public static final DeferredHolder<EntityType<?>, EntityType<MolotovItemEntity>> MOLOTOV = createType();
	
	private static DeferredHolder<EntityType<?>, EntityType<MolotovItemEntity>> createType(){
		return IPRegisters.registerEntityType("molotov", s -> {
			EntityType<MolotovItemEntity> ret = EntityType.Builder.<MolotovItemEntity> of(MolotovItemEntity::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(s.toString());
			return ret;
		});
	}
	
	public static void forceClassLoad(){
	}
}
