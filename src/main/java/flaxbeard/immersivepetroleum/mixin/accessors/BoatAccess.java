package flaxbeard.immersivepetroleum.mixin.accessors;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Boat.Status;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(Boat.class)
public interface BoatAccess{
	@Accessor(value = "status")
	void status(@Nullable Status status);
	
	@Accessor(value = "oldStatus")
	void oldStatus(@Nullable Status status);
	
	@Nullable
	@Accessor(value = "status")
	Status status();
	
	@Nullable
	@Accessor(value = "oldStatus")
	Status oldStatus();
	
	@Nonnull
	@Invoker
	Status invokeGetStatus();
	
	@Accessor
	void setOutOfControlTicks(float outOfControlTicks);
	
	@Accessor
	float getOutOfControlTicks();
	
	@Accessor
	float[] getPaddlePositions();
	
	@Accessor
	void setDeltaRotation(float deltaRotation);
	
	@Accessor
	float getDeltaRotation();
	
	@Accessor
	boolean isInputLeft();
	
	@Accessor
	boolean isInputRight();
	
	@Accessor
	boolean isInputUp();
	
	@Accessor
	boolean isInputDown();
	
	@Invoker
	void invokeTickLerp();
	
	@Invoker
	void invokeTickBubbleColumn();
	
	@Invoker
	void invokeFloatBoat();
}
