package flaxbeard.immersivepetroleum.common.sound;

import net.minecraft.resources.ResourceLocation;

public interface IPlaySound{
	boolean soundShouldStop(ResourceLocation soundLocation);
	default float soundRadiusSqr(){
		return 64.0F;
	}
}
