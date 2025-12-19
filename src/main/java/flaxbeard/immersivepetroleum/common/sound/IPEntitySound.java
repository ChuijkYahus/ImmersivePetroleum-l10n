package flaxbeard.immersivepetroleum.common.sound;

import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.UUID;

public class IPEntitySound extends IPTickableSound{
	protected Entity entity;
	public IPEntitySound(Entity entity, SoundEvent sound, float volume, float pitch){
		super(sound, volume, pitch);
		this.entity = entity;
	}
	
	@Override
	public final void tick(){
		if(this.entity.isAlive() && this.entity instanceof IPlaySound soundPlayer){
			this.stop = soundPlayer.stopSound(getLocation());
			
			if(!this.stop && MCUtil.getPlayer() != null){
				float soundRadiusSqr = soundPlayer.soundRadiusSqr();
				
				double distancedSqr = MCUtil.getPlayer().distanceToSqr(this.entity);
				if(distancedSqr > soundRadiusSqr){
					this.stop = true;
				}
			}
		}
		
		if(!this.entity.isAlive() && !isStopped())
			stop();
	}
	
	@Nonnull
	@Override
	public SoundSource getSource(){
		return SoundSource.NEUTRAL;
	}
	
	@Override
	public double getX(){
		return this.entity.getX();
	}
	
	@Override
	public double getY(){
		return this.entity.getY();
	}
	
	@Override
	public double getZ(){
		return this.entity.getZ();
	}
	
	public Entity getEntity(){
		return this.entity;
	}
	
	public UUID getUUID(){
		return this.entity.getUUID();
	}
	
	@Override
	public boolean isLooping(){
		return true;
	}
	
	@Override
	public boolean isRelative(){
		return false;
	}
}
