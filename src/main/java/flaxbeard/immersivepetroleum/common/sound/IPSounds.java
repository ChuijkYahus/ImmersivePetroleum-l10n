package flaxbeard.immersivepetroleum.common.sound;

import flaxbeard.immersivepetroleum.common.IPRegisters;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class IPSounds{
	public final static DeferredHolder<SoundEvent, SoundEvent> FLARESTACK = IPRegisters.registerSoundEvent("flarestack_fire");
	
	// TODO Find/Create a sound for generator and motorboat
	public final static DeferredHolder<SoundEvent, SoundEvent> GAS_GENERATOR = IPRegisters.registerSoundEvent("gas_generator");
	public final static DeferredHolder<SoundEvent, SoundEvent> MOTORBOAT = IPRegisters.registerSoundEvent("motorboat_engine");
	
	/*
		This has been unused for soooo long.
		While it would probably be nice for about a minute but after that I think it would get annoying
	*/
	public final static DeferredHolder<SoundEvent, SoundEvent> PROJECTOR = IPRegisters.registerSoundEvent("projector");
	
	public static void forceClassLoad(){
	}
}
