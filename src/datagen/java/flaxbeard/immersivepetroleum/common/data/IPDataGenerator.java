package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.data.loot.IPLootGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPDataGenerator{
	public static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/DataGenerator");
	
	@SubscribeEvent
	public static void generate(GatherDataEvent event){
		final ExistingFileHelper exHelper = event.getExistingFileHelper();
		final DataGenerator generator = event.getGenerator();
		final PackOutput output = generator.getPackOutput();
		final CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
		
		if(event.includeServer()){
			IPBlockTags blockTags = new IPBlockTags(output, provider, exHelper);
			generator.addProvider(true, blockTags);
			generator.addProvider(true, new IPItemTags(output, provider, blockTags.contentsGetter(), exHelper));
			generator.addProvider(true, new IPFluidTags(output, provider, exHelper));
			generator.addProvider(true, new IPLootGenerator(output, provider));
			generator.addProvider(true, new IPRecipes(output, provider));
			generator.addProvider(true, new IPAdvancements(output, provider, exHelper));
			
			generator.addProvider(true, new IPBlockStates(output, exHelper));
			generator.addProvider(true, new IPItemModels(output, exHelper));
			
			List<DataProvider> providers = IPWorldGen.makeProviders(output, provider);
			if(!providers.isEmpty()){
				for(final DataProvider data: providers){
					generator.addProvider(true, data);
				}
			}
			
			generator.addProvider(true, new IPMultiblockTexturesAttach(output, provider, exHelper));
		}
	}
}
