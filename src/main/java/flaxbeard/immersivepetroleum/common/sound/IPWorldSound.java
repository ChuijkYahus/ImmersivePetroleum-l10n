package flaxbeard.immersivepetroleum.common.sound;

import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class IPWorldSound extends IPTickableSound{
	private final BlockPos pos;
	private final double x, y, z;
	public IPWorldSound(BlockPos pos, SoundEvent event, float volume, float pitch){
		super(event, volume, pitch);
		this.pos = pos;
		this.x = pos.getX() + 0.5;
		this.y = pos.getY() + 0.5;
		this.z = pos.getZ() + 0.5;
	}
	
	@Override
	public void tick(){
		if(MCUtil.getLevel().getBlockEntity(this.pos) instanceof IPlaySound soundPlayer){
			this.stop = soundPlayer.soundShouldStop(getLocation());
			
			if(!this.stop && MCUtil.getPlayer() != null){
				float soundRadiusSqr = soundPlayer.soundRadiusSqr();
				double distancedSqr = MCUtil.getPlayer().distanceToSqr(Vec3.atCenterOf(this.pos));
				
				if(distancedSqr > soundRadiusSqr){
					this.stop = true;
				}
			}
		}else{
			this.stop = true;
		}
	}
	
	@Override
	public boolean isLooping(){
		return true;
	}
	
	@Override
	public boolean isRelative(){
		return false;
	}
	
	@Nonnull
	@Override
	public SoundSource getSource(){
		return SoundSource.BLOCKS;
	}
	
	@Nonnull
	@Override
	public Attenuation getAttenuation(){
		return Attenuation.LINEAR;
	}
	
	public BlockPos getPosition(){
		return this.pos;
	}
	
	@Override
	public double getX(){
		return this.x;
	}
	
	@Override
	public double getY(){
		return this.y;
	}
	
	@Override
	public double getZ(){
		return this.z;
	}
}
