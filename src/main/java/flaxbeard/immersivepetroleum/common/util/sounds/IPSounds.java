package flaxbeard.immersivepetroleum.common.util.sounds;

import flaxbeard.immersivepetroleum.common.IPRegisters;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class IPSounds{
	public final static DeferredHolder<SoundEvent, SoundEvent> FLARESTACK = IPRegisters.registerSoundEvent("flarestack_fire");
	public final static DeferredHolder<SoundEvent, SoundEvent> PROJECTOR = IPRegisters.registerSoundEvent("projector");
	
	public static void forceClassLoad(){
	}
}
