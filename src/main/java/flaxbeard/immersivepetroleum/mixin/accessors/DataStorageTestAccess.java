package flaxbeard.immersivepetroleum.mixin.accessors;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;
import java.util.Map;

/**
 * For future use
 *
 * @author TwistedGate
 */
@Mixin(DimensionDataStorage.class)
public interface DataStorageTestAccess{
	@Accessor
	Map<String, SavedData> getCache();
	
	@Accessor
	File getDataFolder();
	
	@Invoker
	File invokeGetDataFile(String saveDataKey);
}
