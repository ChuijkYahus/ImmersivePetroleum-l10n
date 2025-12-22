package flaxbeard.immersivepetroleum.common.util.survey;

import flaxbeard.immersivepetroleum.common.IPDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ISurveyInfo{
	/** World-X */
	int getX();
	
	/** World-Z */
	int getZ();
	
	void writeToStack(ItemStack stack);
	CompoundTag writeToTag();
	
	@Nullable
	static ISurveyInfo from(ItemStack stack){
		ReservoirInfo info;
		if((info = stack.get(IPDataComponents.RESERVOIR_INFO)) != null)
			return info;
		
		SurveyScan scan;
		if((scan = stack.get(IPDataComponents.SURVEY_SCAN)) != null)
			return scan;
		
		return null;
	}
}
