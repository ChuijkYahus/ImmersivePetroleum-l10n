package flaxbeard.immersivepetroleum.common.util.damageSources;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class IPDamageSources{
	static final ResourceKey<DamageType> FLARESTACK = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceUtils.ip("flarestack"));
	
	private static Holder<DamageType> type(RegistryAccess access, ResourceKey<DamageType> type){
		return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
	}
	
	public static DamageSource flarestack(Level level){
		return new DamageSource(type(level.registryAccess(), FLARESTACK));
	}
	
	//public static final DamageSource FLARESTACK = new DamageSource("ipFlarestack").bypassArmor().setIsFire();
	public static void forceClassLoad(){
		
	}
}
