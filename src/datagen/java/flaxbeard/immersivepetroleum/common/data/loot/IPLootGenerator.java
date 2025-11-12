package flaxbeard.immersivepetroleum.common.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class IPLootGenerator extends LootTableProvider{
	public IPLootGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider){
		super(output, Set.of(), List.of(), provider);
	}
	
	@Nonnull
	@Override
	public List<SubProviderEntry> getTables(){
		//@formatter:off
		return List.of(
			new SubProviderEntry(IPLoot::new, LootContextParamSets.EMPTY),
			new SubProviderEntry(IPBlockLoot::new, LootContextParamSets.BLOCK)
		);
		//@formatter:off
	}
	
	@Override
	protected void validate(@Nonnull WritableRegistry<LootTable> writableregistry, @Nonnull ValidationContext ctx, @Nonnull ProblemReporter.Collector collector){
		super.validate(writableregistry, ctx, collector);
		
		writableregistry.holders().forEach((t) -> t.value().validate(ctx));
	}
}
