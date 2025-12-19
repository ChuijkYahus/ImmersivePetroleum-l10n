package flaxbeard.immersivepetroleum.common.sound;

import net.minecraft.resources.ResourceLocation;

public interface IPlaySound{
	/**
	 * Determines if the supplied sound (which is currently being played) should stop.
	 */
	boolean stopSound(ResourceLocation soundLocation);
	
	default float soundRadiusSqr(){
		return 64.0F;
	}
}
