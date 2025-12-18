package flaxbeard.immersivepetroleum.common.sound;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class IPTickableSound implements TickableSoundInstance{
	protected static final RandomSource RANDOM_SOURCE = RandomSource.create();
	
	protected final ResourceLocation soundRL;
	protected final float volume;
	protected final float pitch;
	
	protected boolean stop = false;
	
	private Sound sound;
	
	protected IPTickableSound(SoundEvent event, float volume, float pitch){
		this.soundRL = event.getLocation();
		this.volume = volume;
		this.pitch = pitch;
	}
	
	public void stop(){
		this.stop = true;
	}
	
	@Override
	public boolean isStopped(){
		return this.stop;
	}
	
	@Nonnull
	@Override
	public ResourceLocation getLocation(){
		return this.soundRL;
	}
	
	@Nullable
	@Override
	public WeighedSoundEvents resolve(SoundManager soundManager){
		WeighedSoundEvents soundEvent = soundManager.getSoundEvent(this.soundRL);
		
		this.sound = soundEvent != null ? soundEvent.getSound(RANDOM_SOURCE) : SoundManager.EMPTY_SOUND;
		
		return soundEvent;
	}
	
	@Nonnull
	@Override
	public Sound getSound(){
		return this.sound;
	}
	
	@Override
	public int getDelay(){
		return 0;
	}
	
	@Override
	public float getVolume(){
		return this.volume;
	}
	
	@Override
	public float getPitch(){
		return this.pitch;
	}
	
	@Nonnull
	@Override
	public Attenuation getAttenuation(){
		return Attenuation.NONE;
	}
}
