package flaxbeard.immersivepetroleum.common.sound;

import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import blusunrize.immersiveengineering.common.items.components.AttachedItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class IPEntitySound extends IPTickableSound{
	public Entity entity;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment = 1;
	
	public IPEntitySound(Entity entity, SoundEvent sound, float volume, float pitch){
		super(sound, volume, pitch);
		this.entity = entity;
	}
	
	@Override
	public void tick(){
		if(MCUtil.getPlayer() != null && MCUtil.getPlayer().level().getDayTime() % 40 == 0)
			evaluateVolume();
	}
	
	public void evaluateVolume(){
		this.volumeAjustment = 1.0F;
		
		if(MCUtil.getPlayer() != null && !MCUtil.getPlayer().getItemBySlot(EquipmentSlot.HEAD).isEmpty()){
			ItemStack stack = MCUtil.getPlayer().getItemBySlot(EquipmentSlot.HEAD);
			
			AttachedItem attachedItem = stack.get(IEDataComponents.CONTAINED_EARMUFF);
			if(attachedItem != null){
				stack = attachedItem.attached();
			}
			if(!stack.isEmpty() && stack.getItem() instanceof EarmuffsItem){
				this.volumeAjustment = EarmuffsItem.getVolumeMod(stack);
			}
		}
		
		if(this.volumeAjustment > 0.1F){
			int xMin = (int) Math.floor(this.entity.getX() - 8) >> 4;
			int zMin = (int) Math.floor(this.entity.getZ() - 8) >> 4;
			int xMax = (int) Math.floor(this.entity.getX() + 8) >> 4;
			int zMax = (int) Math.floor(this.entity.getZ() + 8) >> 4;
			
			for(int dx = xMin;dx <= xMax;dx++){
				for(int dz = zMin;dz <= zMax;dz++){
					for(BlockEntity tile: MCUtil.getPlayer().level().getChunk(dx, dz).getBlockEntities().values()){
						if(tile != null && tile.getClass().getName().contains("SoundMuffler")){
							BlockPos tPos = tile.getBlockPos();
							double d = this.entity.position().distanceTo(new Vec3(tPos.getX() + 0.5, tPos.getY() + 0.5, tPos.getZ() + 0.5));
							if(d <= 64 && d > 0){
								this.volumeAjustment = 0.1F;
							}
						}
					}
				}
			}
		}
		
		if(!this.entity.isAlive())
			stop();
	}
	
	@Nonnull
	@Override
	public SoundSource getSource(){
		return SoundSource.NEUTRAL;
	}
	
	@Override
	public float getVolume(){
		return super.getVolume() * this.volumeAjustment;
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
	
	@Override
	public boolean isLooping(){
		return this.canRepeat;
	}
	
	@Override
	public boolean isRelative(){
		return false;
	}
}
